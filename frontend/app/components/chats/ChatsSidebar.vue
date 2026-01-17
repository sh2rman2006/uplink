<template>
  <aside class="sidebar">
    <div class="topbar">
      <div class="searchRow">
        <IconField icon-position="left" class="grow">
          <InputIcon class="pi pi-search" />
          <InputText v-model="q" placeholder="Search" class="w100" />
        </IconField>

        <Button
          icon="pi pi-refresh"
          severity="secondary"
          text
          :loading="pending"
          @click="refreshAll"
        />
        <Button
          icon="pi pi-plus"
          severity="secondary"
          text
          @click="emit('create')"
        />
      </div>
    </div>

    <div class="listWrap">
      <div v-if="error" class="state">
        <i class="pi pi-exclamation-triangle" />
        <div class="t">Failed to load chats</div>
        <small class="s">{{ (error as any)?.message || error }}</small>
        <Button
          label="Retry"
          icon="pi pi-refresh"
          severity="secondary"
          text
          @click="refreshAll"
        />
      </div>

      <div v-else class="list">
        <div v-if="pending" class="skeleton">
          <Skeleton height="56px" class="mb-2" />
          <Skeleton height="56px" class="mb-2" />
          <Skeleton height="56px" class="mb-2" />
        </div>

        <ChatListItem
          v-for="c in filtered"
          :key="c.chatId"
          :chat="c"
          :active="props.selectedChatId === c.chatId"
          @select="pick"
        />

        <div v-if="!pending && filtered.length === 0" class="state">
          <i class="pi pi-inbox" />
          <div class="t">No chats</div>
          <small class="s">Try another search query</small>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import ChatListItem from "~/components/chats/ChatListItem.vue";
import { useChatsApi } from "~/composables/api/useChatsApi";
import { createPageableParams } from "../../../shared/types/dto";
import type { ChatListItemResponse } from "../../../shared/types/dto";

const props = withDefaults(
  defineProps<{
    selectedChatId?: string | null;
  }>(),
  { selectedChatId: null }
);

const emit = defineEmits<{
  (e: "select", chatId: string): void;
  (e: "create"): void;
  (e: "refresh"): unknown;
}>();

const q = ref("");
const page = ref(0);
const size = ref(50);

const chatsApi = useChatsApi();

const queryParams = computed(() =>
  createPageableParams({ page: page.value, size: size.value })
);

const {
  data,
  error,
  pending,
  refresh: refreshChats,
} = await useAsyncData("chats:list", () => chatsApi.list(queryParams.value), {
  watch: [queryParams],
});

const items = computed<ChatListItemResponse[]>(() => data.value?.content ?? []);

const filtered = computed(() => {
  const s = q.value.trim().toLowerCase();
  if (!s) return items.value;

  return items.value.filter((c) => {
    const title = (c.title ?? "").toLowerCase();
    const desc = (c.description ?? "").toLowerCase();
    const last = (c.lastMessageText ?? "").toLowerCase();
    return title.includes(s) || desc.includes(s) || last.includes(s);
  });
});

function pick(chatId: string) {
  emit("select", chatId);
}

async function refreshAll() {
  emit("refresh");
  await refreshChats();
}
</script>

<style scoped>
.sidebar {
  height: 100%;
  display: grid;
  grid-template-rows: auto 1fr;
  min-height: 0;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(8px);
}

.topbar {
  padding: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.searchRow {
  display: flex;
  gap: 8px;
  align-items: center;
}

.grow {
  flex: 1;
  min-width: 0;
}

.w100 {
  width: 100%;
}

.listWrap {
  min-height: 0;
}

.list {
  height: 100%;
  overflow: auto;
  padding: 6px;
}

.state {
  margin: 12px;
  padding: 14px;
  border-radius: 14px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.03);
  display: grid;
  gap: 8px;
  justify-items: start;
}

.state i {
  font-size: 18px;
  opacity: 0.8;
}

.t {
  font-weight: 800;
}

.s {
  opacity: 0.7;
}
</style>
