package com.community.community.service;

import com.community.community.entity.Post;

public interface ViewCountService {

    int incrementAndGet(Post post);
}
