package live.xavi.text_to_video.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import live.xavi.text_to_video.dtos.LoginRequest;
import live.xavi.text_to_video.dtos.RegisterRequest;
import live.xavi.text_to_video.models.User;
import live.xavi.text_to_video.dtos.JwtAuthenticationResponse;
import live.xavi.text_to_video.security.jwt.JwtUtils;
import live.xavi.text_to_video.services.UserDetailsImpl;
import live.xavi.text_to_video.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest,
                                       HttpServletResponse response) {

        JwtAuthenticationResponse authResponse = userService.authenticateUser(loginRequest);

        String refreshToken = jwtUtils.generateRefreshToken(authResponse.getUsername());

        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/api/auth/refresh");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(401).body("No refresh token found");
        }

        if (!jwtUtils.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body("Invalid refresh token");
        }

        String username = jwtUtils.getUsernameFromJwtToken(refreshToken);

        User user = userService.findByUsername(username);
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        String newAccessToken = jwtUtils.generateToken(userDetails);

        return ResponseEntity.ok(new JwtAuthenticationResponse(newAccessToken, username));
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
