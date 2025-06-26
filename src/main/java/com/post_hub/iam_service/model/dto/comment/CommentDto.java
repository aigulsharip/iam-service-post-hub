package com.post_hub.iam_service.model.dto.comment;

import com.post_hub.iam_service.model.dto.post.PostOwnerDto;
import com.post_hub.iam_service.model.entity.Post;
import com.post_hub.iam_service.model.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto implements Serializable {
    private Integer id;
    private String message;
    private LocalDateTime created;
    private LocalDateTime updated;
    private PostOwnerDto postOwnerDto;
    private Integer postId;
}
