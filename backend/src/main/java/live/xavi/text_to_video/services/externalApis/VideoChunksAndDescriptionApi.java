package live.xavi.text_to_video.services.externalApis;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import live.xavi.text_to_video.dtos.ChunksApiResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class VideoChunksAndDescriptionApi {

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.api.prompt.rules}")
    private String promptRules;

    public List<ChunksApiResponseDto> QueryVideoDescriptiveChunks(String videoInstructions) throws IOException {

        // Initialize the client
        AnthropicClient client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();

        // Prepare the request parameters
        MessageCreateParams params = MessageCreateParams.builder()
                .model("claude-sonnet-4-5-20250929")
                .maxTokens(1000)
                .addUserMessage(promptRules + videoInstructions)
                .build();

        // Send the request and get the response
        Message message = client.messages().create(params);

        // Extract the raw JSON from the response
        String responseContent = message.content().toString();
        String jsonResponse = extractJsonFromContent(responseContent);

        // Map the JSON response to DTOs
        return mapJsonToAiChunksApiResponseDto(jsonResponse);
    }

    // Extracts the JSON part from the response string
    private String extractJsonFromContent(String content) {
        int jsonStartIndex = content.indexOf("```json") + 7;  // Length of "```json" is 7
        int jsonEndIndex = content.indexOf("```", jsonStartIndex);

        if (jsonStartIndex != -1 && jsonEndIndex != -1) {
            return content.substring(jsonStartIndex, jsonEndIndex).trim();
        }
        return "";
    }

    // Maps the extracted JSON to a list of AiChunksApiResponseDto
    private List<ChunksApiResponseDto> mapJsonToAiChunksApiResponseDto(String jsonResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // Parse the JSON string into a JsonNode
        JsonNode rootNode = mapper.readTree(jsonResponse);

        // Extract the "story_chunks" array
        JsonNode storyChunksNode = rootNode.get("story_chunks");

        List<ChunksApiResponseDto> aiChunksList = new ArrayList<>();

        // Iterate through the story chunks and map each chunk to AiChunksApiResponseDto
        if (storyChunksNode != null && storyChunksNode.isArray()) {
            for (JsonNode chunkNode : storyChunksNode) {
                ChunksApiResponseDto dto = new ChunksApiResponseDto();
                dto.setChunkNumber(chunkNode.get("chunk_number").asInt());
                dto.setStoryText(chunkNode.get("story_text").asText());
                dto.setMediaDescription(chunkNode.get("image_description").asText());
                aiChunksList.add(dto);
            }
        }
        return aiChunksList;
    }
}
