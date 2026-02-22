package com.loandingjr.chat.dto.security;

import lombok.Builder;

@Builder
public record JwtResponse (
        String token,
        String id,
        String username,
        String email
) {
}
