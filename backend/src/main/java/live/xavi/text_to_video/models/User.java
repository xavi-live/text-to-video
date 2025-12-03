package live.xavi.text_to_video.models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String password;
    private String role = "ROLE_USER";

    @OneToMany(mappedBy = "user")
    private List<Video> videos;

    private LocalDateTime createdDate;
}
