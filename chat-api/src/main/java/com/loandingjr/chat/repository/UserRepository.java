package com.loandingjr.chat.repository;

import com.loandingjr.chat.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmailOrUsername(String email, String username);

    Page<User> findAllByUsernameContainingIgnoreCase(String username, Pageable pageable);
    Optional<User> findByEmailContainingIgnoreCase(String email);

    @Query("UPDATE User u SET u.isActive = false WHERE u.id = :id")
    @Modifying
    int deactivateUserById(String id);
}
