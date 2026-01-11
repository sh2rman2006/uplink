package tech.sh2rman.coreservice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tech.sh2rman.coreservice.domain.user.dto.MyProfileResponse;
import tech.sh2rman.coreservice.domain.user.dto.UpdateMyProfileRequest;
import tech.sh2rman.coreservice.domain.user.dto.UserProfileSearchItemResponse;
import tech.sh2rman.coreservice.domain.user.service.UserProfileService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Пользователи и профили")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @Operation(summary = "Поиск пользователей (username/displayName)")
    @GetMapping("/search")
    public Page<UserProfileSearchItemResponse> search(
            @RequestParam String q,
            Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID me = UUID.fromString(jwt.getSubject());
        return userProfileService.search(q, me, pageable);
    }

    @Operation(summary = "Мой профиль")
    @GetMapping("/me")
    public MyProfileResponse me(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID me = UUID.fromString(jwt.getSubject());
        return userProfileService.getMe(me);
    }

    @Operation(summary = "Обновить мой профиль")
    @PatchMapping("/me")
    public MyProfileResponse updateMe(
            @Valid @RequestBody UpdateMyProfileRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID me = UUID.fromString(jwt.getSubject());
        return userProfileService.updateMe(me, req);
    }

}
