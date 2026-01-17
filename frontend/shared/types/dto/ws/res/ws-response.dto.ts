import type { UUID, OffsetDateTime } from "../../core/primitives";

export interface MessageDeliveredPayload {
  chatId: UUID;
  messageId: UUID;
  userId: UUID;
  deliveredAt: OffsetDateTime;
}

export interface MessageReadUpToPayload {
  chatId: UUID;
  userId: UUID;
  upToMessageId: UUID;
  readAt: OffsetDateTime;
}

export interface UserOnlinePayload {
  userId: UUID;
  at: OffsetDateTime;
}

export interface UserOfflinePayload {
  userId: UUID;
  at: OffsetDateTime;
}

export interface UserTypingPayload {
  chatId: UUID;
  userId: UUID;
  at: OffsetDateTime;
}

export interface UserStopTypingPayload {
  chatId: UUID;
  userId: UUID;
  at: OffsetDateTime;
}
