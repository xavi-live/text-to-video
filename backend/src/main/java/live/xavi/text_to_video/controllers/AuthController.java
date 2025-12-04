package live.xavi.text_to_video.controllers;

import live.xavi.text_to_video.dtos.LoginRequest;
import live.xavi.text_to_video.dtos.RegisterRequest;
import live.xavi.text_to_video.dtos.UserProfileResponseDto;
import live.xavi.text_to_video.models.User;
import live.xavi.text_to_video.models.Video;
import live.xavi.text_to_video.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(userService.authenticateUser(loginRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser (@RequestBody RegisterRequest registerRequest) {
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setUsername(registerRequest.getUsername());
        user.setPassword(registerRequest.getPassword());
        user.setRole("ROLE_USER");
        user.setCreatedDate(LocalDateTime.now());
        userService.registerUser(user);
        return ResponseEntity.ok("User registered successfully");
    }


}
