import { useEffect, useState } from 'react';
import { useApi, ApiError } from '../api/useApi';
import { useAuth } from '../auth/auth';
import type { UserFullView } from '../types';
import GuardianHome from './GuardianHome';
import MonitorHome from './MonitorHome';

// Role-aware landing: fetch the current user, then render the right home.
export default function Home() {
  const api = useApi();
  const { logout } = useAuth();
  const [me, setMe] = useState<UserFullView | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .get<UserFullView>('/users/me')
      .then(setMe)
      .catch((e: ApiError) => setError(`${e.status}: ${e.message}`));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (error) {
    return (
      <Centered>
        <p className="text-sm text-red-700">{error}</p>
        <button onClick={logout} className="mt-3 rounded border px-3 py-1 text-sm">Log out</button>
      </Centered>
    );
  }
  if (!me) return <Centered><p className="text-sm text-navy-500">Loading…</p></Centered>;

  if (me.role === 'PARENT_HELPER') return <GuardianHome me={me} />;
  if (me.role === 'MONITOR' || me.role === 'ADMIN') return <MonitorHome me={me} />;

  // PENPAL: real penpals are reached via a guardian's acting-as flow, not a direct login.
  return (
    <Centered>
      <p className="max-w-sm text-center text-sm text-navy-700">
        Penpals are accessed through their guardian (acting-as). Sign in as a parent/helper to view them.
      </p>
      <button onClick={logout} className="mt-3 rounded border px-3 py-1 text-sm">Log out</button>
    </Centered>
  );
}

function Centered({ children }: { children: React.ReactNode }) {
  return <div className="grid min-h-screen place-items-center bg-navy-50">{children}</div>;
}
