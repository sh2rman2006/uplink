import type { UUID, OffsetDateTime } from "../core/primitives";

export interface MyProfileResponse {
  id: UUID;
  username: string;
  email: string;
  displayName?: string;
  bio?: string;
  avatarUrl?: string;
  avatarVersion?: number;
  isProfilePublic: boolean;
  status?: string;
  createdAt: OffsetDateTime;
  updatedAt: OffsetDateTime;
}

export interface UserProfileSearchItemResponse {
  id: UUID;
  username: string;
  displayName?: string;
  avatarUrl?: string;
  avatarVersion?: number;
  isProfilePublic: boolean;
}
