package com.community.community.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Embeddable
@Getter
@EqualsAndHashCode
public class PostLikeId implements Serializable {

    @Column(name = "post_id")
    private Integer postId;

    @Column(name = "user_id")
    private Integer userId;

    protected PostLikeId() {
    }

    public PostLikeId(Integer postId, Integer userId) {
        this.postId = postId;
        this.userId = userId;
    }
}
