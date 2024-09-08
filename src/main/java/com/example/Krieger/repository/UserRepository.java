package com.example.Krieger.repository;

import com.example.Krieger.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JPA repository for User
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}