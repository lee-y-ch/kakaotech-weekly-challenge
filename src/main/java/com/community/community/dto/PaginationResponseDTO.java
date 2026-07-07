package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PaginationResponseDTO {

    @JsonProperty("next_cursor")
    private final Integer nextCursor;

    @JsonProperty("has_next")
    private final boolean hasNext;

    public PaginationResponseDTO(Integer nextCursor, boolean hasNext) {
        this.nextCursor = nextCursor;
        this.hasNext = hasNext;
    }
}
