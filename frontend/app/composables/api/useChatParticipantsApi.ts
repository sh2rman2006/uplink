import type {
  ChatParticipantResponse,
  UUID,
  PageableRequest,
  PageableResponse,
  SpringPage,
  AddChatParticipantRequest,
  ChangeChatParticipantRoleRequest,
} from "../../../shared/types/dto";
import {
  createPageableParams,
  toPageableResponse,
} from "../../../shared/types/dto";
import { useApiClient } from "~/composables/api/useApiClient";

export function useChatParticipantsApi() {
  const api = useApiClient();

  return {
    list: async (
      chatId: UUID,
      pageable: PageableRequest = {page: 0, size: 50}
    ): Promise<PageableResponse<ChatParticipantResponse>> => {
      const raw = await api.get<SpringPage<ChatParticipantResponse>>(
        `/api/v1/chat/${chatId}/participants`,
        { query: createPageableParams(pageable) }
      );

      return toPageableResponse(raw);
    },

    add: (chatId: UUID, body: AddChatParticipantRequest) =>
      api.post<unknown>(`/api/v1/chat/${chatId}/participants`, { body }),

    leaveMe: (chatId: UUID) =>
      api.post<unknown>(`/api/v1/chat/${chatId}/participants/me:leave`),

    changeRole: (
      chatId: UUID,
      userId: UUID,
      body: ChangeChatParticipantRoleRequest
    ) =>
      api.patch<unknown>(`/api/v1/chat/${chatId}/participants/${userId}/role`, {
        body,
      }),

    remove: (chatId: UUID, userId: UUID) =>
      api.delete<unknown>(`/api/v1/chat/${chatId}/participants/${userId}`),
  };
}
