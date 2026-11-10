package com.outfitai.user;

import com.outfitai.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserResponse me(@AuthenticationPrincipal AuthenticatedUser caller) {
        return userService.getProfile(caller.id());
    }

    @PutMapping
    public UserResponse update(@AuthenticationPrincipal AuthenticatedUser caller,
                               @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(caller.id(), request);
    }
}
