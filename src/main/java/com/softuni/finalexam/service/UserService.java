package com.softuni.finalexam.service;

import com.softuni.finalexam.models.entity.User;
import com.softuni.finalexam.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service

public class UserService {


    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public User getById(UUID userId) {

        return userRepository.findById(userId).orElse(null);
    }
}
