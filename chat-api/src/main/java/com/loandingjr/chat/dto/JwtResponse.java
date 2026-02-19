package com.loandingjr.chat.dto;

import lombok.Builder;

@Builder
public record JwtResponse (
        String token,
        String id,
        String username,
        String email
) {
}
