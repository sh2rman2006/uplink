<script setup lang="ts">
const { clear } = useUserSession();
await clear();
await navigateTo("/login");
</script>

<template>
  <div />
</template>
