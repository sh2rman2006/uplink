<template>
  <button class="row" :class="{ active }" @click="$emit('select', chat.chatId)">
    <div class="avatar">
      <Avatar v-if="avatarUrl" shape="circle" size="large" :image="avatarUrl" />
      <Avatar v-else shape="circle" size="large" :label="label" />
    </div>

    <div class="meta">
      <div class="top">
        <div class="title">{{ title }}</div>
        <div class="time">{{ time }}</div>
      </div>

      <div class="bottom">
        <div class="preview">{{ preview }}</div>
        <Badge v-if="chat.unreadCount > 0" :value="chat.unreadCount" />
      </div>
    </div>
  </button>
</template>

<script setup lang="ts">
import { useChatParticipantsApi } from "~/composables/api/useChatParticipantsApi";
import type { ChatListItemResponse } from "../../../shared/types/dto";
import { ChatType } from "../../../shared/types/dto";

const props = defineProps<{
  chat: ChatListItemResponse;
  active?: boolean;
}>();

defineEmits<{
  (e: "select", chatId: string): void;
}>();

const me = useMeStore();

function pickPeer(participants: ChatParticipantResponse[]): ChatParticipantResponse | null {
  const myId = me.myUserId;
  if (!myId) return participants?.[0] ?? null;

  return participants.find((p) => p.userId !== myId) ?? participants?.[0] ?? null;
}

const { data: participants } = await useAsyncData(
  `chat-participants-${props.chat.chatId}`,
  () => useChatParticipantsApi().list(props.chat.chatId),
  {
    server: true,
  }
);

const title = computed<string>(() => {
  if (props.chat.type === ChatType.PRIVATE && participants.value) {
    const otherParticipant = pickPeer(participants.value.content);
    return (
      otherParticipant?.displayName ||
      otherParticipant?.username ||
      "Private chat"
    );
  }
  return props.chat.title || "Group chat";
});

const avatarUrl = computed<string | null>(() => {
  if (props.chat.type === ChatType.PRIVATE) {
    return pickPeer(participants.value?.content || [])?.avatarUrl || null;
  }
  return props.chat.avatarUrl || null;
});

const preview = computed(() => {
  return (
    props.chat.lastMessageText || props.chat.description || "No messages yet"
  );
});

const time = computed(() => {
  const iso = props.chat.lastMessageAt || props.chat.updatedAt;
  if (!iso) return "";
  return iso.slice(11, 16);
});

const label = computed(() => {
  const s = (title.value || "C").trim();
  return (s[0] || "C").toUpperCase();
});
</script>

<style scoped>
.row {
  width: 100%;
  display: grid;
  grid-template-columns: 52px 1fr;
  gap: 10px;

  padding: 10px;
  border-radius: 14px;
  border: 1px solid transparent;
  background: transparent;
  color: inherit;
  text-align: left;
  cursor: pointer;
  transition: background 0.12s ease, border-color 0.12s ease;
}

.row:hover {
  background: rgba(255, 255, 255, 0.05);
}

.row.active {
  background: rgba(0, 255, 180, 0.1);
  border-color: rgba(0, 255, 180, 0.22);
}

.avatar {
  display: grid;
  place-items: center;
}

.meta {
  min-width: 0;
  display: grid;
  gap: 6px;
}

.top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.title {
  font-weight: 800;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.time {
  font-size: 12px;
  opacity: 0.65;
  flex: none;
}

.bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.preview {
  font-size: 13px;
  opacity: 0.75;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
