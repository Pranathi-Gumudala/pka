package com.pranathi.pka.controller;

import com.pranathi.pka.entity.User;
import com.pranathi.pka.security.JwtUtil;
import com.pranathi.pka.service.UserService;
import com.pranathi.pka.dto.LoginResponse;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
private final JwtUtil jwtUtil;

public AuthController(UserService userService,
                      JwtUtil jwtUtil) {

    this.userService = userService;
    this.jwtUtil = jwtUtil;
}

    @PostMapping("/register")
    public User register(@Valid @RequestBody User user) {
        return userService.register(user);
    }
   @PostMapping("/login")
public LoginResponse login(@RequestBody User user) {

    User loggedInUser = userService.login(
            user.getEmail(),
            user.getPassword());

    String token = jwtUtil.generateToken(loggedInUser.getEmail());

    return new LoginResponse(token);
}
}
