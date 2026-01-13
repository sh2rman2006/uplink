package tech.sh2rman.coreservice.domain.chat.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.sh2rman.coreservice.domain.chat.dto.req.CreateChatRequest;
import tech.sh2rman.coreservice.domain.chat.dto.req.EditChatRequest;
import tech.sh2rman.coreservice.domain.chat.dto.res.ChatListItemResponse;
import tech.sh2rman.coreservice.domain.chat.entity.Chat;
import tech.sh2rman.coreservice.domain.chat.entity.ChatParticipant;
import tech.sh2rman.coreservice.domain.chat.entity.Message;
import tech.sh2rman.coreservice.domain.chat.exception.ChatBadRequestException;
import tech.sh2rman.coreservice.domain.chat.model.ChatRole;
import tech.sh2rman.coreservice.domain.chat.model.ChatType;
import tech.sh2rman.coreservice.domain.chat.model.ParticipantStatus;
import tech.sh2rman.coreservice.domain.chat.repository.ChatParticipantRepository;
import tech.sh2rman.coreservice.domain.chat.repository.ChatRepository;
import tech.sh2rman.coreservice.domain.chat.repository.ChatUnreadCountProjection;
import tech.sh2rman.coreservice.domain.chat.repository.MessageRepository;
import tech.sh2rman.coreservice.domain.chat.service.ChatAccessService;
import tech.sh2rman.coreservice.domain.chat.service.ChatService;
import tech.sh2rman.coreservice.domain.user.entity.UserProfileEntity;
import tech.sh2rman.coreservice.domain.user.repository.UserProfileRepository;
import tech.sh2rman.coreservice.integration.minio.MinioStorageService;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final UserProfileRepository userProfileRepository;
    private final MessageRepository messageRepository;
    private final MinioStorageService minioStorageService;
    private final ChatAccessService access;

    @Override
    @Transactional
    public Chat createChat(UUID creatorUserId, CreateChatRequest req) {
        if (req == null || req.type() == null) {
            throw new ChatBadRequestException("type is required");
        }

        Chat createdChat = switch (req.type()) {
            case PRIVATE -> createPrivateChat(creatorUserId, req);
            case GROUP -> createGroupChat(creatorUserId, req);
            case CHANNEL, SECRET -> throw new ChatBadRequestException(req.type() + " not supported yet");
        };

        if (req.coverFile() != null && !createdChat.getType().equals(ChatType.PRIVATE)) {
            String objectKey = minioStorageService.putChatAvatar(createdChat.getId(), req.coverFile());
            createdChat.setAvatarObjectKey(objectKey);
            return chatRepository.save(createdChat);
        }
        return createdChat;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatListItemResponse> listMyChats(UUID userId, Pageable pageable) {
        List<ParticipantStatus> statuses = List.of(ParticipantStatus.ACTIVE, ParticipantStatus.MUTED);
        Page<Chat> page = chatRepository.findMyChats(userId, statuses, pageable);
        return enrichChatList(page, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatListItemResponse> searchMyChats(UUID userId, String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return listMyChats(userId, pageable);
        }
        List<ParticipantStatus> statuses = List.of(ParticipantStatus.ACTIVE, ParticipantStatus.MUTED);
        Page<Chat> page = chatRepository.searchMyChats(userId, statuses, q.trim(), pageable);
        return enrichChatList(page, userId);
    }

    @Override
    @Transactional
    public Chat editChat(UUID userId, UUID chatId, EditChatRequest req) {
        if (req == null) throw new ChatBadRequestException("Request is required");

        Chat chat = access.requireChat(chatId);
        ChatParticipant me = access.requireParticipant(chatId, userId);

        if (chat.getType() == ChatType.PRIVATE) {
            throw new ChatBadRequestException("PRIVATE chat cannot be edited");
        }

        boolean changed = false;
        OffsetDateTime now = OffsetDateTime.now();

        boolean wantsInfoChange =
                req.title() != null ||
                        req.description() != null ||
                        (req.coverFile() != null && !req.coverFile().isEmpty());

        if (wantsInfoChange) {
            access.assertCanChangeChatInfo(chat, me);

            if (req.title() != null) {
                chat.setTitle(normalizePatchString(req.title()));
                changed = true;
            }

            if (req.description() != null) {
                chat.setDescription(normalizePatchString(req.description()));
                changed = true;
            }

            if (req.coverFile() != null && !req.coverFile().isEmpty()) {
                chat.setAvatarObjectKey(minioStorageService.putChatAvatar(chatId, req.coverFile()));
                changed = true;
            }
        }

        boolean wantsFlags =
                req.allowSendMedia() != null ||
                        req.allowAddUsers() != null ||
                        req.allowPinMessages() != null ||
                        req.allowChangeInfo() != null;

        if (wantsFlags) {
            access.assertCanChangeChatSettings(chat, me);

            if (req.allowSendMedia() != null) {
                chat.setAllowSendMedia(req.allowSendMedia());
                changed = true;
            }
            if (req.allowAddUsers() != null) {
                chat.setAllowAddUsers(req.allowAddUsers());
                changed = true;
            }
            if (req.allowPinMessages() != null) {
                chat.setAllowPinMessages(req.allowPinMessages());
                changed = true;
            }
            if (req.allowChangeInfo() != null) {
                chat.setAllowChangeInfo(req.allowChangeInfo());
                changed = true;
            }
        }

        if (!changed) return chat;

        chat.setUpdatedAt(now);
        return chatRepository.save(chat);
    }

    private String normalizePatchString(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }


    private Page<ChatListItemResponse> enrichChatList(Page<Chat> page, UUID userId) {

        List<Chat> chats = page.getContent();
        if (chats.isEmpty()) {
            return new PageImpl<>(List.of(), page.getPageable(), page.getTotalElements());
        }

        List<UUID> chatIds = chats.stream().map(Chat::getId).toList();

        Map<UUID, Long> unreadByChatId = messageRepository.countUnreadByChats(userId, chatIds).stream()
                .collect(Collectors.toMap(ChatUnreadCountProjection::getChatId, ChatUnreadCountProjection::getUnread, (a, b) -> a));

        Set<UUID> lastMessageIds = chats.stream()
                .map(Chat::getLastMessageId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Message> lastMessageById = lastMessageIds.isEmpty()
                ? Map.of()
                : messageRepository.findAllById(lastMessageIds).stream()
                .collect(Collectors.toMap(Message::getId, Function.identity(), (a, b) -> a));


        List<UUID> privateChatIds = chats.stream()
                .filter(c -> c.getType() == ChatType.PRIVATE)
                .map(Chat::getId)
                .toList();

        Map<UUID, UUID> peerUserIdByChatId = privateChatIds.isEmpty()
                ? Map.of()
                : chatParticipantRepository.findByChatIdIn(privateChatIds).stream()
                .filter(p -> p.getUser() != null && p.getUser().getId() != null)
                .filter(p -> !p.getUser().getId().equals(userId)) // НЕ я
                .collect(Collectors.toMap(
                        p -> p.getChat().getId(),
                        p -> p.getUser().getId(),
                        (a, b) -> a
                ));

        Set<UUID> peerUserIds = new HashSet<>(peerUserIdByChatId.values());

        Map<UUID, UserProfileEntity> peerProfileById = peerUserIds.isEmpty()
                ? Map.of()
                : userProfileRepository.findAllById(peerUserIds).stream()
                .collect(Collectors.toMap(UserProfileEntity::getId, Function.identity(), (a, b) -> a));

        List<ChatListItemResponse> items = new ArrayList<>(chats.size());

        for (Chat chat : chats) {

            ChatListItemResponse r = new ChatListItemResponse();
            r.setChatId(chat.getId());
            r.setType(chat.getType());
            r.setTitle(chat.getTitle());
            r.setDescription(chat.getDescription());
            r.setUpdatedAt(chat.getUpdatedAt());

            r.setLastMessageId(chat.getLastMessageId());
            r.setLastMessageAt(chat.getLastMessageAt());

            long unread = unreadByChatId.getOrDefault(chat.getId(), 0L);
            r.setUnreadCount(unread);

            UUID lastId = chat.getLastMessageId();
            if (lastId != null) {
                Message m = lastMessageById.get(lastId);
                if (m != null && m.getDeletedAt() == null) {
                    r.setLastMessageType(m.getType());
                    r.setLastMessageText(m.getText());
                    r.setLastMessageCreatedAt(m.getCreatedAt());
                    if (m.getSender() != null) {
                        r.setLastMessageSenderId(m.getSender().getId());
                    }
                }
            }

            if (chat.getType() == ChatType.PRIVATE) {
                UUID peerId = peerUserIdByChatId.get(chat.getId());
                UserProfileEntity peer = (peerId == null) ? null : peerProfileById.get(peerId);

                if (peer != null && peer.getAvatarVersion() != null) {
                    r.setAvatarUrl(minioStorageService.getUserAvatarObjectUrl(
                            peer.getId(),
                            String.valueOf(peer.getAvatarVersion())
                    ));
                } else {
                    r.setAvatarUrl(null);
                }
            } else {
                r.setAvatarUrl(minioStorageService.getChatAvatarObjectUrl(chat.getId()));
            }

            items.add(r);
        }

        return new PageImpl<>(items, page.getPageable(), page.getTotalElements());
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

