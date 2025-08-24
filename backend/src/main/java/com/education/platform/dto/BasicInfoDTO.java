package com.education.platform.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BasicInfoDTO {
    private Long userId;
    private String username;
    private String email;
    private String realName;
    private String role;
    private LocalDateTime createTime;
}
