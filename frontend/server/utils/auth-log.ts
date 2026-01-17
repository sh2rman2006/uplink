// server/utils/auth-log.ts
import type { H3Event } from "h3";

export function authLog(event: H3Event, msg: string, extra?: any) {
  const url = event.path || event.node.req.url;
  // ip может быть undefined локально — норм
  const ip =
    (event.node.req.headers["x-forwarded-for"] as string | undefined) ??
    (event.node.req.socket?.remoteAddress as string | undefined);

  const payload = extra ? ` ${JSON.stringify(extra)}` : "";
  // eslint-disable-next-line no-console
  console.log(`[AUTH] ${msg} | url=${url} ip=${ip}${payload}`);
}
