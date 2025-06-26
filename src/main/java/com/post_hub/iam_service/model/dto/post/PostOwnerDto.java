package com.post_hub.iam_service.model.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostOwnerDto implements Serializable {
    private Integer id;
    private String username;
    private String email;
}
