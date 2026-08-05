package com.community.community.service;

import com.community.community.config.ViewCountBufferProperties;
import com.community.community.entity.Post;
import com.community.community.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@ConditionalOnProperty(
        name = "view-count.buffer.enabled",
        havingValue = "true"
)
public class DeferredViewCountService implements ViewCountService {

    private static final Logger log = LoggerFactory.getLogger(DeferredViewCountService.class);

    private static final DefaultRedisScript<Long> INCREMENT_SCRIPT =
            new DefaultRedisScript<>("""
                    local current = redis.call('GET', KEYS[1])
                    local databaseValue = tonumber(ARGV[2])
                    if (not current) or (tonumber(current) < databaseValue) then
                        redis.call('SET', KEYS[1], databaseValue)
                    end
                    local total = redis.call('INCR', KEYS[1])
                    redis.call('EXPIRE', KEYS[1], ARGV[3])
                    redis.call('HINCRBY', KEYS[2], ARGV[1], 1)
                    return total
                    """, Long.class);

    @SuppressWarnings("rawtypes")
    private static final DefaultRedisScript<List> CLAIM_SCRIPT =
            new DefaultRedisScript<>("""
                    if redis.call('EXISTS', KEYS[2]) == 0
                       and redis.call('EXISTS', KEYS[1]) == 1 then
                        redis.call('RENAME', KEYS[1], KEYS[2])
                    end
                    return redis.call('HGETALL', KEYS[2])
                    """, List.class);

    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT =
            new DefaultRedisScript<>("""
                    if redis.call('GET', KEYS[1]) == ARGV[1] then
                        return redis.call('DEL', KEYS[1])
                    end
                    return 0
                    """, Long.class);

    private final StringRedisTemplate redisTemplate;
    private final PostRepository postRepository;
    private final ViewCountBatchWriter batchWriter;
    private final ViewCountBufferProperties properties;

    public DeferredViewCountService(
            StringRedisTemplate redisTemplate,
            PostRepository postRepository,
            ViewCountBatchWriter batchWriter,
            ViewCountBufferProperties properties
    ) {
        this.redisTemplate = redisTemplate;
        this.postRepository = postRepository;
        this.batchWriter = batchWriter;
        this.properties = properties;
    }

    @Override
    @Transactional
    public int incrementAndGet(Post post) {
        int postId = post.getPostId();
        int partition = ViewCountKeys.partition(postId, properties.partitions());

        try {
            Long total = redisTemplate.execute(
                    INCREMENT_SCRIPT,
                    List.of(
                            ViewCountKeys.total(partition, postId),
                            ViewCountKeys.pending(partition)
                    ),
                    Integer.toString(postId),
                    Integer.toString(post.getViewCount()),
                    Long.toString(properties.totalTtl().toSeconds())
            );

            if (total == null) {
                log.warn("Valkey returned no buffered view count; falling back to direct DB update");
                return incrementDirectly(postId);
            }

            if (total > Integer.MAX_VALUE) {
                throw new IllegalStateException("Buffered view count exceeds API integer range: " + total);
            }

            return total.intValue();
        } catch (RedisConnectionFailureException exception) {
            log.warn("Valkey unavailable; falling back to direct DB view-count update", exception);
            return incrementDirectly(postId);
        } catch (DataAccessException exception) {
            log.warn("Valkey command failed; falling back to direct DB view-count update", exception);
            return incrementDirectly(postId);
        }
    }

    @Scheduled(
            fixedDelayString = "${view-count.buffer.flush-interval-ms:10000}",
            initialDelayString = "${view-count.buffer.flush-interval-ms:10000}"
    )
    public void flushPendingCounts() {
        for (int partition = 0; partition < properties.partitions(); partition++) {
            flushPartition(partition);
        }
    }

    private void flushPartition(int partition) {
        String lockKey = ViewCountKeys.lock(partition);
        String lockToken = UUID.randomUUID().toString();
        Boolean acquired;

        try {
            acquired = redisTemplate.opsForValue().setIfAbsent(
                    lockKey,
                    lockToken,
                    properties.lockTtl()
            );
        } catch (DataAccessException exception) {
            log.warn("Could not acquire view-count flush lock for partition {}", partition, exception);
            return;
        }

        if (!Boolean.TRUE.equals(acquired)) {
            return;
        }

        try {
            Map<Integer, Long> deltas = claim(partition);
            if (deltas.isEmpty()) {
                return;
            }

            batchWriter.apply(deltas);
            redisTemplate.delete(ViewCountKeys.processing(partition));
            long totalDelta = deltas.values().stream()
                    .mapToLong(Long::longValue)
                    .sum();
            log.info(
                    "Flushed buffered view counts: partition={}, posts={}, delta={}",
                    partition,
                    deltas.size(),
                    totalDelta
            );
        } catch (RuntimeException exception) {
            // processing hash is deliberately retained so a later scheduler run can retry it.
            log.error("View-count flush failed for partition {}; retrying later", partition, exception);
        } finally {
            try {
                redisTemplate.execute(
                        RELEASE_LOCK_SCRIPT,
                        List.of(lockKey),
                        lockToken
                );
            } catch (DataAccessException exception) {
                log.warn("Could not release view-count flush lock for partition {}", partition, exception);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Long> claim(int partition) {
        List<Object> rawEntries = redisTemplate.execute(
                CLAIM_SCRIPT,
                List.of(
                        ViewCountKeys.pending(partition),
                        ViewCountKeys.processing(partition)
                )
        );

        if (rawEntries == null || rawEntries.isEmpty()) {
            return Map.of();
        }

        if (rawEntries.size() % 2 != 0) {
            throw new IllegalStateException("Invalid processing hash response");
        }

        Map<Integer, Long> deltas = new LinkedHashMap<>();
        List<Object> entries = new ArrayList<>(rawEntries);

        for (int index = 0; index < entries.size(); index += 2) {
            int postId = Integer.parseInt(entries.get(index).toString());
            long delta = Long.parseLong(entries.get(index + 1).toString());
            deltas.put(postId, delta);
        }

        return deltas;
    }

    private int incrementDirectly(int postId) {
        postRepository.increaseViewCount(postId);
        return postRepository.findViewCountByPostId(postId);
    }
}
