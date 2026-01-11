package tech.sh2rman.coreservice.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MyProfileResponse {

    private UUID id;
    private String username;
    private String email;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private Long avatarVersion;
    private Boolean isProfilePublic;
    private String status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

}
