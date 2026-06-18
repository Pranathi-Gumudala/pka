package com.pranathi.pka.service;

import com.pranathi.pka.entity.User;
import com.pranathi.pka.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
        throw new EmailAlreadyExistsException("Email already registered");
    }

    return userRepository.save(user);
    }

public User login(String email, String password) {

    return userRepository
            .findByEmailAndPassword(email, password)
            .orElseThrow(() ->
                new RuntimeException("Invalid email or password"));
}
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
}