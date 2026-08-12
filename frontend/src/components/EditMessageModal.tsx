import { useState } from 'react';
import { useApi, ApiError } from '../api/useApi';
import type { MessageMonitorView } from '../types';
import Modal from './Modal';
import { HighlightTextarea } from '../naughty';

const errText = (e: unknown) => `${(e as ApiError).status}: ${(e as ApiError).message}`;
const names = (m: { firstName: string }[]) => m.map((p) => p.firstName).join(' & ');
const fmt = (iso: string) => new Date(iso).toLocaleString();
const status = (approved: boolean | null) =>
  approved == null ? '⏳ pending' : approved ? '✓ approved' : '✕ rejected';

// Shared edit-message modal used by both the Dashboard and Chats tabs. Shows the
// (non-editable) chat + author context above the editable message text.
export default function EditMessageModal({
  message,
  onClose,
  onSaved,
}: {
  message: MessageMonitorView;
  onClose: () => void;
  onSaved: () => void;
}) {
  const api = useApi();
  const [draft, setDraft] = useState(message.text);
  const [error, setError] = useState<string | null>(null);

  async function save() {
    try {
      await api.put(`/penpal/monitors/messages/${message.id}`, { text: draft });
      onSaved();
    } catch (e) {
      setError(errText(e));
    }
  }

  return (
    <Modal title="Edit message" onClose={onClose}>
      {error && <p className="mb-3 rounded bg-coral-50 p-2 text-sm text-coral-700">{error}</p>}

      {/* read-only context: which chat + who wrote it */}
      <dl className="mb-3 rounded-lg bg-navy-50 p-3 text-sm">
        <div className="flex justify-between gap-4">
          <dt className="text-navy-500">Chat</dt>
          <dd className="text-right font-semibold">
            #{message.chat.id} · {names(message.chat.members)}
          </dd>
        </div>
        <div className="flex justify-between gap-4">
          <dt className="text-navy-500">Author</dt>
          <dd className="text-right font-semibold">
            {message.penpalAuthor.firstName} {message.penpalAuthor.lastName}
            {message.penpalAuthor.state ? ` · ${message.penpalAuthor.state}` : ''}
          </dd>
        </div>
        <div className="flex justify-between gap-4">
          <dt className="text-navy-500">Created</dt>
          <dd className="text-right font-semibold">{fmt(message.createTime)}</dd>
        </div>
        <div className="flex justify-between gap-4">
          <dt className="text-navy-500">Status</dt>
          <dd className="text-right font-semibold">{status(message.approved)}</dd>
        </div>
      </dl>

      <HighlightTextarea
        value={draft}
        onChange={setDraft}
        rows={10}
        className="max-h-[55vh] rounded-lg border px-3 py-2 text-sm"
      />
      <div className="mt-3 flex justify-end gap-2">
        <button onClick={onClose} className="rounded-full border px-4 py-1 text-sm">
          Cancel
        </button>
        <button
          onClick={save}
          disabled={!draft.trim()}
          className="rounded-full bg-teal-600 px-4 py-1 text-sm font-semibold text-white hover:bg-teal-700 disabled:opacity-40"
        >
          Save
        </button>
      </div>
    </Modal>
  );
}
