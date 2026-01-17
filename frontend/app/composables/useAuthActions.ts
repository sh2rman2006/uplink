export function useAuthActions() {
  const route = useRoute();
  const next = computed(() => route.query.next?.toString() || "/chats");

  function login() {
    window.location.href = `/auth/login?next=${encodeURIComponent(next.value)}`;
  }

  async function logout() {
    await $fetch("/auth/logout", { method: "POST" });
    await useUserSession().fetch();
    await navigateTo("/login");
  }

  return { next, login, logout };
}
