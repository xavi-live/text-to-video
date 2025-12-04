package live.xavi.text_to_video.dtos;

import jakarta.persistence.OneToMany;
import live.xavi.text_to_video.models.Video;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserProfileResponseDto {
        private String username;
        private String email;
        private List<VideoResponseDto> videos;
        private LocalDateTime createdDate;
}

