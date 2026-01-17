import type {
  MyProfileResponse,
  UserProfileSearchItemResponse,
  UpdateMyProfileRequest,
} from "../../../shared/types/dto";
import { useApiClient } from "./useApiClient";

export function useUsersApi() {
  const api = useApiClient();

  return {
    me: () => api.get<MyProfileResponse>("/api/v1/users/me"),
    updateMe: (body: UpdateMyProfileRequest) =>
      api.patch<MyProfileResponse>("/api/v1/users/me", { body }),
    search: (q: string) =>
      api.get<PageableResponse<UserProfileSearchItemResponse>>("/api/v1/users/search", {
        query: { q },
      }),
  };
}
