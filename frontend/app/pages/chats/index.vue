<script setup lang="ts">
definePageMeta({ middleware: ["auth"], layout: "chats" });
</script>

<template>
  <div class="empty">
    <div class="bubble">Select a chat to start messaging</div>
  </div>
</template>

<style scoped>
.empty {
  height: 100%;
  display: grid;
  place-items: center;
}
.bubble {
  padding: 8px 12px;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  background: rgba(255, 255, 255, 0.04);
  opacity: 0.8;
}
</style>
