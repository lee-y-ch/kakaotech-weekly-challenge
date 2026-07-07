package com.community.community.repository;

import com.community.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer>, CommentRepositoryCustom {
}