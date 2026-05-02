package com.booknest.auth.dto.request;

import lombok.Data;

@Data
public class RegisterRequest {

    private String fullName;
    private String email;
    private String password;
    private Long mobile;

}