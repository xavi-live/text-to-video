package live.xavi.text_to_video.services;

import live.xavi.text_to_video.dtos.LoginRequest;
import live.xavi.text_to_video.dtos.UserProfileResponseDto;
import live.xavi.text_to_video.dtos.VideoResponseDto;
import live.xavi.text_to_video.models.User;
import live.xavi.text_to_video.repositories.UserRepository;
import live.xavi.text_to_video.dtos.JwtAuthenticationResponse;
import live.xavi.text_to_video.security.jwt.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public User registerUser (User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public JwtAuthenticationResponse authenticateUser (LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateToken(userDetails);

        return new JwtAuthenticationResponse(jwt, userDetails.getUsername());

    }

    public User findByUsername(String name) {
        return userRepository.findByUsername(name).orElseThrow(
                () -> new UsernameNotFoundException("User not found with username: " + name)
        );
    }

    public UserProfileResponseDto getUserProfile(User user) {
        UserProfileResponseDto dto = new UserProfileResponseDto();

        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setCreatedDate(user.getCreatedDate());

        List<VideoResponseDto> videoDtos = user.getVideos()
                .stream()
                .map(video -> {
                    VideoResponseDto v = new VideoResponseDto();
                    v.setId(video.getId());
                    v.setTitle(video.getTitle());
                    v.setFileUrl(video.getFileUrl());
                    v.setDuration(video.getDuration());
                    v.setSizeInBytes(video.getSizeInBytes());
                    v.setViews(video.getViews());
                    v.setInstructions(video.getInstructions());
                    v.setCreatedDate(video.getCreatedDate());
                    return v;
                })
                .toList();

        dto.setVideos(videoDtos);

        return dto;
    }

}
