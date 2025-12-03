package live.xavi.text_to_video.services.externalApis;

import live.xavi.text_to_video.dtos.ChunksApiResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class VideosApi {

    private final WebClient client;

    @Value("${pexels.api.key}")
    private String pexelsApiKey;

    public VideosApi(@Value("${pexels.api.key}") String apiKey) {
        this.client = WebClient.builder()
                .baseUrl("https://api.pexels.com")
                .defaultHeader("Authorization", apiKey) // put your key here
                .build();
    }

    public ChunksApiResponseDto getFirstVideo(String query) {
        Mono<ChunksApiResponseDto> mono = client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/videos/search")
                        .queryParam("query", query)
                        .queryParam("per_page", 1)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(map -> {
                    Map firstVideo = ((java.util.List<Map>) map.get("videos")).get(0);
                    Map videoFile = ((java.util.List<Map>) firstVideo.get("video_files")).get(0);

                    ChunksApiResponseDto dto = new ChunksApiResponseDto();
                    dto.setMediaUrl((String) videoFile.get("link")); // set only mediaUrl
                    return dto;
                });

        return mono.block(); // blocking for CLI / simple usage
    }
}
