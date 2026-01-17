<script setup lang="ts">
const route = useRoute();
const { loggedIn } = useUserSession();
const { next, login } = useAuthActions();

const error = computed(() => route.query.error?.toString() || null);

watchEffect(() => {
  if (loggedIn.value) navigateTo(next.value);
});

const busy = ref(false);
async function onLogin() {
  try {
    busy.value = true;
    await login();
  } finally {
    busy.value = false;
  }
}
</script>

<template>
  <div class="auth-wrap">
    <div class="auth-bg" />
    <div class="auth-content">
      <div class="auth-brand">
        <div class="auth-logo">U</div>
        <div class="auth-brandText">
          <div class="auth-title">Uplink</div>
          <div class="auth-subtitle">Secure messenger</div>
        </div>
      </div>

      <Card class="auth-card">
        <template #title>
          <div class="auth-cardTitle">Sign in</div>
        </template>

        <template #content>
          <p class="auth-hint">
            Авторизация через Keycloak. После входа ты вернёшься на:
            <span class="auth-mono">{{ next }}</span>
          </p>

          <Message
            v-if="error"
            severity="error"
            :closable="false"
            class="auth-err"
          >
            {{ error }}
          </Message>

          <Button
            class="auth-loginBtn"
            label="Continue with Keycloak"
            icon="pi pi-lock"
            :loading="busy"
            @click="onLogin"
          />

          <div class="auth-small">
            <span class="auth-dot" />
            Cookies-based SSR session enabled
          </div>
        </template>
      </Card>

      <div class="auth-footer">
        <span class="auth-muted">© {{ new Date().getFullYear() }} Uplink</span>
      </div>
    </div>
  </div>
</template>

<style>
.auth-wrap {
  position: relative;
  min-height: 100dvh;
  overflow: hidden;
  display: grid;
  place-items: center;
}

.auth-bg {
  position: absolute;
  inset: 0;
  background: radial-gradient(
      800px 400px at 20% 20%,
      rgba(99, 102, 241, 0.18),
      transparent 60%
    ),
    radial-gradient(
      700px 380px at 80% 30%,
      rgba(34, 197, 94, 0.14),
      transparent 60%
    ),
    radial-gradient(
      900px 520px at 50% 90%,
      rgba(236, 72, 153, 0.12),
      transparent 60%
    ),
    linear-gradient(180deg, rgba(10, 10, 12, 1) 0%, rgba(14, 14, 18, 1) 100%);
}

.auth-content {
  position: relative;
  width: min(520px, calc(100vw - 32px));
  display: grid;
  gap: 18px;
}

.auth-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 8px 6px;
}

.auth-logo {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  font-weight: 800;
  font-size: 20px;
  letter-spacing: -0.02em;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.92);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.35);
}

.auth-title {
  font-size: 18px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.92);
}
.auth-subtitle {
  margin-top: 2px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.58);
}

.auth-card {
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.06) !important;
  border: 1px solid rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
  box-shadow: 0 20px 70px rgba(0, 0, 0, 0.45);
}

.auth-cardTitle {
  font-size: 18px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.92);
}

.auth-hint {
  margin: 0 0 14px 0;
  font-size: 13px;
  line-height: 1.45;
  color: rgba(255, 255, 255, 0.62);
}

.auth-mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas,
    "Liberation Mono", "Courier New", monospace;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.76);
}

.auth-err {
  margin: 0 0 14px 0;
}

.auth-loginBtn {
  width: 100%;
}

.auth-small {
  margin-top: 14px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.52);
  display: flex;
  align-items: center;
  gap: 8px;
}

.auth-dot {
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: rgba(34, 197, 94, 0.9);
  box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.18);
}

.auth-footer {
  display: flex;
  justify-content: center;
  padding-bottom: 6px;
}

.auth-muted {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.45);
}
</style>
