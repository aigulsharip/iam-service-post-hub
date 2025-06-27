package com.post_hub.iam_service.controller;

import com.post_hub.iam_service.model.constants.ApiLogMessage;
import com.post_hub.iam_service.model.dto.comment.CommentDto;
import com.post_hub.iam_service.model.dto.comment.CommentSearchDto;
import com.post_hub.iam_service.model.request.comment.CommentRequest;
import com.post_hub.iam_service.model.request.comment.CommentSearchRequest;
import com.post_hub.iam_service.model.request.comment.UpdateCommentRequest;
import com.post_hub.iam_service.model.response.IamResponse;
import com.post_hub.iam_service.model.response.PaginationResponse;
import com.post_hub.iam_service.service.CommentService;
import com.post_hub.iam_service.utils.ApiUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("${end.point.comments}")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("${end.point.id}")
    public ResponseEntity<IamResponse<CommentDto>> getCommentById(
            @PathVariable(name = "id") Integer commentId) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        IamResponse<CommentDto> response = commentService.getCommentById(commentId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("${end.points.create}")
    public ResponseEntity<IamResponse<CommentDto>> createComment(
            @RequestBody @Valid CommentRequest request){
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());
        IamResponse<CommentDto> response = commentService.createComment(request);
        return ResponseEntity.ok(response);
    }


    @PutMapping("${end.point.id}")
    public ResponseEntity<IamResponse<CommentDto>> updateComment(@PathVariable (name = "id") Integer commentId,
                                                           @RequestBody @Valid UpdateCommentRequest request) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        IamResponse<CommentDto> response = commentService.updateComment(commentId, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("${end.point.id}")
    public ResponseEntity<Void> softDeleteComment(
            @PathVariable(name = "id") Integer commentId) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        commentService.softDelete(commentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("${end.point.all}")
    public ResponseEntity<IamResponse<PaginationResponse<CommentSearchDto>>> getAllComments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());
        Pageable pageable = PageRequest.of(page, limit);
        IamResponse<PaginationResponse<CommentSearchDto>> response = commentService.findAllComments(pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("${end.points.search}")
    public ResponseEntity<IamResponse<PaginationResponse<CommentSearchDto>>> searchComments(
            @RequestBody @Valid CommentSearchRequest request,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit
    ) {
        log.trace(ApiLogMessage.NAME_OF_CURRENT_METHOD.getValue(), ApiUtils.getMethodName());

        Pageable pageable = PageRequest.of(page, limit);
        IamResponse<PaginationResponse<CommentSearchDto>> response = commentService.searchComments(request, pageable);
        return ResponseEntity.ok(response);
    }
}
