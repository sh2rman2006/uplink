<template>
  <div class="shell">
    <aside class="rail">
      <Button
        class="railBtn"
        icon="pi pi-bars"
        severity="secondary"
        text
        aria-label="Menu"
        @click="menuOpen = true"
      />

      <div class="railSep" />

      <Button class="railBtn" icon="pi pi-comments" severity="secondary" text />
      <Button class="railBtn" icon="pi pi-users" severity="secondary" text />
      <Button class="railBtn" icon="pi pi-bookmark" severity="secondary" text />

      <div class="railGrow" />

      <Button
        class="railBtn"
        icon="pi pi-sign-out"
        severity="secondary"
        text
        aria-label="Logout"
        @click="logout"
      />
    </aside>

    <ChatsSidebar
      class="chats"
      :selected-chat-id="selectedChatId"
      @select="onSelect"
      @create="onCreate"
      @refresh="onRefresh"
    />

    <main class="content">
      <slot />
    </main>

    <Sidebar v-model:visible="menuOpen" position="left" class="menu">
      <template #header>
        <div class="menuHead">
          <Avatar shape="circle" size="large" label="U" />
          <div class="menuHeadMeta">
            <div class="menuTitle">Uplink</div>
            <div class="menuSub">Settings</div>
          </div>
        </div>
      </template>

      <div class="menuList">
        <Button class="menuItem" icon="pi pi-user" label="Profile" text />
        <Button class="menuItem" icon="pi pi-cog" label="Settings" text />
        <Button class="menuItem" icon="pi pi-bell" label="Notifications" text />
        <div class="menuSep" />
        <Button
          class="menuItem danger"
          icon="pi pi-sign-out"
          label="Logout"
          text
          @click="logout"
        />
      </div>
    </Sidebar>
  </div>
</template>

<script setup lang="ts">
import ChatsSidebar from "~/components/chats/ChatsSidebar.vue";

const me = useMeStore();

await useAsyncData(
  "get-my-profile",
  () => me.ensureLoaded(),
  { server: true }
);

const route = useRoute();
const router = useRouter();

const menuOpen = ref(false);

const selectedChatId = computed<string | null>(() => {
  const id = route.params.chatId;
  return typeof id === "string" ? id : null;
});

function onSelect(chatId: string) {
  router.push(`/chats/${chatId}`);
}

function onCreate() {
  console.log("create chat");
}

function onRefresh() {
  console.log("refresh");
}

const { clear } = useUserSession();

async function logout() {
  menuOpen.value = false;
  await clear();
  await navigateTo("/login");
}
</script>

<style scoped>
.shell {
  height: 100dvh;
  display: grid;
  grid-template-columns: 56px 320px 1fr;
  min-height: 0;
  overflow: hidden;
}

.rail {
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(0, 0, 0, 0.14);
  backdrop-filter: blur(10px);

  display: grid;
  grid-template-rows: auto auto auto auto auto 1fr auto;
  gap: 6px;
  padding: 10px 6px;
  min-height: 0;
}

.railBtn {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  justify-self: center;
}

.railSep {
  height: 1px;
  background: rgba(255, 255, 255, 0.06);
  margin: 6px 8px;
}

.railGrow {
  min-height: 0;
}

.chats {
  min-height: 0;
}

.content {
  min-height: 0;
  overflow: hidden;
}

:deep(.menu) {
  width: 320px;
  background: rgba(9, 14, 32, 0.92);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(12px);
}

.menuHead {
  display: flex;
  gap: 10px;
  align-items: center;
}

.menuHeadMeta {
  display: grid;
  gap: 2px;
}

.menuTitle {
  font-weight: 900;
}

.menuSub {
  opacity: 0.7;
  font-size: 12px;
}

.menuList {
  display: grid;
  gap: 6px;
  padding-top: 6px;
}

.menuItem {
  justify-content: flex-start;
  border-radius: 14px;
}

.menuSep {
  height: 1px;
  background: rgba(255, 255, 255, 0.08);
  margin: 8px 0;
}

.menuItem.danger :deep(.p-button-label) {
  color: #ffb4b4;
}
</style>
