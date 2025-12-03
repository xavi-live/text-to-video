package live.xavi.text_to_video.dtos;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}