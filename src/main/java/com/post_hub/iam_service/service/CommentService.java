package com.post_hub.iam_service.service;

import com.post_hub.iam_service.model.dto.comment.CommentDto;
import com.post_hub.iam_service.model.dto.comment.CommentSearchDto;
import com.post_hub.iam_service.model.request.comment.CommentRequest;
import com.post_hub.iam_service.model.request.comment.CommentSearchRequest;
import com.post_hub.iam_service.model.request.comment.UpdateCommentRequest;
import com.post_hub.iam_service.model.response.IamResponse;
import com.post_hub.iam_service.model.response.PaginationResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;

import java.util.LinkedList;

public interface CommentService {

    IamResponse<CommentDto> getCommentById(@NotNull Integer commentId);

    IamResponse<CommentDto> createComment(@NotNull CommentRequest request);

    IamResponse<CommentDto> updateComment(@NotNull Integer commentId, @NotNull UpdateCommentRequest request);

    void softDelete(@NotNull Integer commentId);

    IamResponse<PaginationResponse<CommentSearchDto>> findAllComments(Pageable pageable);

    IamResponse<PaginationResponse<CommentSearchDto>> searchComments(@NotNull CommentSearchRequest request, Pageable pageable);

    IamResponse<LinkedList<CommentDto>> findAllCommentsByUser();
}
