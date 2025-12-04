package live.xavi.text_to_video.controllers;

import live.xavi.text_to_video.dtos.UserProfileResponseDto;
import live.xavi.text_to_video.models.User;
import live.xavi.text_to_video.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserDataController {

    private final UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileResponseDto> profile (Principal principal) {
        User user = userService.findByUsername(principal.getName());
        UserProfileResponseDto userProfileResponseDto = userService.getUserProfile(user);
        return ResponseEntity.ok(userProfileResponseDto);
    }
}
