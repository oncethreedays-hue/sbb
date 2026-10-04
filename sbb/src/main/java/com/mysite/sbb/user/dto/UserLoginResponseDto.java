package com.mysite.sbb.user.dto;

import com.mysite.sbb.user.entity.SiteUser;

import lombok.Getter;

@Getter
public class UserLoginResponseDto {
    private final String accessToken;
    private final String refreshToken;
    private final String username;
    private final String email;

    public UserLoginResponseDto(String accessToken, String refreshToken, SiteUser user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.username = user.getUsername();
        this.email = user.getEmail();
    }
}
