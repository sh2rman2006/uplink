import { authLog } from "../../utils/auth-log";
import { kcEndpoints } from "../../utils/keycloak";

export default defineEventHandler(async (event) => {
  const cfg = useRuntimeConfig();
  const q = getQuery(event);

  const code = q.code?.toString();
  const state = q.state?.toString();
  const error = q.error?.toString();

  authLog(event, "Callback hit", { hasCode: !!code, hasState: !!state, error });

  if (error)
    return sendRedirect(
      event,
      `/login?error=${encodeURIComponent(error)}`,
      302
    );
  if (!code || !state)
    return sendRedirect(event, `/login?error=missing_code_or_state`, 302);

  const session = await getUserSession(event);
  const pkce = session.secure?.pkce as
    | { state: string; verifier: string; redirectUri: string; next?: string }
    | undefined;

  if (!pkce) {
    authLog(event, "PKCE not found in session.secure");
    return sendRedirect(event, `/login?error=pkce_missing`, 302);
  }

  if (pkce.state !== state) {
    authLog(event, "State mismatch", { expected: pkce.state, got: state });
    return sendRedirect(event, `/login?error=state_mismatch`, 302);
  }

  const { token, userinfo } = kcEndpoints({
    kcUrl: cfg.kcUrl,
    kcRealm: cfg.kcRealm,
  });

  authLog(event, "Exchange code -> tokens", { tokenEndpoint: token });

  // PKCE public client: НЕ используем client_secret
  const tokenRes = await $fetch<any>(token, {
    method: "POST",
    headers: { "content-type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      grant_type: "authorization_code",
      client_id: cfg.kcClientId,
      code,
      redirect_uri: pkce.redirectUri,
      code_verifier: pkce.verifier,
    }).toString(),
  });

  const accessToken = tokenRes?.access_token as string | undefined;
  const refreshToken = tokenRes?.refresh_token as string | undefined;

  authLog(event, "Tokens received", {
    hasAccess: !!accessToken,
    hasRefresh: !!refreshToken,
  });

  if (!accessToken)
    return sendRedirect(event, `/login?error=no_access_token`, 302);

  const user = await $fetch<any>(userinfo, {
    headers: { authorization: `Bearer ${accessToken}` },
  });

  authLog(event, "Userinfo received", {
    sub: user?.sub,
    preferred_username: user?.preferred_username,
    email: user?.email,
  });

  // кладём user + токены в сессию
  await setUserSession(event, {
    user: {
      sub: user.sub,
      email: user.email,
      username: user.preferred_username,
      name: user.name,
    },
    secure: {
      accessToken,
      refreshToken,
    },
    loggedInAt: Date.now(),
  });

  // очищаем pkce из secure, чтобы не висел
  await setUserSession(event, { secure: { pkce: undefined } as any });

  const next = pkce.next || "/";
  authLog(event, "Session updated (user set). Redirecting", { next });
  return sendRedirect(event, next, 302);
});
