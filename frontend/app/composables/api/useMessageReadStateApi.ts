import type { UUID, MessageReadStateDto } from "../../../shared/types/dto";
import { useApiClient } from "./useApiClient";

export function useMessageReadStateApi() {
  const api = useApiClient();

  return {
    getMy: (chatId: UUID) =>
      api.get<MessageReadStateDto>(`/api/v1/chat/${chatId}/read-state`),
  };
}
