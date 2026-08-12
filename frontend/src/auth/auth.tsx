import {
  createContext,
  useCallback,
  useContext,
  useState,
  type ReactNode,
} from 'react';
import { Auth0Provider, useAuth0 } from '@auth0/auth0-react';

const MODE = import.meta.env.VITE_AUTH_MODE ?? 'dev';
// Auth0 custom claim that carries roles (matches RealSecurityConfig's roles claim).
const ROLES_CLAIM = 'https://penpals.example.com/roles';

export interface AuthApi {
  isAuthenticated: boolean;
  user: { name: string; roles: string[] } | null;
  /** dev mode expects {username,password}; auth0 mode ignores the arg and redirects. */
  login: (creds?: { username: string; password: string }) => void;
  logout: () => void;
  /** Authorization header value ("Basic ..." or "Bearer ..."), or null if signed out. */
  getAuthHeader: () => Promise<string | null>;
}

const AuthContext = createContext<AuthApi | null>(null);

export function useAuth(): AuthApi {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within <AuthProvider>');
  return ctx;
}

// ---- dev mode: HTTP Basic against the Spring dev profile -------------------
function DevAuthProvider({ children }: { children: ReactNode }) {
  const [creds, setCreds] = useState<{ username: string; password: string } | null>(() => {
    const raw = localStorage.getItem('devCreds');
    return raw ? JSON.parse(raw) : null;
  });

  const value: AuthApi = {
    isAuthenticated: !!creds,
    user: creds ? { name: creds.username, roles: [] } : null,
    login: (c) => {
      if (!c) return;
      setCreds(c);
      localStorage.setItem('devCreds', JSON.stringify(c));
    },
    logout: () => {
      setCreds(null);
      localStorage.removeItem('devCreds');
    },
    getAuthHeader: async () =>
      creds ? 'Basic ' + btoa(`${creds.username}:${creds.password}`) : null,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// ---- auth0 mode: bearer JWT ------------------------------------------------
function Auth0Bridge({ children }: { children: ReactNode }) {
  const { isAuthenticated, user, loginWithRedirect, logout, getAccessTokenSilently } = useAuth0();

  const getAuthHeader = useCallback(async () => {
    if (!isAuthenticated) return null;
    const token = await getAccessTokenSilently();
    return `Bearer ${token}`;
  }, [isAuthenticated, getAccessTokenSilently]);

  const value: AuthApi = {
    isAuthenticated,
    user: user
      ? { name: user.name ?? user.email ?? 'user', roles: (user[ROLES_CLAIM] as string[]) ?? [] }
      : null,
    login: () => void loginWithRedirect(),
    logout: () => logout({ logoutParams: { returnTo: window.location.origin } }),
    getAuthHeader,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function AuthProvider({ children }: { children: ReactNode }) {
  if (MODE === 'auth0') {
    return (
      <Auth0Provider
        domain={import.meta.env.VITE_AUTH0_DOMAIN ?? ''}
        clientId={import.meta.env.VITE_AUTH0_CLIENT_ID ?? ''}
        authorizationParams={{
          redirect_uri: window.location.origin,
          audience: import.meta.env.VITE_AUTH0_AUDIENCE,
        }}
      >
        <Auth0Bridge>{children}</Auth0Bridge>
      </Auth0Provider>
    );
  }
  return <DevAuthProvider>{children}</DevAuthProvider>;
}
