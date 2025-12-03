package live.xavi.text_to_video.dtos;

import jakarta.persistence.Id;
import lombok.Data;

import java.util.UUID;

@Data
public class ChunksApiResponseDto {

    private UUID id = UUID.randomUUID();
    private int chunkNumber;
    private String mediaDescription;
    private String storyText;
    private String mediaUrl;
    private String audioUrl;
}
