package com.andimm09.discordclone.web;

import com.andimm09.discordclone.domain.User;
import com.andimm09.discordclone.security.CurrentUserId;
import com.andimm09.discordclone.security.TokenService;
import com.andimm09.discordclone.service.UserService;
import com.andimm09.discordclone.web.dto.Requests.Login;
import com.andimm09.discordclone.web.dto.Requests.RegisterUser;
import com.andimm09.discordclone.web.dto.Responses.LoginResponse;
import com.andimm09.discordclone.web.dto.Responses.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final TokenService tokenService;

    public AuthController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterUser body) {
        return UserResponse.from(userService.register(body.username(), body.email(), body.password()));
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody Login body) {
        User user = userService.authenticate(body.username(), body.password());
        return new LoginResponse(tokenService.issue(user), "Bearer", tokenService.ttlSeconds(), UserResponse.from(user));
    }

    /** Cine sunt eu? (userul din token) */
    @GetMapping("/me")
    public UserResponse me(@CurrentUserId Long userId) {
        return UserResponse.from(userService.get(userId));
    }
}
