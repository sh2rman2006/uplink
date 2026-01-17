import type { UUID } from "../core/primitives";
import type { ChatType, ChatRole } from "../enums/chat-enums";

export interface AddChatParticipantRequest {
  userId: UUID;
  role?: ChatRole;
}

export interface ChangeChatParticipantRoleRequest {
  role: ChatRole;
}

export interface CreateChatRequest {
  type: ChatType;
  title?: string;
  description?: string;
  memberUserIds?: UUID[];
}

export interface CreateFilesMessageRequest {
  text?: string;
  replyToMessageId?: UUID;
}

export interface CreateMediaMessageRequest {
  text?: string;
  replyToMessageId?: UUID;
}

export interface CreateTextMessageRequest {
  text: string;
}

export interface CreateVoiceMessageRequest {
  replyToMessageId?: UUID;
}

export interface EditChatRequest {
  title?: string;
  description?: string;
  allowSendMedia?: boolean;
  allowAddUsers?: boolean;
  allowPinMessages?: boolean;
  allowChangeInfo?: boolean;
}

export interface EditTextMessageRequest {
  text: string;
}

export interface InviteToChatRequest {
  userId: UUID;
  message?: string;
}
