import { useEffect, useState } from 'react';
import { useApi, ApiError } from '../../api/useApi';
import type { ChatMonitorView, MessageMonitorView, MonitorChatMessageView, PenpalMonitorView } from '../../types';
import Modal from '../../components/Modal';
import EditMessageModal from '../../components/EditMessageModal';
import { NaughtyText } from '../../naughty';

const fmt = (iso: string | null) => (iso ? new Date(iso).toLocaleString() : '—');
const names = (m: PenpalMonitorView[]) => m.map((p) => p.firstName).join(' & ');
const errText = (e: unknown) => `${(e as ApiError).status}: ${(e as ApiError).message}`;

// chat + the message aggregates the list endpoint doesn't carry (derived from /messages/all)
type ChatRow = ChatMonitorView & {
  messages: MessageMonitorView[];
  messageCount: number;
  lastMessageTime: string | null;
  unapprovedCount: number;
};

// Tab 3 — chat list. The backend keeps messages separate from chat views, so we
// stitch /chats/all together with /messages/all here to show counts + threads.
export default function ChatsTab() {
  const api = useApi();
  const [chats, setChats] = useState<ChatRow[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [open, setOpen] = useState<ChatRow | null>(null);
  const [blasting, setBlasting] = useState(false);
  const [blastText, setBlastText] = useState('');
  const [editing, setEditing] = useState<MessageMonitorView | null>(null);

  const fetchRows = async (): Promise<ChatRow[]> => {
    // one call: each chat already comes bundled with its messages
    const list = await api.get<MonitorChatMessageView[]>('/penpal/monitors/chats/all');

    const rows: ChatRow[] = list.map(({ chatInfo, messages }) => {
      const ms = messages.slice().sort((a, b) => a.createTime.localeCompare(b.createTime));
      return {
        ...chatInfo,
        messages: ms,
        messageCount: ms.length,
        lastMessageTime: ms.length ? ms[ms.length - 1].createTime : null,
        unapprovedCount: ms.filter((m) => m.approved == null).length,
      };
    });

    // active chats first; inactive after. Within each, most-recently-active first.
    rows.sort(
      (a, b) =>
        Number(b.active) - Number(a.active) ||
        (b.lastMessageTime ?? '').localeCompare(a.lastMessageTime ?? ''),
    );

    return rows;
  };

  const load = async () => {
    try {
      setChats(await fetchRows());
    } catch (e) {
      setError(errText(e));
    }
  };

  async function approve(messageId: number) {
    try {
      await api.patch(`/penpal/monitors/messages/${messageId}/approval`, { approved: true });
      const rows = await fetchRows();
      setChats(rows);
      // keep the open modal in sync (message flips to approved, button disappears)
      setOpen((o) => (o ? rows.find((r) => r.id === o.id) ?? null : null));
    } catch (e) {
      setError(errText(e));
    }
  }

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function afterEdit() {
    setEditing(null);
    const rows = await fetchRows();
    setChats(rows);
    setOpen((o) => (o ? rows.find((r) => r.id === o.id) ?? null : null));
  }

  async function sendBlast() {
    try {
      await api.post('/penpal/monitors/messages', { text: blastText });
      setBlasting(false);
      setBlastText('');
      void load();
    } catch (e) {
      setError(errText(e));
    }
  }

  async function toggleActive(c: ChatRow) {
    try {
      await api.patch(`/penpal/monitors/chats/${c.id}/activation?active=${!c.active}`, {});
      await load();
    } catch (e) {
      setError(errText(e));
    }
  }

  const firstInactive = chats.findIndex((c) => !c.active);

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-sm font-bold uppercase tracking-wide text-navy-500">Chats</h2>
        <button
          onClick={() => { setBlastText(''); setBlasting(true); }}
          className="flex items-center gap-1.5 rounded-full bg-coral-500 px-4 py-1.5 text-sm font-semibold text-white hover:bg-coral-600"
        >
          <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth={2.5} strokeLinecap="round" strokeLinejoin="round" aria-hidden>
            <path d="M3 11l18-5v12L3 14v-3z" />
            <path d="M11.6 16.8a3 3 0 1 1-5.8-1.6" />
          </svg>
          New blast
        </button>
      </div>

      {error && <p className="mb-4 rounded-lg bg-coral-50 p-3 text-sm text-coral-700">{error}</p>}

      <ul className="divide-y rounded-2xl border bg-white">
        {chats.map((c, i) => (
          <li key={c.id}>
            {/* a header row where the inactive section begins */}
            {i === firstInactive && firstInactive > 0 && (
              <div className="bg-navy-50 px-4 py-1 text-[11px] font-semibold uppercase tracking-wide text-navy-400">
                Inactive
              </div>
            )}
            <div className={`flex items-center ${c.active ? '' : 'opacity-60'}`}>
              <button
                onClick={() => setOpen(c)}
                className="flex flex-1 items-center justify-between px-4 py-3 text-left hover:bg-navy-50"
              >
                <span className="flex flex-col">
                  <span className="font-semibold">{names(c.members)}</span>
                  <span className="text-xs text-navy-400">
                    {c.members.map((p) => p.state ?? '—').join(' & ')}
                  </span>
                </span>
                <span className="flex items-center gap-3 text-xs text-navy-500">
                  {c.unapprovedCount > 0 && (
                    <span
                      title={`${c.unapprovedCount} awaiting approval`}
                      className="rounded-full bg-coral-100 px-2 py-0.5 font-semibold text-coral-700"
                    >
                      ● {c.unapprovedCount}
                    </span>
                  )}
                  <span>{c.messageCount} msgs</span>
                  <span>{fmt(c.lastMessageTime)}</span>
                </span>
              </button>
              <button
                onClick={() => toggleActive(c)}
                className={`mr-3 shrink-0 rounded-full border px-3 py-1 text-xs font-semibold transition ${
                  c.active
                    ? 'border-coral-200 text-coral-600 hover:bg-coral-50'
                    : 'border-teal-200 text-teal-600 hover:bg-teal-50'
                }`}
              >
                {c.active ? 'Deactivate' : 'Activate'}
              </button>
            </div>
          </li>
        ))}
      </ul>

      {chats.length === 0 && !error && <p className="mt-4 text-sm text-navy-500">No chats yet.</p>}

      {blasting && (
        <Modal title="New blast message" onClose={() => setBlasting(false)}>
          <p className="mb-2 text-sm text-navy-500">
            This message is sent to <span className="font-semibold">every active chat</span> and is auto-approved.
          </p>
          <textarea
            value={blastText}
            onChange={(e) => setBlastText(e.target.value)}
            rows={4}
            placeholder="e.g. Reminder: always be kind online!"
            className="w-full rounded-lg border px-3 py-2 text-sm"
          />
          <div className="mt-3 flex justify-end gap-2">
            <button onClick={() => setBlasting(false)} className="rounded-full border px-4 py-1 text-sm">
              Cancel
            </button>
            <button
              onClick={sendBlast}
              disabled={!blastText.trim()}
              className="rounded-full bg-coral-500 px-4 py-1 text-sm font-semibold text-white hover:bg-coral-600 disabled:opacity-40"
            >
              Send blast
            </button>
          </div>
        </Modal>
      )}

      {open && (
        <Modal
          title={`${names(open.members)}${open.active ? '' : ' · inactive'}`}
          onClose={() => setOpen(null)}
          wide
        >
          {open.messages.length === 0 ? (
            <p className="text-sm text-navy-500">No messages yet.</p>
          ) : (
            <ul className="max-h-[70vh] space-y-2 overflow-y-auto pr-1">
              {open.messages.map((m) => (
                <li key={m.id} className="rounded-lg border p-2 text-sm">
                  <div className="flex justify-between text-xs text-navy-500">
                    <span>{m.penpalAuthor.firstName}</span>
                    {m.approved == null ? (
                      <span className="inline-flex items-center gap-1 font-semibold text-coral-600">
                        <svg viewBox="0 0 24 24" className="h-3.5 w-3.5" fill="none" stroke="currentColor" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" aria-hidden><circle cx="12" cy="12" r="9" /><path d="M12 7v5l3 2" /></svg>
                        pending
                      </span>
                    ) : m.approved ? (
                      <span className="inline-flex items-center gap-1 font-semibold text-teal-600">
                        <svg viewBox="0 0 24 24" className="h-3.5 w-3.5" fill="none" stroke="currentColor" strokeWidth={3} strokeLinecap="round" strokeLinejoin="round" aria-hidden><path d="M5 13l4 4L19 7" /></svg>
                        approved
                      </span>
                    ) : (
                      <span className="inline-flex items-center gap-1 font-semibold text-coral-700">✕ rejected</span>
                    )}
                  </div>
                  <p className="whitespace-pre-wrap"><NaughtyText text={m.text} /></p>
                  <div className="mt-0.5 flex items-center justify-between">
                    <span className="text-[11px] text-navy-400">{fmt(m.createTime)}</span>
                    <span className="flex gap-2">
                      <button
                        onClick={() => setEditing(m)}
                        className="rounded-full border px-3 py-1 text-xs font-semibold hover:bg-navy-50"
                      >
                        Edit
                      </button>
                      {m.approved == null && (
                        <button
                          onClick={() => approve(m.id)}
                          className="rounded-full bg-teal-600 px-3 py-1 text-xs font-semibold text-white hover:bg-teal-700"
                        >
                          Approve
                        </button>
                      )}
                    </span>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </Modal>
      )}

      {/* rendered last so it stacks above the open thread modal */}
      {editing && (
        <EditMessageModal message={editing} onClose={() => setEditing(null)} onSaved={afterEdit} />
      )}
    </div>
  );
}
