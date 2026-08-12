import { useEffect, useState, type ReactNode } from 'react';
import { useAuth } from '../auth/auth';
import { useApi, ApiError } from '../api/useApi';
import type { UserFullView } from '../types';
import Modal from './Modal';
import logo from '../assets/logo.webp';

const initials = (a: string, b: string) => `${a[0] ?? ''}${b[0] ?? ''}`.toUpperCase();
const errText = (e: unknown) => `${(e as ApiError).status}: ${(e as ApiError).message}`;

export default function PageShell({
  user,
  children,
  wide,
}: {
  title: string;
  user: UserFullView;
  children: ReactNode;
  wide?: boolean;
}) {
  const { logout } = useAuth();
  const [profileOpen, setProfileOpen] = useState(false);

  return (
    <div className="min-h-screen bg-navy-50 text-navy-900">
      {/* single navy top bar: logo · title · user · logout */}
      <div className="bg-navy-900 text-white">
        <div className={`mx-auto flex ${wide ? 'max-w-6xl' : 'max-w-3xl'} items-center justify-between px-6 py-3`}>
          <div className="flex items-center gap-2.5">
            <a href="https://alinafeglobaloutreach.org/" target="_blank" rel="noopener noreferrer" aria-label="Alinafe Global Outreach">
              <img src={logo} alt="Alinafe" className="h-8 w-auto" />
            </a>
            <span className="text-2xl font-black tracking-tight">Alinafe Penpal Program</span>
          </div>
          <div className="flex items-center gap-3">
            {/* heart avatar + name → opens the current user's profile (from /me) */}
            <button
              onClick={() => setProfileOpen(true)}
              aria-label="Your profile"
              className="flex items-center gap-2 rounded-full py-0.5 pl-0.5 pr-2 transition hover:bg-white/10"
            >
              <HeartAvatar text={initials(user.firstName, user.lastName)} />
              <span className="hidden text-left leading-tight sm:block">
                <span className="block text-sm font-semibold">{user.firstName} {user.lastName}</span>
                <span className="block text-[11px] text-white/60">{user.role}</span>
              </span>
            </button>
            <button
              onClick={logout}
              className="rounded-full border border-white/30 px-3 py-1 text-sm transition hover:border-coral-400 hover:bg-coral-500/20 hover:text-coral-100 hover:shadow-lg hover:shadow-coral-500/30"
            >
              Log out
            </button>
          </div>
        </div>
      </div>

      <div className={`mx-auto ${wide ? 'max-w-6xl' : 'max-w-3xl'} p-6`}>
        {children}
      </div>

      {profileOpen && <ProfileModal onClose={() => setProfileOpen(false)} />}
    </div>
  );
}

function HeartAvatar({ text }: { text: string }) {
  return (
    <span className="relative grid h-10 w-10 shrink-0 place-items-center">
      <svg viewBox="0 0 24 24" className="absolute h-10 w-10 text-teal-500" fill="currentColor" aria-hidden>
        <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
      </svg>
      <span className="relative -mt-0.5 text-sm font-black text-white">{text}</span>
    </span>
  );
}

// self profile, loaded fresh from /me (read-only)
function ProfileModal({ onClose }: { onClose: () => void }) {
  const api = useApi();
  const [me, setMe] = useState<UserFullView | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get<UserFullView>('/users/me').then(setMe).catch((e) => setError(errText(e)));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <Modal title="Your profile" onClose={onClose}>
      {error && <p className="mb-3 rounded bg-coral-50 p-2 text-sm text-coral-700">{error}</p>}

      {!me ? (
        <p className="text-sm text-navy-500">Loading…</p>
      ) : (
        <dl className="space-y-1 text-sm">
          <Row label="Name" value={`${me.firstName} ${me.lastName}`} />
          <Row label="Role" value={me.role} />
          <Row label="Email" value={me.email} />
          <Row label="Phone" value={me.phone} />
          <Row label="WhatsApp" value={me.whatsapp} />
        </dl>
      )}
    </Modal>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-4">
      <dt className="text-navy-500">{label}</dt>
      <dd className="text-right">{value}</dd>
    </div>
  );
}
