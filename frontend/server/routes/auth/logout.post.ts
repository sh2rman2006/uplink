import { authLog } from "../../utils/auth-log";

export default defineEventHandler(async (event) => {
  authLog(event, "Logout");
  await clearUserSession(event);
  return { ok: true };
});
