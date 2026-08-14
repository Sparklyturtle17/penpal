import { useState } from 'react';
import type { UserFullView } from '../types';
import PageShell from '../components/PageShell';
import DashboardTab from './monitor/DashboardTab';
import UsersTab from './monitor/UsersTab';
import ChatsTab from './monitor/ChatsTab';
import AuditsTab from './monitor/AuditsTab';
import { NaughtyProvider } from '../naughty';

const BASE_TABS = [
  { key: 'dashboard', label: 'Dashboard' },
  { key: 'users', label: 'Users' },
  { key: 'chats', label: 'Chats' },
  { key: 'learning', label: 'Learning' },
  { key: 'ideas', label: 'Ideas' },
] as const;

// admin-only, floated to the right of the tab bar
const ADMIN_TABS = [
  { key: 'audits', label: 'Message Audits' },
  { key: 'logs', label: 'Logs' },
] as const;

type TabKey = (typeof BASE_TABS)[number]['key'] | (typeof ADMIN_TABS)[number]['key'];

export default function MonitorHome({ me }: { me: UserFullView }) {
  const [tab, setTab] = useState<TabKey>('dashboard');
  const isAdmin = me.role === 'ADMIN';

  const tabButton = (t: { key: TabKey; label: string }, opts: { extra?: string; admin?: boolean } = {}) => {
    const active = tab === t.key;
    const { extra = '', admin = false } = opts;
    const tone = admin
      ? active
        ? 'border-coral-500 font-bold text-coral-700'
        : 'border-transparent font-semibold text-coral-400 hover:text-coral-600'
      : active
        ? 'border-teal-600 font-bold text-teal-700'
        : 'border-transparent font-semibold text-navy-400 hover:text-navy-700';
    return (
      <button
        key={t.key}
        onClick={() => setTab(t.key)}
        className={`${extra} -mb-0.5 border-b-[3px] px-1 pb-2.5 text-base transition-colors ${tone}`}
      >
        {t.label}
      </button>
    );
  };

  return (
    <NaughtyProvider>
      <PageShell title="Monitor console" user={me} wide>
        <nav className="mb-6 flex gap-6 border-b-2 border-navy-100">
          {BASE_TABS.map((t) => tabButton(t))}
          {isAdmin && ADMIN_TABS.map((t, i) => tabButton(t, { extra: i === 0 ? 'ml-auto' : '', admin: true }))}
        </nav>

        {tab === 'dashboard' && <DashboardTab />}
        {tab === 'users' && <UsersTab />}
        {tab === 'chats' && <ChatsTab />}
        {tab === 'learning' && <Placeholder label="Learning" />}
        {tab === 'ideas' && <Placeholder label="Ideas" />}
        {tab === 'audits' && <AuditsTab />}
        {tab === 'logs' && <Placeholder label="Logs" />}
      </PageShell>
    </NaughtyProvider>
  );
}

// intentionally empty for now
function Placeholder({ label }: { label: string }) {
  return (
    <div className="grid place-items-center rounded-2xl border border-dashed border-navy-200 py-16 text-sm text-navy-400">
      {label} — coming soon.
    </div>
  );
}
