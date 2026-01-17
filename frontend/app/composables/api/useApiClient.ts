type ApiOptions = {
  query?: Record<string, unknown>;
  body?: any;
  headers?: Record<string, string>;
};

function withLeadingSlash(path: string) {
  return path.startsWith("/") ? path : `/${path}`;
}

export function useApiClient() {
  const baseURL = "";

  const ssrCookieHeaders = import.meta.server
    ? (useRequestHeaders(["cookie"]) as Record<string, string>)
    : undefined;

  async function request<T>(
    method: string,
    path: string,
    opts: ApiOptions = {}
  ): Promise<T> {
    const url = withLeadingSlash(path);

    return await $fetch<T>(url, {
      baseURL,
      method: method as any,
      query: opts.query,
      body: opts.body,
      credentials: "include",
      headers: {
        ...(ssrCookieHeaders ?? {}),
        ...(opts.headers ?? {}),
      },
    });
  }

  return {
    get: <T>(path: string, opts?: Omit<ApiOptions, "body">) =>
      request<T>("GET", path, opts),
    post: <T>(path: string, opts?: ApiOptions) =>
      request<T>("POST", path, opts),
    patch: <T>(path: string, opts?: ApiOptions) =>
      request<T>("PATCH", path, opts),
    put: <T>(path: string, opts?: ApiOptions) => request<T>("PUT", path, opts),
    delete: <T>(path: string, opts?: ApiOptions) =>
      request<T>("DELETE", path, opts),
  };
}
