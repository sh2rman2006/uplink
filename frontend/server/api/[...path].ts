export default defineEventHandler(async (event) => {
  const cfg = useRuntimeConfig();
  const session = await getUserSession(event);
  const accessToken = session.secure?.accessToken as string | undefined;

  if (!accessToken) {
    throw createError({ statusCode: 401, statusMessage: "Unauthorized" });
  }

  const path = getRouterParam(event, "path") || "";
  const base = cfg.public.apiBase.replace(/\/+$/, "");
  const targetUrl = `${base}/${path}`;

  const headers = { ...getRequestHeaders(event) } as Record<string, string>;

  delete headers.host;
  delete headers["content-length"];

  headers.authorization = `Bearer ${accessToken}`;

  return proxyRequest(event, targetUrl, { headers });
});
