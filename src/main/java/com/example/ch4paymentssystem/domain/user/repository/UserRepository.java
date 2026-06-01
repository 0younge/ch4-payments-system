package com.example.ch4paymentssystem.domain.user.repository;

import com.example.ch4paymentssystem.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
