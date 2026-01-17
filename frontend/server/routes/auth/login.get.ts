import { authLog } from "../../utils/auth-log";
import { makePkce } from "../../utils/pkce";
import { kcEndpoints } from "../../utils/keycloak";

export default defineEventHandler(async (event) => {
  const cfg = useRuntimeConfig();

  const next = getQuery(event).next?.toString() || "/";
  const redirectUri = `${getRequestURL(event).origin}/auth/callback`;

  authLog(event, "Start login", { next, redirectUri });

  const { auth } = kcEndpoints({ kcUrl: cfg.kcUrl, kcRealm: cfg.kcRealm });
  const { state, verifier, challenge } = makePkce();

  // хранить PKCE в secure части сессии (только сервер)
  await setUserSession(
    event,
    {
      secure: {
        pkce: { state, verifier, redirectUri, next },
      },
    },
    { maxAge: 10 * 60 }
  );

  const url = new URL(auth);
  url.searchParams.set("client_id", cfg.kcClientId);
  url.searchParams.set("response_type", "code");
  url.searchParams.set("scope", "openid profile email");
  url.searchParams.set("redirect_uri", redirectUri);
  url.searchParams.set("state", state);
  url.searchParams.set("code_challenge", challenge);
  url.searchParams.set("code_challenge_method", "S256");

  authLog(event, "Redirect to Keycloak", { to: url.toString() });

  return sendRedirect(event, url.toString(), 302);
});
