<template>
  <NuxtLayout>
    <NuxtPage />
  </NuxtLayout>
</template>

<script setup lang="ts">
useHead({
  htmlAttrs: { class: "p-dark" },
  bodyAttrs: { class: "app-body" },
});
</script>

<style>
html,
body,
#__nuxt {
  height: 100%;
}

.app-body {
  margin: 0;
  min-height: 100%;
  background: #0b1020;
}
</style>
