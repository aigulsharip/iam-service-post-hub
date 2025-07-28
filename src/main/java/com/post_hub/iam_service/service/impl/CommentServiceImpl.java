package com.post_hub.iam_service.service.impl;

import com.post_hub.iam_service.kafka.service.KafkaMessageService;
import com.post_hub.iam_service.mapper.CommentMapper;
import com.post_hub.iam_service.model.constants.ApiErrorMessage;
import com.post_hub.iam_service.model.dto.comment.CommentDto;
import com.post_hub.iam_service.model.dto.comment.CommentSearchDto;
import com.post_hub.iam_service.model.dto.post.PostDTO;
import com.post_hub.iam_service.model.entity.Comment;
import com.post_hub.iam_service.model.entity.Post;
import com.post_hub.iam_service.model.entity.User;
import com.post_hub.iam_service.model.exception.DataExistException;
import com.post_hub.iam_service.model.exception.NotFoundException;
import com.post_hub.iam_service.model.request.comment.CommentRequest;
import com.post_hub.iam_service.model.request.comment.CommentSearchRequest;
import com.post_hub.iam_service.model.request.comment.UpdateCommentRequest;
import com.post_hub.iam_service.model.response.IamResponse;
import com.post_hub.iam_service.model.response.PaginationResponse;
import com.post_hub.iam_service.repository.CommentRepository;
import com.post_hub.iam_service.repository.PostRepository;
import com.post_hub.iam_service.repository.UserRepository;
import com.post_hub.iam_service.repository.criteria.CommentSearchCriteria;
import com.post_hub.iam_service.security.validation.AccessValidator;
import com.post_hub.iam_service.service.CommentService;
import com.post_hub.iam_service.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final ApiUtils apiUtils;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final AccessValidator  accessValidator;
    private final KafkaMessageService kafkaMessageService;

    @Override
    public IamResponse<CommentDto> getCommentById(Integer id) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.COMMENT_NOT_FOUND_BY_ID.getMessage(id)));
        CommentDto commentDto = commentMapper.toCommentDto(comment);

        return IamResponse.createSuccessful(commentDto);
    }

    @Override
    public IamResponse<CommentDto> createComment(CommentRequest commentRequest) {
        Integer userId = apiUtils.getUserIdFromAuthentication();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId)));

        Post post = postRepository.findById(commentRequest.getPostId())
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.POST_NOT_FOUND_BY_ID.getMessage(commentRequest.getPostId())));

        Comment comment = commentMapper.createComment(commentRequest, user, post);
        comment = commentRepository.save(comment);
        postRepository.save(post);
        kafkaMessageService.sendCommentCreatedMessage(userId, comment.getId());

        return IamResponse.createSuccessful(commentMapper.toCommentDto(comment));
    }

    @Override
    public IamResponse<CommentDto> updateComment(Integer commentId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.COMMENT_NOT_FOUND_BY_ID.getMessage(commentId)));
        accessValidator.validateAdminOrOwnAccess(comment.getUser().getId());

        if (request.getPostId() != null) {
            Post post = postRepository.findByIdAndDeletedFalse(request.getPostId())
                    .orElseThrow(() -> new NotFoundException(ApiErrorMessage.POST_NOT_FOUND_BY_ID.getMessage(request.getPostId())));
            comment.setPost(post);
        }

        commentMapper.updateComment(comment, request);
        comment = commentRepository.save(comment);
        kafkaMessageService.sendCommentUpdatedMessage(comment.getPost().getUser().getId(),comment.getId(), comment.getMessage());

        return IamResponse.createSuccessful(commentMapper.toCommentDto(comment));

    }

    @Override
    public void softDelete(Integer commentId) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.COMMENT_NOT_FOUND_BY_ID.getMessage(commentId)));
        accessValidator.validateAdminOrOwnAccess(comment.getUser().getId());

        comment.setDeleted(true);
        commentRepository.save(comment);
        kafkaMessageService.sendCommentDeletedMessage(comment.getPost().getUser().getId(),comment.getId());
    }

    @Override
    public IamResponse<PaginationResponse<CommentSearchDto>> findAllComments(Pageable pageable) {

        Page<CommentSearchDto> comments = commentRepository.findAll(pageable)
                .map(commentMapper::toCommentSearchDTO);

        PaginationResponse<CommentSearchDto> paginationResponse = new PaginationResponse<>(
                comments.getContent(),
                new PaginationResponse.Pagination(
                        comments.getTotalElements(),
                        pageable.getPageSize(),
                        comments.getNumber() + 1,
                        comments.getTotalPages()
                )
        );
        return IamResponse.createSuccessful(paginationResponse);


    }

    @Override
    public IamResponse<PaginationResponse<CommentSearchDto>> searchComments(CommentSearchRequest request, Pageable pageable) {
        Specification<Comment> specification = new CommentSearchCriteria(request);

        Page<CommentSearchDto> commentsPage = commentRepository.findAll(specification, pageable)
                .map(commentMapper::toCommentSearchDTO);

        PaginationResponse<CommentSearchDto> response = PaginationResponse.<CommentSearchDto>builder()
                .content(commentsPage.getContent())
                .pagination((PaginationResponse.Pagination.builder()
                        .total(commentsPage.getTotalElements())
                        .limit(pageable.getPageSize())
                        .page(commentsPage.getNumber() + 1)
                        .pages(commentsPage.getTotalPages())
                        .build()))
                .build();

        return IamResponse.createSuccessful(response);
    }

}
