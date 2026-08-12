import { useEffect, useRef, useState } from 'react';
import { useApi, ApiError } from '../api/useApi';
import type { ChatSimpleView, PenpalMonitorView, SimpleChatMessageView } from '../types';
import Modal from '../components/Modal';
import { STATE_ZONE } from '../stateZones';
import exitImg from '../assets/exit.webp';

const errText = (e: unknown) => `${(e as ApiError).status}: ${(e as ApiError).message}`;
type Api = ReturnType<typeof useApi>;

const ordinal = (n: number) => {
  const v = n % 100;
  if (v >= 11 && v <= 13) return 'th';
  return ['th', 'st', 'nd', 'rd'][n % 10] ?? 'th';
};
// e.g. "1pm on Aug 15th, 2026" — rounded to the nearest hour, in the AUTHOR's local time
function friendlyTime(iso: string, state: string | null): string {
  const zone = (state && STATE_ZONE[state]) || undefined; // undefined -> viewer's local time
  const r = new Date(Math.round(new Date(iso).getTime() / 3_600_000) * 3_600_000); // nearest hour
  const parts = new Intl.DateTimeFormat('en-US', {
    timeZone: zone, hour: 'numeric', hour12: true, month: 'short', day: 'numeric', year: 'numeric',
  }).formatToParts(r);
  const get = (t: string) => parts.find((p) => p.type === t)?.value ?? '';
  const day = Number(get('day'));
  const ap = get('dayPeriod').toLowerCase().replace(/[^a-z]/g, '');
  return `${get('hour')}${ap} on ${get('month')} ${day}${ordinal(day)}, ${get('year')}`;
}
type Tab = 'chat' | 'old' | 'ideas' | 'learning';

// Full-screen penpal experience (guardian acting as this penpal). No app chrome —
// green header with the penpal's name, and a big bottom nav. Mobile-first, low data.
export default function PenpalMode({ penpal, onExit }: { penpal: PenpalMonitorView; onExit: () => void }) {
  const api = useApi();
  const [tab, setTab] = useState<Tab>('chat');
  const [chats, setChats] = useState<ChatSimpleView[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [openOldId, setOpenOldId] = useState<number | null>(null);
  const [showInfo, setShowInfo] = useState(false);

  useEffect(() => {
    api.get<ChatSimpleView[]>('/penpal/penpals/chats', penpal.id)
      .then(setChats)
      .catch((e) => setError(errText(e)));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [penpal.id]);

  const active = chats?.find((c) => c.active) ?? null;
  const inactive = chats?.filter((c) => !c.active) ?? [];

  return (
    <div className="flex h-screen flex-col bg-navy-50">
      <header className="flex shrink-0 items-center justify-between bg-teal-600 px-4 py-3 text-white shadow-md">
        <button onClick={() => setShowInfo(true)} className="text-left text-xl font-bold underline-offset-4 hover:underline">
          {penpal.firstName} {penpal.lastName}
        </button>
        <div className="flex items-center gap-3">
          <button onClick={onExit} className="rounded-full border border-white/40 px-3 py-1 text-sm font-semibold active:bg-white/10">
            Exit
          </button>
          <img src={exitImg} alt="" className="h-8 w-auto" />
        </div>
      </header>

      <main className="w-full max-w-xl mx-auto min-h-0 flex-1 overflow-y-auto p-4">
        {error && <p className="mb-4 rounded-lg bg-coral-50 p-3 text-sm text-coral-700">{error}</p>}

        {tab === 'chat' && (
          chats === null ? <Loading /> :
          active ? <ChatThread chatId={active.id} penpalId={penpal.id} api={api} /> :
          <Empty text="No chat yet." />
        )}

        {tab === 'old' && (
          openOldId !== null
            ? <ChatThread chatId={openOldId} penpalId={penpal.id} api={api} readOnly onBack={() => setOpenOldId(null)} />
            : <OldChats chats={inactive} penpalId={penpal.id} onOpen={setOpenOldId} loading={chats === null} />
        )}

        {tab === 'ideas' && <Empty text="Ideas — coming soon." />}
        {tab === 'learning' && <Empty text="Learning — coming soon." />}
      </main>

      <nav className="flex shrink-0 border-t border-navy-100 bg-white shadow-[0_-2px_8px_rgba(15,39,71,0.08)]">
        <NavButton active={tab === 'chat'} label="Chat" onClick={() => setTab('chat')} icon={
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
        } />
        <NavButton active={tab === 'ideas'} label="Ideas" onClick={() => setTab('ideas')} icon={
          <><path d="M9 18h6" /><path d="M10 22h4" /><path d="M12 2a7 7 0 0 0-4 12.8c.6.5 1 1.2 1 2V17h6v-.2c0-.8.4-1.5 1-2A7 7 0 0 0 12 2z" /></>
        } />
        <NavButton active={tab === 'learning'} label="Learning" onClick={() => setTab('learning')} icon={
          <><path d="M2 5h6a3 3 0 0 1 3 3v12a2.5 2.5 0 0 0-2.5-2H2z" /><path d="M22 5h-6a3 3 0 0 0-3 3v12a2.5 2.5 0 0 1 2.5-2H22z" /></>
        } />
        <NavButton active={tab === 'old'} label="Old chats" onClick={() => { setTab('old'); setOpenOldId(null); }} icon={
          <><path d="M3 4h18v4H3z" /><path d="M5 8v12h14V8" /><path d="M9 12h6" /></>
        } />
      </nav>

      {showInfo && (
        <Modal title={`${penpal.firstName} ${penpal.lastName}`} onClose={() => setShowInfo(false)}>
          <dl className="space-y-1 text-base">
            {penpal.age != null && <InfoRow label="Age" value={String(penpal.age)} />}
            {penpal.state && <InfoRow label="Place" value={penpal.state} />}
          </dl>
          {penpal.biography && (
            <div className="mt-3">
              <p className="text-sm font-bold text-navy-500">About me</p>
              <p className="text-base">{penpal.biography}</p>
            </div>
          )}
        </Modal>
      )}
    </div>
  );
}

function InfoRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-4">
      <dt className="text-navy-500">{label}</dt>
      <dd className="font-semibold">{value}</dd>
    </div>
  );
}

function ChatThread({
  chatId, penpalId, api, readOnly, onBack,
}: {
  chatId: number; penpalId: number; api: Api; readOnly?: boolean; onBack?: () => void;
}) {
  const [data, setData] = useState<SimpleChatMessageView | null>(null);
  const [text, setText] = useState('');
  const [error, setError] = useState<string | null>(null);

  const load = () =>
    api.get<SimpleChatMessageView>(`/penpal/penpals/chats/${chatId}`, penpalId)
      .then(setData)
      .catch((e) => setError(errText(e)));

  useEffect(() => { void load(); /* eslint-disable-next-line */ }, [chatId]);

  // compose box grows with what's typed, up to a max, then scrolls
  const taRef = useRef<HTMLTextAreaElement>(null);
  useEffect(() => {
    const el = taRef.current;
    if (!el) return;
    el.style.height = 'auto';
    el.style.height = `${el.scrollHeight}px`;
  }, [text]);

  const companion = data?.chatInfo.members.find((m) => m.id !== penpalId);
  const me = data?.chatInfo.members.find((m) => m.id === penpalId);

  async function send() {
    try {
      await api.post('/penpal/penpals/messages', { text, chatId }, penpalId);
      setText('');
      void load();
    } catch (e) {
      setError(errText(e));
    }
  }

  return (
    <div className="flex h-full flex-col">
      {onBack && (
        <button onClick={onBack} className="mb-3 text-base font-bold text-teal-700">← Back</button>
      )}
      {error && <p className="mb-3 rounded-lg bg-coral-50 p-3 text-sm text-coral-700">{error}</p>}

      {/* companion bio — offset out to the left, narrow, so it stands apart from the chat */}
      {companion && (
        <div className="mb-5 -ml-2 mr-auto max-w-[62%] rounded-3xl bg-coral-300 p-4 text-coral-900 shadow-lg shadow-coral-900/40">
          <p className="text-xs font-bold uppercase tracking-wide text-coral-700">Your friend</p>
          <p className="text-xl font-bold">
            {companion.firstName}{companion.age != null ? ` · ${companion.age}` : ''}
          </p>
          {companion.state && <p className="text-sm text-coral-700">{companion.state}</p>}
          {companion.biography && <p className="mt-1 text-sm">{companion.biography}</p>}
        </div>
      )}

      <div className="flex-1 space-y-2">
        {data === null ? <Loading /> :
          data.messages.length === 0 ? <Empty text="No messages yet." /> :
          data.messages.map((m) => {
            // monitor broadcast: synthesized author has no id
            if (m.penpal?.id == null) {
              return (
                <div key={m.id} className="mx-auto max-w-[92%] rounded-2xl bg-navy-500 px-3 py-2 text-center text-base text-white">
                  <p className="text-xs font-bold uppercase tracking-wide text-white/70">📢 Monitor</p>
                  <p className="whitespace-pre-wrap">{m.text}</p>
                </div>
              );
            }
            const mine = m.penpal.id === penpalId;
            const { parts, notes } = parseMonitorEdits(m.text);
            if (mine) {
              const pending = m.approved !== true; // only a monitor's approval (true) is "approved"
              // handwritten letter: teal "pen" on paper — lighter while pending, darker once approved
              return (
                <div key={m.id} className="flex items-start justify-end gap-2">
                  {notes.length > 0 && <MonitorNotes notes={notes} side="left" />}
                  <div className={`max-w-[85%] rounded-sm border-2 bg-[#f7f1e1] px-4 py-2.5 ${pending ? 'border-teal-300 text-teal-600' : 'border-teal-600 text-teal-800'}`}>
                    <div className="flex items-baseline justify-between gap-3">
                      <p className="font-shadows text-2xl leading-snug">Dear {companion?.firstName ?? 'friend'},</p>
                      <p className="shrink-0 font-shadows text-lg leading-none">{friendlyTime(m.createTime, m.penpal.state ?? null)}</p>
                    </div>
                    <LetterText parts={parts} className="font-shadows text-2xl leading-snug" />
                    <p className="mt-2 whitespace-pre-wrap text-right font-shadows text-2xl leading-snug">
                      Your friend,{'\n'}{me?.firstName ?? ''}
                    </p>
                    <p className="mt-1 text-right font-sans text-xs font-bold">
                      {pending ? '✓ sent' : '✓ approved'}
                    </p>
                  </div>
                </div>
              );
            }
            return (
              <div key={m.id} className="flex items-start gap-2">
                <div className="max-w-[85%] rounded-sm border-2 border-coral-400 bg-white px-4 py-2.5 text-coral-700">
                  <div className="flex items-baseline justify-between gap-3">
                    <p className="font-caveat text-2xl leading-snug">Dear {me?.firstName ?? 'friend'},</p>
                    <p className="shrink-0 font-caveat text-lg leading-none">{friendlyTime(m.createTime, m.penpal.state ?? null)}</p>
                  </div>
                  <LetterText parts={parts} className="font-caveat text-2xl leading-snug" />
                  <p className="mt-2 whitespace-pre-wrap text-right font-caveat text-2xl leading-snug">
                    Your friend,{'\n'}{m.penpal.firstName}
                  </p>
                </div>
                {notes.length > 0 && <MonitorNotes notes={notes} side="right" />}
              </div>
            );
          })}
      </div>

      {!readOnly && (
        <div className="mt-3 rounded-sm border-2 border-teal-300 bg-[#fbf8f0] p-4">
          {/* gray letter frame — auto-added, so the user only writes the middle */}
          <p className="font-shadows text-2xl leading-snug text-navy-400">Dear {companion?.firstName ?? 'friend'},</p>
          <textarea
            ref={taRef}
            value={text}
            onChange={(e) => setText(e.target.value)}
            rows={2}
            placeholder="Write your letter…"
            className="my-1 max-h-[45vh] min-h-[3.5rem] w-full resize-none overflow-y-auto bg-transparent font-shadows text-2xl leading-snug text-teal-800 outline-none placeholder:font-sans placeholder:text-base placeholder:text-navy-400"
          />
          <p className="mt-1 text-right font-shadows text-2xl leading-snug text-navy-400">Your friend,</p>
          <p className="text-right font-shadows text-2xl leading-snug text-navy-400">{me?.firstName ?? ''}</p>
          <button onClick={send} disabled={!text.trim()} className="mt-3 w-full rounded-xl bg-teal-600 py-3 text-base font-bold text-white active:bg-teal-700 disabled:opacity-40">
            Send
          </button>
        </div>
      )}
    </div>
  );
}

function OldChats({
  chats, penpalId, onOpen, loading,
}: {
  chats: ChatSimpleView[]; penpalId: number; onOpen: (id: number) => void; loading: boolean;
}) {
  if (loading) return <Loading />;
  if (chats.length === 0) return <Empty text="No old chats." />;
  return (
    <ul className="space-y-2">
      {chats.map((c) => {
        const friend = c.members.find((m) => m.id !== penpalId);
        return (
          <li key={c.id}>
            <button onClick={() => onOpen(c.id)} className="w-full rounded-2xl border bg-white p-4 text-left active:bg-navy-50">
              <p className="text-lg font-bold">{friend?.firstName ?? 'Chat'}</p>
              {friend?.state && <p className="text-sm text-navy-500">{friend.state}</p>}
            </button>
          </li>
        );
      })}
    </ul>
  );
}

function NavButton({
  active, label, onClick, icon,
}: {
  active: boolean; label: string; onClick: () => void; icon: React.ReactNode;
}) {
  return (
    <button onClick={onClick} className={`flex flex-1 flex-col items-center gap-1 border-t-4 py-2 text-xs font-bold ${active ? 'border-teal-600 bg-teal-600 text-white' : 'border-transparent text-navy-400'}`}>
      <svg viewBox="0 0 24 24" className="h-6 w-6" fill="none" stroke="currentColor" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" aria-hidden>
        {icon}
      </svg>
      {label}
    </button>
  );
}

// monitor inline edits: "(original)[correction]" -> highlight the original, and
// surface the correction as a side note. (Authored by the monitor by hand for now.)
type Part = { t: 'text'; v: string } | { t: 'mark'; orig: string; note: string };
function parseMonitorEdits(text: string): { parts: Part[]; notes: string[] } {
  const re = /\(([^()]*)\)\[([^\]]*)\]/g;
  const parts: Part[] = [];
  const notes: string[] = [];
  let last = 0;
  let m: RegExpExecArray | null;
  while ((m = re.exec(text)) !== null) {
    if (m.index > last) parts.push({ t: 'text', v: text.slice(last, m.index) });
    parts.push({ t: 'mark', orig: m[1], note: m[2] });
    notes.push(m[2]);
    last = re.lastIndex;
  }
  if (last < text.length) parts.push({ t: 'text', v: text.slice(last) });
  return { parts, notes };
}

function LetterText({ parts, className }: { parts: Part[]; className: string }) {
  return (
    <p className={`whitespace-pre-wrap ${className}`}>
      {parts.map((p, i) =>
        p.t === 'text'
          ? <span key={i}>{p.v}</span>
          : <span key={i} className="rounded bg-navy-100 px-1 text-navy-800">{p.orig}</span>,
      )}
    </p>
  );
}

// navy "monitor" comment bubbles beside the letter, pointing toward it
function MonitorNotes({ notes, side }: { notes: string[]; side: 'left' | 'right' }) {
  return (
    <div className="flex w-[30%] shrink-0 flex-col gap-2 pt-9">
      {notes.map((n, i) => (
        <div key={i} className={`relative rounded-2xl bg-navy-600 px-3 py-1.5 text-white ${side === 'left' ? 'self-end' : 'self-start'}`}>
          <span className={`absolute top-3 h-2.5 w-2.5 rotate-45 bg-navy-600 ${side === 'left' ? '-right-1' : '-left-1'}`} />
          <span className="relative block text-[10px] font-bold uppercase tracking-wide text-white/60">Monitor</span>
          <span className="relative block text-sm leading-snug">{n}</span>
        </div>
      ))}
    </div>
  );
}

function Loading() {
  return <p className="text-center text-base text-navy-500">Loading…</p>;
}

function Empty({ text }: { text: string }) {
  return <p className="text-center text-base text-navy-400">{text}</p>;
}
