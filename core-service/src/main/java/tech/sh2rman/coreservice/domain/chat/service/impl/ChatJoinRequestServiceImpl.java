package tech.sh2rman.coreservice.domain.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sh2rman.coreservice.domain.chat.dto.ChatJoinRequestResponse;
import tech.sh2rman.coreservice.domain.chat.dto.InviteToChatRequest;
import tech.sh2rman.coreservice.domain.chat.entity.Chat;
import tech.sh2rman.coreservice.domain.chat.entity.ChatJoinRequest;
import tech.sh2rman.coreservice.domain.chat.entity.ChatParticipant;
import tech.sh2rman.coreservice.domain.chat.exception.ChatBadRequestException;
import tech.sh2rman.coreservice.domain.chat.exception.ChatParticipantNotFoundException;
import tech.sh2rman.coreservice.domain.chat.exception.MessageForbiddenException;
import tech.sh2rman.coreservice.domain.chat.model.ChatRole;
import tech.sh2rman.coreservice.domain.chat.model.ChatType;
import tech.sh2rman.coreservice.domain.chat.model.ParticipantStatus;
import tech.sh2rman.coreservice.domain.chat.repository.ChatJoinRequestRepository;
import tech.sh2rman.coreservice.domain.chat.repository.ChatParticipantRepository;
import tech.sh2rman.coreservice.domain.chat.service.ChatJoinRequestService;
import tech.sh2rman.coreservice.domain.chat.service.MessageAccessService;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatJoinRequestServiceImpl implements ChatJoinRequestService {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private final MessageAccessService access;
    private final ChatJoinRequestRepository chatJoinRequestRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    @Override
    @Transactional
    public ChatJoinRequestResponse invite(UUID chatId, UUID actorUserId, InviteToChatRequest req) {

        if (req == null || req.userId() == null) {
            throw new ChatBadRequestException("userId is required");
        }
        if (req.userId().equals(actorUserId)) {
            throw new ChatBadRequestException("Cannot invite yourself");
        }

        Chat chat = access.requireChat(chatId);
        ChatParticipant me = access.requireParticipant(chatId, actorUserId);

        assertCanInvite(chat, me);

        if (chat.getType() == ChatType.PRIVATE) {
            throw new MessageForbiddenException("Not allowed for PRIVATE chat");
        }

        UUID targetUserId = req.userId();

        Optional<ChatParticipant> existingParticipant = chatParticipantRepository.findByChatIdAndUserId(chatId, targetUserId);
        if (existingParticipant.isPresent() && existingParticipant.get().getStatus() == ParticipantStatus.ACTIVE) {
            throw new ChatBadRequestException("User already in chat");
        }
        if (existingParticipant.isPresent() && existingParticipant.get().getStatus() == ParticipantStatus.BANNED) {
            throw new MessageForbiddenException("User is banned");
        }

        UserProfileEntity target = access.requireUser(targetUserId);

        OffsetDateTime now = OffsetDateTime.now();

        ChatJoinRequest jr = chatJoinRequestRepository.findByChat_IdAndUser_Id(chatId, targetUserId).orElse(null);
        if (jr == null) {
            jr = new ChatJoinRequest();
            jr.setChat(chat);
            jr.setUser(target);
            jr.setCreatedAt(now);
        }

        String currentStatus = normalizeStatus(jr.getStatus());
        if (STATUS_PENDING.equals(currentStatus)) {
            jr.setMessage(req.message());
            return toResponse(chatJoinRequestRepository.save(jr));
        }

        if (STATUS_APPROVED.equals(currentStatus)) {
            throw new ChatBadRequestException("Invite already approved");
        }

        jr.setStatus(STATUS_PENDING);
        jr.setMessage(req.message());
        jr.setReviewedBy(null);
        jr.setReviewedAt(null);

        ChatJoinRequest saved = chatJoinRequestRepository.save(jr);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatJoinRequestResponse> inbox(UUID userId, String status, Pageable pageable) {
        String st = normalizeStatus(status);
        if (st == null) st = STATUS_PENDING;

        return chatJoinRequestRepository.findByUser_IdAndStatusOrderByCreatedAtDesc(userId, st, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatJoinRequestResponse> listByChat(UUID chatId, UUID actorUserId, String status, Pageable pageable) {
        ChatParticipant me = access.requireParticipant(chatId, actorUserId);
        assertCanManageInvites(me);

        String st = normalizeStatus(status);
        if (st == null) st = STATUS_PENDING;

        return chatJoinRequestRepository.findByChat_IdAndStatusOrderByCreatedAtDesc(chatId, st, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public ChatJoinRequestResponse accept(UUID requestId, UUID userId) {
        ChatJoinRequest jr = chatJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ChatBadRequestException("Join request not found"));

        if (jr.getUser() == null || jr.getUser().getId() == null || !jr.getUser().getId().equals(userId)) {
            throw new MessageForbiddenException("Not your join request");
        }

        String st = normalizeStatus(jr.getStatus());
        if (!STATUS_PENDING.equals(st)) {
            throw new ChatBadRequestException("Join request is not pending");
        }

        Chat chat = jr.getChat();
        if (chat == null || chat.getId() == null) {
            throw new ChatBadRequestException("Join request chat is null");
        }

        ChatParticipant meInChat = null;
        try {
            meInChat = access.requireParticipant(chat.getId(), userId);
        } catch (ChatParticipantNotFoundException ignored) {}

        OffsetDateTime now = OffsetDateTime.now();

        if (meInChat == null) {
            UserProfileEntity user = access.requireUser(userId);

            ChatParticipant p = new ChatParticipant();
            p.setChat(chat);
            p.setUser(user);

            p.setStatus(ParticipantStatus.ACTIVE);
            p.setRole(ChatRole.MEMBER);

            p.setCanSendMessages(true);
            p.setCanSendMedia(true);
            p.setCanAddUsers(false);
            p.setCanPinMessages(true);
            p.setCanChangeInfo(false);
            p.setCanDeleteMessages(false);
            p.setCanBanUsers(false);

            p.setJoinedAt(now);
            p.setCreatedAt(now);
            p.setUpdatedAt(now);

            chatParticipantRepository.save(p);
        } else {
            if (meInChat.getStatus() == ParticipantStatus.BANNED) {
                throw new MessageForbiddenException("You are banned");
            }
            meInChat.setStatus(ParticipantStatus.ACTIVE);
            meInChat.setLeftAt(null);
            meInChat.setUpdatedAt(now);
            chatParticipantRepository.save(meInChat);
        }

        UserProfileEntity reviewer = access.requireUser(userId);

        jr.setStatus(STATUS_APPROVED);
        jr.setReviewedBy(reviewer);
        jr.setReviewedAt(now);

        ChatJoinRequest saved = chatJoinRequestRepository.save(jr);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ChatJoinRequestResponse reject(UUID requestId, UUID userId) {
        ChatJoinRequest jr = chatJoinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ChatBadRequestException("Join request not found"));

        if (jr.getUser() == null || jr.getUser().getId() == null || !jr.getUser().getId().equals(userId)) {
            throw new MessageForbiddenException("Not your join request");
        }

        String st = normalizeStatus(jr.getStatus());
        if (!STATUS_PENDING.equals(st)) {
            throw new ChatBadRequestException("Join request is not pending");
        }

        UserProfileEntity reviewer = access.requireUser(userId);

        OffsetDateTime now = OffsetDateTime.now();
        jr.setStatus(STATUS_REJECTED);
        jr.setReviewedBy(reviewer);
        jr.setReviewedAt(now);

        ChatJoinRequest saved = chatJoinRequestRepository.save(jr);
        return toResponse(saved);
    }

    private void assertCanInvite(Chat chat, ChatParticipant me) {
        if (me.getStatus() != ParticipantStatus.ACTIVE && me.getStatus() != ParticipantStatus.MUTED) {
            throw new MessageForbiddenException("Participant not active");
        }

        if (me.getRole() != ChatRole.OWNER && me.getRole() != ChatRole.ADMIN) {
            throw new MessageForbiddenException("Not allowed");
        }

        if (Boolean.FALSE.equals(chat.getAllowAddUsers())) {
            throw new MessageForbiddenException("Chat does not allow adding users");
        }

        if (Boolean.FALSE.equals(me.getCanAddUsers())) {
            throw new MessageForbiddenException("Not allowed to invite users");
        }
    }

    private void assertCanManageInvites(ChatParticipant me) {
        if (me.getStatus() != ParticipantStatus.ACTIVE && me.getStatus() != ParticipantStatus.MUTED) {
            throw new MessageForbiddenException("Participant not active");
        }
        if (me.getRole() != ChatRole.OWNER && me.getRole() != ChatRole.ADMIN) {
            throw new MessageForbiddenException("Not allowed");
        }
    }

    private String normalizeStatus(String s) {
        if (s == null) return null;
        String t = s.trim().toUpperCase();
        return t.isEmpty() ? null : t;
    }

    private ChatJoinRequestResponse toResponse(ChatJoinRequest jr) {
        ChatJoinRequestResponse r = new ChatJoinRequestResponse();
        r.setId(jr.getId());
        r.setStatus(jr.getStatus());
        r.setMessage(trimOrNull(jr.getMessage()));

        if (jr.getChat() != null) r.setChatId(jr.getChat().getId());
        if (jr.getUser() != null) r.setUserId(jr.getUser().getId());

        if (jr.getReviewedBy() != null) r.setReviewedById(jr.getReviewedBy().getId());
        r.setReviewedAt(jr.getReviewedAt());
        r.setCreatedAt(jr.getCreatedAt());
        return r;
    }

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

