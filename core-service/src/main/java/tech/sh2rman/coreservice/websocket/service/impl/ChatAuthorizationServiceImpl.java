package tech.sh2rman.coreservice.websocket.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import tech.sh2rman.coreservice.domain.chat.entity.Chat;
import tech.sh2rman.coreservice.domain.chat.entity.ChatParticipant;
import tech.sh2rman.coreservice.domain.chat.model.ParticipantStatus;
import tech.sh2rman.coreservice.domain.chat.repository.ChatParticipantRepository;
import tech.sh2rman.coreservice.domain.chat.repository.ChatRepository;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;
import tech.sh2rman.coreservice.domain.user.repository.UserProfileRepository;
import tech.sh2rman.coreservice.websocket.service.ChatAuthorizationService;

import java.time.OffsetDateTime;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class ChatAuthorizationServiceImpl implements ChatAuthorizationService {

    private final UserProfileRepository userProfileRepository;
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Override
    public void assertCanSubscribe(UUID chatId, String keycloakSub) {
        UUID userId = parseUserId(keycloakSub);

        UserProfileEntity user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("User profile not found"));

        ChatParticipant participant = chatParticipantRepository.findByChatIdAndUserId(chatId, user.getId())
                .orElseThrow(() -> new AccessDeniedException("Not a chat participant"));

        if (participant.getStatus() != ParticipantStatus.ACTIVE) {
            throw new AccessDeniedException("Participant not active");
        }

        if (participant.getBannedUntil() != null && participant.getBannedUntil().isAfter(OffsetDateTime.now())) {
            throw new AccessDeniedException("Participant is banned");
        }
    }

    @Override
    public void assertCanSend(UUID chatId, String keycloakSub) {
        UUID userId = parseUserId(keycloakSub);

        UserProfileEntity user = userProfileRepository.findById(userId)
                .orElseThrow(() -> new AccessDeniedException("User profile not found"));

        ChatParticipant participant = chatParticipantRepository.findByChatIdAndUserId(chatId, user.getId())
                .orElseThrow(() -> new AccessDeniedException("Not a chat participant"));

        if (participant.getStatus() != ParticipantStatus.ACTIVE) {
            throw new AccessDeniedException("Participant not active");
        }

        if (participant.getBannedUntil() != null && participant.getBannedUntil().isAfter(OffsetDateTime.now())) {
            throw new AccessDeniedException("Participant is banned");
        }

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new AccessDeniedException("Chat not found"));

        if (Boolean.FALSE.equals(chat.getAllowSendMessages())) {
            throw new AccessDeniedException("Chat does not allow sending messages");
        }

        if (Boolean.FALSE.equals(participant.getCanSendMessages())) {
            throw new AccessDeniedException("No permission to send messages");
        }
    }

    private UUID parseUserId(String keycloakSub) {
        try {
            return UUID.fromString(keycloakSub);
        } catch (Exception e) {
            throw new AccessDeniedException("Invalid user id in token (sub is not UUID)");
        }
    }
}
