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
    private String addressStreet;
    private String addressCity;
    private String addressState;
    private String addressPincode;

    public UserResponse(User user){

        this.userId = user.getUserId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.mobile = user.getMobile();
        this.profileImageUrl = user.getProfileImageUrl();
        this.addressStreet = user.getAddressStreet();
        this.addressCity = user.getAddressCity();
        this.addressState = user.getAddressState();
        this.addressPincode = user.getAddressPincode();
    }
}