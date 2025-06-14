package com.post_hub.iam_service.model.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchDto {
    private Integer id;
    private String username;
    private String email;
    private LocalDateTime created;
    private Boolean isDeleted;
}
