package com.community.community.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ViewCountKeysTest {

    @Test
    void keysInSamePartitionShareClusterHashTag() {
        int partition = ViewCountKeys.partition(500001, 16);

        assertThat(partition).isEqualTo(1);
        assertThat(ViewCountKeys.total(partition, 500001)).startsWith("{view-count:01}");
        assertThat(ViewCountKeys.pending(partition)).startsWith("{view-count:01}");
        assertThat(ViewCountKeys.processing(partition)).startsWith("{view-count:01}");
        assertThat(ViewCountKeys.lock(partition)).startsWith("{view-count:01}");
    }

    @Test
    void partitionHandlesEveryIntegerPostId() {
        assertThat(ViewCountKeys.partition(-1, 16)).isEqualTo(15);
        assertThat(ViewCountKeys.partition(0, 16)).isZero();
        assertThat(ViewCountKeys.partition(16, 16)).isZero();
    }
}
