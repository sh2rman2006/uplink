package tech.sh2rman.coreservice.domain.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sh2rman.coreservice.domain.chat.dto.CreateChatRequest;
import tech.sh2rman.coreservice.domain.chat.entity.Chat;
import tech.sh2rman.coreservice.domain.chat.entity.ChatParticipant;
import tech.sh2rman.coreservice.domain.chat.exception.ChatBadRequestException;
import tech.sh2rman.coreservice.domain.chat.model.ChatRole;
import tech.sh2rman.coreservice.domain.chat.model.ChatType;
import tech.sh2rman.coreservice.domain.chat.model.ParticipantStatus;
import tech.sh2rman.coreservice.domain.chat.repository.ChatParticipantRepository;
import tech.sh2rman.coreservice.domain.chat.repository.ChatRepository;
import tech.sh2rman.coreservice.domain.chat.service.ChatService;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;
import tech.sh2rman.coreservice.domain.user.repository.UserProfileRepository;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public Chat createChat(UUID creatorUserId, CreateChatRequest req) {
        if (req == null || req.type() == null) {
            throw new ChatBadRequestException("type is required");
        }

        return switch (req.type()) {
            case PRIVATE -> createPrivateChat(creatorUserId, req);
            case GROUP -> createGroupChat(creatorUserId, req);
            case CHANNEL, SECRET -> throw new ChatBadRequestException(req.type() + " not supported yet");
        };
    }

    private Chat createPrivateChat(UUID creatorUserId, CreateChatRequest req) {
        Set<UUID> members = req.memberUserIds();

        if (members == null || members.size() != 1 || members.contains(creatorUserId)) {
            throw new ChatBadRequestException("PRIVATE chat requires exactly one other participant");
        }

        UUID peerUserId = members.iterator().next();

        UserProfileEntity creator = userProfileRepository.findById(creatorUserId)
                .orElseThrow(() -> new ChatBadRequestException("Creator profile not found"));

        UserProfileEntity peer = userProfileRepository.findById(peerUserId)
                .orElseThrow(() -> new ChatBadRequestException("Peer profile not found"));

        Chat existing = chatRepository.findChatByTypeAndBothUsers(ChatType.PRIVATE, creatorUserId, peerUserId).orElse(null);
        if (existing != null) {
            return existing;
        }

        OffsetDateTime now = OffsetDateTime.now();

        Chat chat = Chat.builder()
                .type(ChatType.PRIVATE)

                .description(req.description())
                .createdBy(creator)

                .isPrivate(true)
                .isPublic(false)

                .allowSendMessages(true)
                .allowSendMedia(true)
                .allowAddUsers(false)
                .allowPinMessages(true)
                .allowChangeInfo(false)
                .isEncrypted(false)

                .createdAt(now)
                .updatedAt(now)
                .build();

        Chat saved = chatRepository.save(chat);

        saveParticipant(saved, creator, creator, ChatRole.MEMBER, now);
        saveParticipant(saved, peer, creator, ChatRole.MEMBER, now);

        return saved;
    }

    private Chat createGroupChat(UUID creatorUserId, CreateChatRequest req) {
        UserProfileEntity creator = userProfileRepository.findById(creatorUserId)
                .orElseThrow(() -> new ChatBadRequestException("Creator profile not found"));

        OffsetDateTime now = OffsetDateTime.now();

        Chat chat = Chat.builder()
                .type(ChatType.GROUP)
                .title(req.title())
                .description(req.description())
                .createdBy(creator)

                .isPrivate(false)
                .isPublic(false)

                .allowSendMessages(true)
                .allowSendMedia(true)
                .allowAddUsers(true)
                .allowPinMessages(true)
                .allowChangeInfo(true)
                .isEncrypted(false)

                .createdAt(now)
                .updatedAt(now)
                .build();

        Chat saved = chatRepository.save(chat);

        saveParticipant(saved, creator, creator, ChatRole.OWNER, now);

        Set<UUID> members = normalizeMembers(req.memberUserIds(), creatorUserId);
        for (UUID memberId : members) {
            UserProfileEntity member = userProfileRepository.findById(memberId)
                    .orElseThrow(() -> new ChatBadRequestException("Member profile not found: " + memberId));
            saveParticipant(saved, member, creator, ChatRole.MEMBER, now);
        }

        return saved;
    }

    private Set<UUID> normalizeMembers(Set<UUID> memberUserIds, UUID creatorUserId) {
        if (memberUserIds == null || memberUserIds.isEmpty()) {
            return Set.of();
        }
        Set<UUID> set = new LinkedHashSet<>(memberUserIds);
        set.remove(creatorUserId);
        return set;
    }

    private void saveParticipant(Chat chat,
                                 UserProfileEntity user,
                                 UserProfileEntity invitedBy,
                                 ChatRole role,
                                 OffsetDateTime now) {

        ChatParticipant p = new ChatParticipant();
        p.setChat(chat);
        p.setUser(user);

        p.setRole(role);
        p.setStatus(ParticipantStatus.ACTIVE);

        p.setCanSendMessages(true);
        p.setCanSendMedia(true);
        p.setCanAddUsers(role == ChatRole.OWNER);
        p.setCanPinMessages(true);
        p.setCanChangeInfo(role == ChatRole.OWNER);
        p.setCanDeleteMessages(role == ChatRole.OWNER);
        p.setCanBanUsers(role == ChatRole.OWNER);

        p.setJoinedAt(now);
        p.setInvitedBy(invitedBy);

        p.setCreatedAt(now);
        p.setUpdatedAt(now);

        chatParticipantRepository.save(p);
    }
}

