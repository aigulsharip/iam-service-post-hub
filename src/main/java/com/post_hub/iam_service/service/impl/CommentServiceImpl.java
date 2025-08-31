package com.post_hub.iam_service.service.impl;

import com.post_hub.iam_service.kafka.service.KafkaMessageService;
import com.post_hub.iam_service.mapper.CommentMapper;
import com.post_hub.iam_service.mapper.PostMapper;
import com.post_hub.iam_service.model.constants.ApiErrorMessage;
import com.post_hub.iam_service.model.dto.comment.CommentDto;
import com.post_hub.iam_service.model.dto.comment.CommentSearchDto;
import com.post_hub.iam_service.model.entity.Comment;
import com.post_hub.iam_service.model.entity.Post;
import com.post_hub.iam_service.model.entity.User;
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
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final ApiUtils apiUtils;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final AccessValidator accessValidator;
    private final KafkaMessageService kafkaMessageService;

    @Override
    @Transactional(readOnly = true)
    public IamResponse<CommentDto> getCommentById(@NotNull Integer commentId) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.COMMENT_NOT_FOUND_BY_ID.getMessage(commentId)));

        return IamResponse.createSuccessful(commentMapper.toCommentDto(comment));
    }

    @Override
    @Transactional
    public IamResponse<CommentDto> createComment(@NotNull CommentRequest request) {
        Integer userId = apiUtils.getUserIdFromAuthentication();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId)));

        Post post = postRepository.findByIdAndDeletedFalse(request.getPostId())
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.POST_NOT_FOUND_BY_ID.getMessage(request.getPostId())));

        Comment comment = commentMapper.createComment(request, user, post);
        comment = commentRepository.save(comment);
        post.incrementCommentsCount();
        postRepository.save(post);

        kafkaMessageService.sendCommentCreatedMessage(user.getId(), comment.getId());

        return IamResponse.createSuccessful(commentMapper.toCommentDto(comment));
    }

    @Override
    @Transactional
    public IamResponse<CommentDto> updateComment(@NotNull Integer commentId, @NotNull UpdateCommentRequest request) {
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

        kafkaMessageService.sendCommentUpdatedMessage(comment.getUser().getId(), comment.getId(), comment.getMessage());

        return IamResponse.createSuccessful(commentMapper.toCommentDto(comment));
    }

    @Override
    @Transactional
    public void softDelete(@NotNull Integer commentId) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.COMMENT_NOT_FOUND_BY_ID.getMessage(commentId)));

        accessValidator.validateAdminOrOwnAccess(comment.getUser().getId());

        comment.setDeleted(true);
        commentRepository.save(comment);

        Post post = comment.getPost();
        post.decrementCommentsCount();
        postRepository.save(post);

        kafkaMessageService.sendCommentDeletedMessage(comment.getUser().getId(), comment.getId());

        IamResponse.createSuccessful(postMapper.toPostDTO(post));
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public IamResponse<PaginationResponse<CommentSearchDto>> searchComments(@NotNull CommentSearchRequest request, Pageable pageable) {
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

    @Transactional(readOnly = true)
    public IamResponse<LinkedList<CommentDto>> findAllCommentsByUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(username)));

        LinkedList<Comment> comments = commentRepository.findAllByUserIdAndDeletedFalse(user.getId());
        LinkedList<CommentDto> commentDto = commentMapper.toDtoList(comments);
        return IamResponse.createSuccessful(commentDto);
    }
}
