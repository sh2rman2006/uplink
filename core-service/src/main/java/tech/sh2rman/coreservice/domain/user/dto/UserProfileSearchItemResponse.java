package tech.sh2rman.coreservice.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserProfileSearchItemResponse {

    private UUID id;
    private String username;
    private String displayName;
    private String avatarUrl;
    private Long avatarVersion;
    private Boolean isProfilePublic;

}
