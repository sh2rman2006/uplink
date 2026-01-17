export {};

declare module "#auth-utils" {
  interface User {
    sub: string;
    preferred_username?: string;
    email?: string;
    name?: string;
  }

  interface SecureSessionData {
    accessToken?: string;
    refreshToken?: string;
    idToken?: string;
    expiresAt?: number;
    pkce?: {
      verifier: string;
    };
  }

  interface UserSession {
    loggedInAt?: number;
  }
}
