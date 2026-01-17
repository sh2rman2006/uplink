<template>
  <div class="chat">
    <header class="head">
      <div class="left">
        <div class="title">{{ chatTitle }}</div>

        <div v-if="typingText" class="sub">{{ typingText }}</div>
        <div v-else class="sub">{{ chat?.description || " " }}</div>
      </div>
    </header>

    <div class="body">
      <div class="topActions">
        <Button
          label="Load more"
          icon="pi pi-angle-up"
          severity="secondary"
          text
          :loading="loadingMore"
          :disabled="loading || loadingMore || messages.length === 0"
          @click="loadMore"
        />
      </div>

      <div ref="listEl" class="list">
        <div v-if="loading" class="loading">
          <Skeleton height="18px" class="mb-2" />
          <Skeleton height="18px" class="mb-2" />
          <Skeleton height="18px" class="mb-2" />
        </div>

        <div v-else-if="messages.length === 0" class="empty">
          No messages yet
        </div>

        <div v-else class="msgs">
          <div
            v-for="m in messages"
            :key="m.id"
            class="msg"
            :class="{ mine: me.isMe(String(m.senderId)) }"
          >
            <div class="bubble">
              <div class="text">{{ m.text || "[empty]" }}</div>
              <div class="meta">{{ formatTime(String(m.createdAt)) }}</div>
            </div>
          </div>
        </div>
      </div>

      <footer class="composer">
        <InputText
          v-model="draft"
          class="input"
          placeholder="Type a message…"
          @input="onInput"
          @keyup.enter="sendText"
        />
        <Button
          icon="pi pi-send"
          :loading="sending"
          :disabled="sending || !draft.trim()"
          @click="sendText"
        />
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useChatsApi } from "~/composables/api/useChatsApi";
import { useChatMessagesApi } from "~/composables/api/useChatMessagesApi";
import { useWs } from "~/composables/ws/useWs";

import {
  isMessageCreatedEvent,
  isMessageEditedEvent,
  isMessageDeletedEvent,
  isUserTypingEvent,
  isUserStopTypingEvent,
} from "../../../shared/types/dto";

import type {
  WsEventUnion,
  ChatDto,
  MessageDto,
  UUID,
} from "../../../shared/types/dto";

definePageMeta({ middleware: ["auth"], layout: "chats" });

const route = useRoute();
const chatId = computed<UUID>(() => route.params.chatId as UUID);

const chatsApi = useChatsApi();
const msgApi = useChatMessagesApi();
const ws = useWs();
const me = useMeStore();

const chat = ref<ChatDto | null>(null);

const messages = ref<MessageDto[]>([]);
const loading = ref(true);
const loadingMore = ref(false);
const sending = ref(false);

const draft = ref("");
const listEl = ref<HTMLElement | null>(null);

const typingUsers = ref<Set<string>>(new Set());

const typingText = computed(() => {
  const arr = Array.from(typingUsers.value);
  if (arr.length === 0) return "";
  if (arr.length === 1) return "Typing…";
  return "Several users typing…";
});

let typingStopTimer: ReturnType<typeof setTimeout> | null = null;
let typingSent = false;

function sortAsc(a: MessageDto, b: MessageDto) {
  return String(a.createdAt).localeCompare(String(b.createdAt));
}

function upsertMessage(dto: MessageDto) {
  const idx = messages.value.findIndex((m) => m.id === dto.id);
  if (idx >= 0) messages.value[idx] = dto;
  else messages.value.push(dto);

  messages.value.sort(sortAsc);
}

function removeMessage(messageId: string) {
  messages.value = messages.value.filter((m) => m.id !== messageId);
}

function scrollToBottom() {
  nextTick(() => {
    const el = listEl.value;
    if (!el) return;
    el.scrollTop = el.scrollHeight;
  });
}

async function loadInitial() {
  loading.value = true;
  try {
    await me.ensureLoaded();
    chat.value = await chatsApi.get(String(chatId.value));

    const res = await msgApi.list(chatId.value);
    messages.value = (res ?? []).slice().sort(sortAsc);

    scrollToBottom();
  } finally {
    loading.value = false;
  }
}

async function loadMore() {
  if (loadingMore.value) return;

  const oldest = messages.value[0];
  if (!oldest) return;

  loadingMore.value = true;
  try {
    const older = await msgApi.list(chatId.value, String(oldest.createdAt));

    if (older && older.length > 0) {
      const merged = [...older, ...messages.value];
      const uniq = new Map<string, MessageDto>();
      for (const m of merged) uniq.set(String(m.id), m);
      messages.value = Array.from(uniq.values()).sort(sortAsc);
    }
  } finally {
    loadingMore.value = false;
  }
}

async function sendText() {
  const text = draft.value.trim();
  if (!text) return;
  if (sending.value) return;

  sending.value = true;
  try {
    const created = await msgApi.sendText(chatId.value, { text });
    upsertMessage(created);
    draft.value = "";
    stopTypingNow();
    scrollToBottom();
  } finally {
    sending.value = false;
  }
}

function onWsEvent(e: WsEventUnion) {
  if (isMessageCreatedEvent(e) || isMessageEditedEvent(e)) {
    upsertMessage(e.payload);
    scrollToBottom();
    return;
  }

  if (isMessageDeletedEvent(e)) {
    removeMessage(String(e.payload.messageId));
    return;
  }

  if (isUserTypingEvent(e)) {
    const uid = String(e.payload.userId);
    if (!me.isMe(uid)) {
      typingUsers.value.add(uid);
      typingUsers.value = new Set(typingUsers.value);
    }
    return;
  }

  if (isUserStopTypingEvent(e)) {
    const uid = String(e.payload.userId);
    if (typingUsers.value.has(uid)) {
      typingUsers.value.delete(uid);
      typingUsers.value = new Set(typingUsers.value);
    }
  }
}

async function connectWs() {
  await ws.connect();
  ws.subscribeChat(chatId.value, onWsEvent);

  const last = messages.value[messages.value.length - 1];
  if (last?.id) {
    ws.sendReadUpTo({ chatId: chatId.value, upToMessageId: last.id });
  }
}

function cleanupWs() {
  try {
    ws.unsubscribeChat(chatId.value);
  } catch (e) {
    console.debug("unsubscribeChat failed", e);
  }
}

function startTyping() {
  if (!typingSent) {
    typingSent = true;
    ws.sendTyping(chatId.value, true);
  }

  if (typingStopTimer) clearTimeout(typingStopTimer);
  typingStopTimer = setTimeout(() => stopTypingNow(), 1500);
}

function stopTypingNow() {
  if (typingStopTimer) clearTimeout(typingStopTimer);
  typingStopTimer = null;

  if (typingSent) {
    typingSent = false;
    ws.sendTyping(chatId.value, false);
  }
}

function onInput() {
  if (!draft.value.trim()) {
    stopTypingNow();
    return;
  }

  if (!ws.connected.value) return;
  startTyping();
}

function formatTime(iso: string) {
  return String(iso).slice(11, 16);
}

const chatTitle = computed(() => chat.value?.title || `Chat ${chatId.value}`);

await loadInitial();

onMounted(async () => {
  await connectWs();
});

onBeforeUnmount(() => {
  cleanupWs();
  stopTypingNow();
});
</script>

<style scoped>
.chat {
  height: 100%;
  display: grid;
  grid-template-rows: auto 1fr;
  min-height: 0;
}

.head {
  padding: 14px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
}

.left {
  min-width: 0;
  display: grid;
  gap: 2px;
}

.title {
  font-weight: 900;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sub {
  font-size: 12px;
  opacity: 0.7;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.body {
  min-height: 0;
  display: grid;
  grid-template-rows: auto 1fr auto;
}

.topActions {
  padding: 8px 12px;
  display: flex;
  justify-content: center;
}

.list {
  min-height: 0;
  overflow: auto;
  padding: 12px;
}

.loading {
  padding: 12px;
}

.empty {
  opacity: 0.7;
  display: grid;
  place-items: center;
  height: 100%;
}

.msgs {
  display: grid;
  gap: 10px;
}

.msg {
  display: flex;
}

.msg.mine {
  justify-content: flex-end;
}

.bubble {
  max-width: min(680px, 88%);
  padding: 10px 12px;
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
}

.msg.mine .bubble {
  background: rgba(0, 255, 180, 0.08);
  border-color: rgba(0, 255, 180, 0.16);
}

.text {
  white-space: pre-wrap;
  word-break: break-word;
}

.meta {
  margin-top: 6px;
  font-size: 11px;
  opacity: 0.6;
  text-align: right;
}

.composer {
  padding: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
  background: rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
}

.input {
  width: 100%;
}
</style>
