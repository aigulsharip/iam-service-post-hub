package com.post_hub.iam_service.service.impl;

import com.post_hub.iam_service.mapper.CommentMapper;
import com.post_hub.iam_service.model.constants.ApiErrorMessage;
import com.post_hub.iam_service.model.dto.comment.CommentDto;
import com.post_hub.iam_service.model.entity.Comment;
import com.post_hub.iam_service.model.exception.NotFoundException;
import com.post_hub.iam_service.model.response.IamResponse;
import com.post_hub.iam_service.repository.CommentRepository;
import com.post_hub.iam_service.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    public IamResponse<CommentDto> getCommentById(Integer id) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException(ApiErrorMessage.COMMENT_NOT_FOUND_BY_ID.getMessage(id)));
        CommentDto commentDto = commentMapper.toCommentDto(comment);

        return IamResponse.createSuccessful(commentDto);
    }
}
