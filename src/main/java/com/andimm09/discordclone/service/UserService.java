package com.andimm09.discordclone.service;

import com.andimm09.discordclone.domain.User;
import com.andimm09.discordclone.exception.ConflictException;
import com.andimm09.discordclone.exception.NotFoundException;
import com.andimm09.discordclone.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(String username, String email, String rawPassword) {
        if (users.existsByUsername(username)) {
            throw new ConflictException("Username '" + username + "' is already taken");
        }
        if (users.existsByEmail(email)) {
            throw new ConflictException("Email '" + email + "' is already registered");
        }
        return users.save(new User(username, email, passwordEncoder.encode(rawPassword)));
    }

    /** Acelasi mesaj si pentru user inexistent si pentru parola gresita, ca sa nu aflam care useri exista. */
    @Transactional(readOnly = true)
    public User authenticate(String username, String rawPassword) {
        return users.findByUsername(username)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
    }

    @Transactional(readOnly = true)
    public User get(Long id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("User", id));
    }
}
