package com.post_hub.iam_service.model.request.comment;

import com.post_hub.iam_service.model.enums.CommentSortField;
import com.post_hub.iam_service.model.enums.PostSortField;
import lombok.Data;

import java.io.Serializable;
@Data
public class CommentSearchRequest implements Serializable {
    private Integer postId;
    private String message;
    private String createdBy;

    private Boolean deleted;
    private String keyword;
    private CommentSortField sortField;


}


