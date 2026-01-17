import type {
  UUID,
  OffsetDateTime,
  MessageDto,
  CreateTextMessageRequest,
  //   CreateMediaMessageRequest,
  //   CreateFilesMessageRequest,
  //   CreateVoiceMessageRequest,
  EditTextMessageRequest,
} from "../../../shared/types/dto";

import { useApiClient } from "./useApiClient";

type IsoString = string;

export function useChatMessagesApi() {
  const api = useApiClient();

  return {
    sendText: (chatId: UUID, body: CreateTextMessageRequest) =>
      api.post<MessageDto>(`/api/v1/chat/${chatId}/message/text`, { body }),

    sendMedia: (chatId: UUID, form: FormData) =>
      api.post<MessageDto>(`/api/v1/chat/${chatId}/message/media`, {
        body: form,
      }),

    sendFiles: (chatId: UUID, form: FormData) =>
      api.post<MessageDto>(`/api/v1/chat/${chatId}/message/files`, {
        body: form,
      }),

    sendVoice: (chatId: UUID, form: FormData) =>
      api.post<MessageDto>(`/api/v1/chat/${chatId}/message/voice`, {
        body: form,
      }),

    list: (chatId: UUID, before?: OffsetDateTime | IsoString | null) =>
      api.get<MessageDto[]>(`/api/v1/chat/${chatId}/messages`, {
        query: before ? { before: String(before) } : undefined,
      }),

    editText: (chatId: UUID, messageId: UUID, body: EditTextMessageRequest) =>
      api.patch<MessageDto>(
        `/api/v1/chat/${chatId}/message/${messageId}/text`,
        {
          body,
        }
      ),

    delete: (chatId: UUID, messageId: UUID) =>
      api.delete<unknown>(`/api/v1/chat/${chatId}/message/${messageId}`),
  };
}
