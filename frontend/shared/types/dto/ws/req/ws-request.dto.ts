import type { UUID } from "../../core/primitives";

export interface MessageDeliveredRequest {
  chatId: UUID;
  messageId: UUID;
}

export interface MessageReadUpToRequest {
  chatId: UUID;
  upToMessageId: UUID;
}

export interface TypingRequest {
  typing: boolean;
}
