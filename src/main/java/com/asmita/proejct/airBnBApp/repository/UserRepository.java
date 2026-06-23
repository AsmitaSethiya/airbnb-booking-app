package com.asmita.proejct.airBnBApp.repository;

import com.asmita.proejct.airBnBApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
}
