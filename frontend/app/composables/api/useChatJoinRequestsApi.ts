import type {
  UUID,
  ChatJoinRequestResponse,
  InviteToChatRequest,
  PageableRequest,
  PageableResponse,
} from "../../../shared/types/dto";
import { createPageableParams } from "../../../shared/types/dto";
import { useApiClient } from "./useApiClient";

export function useChatJoinRequestsApi() {
  const api = useApiClient();

  return {
    invite: (chatId: UUID, body: InviteToChatRequest) =>
      api.post<ChatJoinRequestResponse>(
        `/api/v1/chat/${chatId}/join-requests/invite`,
        { body }
      ),

    accept: (requestId: UUID) =>
      api.post<unknown>(`/api/v1/chat/join-requests/${requestId}/accept`),

    reject: (requestId: UUID) =>
      api.post<unknown>(`/api/v1/chat/join-requests/${requestId}/reject`),

    listByChat: (chatId: UUID, pageable: PageableRequest = {}) =>
      api.get<PageableResponse<ChatJoinRequestResponse>>(
        `/api/v1/chat/${chatId}/join-requests`,
        { query: createPageableParams(pageable) }
      ),

    inbox: (pageable: PageableRequest = {}) =>
      api.get<PageableResponse<ChatJoinRequestResponse>>(
        `/api/v1/chat/join-requests/inbox`,
        { query: createPageableParams(pageable) }
      ),
  };
}
