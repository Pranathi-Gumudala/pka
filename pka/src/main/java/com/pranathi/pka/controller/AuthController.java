package com.pranathi.pka.controller;

import com.pranathi.pka.entity.User;
import com.pranathi.pka.service.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public User register(@Valid @RequestBody User user) {
        return userService.register(user);
    }
    @PostMapping("/login")
public User login(@RequestBody User user) {
    return userService.login(
            user.getEmail(),
            user.getPassword());
}
}
