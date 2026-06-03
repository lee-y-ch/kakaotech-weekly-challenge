package com.community.community.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostUpdateRequestDTO {

    private String title;
    private String content;

    @JsonProperty("image_url")
    private String imageUrl;

    public PostUpdateRequestDTO() {
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
