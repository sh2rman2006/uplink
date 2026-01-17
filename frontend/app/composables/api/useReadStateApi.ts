import type { MessageReadStateDto, UUID } from "../../../shared/types/dto";
import { useApiClient } from "./useApiClient";

export function useReadStateApi() {
  const api = useApiClient();

  return {
    getMyReadState: (chatId: UUID) =>
      api.get<MessageReadStateDto>(`/api/v1/chat/${chatId}/read-state`),
  };
}
