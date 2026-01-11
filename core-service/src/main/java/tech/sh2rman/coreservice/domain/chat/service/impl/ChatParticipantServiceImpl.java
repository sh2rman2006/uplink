package tech.sh2rman.coreservice.domain.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sh2rman.coreservice.domain.chat.dto.ChatParticipantResponse;
import tech.sh2rman.coreservice.domain.chat.entity.Chat;
import tech.sh2rman.coreservice.domain.chat.entity.ChatParticipant;
import tech.sh2rman.coreservice.domain.chat.exception.ChatBadRequestException;
import tech.sh2rman.coreservice.domain.chat.exception.MessageForbiddenException;
import tech.sh2rman.coreservice.domain.chat.model.ChatRole;
import tech.sh2rman.coreservice.domain.chat.model.ChatType;
import tech.sh2rman.coreservice.domain.chat.model.ParticipantStatus;
import tech.sh2rman.coreservice.domain.chat.repository.ChatParticipantRepository;
import tech.sh2rman.coreservice.domain.chat.service.ChatParticipantService;
import tech.sh2rman.coreservice.domain.chat.service.MessageAccessService;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatParticipantServiceImpl implements ChatParticipantService {

    private static final Set<ParticipantStatus> ACTIVE_STATUSES = EnumSet.of(ParticipantStatus.ACTIVE, ParticipantStatus.MUTED);

    private final MessageAccessService access;
    private final ChatParticipantRepository chatParticipantRepository;

    @Override
    @Transactional
    public void add(UUID chatId, UUID actorUserId, UUID targetUserId, ChatRole role) {

        Chat chat = access.requireChat(chatId);
        ChatParticipant me = access.requireParticipant(chatId, actorUserId);

        assertCanManageParticipants(chat, me);

        if (chat.getType() == ChatType.PRIVATE) {
            throw new MessageForbiddenException("Not allowed for PRIVATE chat");
        }

        ChatRole finalRole = (role == null) ? ChatRole.MEMBER : role;

        UserProfileEntity actor = access.requireUser(actorUserId);
        UserProfileEntity targetUser = access.requireUser(targetUserId);

        OffsetDateTime now = OffsetDateTime.now();

        Optional<ChatParticipant> existingOpt = chatParticipantRepository.findByChatIdAndUserId(chatId, targetUserId);
        if (existingOpt.isPresent()) {
            ChatParticipant existing = existingOpt.get();

            if (ACTIVE_STATUSES.contains(existing.getStatus())) {
                return;
            }

            if (existing.getStatus() == ParticipantStatus.BANNED) {
                throw new MessageForbiddenException("Participant is banned");
            }

            existing.setStatus(ParticipantStatus.ACTIVE);
            existing.setLeftAt(null);
            existing.setRole(finalRole);
            applyRolePreset(existing, finalRole);
            existing.setInvitedBy(actor);
            existing.setUpdatedAt(now);
            chatParticipantRepository.save(existing);
            return;
        }

        ChatParticipant p = new ChatParticipant();
        p.setChat(chat);
        p.setUser(targetUser);

        p.setRole(finalRole);
        p.setStatus(ParticipantStatus.ACTIVE);

        applyRolePreset(p, finalRole);

        p.setJoinedAt(now);
        p.setInvitedBy(actor);

        p.setCreatedAt(now);
        p.setUpdatedAt(now);

        chatParticipantRepository.save(p);
    }

    @Override
    @Transactional
    public void kick(UUID chatId, UUID actorUserId, UUID targetUserId) {

        Chat chat = access.requireChat(chatId);
        ChatParticipant me = access.requireParticipant(chatId, actorUserId);

        assertCanManageParticipants(chat, me);

        if (chat.getType() == ChatType.PRIVATE) {
            throw new MessageForbiddenException("Not allowed for PRIVATE chat");
        }

        ChatParticipant target = access.requireParticipant(chatId, targetUserId);

        if (!ACTIVE_STATUSES.contains(target.getStatus())) {
            throw new ChatBadRequestException("Participant not active");
        }

        assertNotLastOwner(chatId, target);

        OffsetDateTime now = OffsetDateTime.now();
        target.setStatus(ParticipantStatus.KICKED);
        target.setLeftAt(now);
        target.setUpdatedAt(now);

        chatParticipantRepository.save(target);
    }

    @Override
    @Transactional
    public void leave(UUID chatId, UUID actorUserId) {

        ChatParticipant me = access.requireParticipant(chatId, actorUserId);

        if (!ACTIVE_STATUSES.contains(me.getStatus())) {
            throw new MessageForbiddenException("Participant not active");
        }

        assertNotLastOwner(chatId, me);

        OffsetDateTime now = OffsetDateTime.now();
        me.setStatus(ParticipantStatus.LEFT);
        me.setLeftAt(now);
        me.setUpdatedAt(now);

        chatParticipantRepository.save(me);
    }

    @Override
    @Transactional
    public void changeRole(UUID chatId, UUID actorUserId, UUID targetUserId, ChatRole role) {

        if (role == null) {
            throw new ChatBadRequestException("role is required");
        }

        Chat chat = access.requireChat(chatId);
        ChatParticipant me = access.requireParticipant(chatId, actorUserId);

        assertCanManageParticipants(chat, me);

        if (chat.getType() == ChatType.PRIVATE) {
            throw new MessageForbiddenException("Not allowed for PRIVATE chat");
        }

        ChatParticipant target = access.requireParticipant(chatId, targetUserId);

        if (!ACTIVE_STATUSES.contains(target.getStatus())) {
            throw new ChatBadRequestException("Participant not active");
        }

        if (target.getRole() == ChatRole.OWNER && role != ChatRole.OWNER) {
            assertNotLastOwner(chatId, target);
        }

        OffsetDateTime now = OffsetDateTime.now();
        target.setRole(role);
        applyRolePreset(target, role);
        target.setUpdatedAt(now);

        chatParticipantRepository.save(target);
    }

    private void assertCanManageParticipants(Chat chat, ChatParticipant me) {

        if (!ACTIVE_STATUSES.contains(me.getStatus())) {
            throw new MessageForbiddenException("Participant not active");
        }

        if (me.getRole() != ChatRole.OWNER && me.getRole() != ChatRole.ADMIN) {
            throw new MessageForbiddenException("Not allowed");
        }

        if (Boolean.FALSE.equals(chat.getAllowAddUsers())) {
            throw new MessageForbiddenException("Chat does not allow adding users");
        }

        if (Boolean.FALSE.equals(me.getCanAddUsers())) {
            throw new MessageForbiddenException("Not allowed to add users");
        }
    }

    private void assertNotLastOwner(UUID chatId, ChatParticipant target) {

        if (target.getRole() != ChatRole.OWNER) {
            return;
        }

        if (!ACTIVE_STATUSES.contains(target.getStatus())) {
            return;
        }

        long ownersActive = chatParticipantRepository.countByChatIdAndRoleAndStatus(chatId, ChatRole.OWNER, ParticipantStatus.ACTIVE);
        if (ownersActive <= 1) {
            throw new ChatBadRequestException("Cannot remove/leave: last OWNER");
        }
    }

    private void applyRolePreset(ChatParticipant p, ChatRole role) {

        switch (role) {
            case OWNER, ADMIN -> {
                p.setCanSendMessages(true);
                p.setCanSendMedia(true);
                p.setCanAddUsers(true);
                p.setCanPinMessages(true);
                p.setCanChangeInfo(true);
                p.setCanDeleteMessages(true);
                p.setCanBanUsers(true);
            }
            case MEMBER -> {
                p.setCanSendMessages(true);
                p.setCanSendMedia(true);
                p.setCanAddUsers(false);
                p.setCanPinMessages(true);
                p.setCanChangeInfo(false);
                p.setCanDeleteMessages(false);
                p.setCanBanUsers(false);
            }
            case READER -> {
                p.setCanSendMessages(false);
                p.setCanSendMedia(false);
                p.setCanAddUsers(false);
                p.setCanPinMessages(false);
                p.setCanChangeInfo(false);
                p.setCanDeleteMessages(false);
                p.setCanBanUsers(false);
            }
            case BANNED -> {
                p.setCanSendMessages(false);
                p.setCanSendMedia(false);
                p.setCanAddUsers(false);
                p.setCanPinMessages(false);
                p.setCanChangeInfo(false);
                p.setCanDeleteMessages(false);
                p.setCanBanUsers(false);
                p.setStatus(ParticipantStatus.BANNED);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatParticipantResponse> listParticipants(UUID chatId, UUID userId, Pageable pageable) {
        ChatParticipant me = access.requireParticipant(chatId, userId);
        access.assertCanRead(me);

        return chatParticipantRepository.findByChatId(chatId, pageable)
                .map(this::toResponse);
    }

    private ChatParticipantResponse toResponse(ChatParticipant p) {
        ChatParticipantResponse r = new ChatParticipantResponse();

        UserProfileEntity u = p.getUser();
        if (u != null) {
            r.setUserId(u.getId());
            r.setUsername(u.getUsername());
            r.setDisplayName(u.getDisplayName());
            r.setAvatarUrl(u.getAvatarUrl());
            r.setAvatarVersion(u.getAvatarVersion());
            r.setLastSeenAt(u.getLastSeenAt());
        }

        r.setRole(p.getRole());
        r.setStatus(p.getStatus());
        r.setJoinedAt(p.getJoinedAt());

        return r;
    }
}