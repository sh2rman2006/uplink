<script setup lang="ts">
definePageMeta({ middleware: ["auth"] });

const { data, error } = await useFetch("/api/v1/chat", {
  method: "GET",
  headers: import.meta.server ? useRequestHeaders(["cookie"]) : undefined,
  credentials: "include",
});
</script>

<template>
  <div style="padding: 24px">
    <h1>Chats</h1>
    <pre v-if="error">{{ error }}</pre>
    <pre v-else>{{ data }}</pre>
  </div>
</template>
