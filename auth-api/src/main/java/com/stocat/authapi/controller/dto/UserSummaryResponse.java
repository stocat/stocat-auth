package com.stocat.authapi.controller.dto;

import com.stocat.authapi.service.dto.UserDto;

public record UserSummaryResponse(
        Long id,
        String email,
        String nickname
) {
    public static UserSummaryResponse from(UserDto user) {
        return new UserSummaryResponse(
                user.id(),
                user.email(),
                user.nickname()
        );
    }
}
