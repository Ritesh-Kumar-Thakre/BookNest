package com.booknest.auth.dto.response;

import com.booknest.user.entity.Role;
import com.booknest.user.entity.User;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserResponse {

    private Integer userId;
    private String fullName;
    private String email;
    private Role role;
    private Long mobile;
    private String profileImageUrl;

    public UserResponse(User user){

        this.userId = user.getUserId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.mobile = user.getMobile();
        this.profileImageUrl = user.getProfileImageUrl();
    }
}