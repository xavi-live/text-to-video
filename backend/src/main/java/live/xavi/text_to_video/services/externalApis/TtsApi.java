package live.xavi.text_to_video.services.externalApis;

import live.xavi.text_to_video.dtos.ChunksApiResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class TtsApi {

    private final WebClient client;

    @Value("${elevenlabs.api.key}")
    private String elevenLabsApiKey;

    public TtsApi(@Value("${elevenlabs.api.key}") String apiKey) {
        this.client = WebClient.builder()
                .baseUrl("https://api.elevenlabs.io")
                .defaultHeader("xi-api-key", apiKey)  // correct header
                .build();
    }

    public byte[] getAudio(String text) {

        Map<String, Object> body = new HashMap<>();
        body.put("text", text);
        body.put("model_id", "eleven_multilingual_v2");

        return client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/text-to-speech/JBFqnCBsd6RMkjVDRZzb")
                        .queryParam("output_format", "mp3_44100_128")
                        .build())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(byte[].class)  // ElevenLabs returns audio bytes
                .block();
    }
}
