package live.xavi.text_to_video.controllers;

import live.xavi.text_to_video.dtos.ChunksApiResponseDto;
import live.xavi.text_to_video.dtos.UpdateViewCountRequest;
import live.xavi.text_to_video.dtos.VideoGenerateRequest;
import live.xavi.text_to_video.dtos.VideoResponseDto;
import live.xavi.text_to_video.models.User;
import live.xavi.text_to_video.models.Video;
import live.xavi.text_to_video.services.UserService;
import live.xavi.text_to_video.services.VideoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/videos")
@AllArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private UserService userService;


    @PostMapping("/generate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<VideoResponseDto> generateVideo (@RequestBody VideoGenerateRequest videoGenerateRequest, Principal principal) throws IOException {

        User user = userService.findByUsername(principal.getName());
        return ResponseEntity.ok(videoService.convertToVideoResponseDto(videoService.createVideo(videoGenerateRequest.getInstructions(), user)));
    }

    @PutMapping("/update-view-count")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateViewCount(@RequestBody UpdateViewCountRequest request) {

        Video video = videoService.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + request.getId()));

        // Increase views
        video.setViews(video.getViews() + 1);

        // Save
        videoService.save(video);

        return ResponseEntity.ok("view count updated successfully!");
    }

}
