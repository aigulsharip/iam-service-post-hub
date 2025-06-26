package com.post_hub.iam_service.model.dto.comment;

import com.post_hub.iam_service.model.dto.post.PostOwnerDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentSearchDto implements Serializable {
    private Integer id;
    private String message;
    private LocalDateTime created;
    private LocalDateTime updated;
    private PostOwnerDto owner;
    private Integer postId;
    private Boolean isDeleted;

}
