import type { UUID, OffsetDateTime } from "../core/primitives";
import type {
  ChatType,
  MessageType,
  ChatRole,
  ParticipantStatus,
} from "../enums/chat-enums";

export interface ChatDto {
  id: UUID;
  type: ChatType;
  title?: string;
  description?: string;
  avatarUrl?: string;
  isPrivate: boolean;
  isPublic: boolean;
  inviteLink?: string;
  inviteLinkExpiresAt?: OffsetDateTime;
  allowSendMessages: boolean;
  allowSendMedia: boolean;
  allowAddUsers: boolean;
  allowPinMessages: boolean;
  allowChangeInfo: boolean;
  isEncrypted: boolean;
  createdById: UUID;
  createdAt: OffsetDateTime;
  updatedAt: OffsetDateTime;
  lastMessageAt?: OffsetDateTime;
  lastMessageId?: UUID;
}

export interface ChatJoinRequestResponse {
  id: UUID;
  chatId: UUID;
  userId: UUID;
  status: string;
  message?: string;
  reviewedById?: UUID;
  reviewedAt?: OffsetDateTime;
  createdAt: OffsetDateTime;
}

export interface ChatListItemResponse {
  chatId: UUID;
  type: ChatType;
  title?: string;
  description?: string;
  avatarUrl?: string;
  updatedAt: OffsetDateTime;
  lastMessageId?: UUID;
  lastMessageAt?: OffsetDateTime;
  lastMessageType?: MessageType;
  lastMessageText?: string;
  lastMessageSenderId?: UUID;
  lastMessageCreatedAt?: OffsetDateTime;
  unreadCount: number;
}

export interface ChatParticipantResponse {
  userId: UUID;
  username: string;
  displayName?: string;
  avatarUrl?: string;
  avatarVersion?: number;
  lastSeenAt?: OffsetDateTime;
  role: ChatRole;
  status: ParticipantStatus;
  joinedAt: OffsetDateTime;
}

export interface MessageDeletedPayload {
  chatId: UUID;
  messageId: UUID;
  deletedAt: OffsetDateTime;
}

export interface AttachmentDto {
  id: UUID;
  type: string;
  mimeType?: string;
  fileName?: string;
  fileSize?: number;
  url: string;
  thumbnailUrl?: string;
}

export interface MessageDto {
  id: UUID;
  chatId: UUID;
  senderId: UUID;
  type: MessageType;
  text?: string;
  replyToMessageId?: UUID;
  createdAt: OffsetDateTime;
  updatedAt: OffsetDateTime;
  editCount?: number;
  lastEditedAt?: OffsetDateTime;
  attachments: AttachmentDto[];
}

export interface MessageReadStateDto {
  chatId: UUID;
  lastReadMessageId?: UUID;
  lastReadAt?: OffsetDateTime;
  unreadCount: number;
}

export interface ChatListPageableResponse {
  content: ChatListItemResponse[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

export interface MessagePageableResponse {
  content: MessageDto[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

export interface ChatParticipantsPageableResponse {
  content: ChatParticipantResponse[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}
