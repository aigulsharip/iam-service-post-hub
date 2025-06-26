package com.post_hub.iam_service.service;

import com.post_hub.iam_service.model.dto.comment.CommentDto;
import com.post_hub.iam_service.model.request.comment.CommentRequest;
import com.post_hub.iam_service.model.response.IamResponse;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;

public interface CommentService {
    IamResponse<CommentDto> getCommentById(@NotNull Integer commentId);

    IamResponse<CommentDto> createComment(@NotNull CommentRequest commentRequest);
}
