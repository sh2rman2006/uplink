export default defineNuxtRouteMiddleware((to) => {
  const { loggedIn, ready } = useUserSession();

  if (!ready.value) return;

  if (!loggedIn.value) {
    return navigateTo(`/login?next=${encodeURIComponent(to.fullPath)}`);
  }
});
