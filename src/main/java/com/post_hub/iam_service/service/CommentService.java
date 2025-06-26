package com.post_hub.iam_service.service;

import com.post_hub.iam_service.model.dto.comment.CommentDto;
import com.post_hub.iam_service.model.response.IamResponse;
import lombok.NonNull;

public interface CommentService {
    IamResponse<CommentDto> getCommentById(@NonNull Integer commentId);
}
