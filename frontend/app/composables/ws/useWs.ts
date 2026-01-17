import { Client, type IMessage, type StompSubscription } from "@stomp/stompjs";
import type {
  UUID,
  MessageDeliveredRequest,
  MessageReadUpToRequest,
  TypingRequest,
  WsEventUnion,
} from "../../../shared/types/dto";
import { parseWsEvent } from "../../../shared/types/dto";

type WsTokenResponse = { token: string };

function safeJsonParse(raw: string) {
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function useWs() {
  const client = shallowRef<Client | null>(null);
  const connected = ref(false);

  const chatSubs = new Map<string, StompSubscription>();
  const presenceSubs = new Map<string, StompSubscription>();

  let pingTimer: ReturnType<typeof setInterval> | null = null;

  async function connect() {
    if (client.value?.active) return;

    const { token } = await $fetch<WsTokenResponse>("/api/ws/token", {
      credentials: "include",
    });

    const c = new Client({
      brokerURL: `ws://${useRuntimeConfig().public.apiDomain}/ws`,
      reconnectDelay: 2000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      onConnect: () => {
        connected.value = true;

        startPing();
      },
      onDisconnect: () => {
        connected.value = false;
        stopPing();
      },
      onStompError: (frame) => {
        console.error("STOMP error", frame.headers, frame.body);
      },
      onWebSocketError: (e) => {
        console.error("WS error", e);
      },
    });

    c.activate();
    client.value = c;
  }

  function disconnect() {
    stopPing();

    for (const s of chatSubs.values()) s.unsubscribe();
    chatSubs.clear();

    for (const s of presenceSubs.values()) s.unsubscribe();
    presenceSubs.clear();

    client.value?.deactivate();
    client.value = null;
    connected.value = false;
  }

  function requireConnected() {
    if (!client.value || !connected.value) {
      throw new Error("WS is not connected");
    }
    return client.value;
  }

  // -------- SUBSCRIBE helpers --------

  function subscribeChat(chatId: UUID, onEvent: (e: WsEventUnion) => void) {
    const key = String(chatId);
    if (chatSubs.has(key)) return;

    const c = requireConnected();
    const sub = c.subscribe(`/topic/chat.${chatId}`, (msg: IMessage) => {
      const parsed = safeJsonParse(msg.body);
      const event = parseWsEvent(parsed);
      if (event) onEvent(event);
    });

    chatSubs.set(key, sub);
  }

  function unsubscribeChat(chatId: UUID) {
    const key = String(chatId);
    chatSubs.get(key)?.unsubscribe();
    chatSubs.delete(key);
  }

  function subscribeUserPresence(
    userId: UUID,
    onEvent: (e: WsEventUnion) => void
  ) {
    const key = String(userId);
    if (presenceSubs.has(key)) return;

    const c = requireConnected();
    const sub = c.subscribe(
      `/topic/user.${userId}.presence`,
      (msg: IMessage) => {
        const parsed = safeJsonParse(msg.body);
        const event = parseWsEvent(parsed);
        if (event) onEvent(event);
      }
    );

    presenceSubs.set(key, sub);
  }

  function unsubscribeUserPresence(userId: UUID) {
    const key = String(userId);
    presenceSubs.get(key)?.unsubscribe();
    presenceSubs.delete(key);
  }

  // -------- SEND helpers --------

  function sendDelivered(req: MessageDeliveredRequest) {
    const c = requireConnected();
    c.publish({
      destination: "/app/chat.message.delivered",
      body: JSON.stringify(req),
    });
  }

  function sendReadUpTo(req: MessageReadUpToRequest) {
    const c = requireConnected();
    c.publish({
      destination: "/app/chat.message.readUpTo",
      body: JSON.stringify(req),
    });
  }

  function sendTyping(chatId: UUID, typing: boolean) {
    const c = requireConnected();
    const body: TypingRequest = { typing };

    c.publish({
      destination: `/app/chat.${chatId}.typing`,
      body: JSON.stringify(body),
    });
  }

  function pingPresence() {
    const c = requireConnected();
    c.publish({
      destination: "/app/presence.ping",
    });
  }

  function startPing() {
    stopPing();
    pingTimer = setInterval(() => {
      if (!connected.value) return;
      try {
        pingPresence();
      } catch (e) {
        console.debug("presence ping failed", e);
      }
    }, 25000);
  }

  function stopPing() {
    if (pingTimer) clearInterval(pingTimer);
    pingTimer = null;
  }

  return {
    connect,
    disconnect,
    connected,

    subscribeChat,
    unsubscribeChat,

    subscribeUserPresence,
    unsubscribeUserPresence,

    sendDelivered,
    sendReadUpTo,
    sendTyping,
    pingPresence,
  };
}
