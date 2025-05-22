package com.post_hub.iam_service.repository;


import com.post_hub.iam_service.model.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PostRepository extends JpaRepository<Post, Integer> {
}
