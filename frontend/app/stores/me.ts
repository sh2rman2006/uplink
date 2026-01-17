import type { MyProfileResponse, UUID } from "../../shared/types/dto";
import { useUsersApi } from "~/composables/api/useUsersApi";

type LoadState = "idle" | "pending" | "loaded" | "error";

type SafeError = {
  message: string;
  status?: number;
};

function toSafeError(err: unknown): SafeError {
  if (err && typeof err === "object") {
    const e = err as Record<string, unknown>;

    const message =
      (typeof e["statusMessage"] === "string" && e["statusMessage"]) ||
      (typeof e["message"] === "string" && e["message"]) ||
      "Unknown error";

    const statusRaw =
      e["statusCode"] ??
      e["status"] ??
      (typeof e["response"] === "object" &&
        e["response"] &&
        (e["response"] as Record<string, unknown>)["status"]);

    const status = typeof statusRaw === "number" ? statusRaw : undefined;

    const data = e["data"];
    if (data && typeof data === "object") {
      const dm = (data as Record<string, unknown>)["message"];
      if (typeof dm === "string" && dm.trim()) {
        return { message: dm, status };
      }
    }

    return { message, status };
  }

  if (typeof err === "string") return { message: err };
  return { message: "Unknown error" };
}

export const useMeStore = defineStore("me", () => {
  const profile = ref<MyProfileResponse | null>(null);

  const error = ref<SafeError | null>(null);

  const state = ref<LoadState>("idle");

  const myUserId = computed<UUID | null>(() => profile.value?.id ?? null);
  const isAuthed = computed(() => Boolean(profile.value && myUserId.value));

  function isMe(userId?: string | null): boolean {
    const me = myUserId.value;
    if (!me || !userId) return false;
    return String(me) === String(userId);
  }

  async function refresh(): Promise<MyProfileResponse> {
    state.value = "pending";
    error.value = null;

    try {
      const api = useUsersApi();
      const res = await api.me();

      profile.value = res;
      state.value = "loaded";

      return res;
    } catch (e: unknown) {
      // не храним FetchError в state
      profile.value = null;
      error.value = toSafeError(e);
      state.value = "error";
      throw e;
    }
  }

  async function ensureLoaded(): Promise<MyProfileResponse | null> {
    if (state.value === "loaded" && profile.value) return profile.value;
    if (state.value === "pending") return profile.value;
    return await refresh();
  }

  function clear(): void {
    profile.value = null;
    error.value = null;
    state.value = "idle";
  }

  return {
    profile,
    myUserId,
    isAuthed,
    isMe,
    state,
    error,
    refresh,
    ensureLoaded,
    clear,
  };
});
