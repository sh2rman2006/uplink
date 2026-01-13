package tech.sh2rman.coreservice.domain.chat.service;

import tech.sh2rman.coreservice.domain.chat.dto.req.CreateFilesMessageRequest;
import tech.sh2rman.coreservice.domain.chat.dto.req.CreateMediaMessageRequest;
import tech.sh2rman.coreservice.domain.chat.dto.req.CreateVoiceMessageRequest;
import tech.sh2rman.coreservice.domain.chat.entity.Message;

import java.util.UUID;

public interface AttachmentMessageService {
    Message sendMedia(UUID chatId, UUID userId, CreateMediaMessageRequest req);
    Message sendFiles(UUID chatId, UUID userId, CreateFilesMessageRequest req);
    Message sendVoice(UUID chatId, UUID userId, CreateVoiceMessageRequest req);
}