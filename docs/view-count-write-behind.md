# View-count write-behind

## Problem proven by the baseline

The original post-detail path executed one InnoDB update for every successful view.
Under a single-post hotspot test at 20 RPS for two minutes, repeated three times:

- 7,203 successful detail requests produced exactly 7,203 `Innodb_rows_updated`.
- The same row accumulated 152 lock waits and 24,528 ms of lock-wait time.
- Request latency had a three-run median p95 of 30.31 ms and p99 of 119.67 ms,
  with a worst observed request of 1.09 seconds.

The optimization target is the database write amplification and hot-row contention,
not merely the average HTTP latency.

## Placement and network

ElastiCache Serverless for Valkey is outside Kubernetes but inside the same VPC.
It is attached to private subnets in `ap-northeast-2a` and `ap-northeast-2c`.
Its security group accepts ports 6379-6380 only from the Kubernetes worker security
group. The application uses the TLS primary endpoint on port 6379.

## Data model

Valkey Serverless uses the cluster-mode protocol. Multi-key Lua scripts therefore
require all keys to share a hash slot. View-count keys are split into 16 partitions:

```text
partition = floorMod(postId, 16)

{view-count:01}:total:500001
{view-count:01}:pending
{view-count:01}:processing
{view-count:01}:lock
```

The hash tag in braces keeps each partition's keys in one slot while distributing
different partitions across the cluster.

## Request path

One Lua script atomically:

1. initializes or reconciles the display total from the database value;
2. increments the display total;
3. increments the post field in the partition's pending hash.

The returned total preserves the existing response contract without waiting for a
database update. A per-post total key expires after 24 hours.

## Flush path

Every ten seconds, each backend instance attempts a Valkey lock for every partition.
The lock allows the design to keep working when the backend is scaled beyond one pod.
The winner atomically renames `pending` to `processing`, then applies one database
update per distinct post:

```sql
UPDATE posts
SET view_count = view_count + :delta
WHERE post_id = :postId;
```

New requests create a new `pending` hash while the previous batch is written. The
processing hash is deleted only after the database transaction commits. On database
failure or pod termination before commit, it remains and is retried by a later run.

## Failure semantics and deliberate trade-off

- Valkey connection/command failure: fail open to the original direct database update.
- Database flush failure: retain the processing hash and retry.
- Multiple backend pods: use a token-owned lock with TTL and compare-and-delete release.
- Deleted post: acknowledge and discard its buffered delta after an update affects zero rows.

The buffer provides **at-least-once flush semantics**. A crash after the MySQL commit
but before deleting the processing hash can replay a batch and over-count views. Full
exactly-once delivery would require a persistent idempotency ledger or transactional
outbox, adding a database write per flush batch and lifecycle management. View counts
are low-criticality analytics data, so the bounded over-count risk is accepted in
exchange for a much smaller implementation and operational surface. This trade-off
must be revisited if view counts become billing, ranking, or reward inputs.

## Verification contract

Only the following tests are required:

1. Unit tests for buffered success, direct fallback, partition hash tags, routing,
   and one database update per distinct post delta.
2. One deployed smoke request verifying immediate response and delayed DB reflection.
3. The same 20 RPS, two-minute, three-run hotspot suite used for the baseline.
4. Final conservation check: successful views equal database increase plus any pending
   delta, followed by an eventual database equality check after the last flush.

The expected primary result is that approximately 7,200 per-request updates become
roughly 36 ten-second aggregate updates for a single hot post.
