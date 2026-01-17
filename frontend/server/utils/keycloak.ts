export function kcEndpoints(opts: { kcUrl: string; kcRealm: string }) {
  const base = `${opts.kcUrl.replace(/\/+$/, "")}/realms/${
    opts.kcRealm
  }/protocol/openid-connect`;
  return {
    auth: `${base}/auth`,
    token: `${base}/token`,
    userinfo: `${base}/userinfo`,
    logout: `${base}/logout`,
  };
}
