export enum NotificationSetting {
  ALL = "ALL",
  MENTIONS_ONLY = "MENTIONS_ONLY",
  NONE = "NONE",
}

export type NotificationSettingType = keyof typeof NotificationSetting;

export enum ParticipantStatus {
  ACTIVE = "ACTIVE",
  MUTED = "MUTED",
  LEFT = "LEFT",
  KICKED = "KICKED",
  BANNED = "BANNED",
}

export type ParticipantStatusType = keyof typeof ParticipantStatus;

export enum AttachmentType {
  IMAGE = "IMAGE",
  VIDEO = "VIDEO",
  AUDIO = "AUDIO",
  VOICE_MESSAGE = "VOICE_MESSAGE",
  DOCUMENT = "DOCUMENT",
  ARCHIVE = "ARCHIVE",
  OTHER = "OTHER",
}

export type AttachmentTypeType = keyof typeof AttachmentType;

export enum ChatRole {
  OWNER = "OWNER",
  ADMIN = "ADMIN",
  MEMBER = "MEMBER",
  READER = "READER",
  BANNED = "BANNED",
}

export type ChatRoleType = keyof typeof ChatRole;

export enum ChatType {
  PRIVATE = "PRIVATE",
  GROUP = "GROUP",
  CHANNEL = "CHANNEL",
  SECRET = "SECRET",
}

export type ChatTypeType = keyof typeof ChatType;

export enum MessageStatus {
  SENDING = "SENDING",
  SENT = "SENT",
  DELIVERED = "DELIVERED",
  READ = "READ",
  FAILED = "FAILED",
  EDITED = "EDITED",
  DELETED = "DELETED",
}

export type MessageStatusType = keyof typeof MessageStatus;

export enum MessageType {
  TEXT = "TEXT",
  IMAGE = "IMAGE",
  VIDEO = "VIDEO",
  AUDIO = "AUDIO",
  VOICE = "VOICE",
  FILE = "FILE",
  POLL = "POLL",
  LOCATION = "LOCATION",
  CONTACT = "CONTACT",
  STICKER = "STICKER",
  SYSTEM = "SYSTEM",
}

export type MessageTypeType = keyof typeof MessageType;
