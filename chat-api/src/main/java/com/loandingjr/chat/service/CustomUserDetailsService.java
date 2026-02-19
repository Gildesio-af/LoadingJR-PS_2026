package com.loandingjr.chat.service;

import com.loandingjr.chat.model.User;
import com.loandingjr.chat.repository.UserRepository;
import com.loandingjr.chat.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Attempting to load user by username: {}", username);

        User user = repository.findByEmailOrUsername(username, username)
                .orElseThrow(() -> {
                    log.error("Authentication failed. User not found in database: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        log.trace("User found. ID: {}, Username: {}", user.getId(), user.getUsername());

        return CustomUserDetails.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
//                .authProvider(user.getAuthProvider())
                .email(user.getEmail())
                .build();
    }
}