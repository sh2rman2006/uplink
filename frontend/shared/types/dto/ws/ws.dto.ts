import type {
  MessageDeliveredPayload,
  MessageReadUpToPayload,
  UserOnlinePayload,
  UserOfflinePayload,
  UserTypingPayload,
  UserStopTypingPayload,
} from "./res/ws-response.dto";
import type { MessageDto, MessageDeletedPayload } from "../res/chat-response.dto";

export enum WsEventType {
  MESSAGE_CREATED = "MESSAGE_CREATED",
  MESSAGE_EDITED = "MESSAGE_EDITED",
  MESSAGE_DELETED = "MESSAGE_DELETED",
  MESSAGE_DELIVERED = "MESSAGE_DELIVERED",
  MESSAGE_READ = "MESSAGE_READ",
  USER_ONLINE = "USER_ONLINE",
  USER_OFFLINE = "USER_OFFLINE",
  USER_TYPING = "USER_TYPING",
  USER_STOP_TYPING = "USER_STOP_TYPING",
}

export interface WsEvent<T = any> {
  type: WsEventType;
  serverTs: number;
  payload: T;
}

export type MessageCreatedEvent = WsEvent<MessageDto>;
export type MessageEditedEvent = WsEvent<MessageDto>;
export type MessageDeletedEvent = WsEvent<MessageDeletedPayload>;
export type MessageDeliveredEvent = WsEvent<MessageDeliveredPayload>;
export type MessageReadEvent = WsEvent<MessageReadUpToPayload>;
export type UserOnlineEvent = WsEvent<UserOnlinePayload>;
export type UserOfflineEvent = WsEvent<UserOfflinePayload>;
export type UserTypingEvent = WsEvent<UserTypingPayload>;
export type UserStopTypingEvent = WsEvent<UserStopTypingPayload>;

export type WsEventUnion =
  | MessageCreatedEvent
  | MessageEditedEvent
  | MessageDeletedEvent
  | MessageDeliveredEvent
  | MessageReadEvent
  | UserOnlineEvent
  | UserOfflineEvent
  | UserTypingEvent
  | UserStopTypingEvent;

export function isMessageCreatedEvent(
  event: WsEvent
): event is MessageCreatedEvent {
  return event.type === WsEventType.MESSAGE_CREATED;
}

export function isMessageEditedEvent(
  event: WsEvent
): event is MessageEditedEvent {
  return event.type === WsEventType.MESSAGE_EDITED;
}

export function isMessageDeletedEvent(
  event: WsEvent
): event is MessageDeletedEvent {
  return event.type === WsEventType.MESSAGE_DELETED;
}

export function isMessageDeliveredEvent(
  event: WsEvent
): event is MessageDeliveredEvent {
  return event.type === WsEventType.MESSAGE_DELIVERED;
}

export function isMessageReadEvent(event: WsEvent): event is MessageReadEvent {
  return event.type === WsEventType.MESSAGE_READ;
}

export function isUserOnlineEvent(event: WsEvent): event is UserOnlineEvent {
  return event.type === WsEventType.USER_ONLINE;
}

export function isUserOfflineEvent(event: WsEvent): event is UserOfflineEvent {
  return event.type === WsEventType.USER_OFFLINE;
}

export function isUserTypingEvent(event: WsEvent): event is UserTypingEvent {
  return event.type === WsEventType.USER_TYPING;
}

export function isUserStopTypingEvent(
  event: WsEvent
): event is UserStopTypingEvent {
  return event.type === WsEventType.USER_STOP_TYPING;
}

export function parseWsEvent(data: any): WsEventUnion | null {
  try {
    const event = data as WsEvent;

    if (!Object.values(WsEventType).includes(event.type)) {
      console.warn("Unknown WebSocket event type:", event.type);
      return null;
    }

    return event as WsEventUnion;
  } catch (error) {
    console.error("Failed to parse WebSocket event:", error, data);
    return null;
  }
}
