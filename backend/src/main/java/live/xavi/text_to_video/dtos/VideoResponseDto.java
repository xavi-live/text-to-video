package live.xavi.text_to_video.dtos;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
public class VideoResponseDto {
    private Long id;
    private String title;
    private String fileUrl;
    private Duration duration;
    private Long sizeInBytes;
    private Long views;
    private String instructions;
    private LocalDateTime createdDate;
}