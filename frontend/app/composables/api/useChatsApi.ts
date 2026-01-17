import type {
  ChatDto,
  ChatListPageableResponse,
  MessagePageableResponse,
  ChatParticipantsPageableResponse,
  CreateChatRequest,
  EditChatRequest,
  AddChatParticipantRequest,
  ChangeChatParticipantRoleRequest,
  InviteToChatRequest,
  CreateTextMessageRequest,
  EditTextMessageRequest,
  CreateFilesMessageRequest,
  CreateMediaMessageRequest,
  CreateVoiceMessageRequest,
  MessageDto,
  MessageReadStateDto,
  PageableRequest,
} from "../../../shared/types/dto";

import { createPageableParams } from "../../../shared/types/dto";
import { useApiClient } from "./useApiClient";

export function useChatsApi() {
  const api = useApiClient();

  return {
    // Chats
    list: (page: PageableRequest = { page: 0, size: 50 }) =>
      api.get<ChatListPageableResponse>("/api/v1/chat", {
        query: createPageableParams(page),
      }),

    get: (chatId: string) => api.get<ChatDto>(`/api/v1/chat/${chatId}`),

    create: (body: CreateChatRequest) =>
      api.post<{ chatId: string }>("/api/v1/chat/create", { body }),

    edit: (chatId: string, body: EditChatRequest) =>
      api.patch<ChatDto>(`/api/v1/chat/${chatId}`, { body }),

    participants: (
      chatId: string,
      page: PageableRequest = { page: 0, size: 50 }
    ) =>
      api.get<ChatParticipantsPageableResponse>(
        `/api/v1/chat/${chatId}/participants`,
        {
          query: createPageableParams(page),
        }
      ),

    addParticipant: (chatId: string, body: AddChatParticipantRequest) =>
      api.post<unknown>(`/api/v1/chat/${chatId}/participants`, { body }),

    changeRole: (
      chatId: string,
      userId: string,
      body: ChangeChatParticipantRoleRequest
    ) =>
      api.patch<unknown>(`/api/v1/chat/${chatId}/participants/${userId}/role`, {
        body,
      }),

    removeParticipant: (chatId: string, userId: string) =>
      api.delete<unknown>(`/api/v1/chat/${chatId}/participants/${userId}`),

    leave: (chatId: string) => api.post<unknown>(`/api/v1/chat/${chatId}/leave`),

    invite: (chatId: string, body: InviteToChatRequest) =>
      api.post<unknown>(`/api/v1/chat/${chatId}/invite`, { body }),

    messages: (chatId: string, page: PageableRequest = { page: 0, size: 50 }) =>
      api.get<MessagePageableResponse>(`/api/v1/chat/${chatId}/messages`, {
        query: createPageableParams(page),
      }),

    sendText: (chatId: string, body: CreateTextMessageRequest) =>
      api.post<MessageDto>(`/api/v1/chat/${chatId}/messages/text`, { body }),

    sendFiles: (
      chatId: string,
      body: CreateFilesMessageRequest,
      formData: FormData
    ) =>
      api.post<MessageDto>(`/api/v1/chat/${chatId}/messages/files`, {
        body,
        // если useApiClient умеет formData — лучше отдельным методом, иначе:
        // body: formData
      }),

    sendMedia: (
      chatId: string,
      body: CreateMediaMessageRequest,
      formData: FormData
    ) =>
      api.post<MessageDto>(`/api/v1/chat/${chatId}/messages/media`, {
        body,
      }),

    sendVoice: (
      chatId: string,
      body: CreateVoiceMessageRequest,
      formData: FormData
    ) =>
      api.post<MessageDto>(`/api/v1/chat/${chatId}/messages/voice`, {
        body,
      }),

    editText: (
      chatId: string,
      messageId: string,
      body: EditTextMessageRequest
    ) =>
      api.patch<MessageDto>(
        `/api/v1/chat/${chatId}/messages/${messageId}/text`,
        { body }
      ),

    deleteMessage: (chatId: string, messageId: string) =>
      api.delete<unknown>(`/api/v1/chat/${chatId}/messages/${messageId}`),

    // Read state
    readState: (chatId: string) =>
      api.get<MessageReadStateDto>(`/api/v1/chat/${chatId}/read-state`),
  };
}
