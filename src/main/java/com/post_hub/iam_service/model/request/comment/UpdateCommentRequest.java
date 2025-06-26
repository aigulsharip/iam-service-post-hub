package com.post_hub.iam_service.model.request.comment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentRequest {

    @NotNull(message = "Post Id cannot be null")
    private Integer postId;

    @NotNull(message = "Content of comment cannot be empty")
    private String message;
}
