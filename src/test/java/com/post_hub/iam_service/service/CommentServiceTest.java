package com.post_hub.iam_service.service;

import com.post_hub.iam_service.kafka.service.KafkaMessageService;
import com.post_hub.iam_service.mapper.CommentMapper;
import com.post_hub.iam_service.model.dto.comment.CommentDto;
import com.post_hub.iam_service.model.entity.Comment;
import com.post_hub.iam_service.model.entity.Post;
import com.post_hub.iam_service.model.entity.User;
import com.post_hub.iam_service.model.exception.NotFoundException;
import com.post_hub.iam_service.model.request.comment.CommentRequest;
import com.post_hub.iam_service.repository.CommentRepository;
import com.post_hub.iam_service.repository.PostRepository;
import com.post_hub.iam_service.repository.UserRepository;
import com.post_hub.iam_service.service.impl.CommentServiceImpl;
import com.post_hub.iam_service.utils.ApiUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private KafkaMessageService kafkaMessageService;


    @Mock
    private ApiUtils apiUtils;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment tesComment;
    private CommentDto testCommentDTO;
    private User testUser;
    private Post testPost;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("TestUser");

        testPost = new Post();
        testPost.setId(1);
        testPost.setTitle("TestPost");

        tesComment = new Comment();
        tesComment.setId(1);
        tesComment.setMessage("Test Comment");
        tesComment.setPost(testPost);
        tesComment.setUser(testUser);

        testCommentDTO = new CommentDto();
        testCommentDTO.setId(1);
        testCommentDTO.setMessage("Test Comment");
    }

    @Test
    void getCommentById_CommentExists_ReturnsCommentDTO() {
        when(commentRepository.findByIdAndDeletedFalse(1)).thenReturn(Optional.of(tesComment));
        when(commentMapper.toCommentDto(tesComment)).thenReturn(testCommentDTO);

        CommentDto result = commentService.getCommentById(1).getPayload();

        assertNotNull(result);
        assertEquals(testCommentDTO.getId(), result.getId());
        assertEquals(testCommentDTO.getMessage(), result.getMessage());

        verify(commentRepository, times(1)).findByIdAndDeletedFalse(1);
        verify(commentMapper, times(1)).toCommentDto(tesComment);
    }

    @Test
    void getCommentById_CommentNotFound_ThrowsException() {
        when(commentRepository.findByIdAndDeletedFalse(999)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> commentService.getCommentById(999));

        assertTrue(exception.getMessage().contains("not found"));

        verify(commentRepository, times(1)).findByIdAndDeletedFalse(999);
        verify(commentMapper, never()).toCommentDto(any(Comment.class));
    }

    @Test
    void createComment_OK() {
        CommentRequest request = new CommentRequest(1, "New comment");

        when(apiUtils.getUserIdFromAuthentication()).thenReturn(testUser.getId());
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(postRepository.findById(testPost.getId())).thenReturn(Optional.of(testPost));
        when(commentMapper.createComment(request, testUser, testPost)).thenReturn(tesComment);
        when(commentRepository.save(any(Comment.class))).thenReturn(tesComment);
        when(commentMapper.toCommentDto(tesComment)).thenReturn(testCommentDTO);

        CommentDto result = commentService.createComment(request).getPayload();

        assertNotNull(result);
        assertEquals(testCommentDTO.getMessage(), result.getMessage());

        verify(apiUtils, times(1)).getUserIdFromAuthentication();
        verify(userRepository, times(1)).findById(testUser.getId());
        verify(postRepository, times(1)).findById(testPost.getId());
        verify(commentRepository, times(1)).save(any(Comment.class));
        verify(commentMapper, times(1)).toCommentDto(any(Comment.class));
        verify(kafkaMessageService, times(1)).sendCommentCreatedMessage(testUser.getId(), testPost.getId());

    }

}

