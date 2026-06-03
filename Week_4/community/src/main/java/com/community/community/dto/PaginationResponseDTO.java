package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaginationResponseDTO {

    @JsonProperty("next_cursor")
    private Integer nextCursor;

    @JsonProperty("has_next")
    private boolean hasNext;

    public PaginationResponseDTO() {
    }

    public Integer getNextCursor() {
        return nextCursor;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setNextCursor(Integer nextCursor) {
        this.nextCursor = nextCursor;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }
}
