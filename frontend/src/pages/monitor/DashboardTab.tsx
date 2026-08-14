import { useEffect, useState } from 'react';
import { useApi, ApiError } from '../../api/useApi';
import type { MessageMonitorView } from '../../types';
import Modal from '../../components/Modal';
import EditMessageModal from '../../components/EditMessageModal';
import { NaughtyBox } from '../../naughty';

const fmt = (iso: string) => new Date(iso).toLocaleString();
const chatLabel = (m: MessageMonitorView) => m.chat.members.map((p) => p.firstName).join(' & ');
const errText = (e: unknown) => `${(e as ApiError).status}: ${(e as ApiError).message}`;

// Tab 1 — unapproved messages, oldest first, with edit + approve.
export default function DashboardTab() {
  const api = useApi();
  const [msgs, setMsgs] = useState<MessageMonitorView[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [editing, setEditing] = useState<MessageMonitorView | null>(null);
  const [blasting, setBlasting] = useState(false);
  const [blastText, setBlastText] = useState('');

  const load = () =>
    api
      .get<MessageMonitorView[]>('/penpal/monitors/messages/unapproved')
      // oldest first (ISO timestamps sort chronologically); backend should also ORDER BY createTime ASC
      .then((list) => setMsgs([...list].sort((a, b) => a.createTime.localeCompare(b.createTime))))
      .catch((e) => setError(errText(e)));

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function approve(id: number) {
    try {
      await api.patch(`/penpal/monitors/messages/${id}/approval`, { approved: true });
      void load();
    } catch (e) {
      setError(errText(e));
    }
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

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-sm font-bold uppercase tracking-wide text-navy-500">Awaiting approval</h2>
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

      {msgs.length === 0 ? (
        <p className="text-sm text-navy-500">Nothing awaiting approval. 🎉</p>
      ) : (
        <ul className="space-y-3">
          {msgs.map((m) => (
            <li key={m.id} className="rounded-2xl border bg-white p-4 shadow-sm">
              <div className="mb-2 flex items-center justify-between text-xs text-navy-500">
                <span>{chatLabel(m)}</span>
                <span>{fmt(m.createTime)}</span>
              </div>
              <p className="mb-1 text-sm text-navy-500">
                From {m.penpalAuthor.firstName} {m.penpalAuthor.lastName}
                {m.penpalAuthor.state ? ` · ${m.penpalAuthor.state}` : ''}
              </p>
              <NaughtyBox text={m.text} className="whitespace-pre-wrap rounded-lg bg-navy-50 p-3 text-sm" />
              <div className="mt-3 flex gap-2">
                <button
                  onClick={() => setEditing(m)}
                  className="rounded-full border px-4 py-1 text-sm hover:bg-navy-50"
                >
                  Edit
                </button>
                <button
                  onClick={() => approve(m.id)}
                  className="rounded-full bg-teal-600 px-4 py-1 text-sm font-semibold text-white hover:bg-teal-700"
                >
                  Approve
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}

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

      {editing && (
        <EditMessageModal
          message={editing}
          onClose={() => setEditing(null)}
          onSaved={() => { setEditing(null); void load(); }}
        />
      )}
    </div>
  );
}
