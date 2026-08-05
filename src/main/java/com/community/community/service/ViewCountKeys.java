package com.community.community.service;

final class ViewCountKeys {

    private static final String PREFIX = "view-count";

    private ViewCountKeys() {
    }

    static int partition(int postId, int partitionCount) {
        return Math.floorMod(postId, partitionCount);
    }

    static String total(int partition, int postId) {
        return tag(partition) + ":total:" + postId;
    }

    static String pending(int partition) {
        return tag(partition) + ":pending";
    }

    static String processing(int partition) {
        return tag(partition) + ":processing";
    }

    static String lock(int partition) {
        return tag(partition) + ":lock";
    }

    private static String tag(int partition) {
        return "{" + PREFIX + ":" + String.format("%02d", partition) + "}";
    }
}
