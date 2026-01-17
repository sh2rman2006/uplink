// https://nuxt.com/docs/api/configuration/nuxt-config
import Aura from "@primeuix/themes/aura";

export default defineNuxtConfig({
  compatibilityDate: "2025-07-15",
  devtools: { enabled: true },

  modules: [
    "@nuxt/eslint",
    "@nuxt/image",
    "@nuxt/hints",
    "@pinia/nuxt",
    "@primevue/nuxt-module",
    "@vueuse/nuxt",
    "nuxt-auth-utils",
  ],

  ssr: true,

  css: [
    "modern-css-reset",
    "primeicons/primeicons.css",
    "primeflex/primeflex.css",
  ],

  primevue: {
    usePrimeVue: true,
    autoImport: true,
    options: {
      ripple: true,
      inputVariant: "filled",
      theme: {
        preset: Aura,
        options: {
          darkModeSelector: ".p-dark",
          cssLayer: false,
        },
      },
    },
  },

  runtimeConfig: {
    session: {
      password: process.env.NUXT_SESSION_PASSWORD,
      cookie: {
        sameSite: "lax",
        secure: false, //dev
      },
    },
    kcUrl: process.env.NUXT_KC_URL,
    kcRealm: process.env.NUXT_KC_REALM,
    kcClientId: process.env.NUXT_KC_CLIENT_ID,

    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE,
      apiDomain: process.env.NUXT_PUBLIC_API_DOMAIN
    },
  },
});
