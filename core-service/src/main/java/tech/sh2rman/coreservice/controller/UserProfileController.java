package tech.sh2rman.coreservice.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tech.sh2rman.coreservice.domain.user.dto.MyProfileResponse;
import tech.sh2rman.coreservice.domain.user.dto.UpdateMyProfileRequest;
import tech.sh2rman.coreservice.domain.user.dto.UserProfileSearchItemResponse;
import tech.sh2rman.coreservice.domain.user.mapper.UserProfileMapper;
import tech.sh2rman.coreservice.domain.user.mapper.UserProfileSearchItemMapper;
import tech.sh2rman.coreservice.domain.user.service.UserProfileService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Пользователи и профили")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final UserProfileSearchItemMapper userProfileSearchItemMapper;
    private final UserProfileMapper userProfileMapper;

    @Operation(summary = "Поиск пользователей (username/displayName)")
    @GetMapping("/search")
    public Page<UserProfileSearchItemResponse> search(
            @RequestParam String q,
            Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID me = UUID.fromString(jwt.getSubject());
        return userProfileService.search(q, me, pageable).map(userProfileSearchItemMapper::toDto);
    }

    @Operation(summary = "Мой профиль")
    @GetMapping("/me")
    public MyProfileResponse me(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID me = UUID.fromString(jwt.getSubject());
        return userProfileMapper.toDto(userProfileService.getMe(me));
    }

    @Operation(summary = "Обновить мой профиль")
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MyProfileResponse updateMe(
            @Valid @ModelAttribute  UpdateMyProfileRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID me = UUID.fromString(jwt.getSubject());
        return userProfileMapper.toDto(userProfileService.updateMe(me, req));
    }

}
