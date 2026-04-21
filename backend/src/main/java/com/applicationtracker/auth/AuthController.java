package com.applicationtracker.auth;

import com.applicationtracker.common.NotFoundException;
import com.applicationtracker.user.UserRepository;
import com.applicationtracker.user.UserSummary;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final UserRepository users;

    public AuthController(AuthService authService, UserRepository users) {
        this.authService = authService;
        this.users = users;
    }

    @PostMapping("/auth/register")
    AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/auth/login")
    AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    UserSummary me(Principal principal) {
        return users.findByEmailIgnoreCase(principal.getName())
                .map(UserSummary::from)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
