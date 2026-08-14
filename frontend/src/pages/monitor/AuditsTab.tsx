import { useEffect, useState } from 'react';
import { useApi, ApiError } from '../../api/useApi';
import type { AuditFullView, ListOfAudits } from '../../types';
import { NaughtyBox } from '../../naughty';

const fmt = (iso: string) => new Date(iso).toLocaleString();
const errText = (e: unknown) => `${(e as ApiError).status}: ${(e as ApiError).message}`;
const fullName = (u: { firstName: string; lastName: string }) => `${u.firstName} ${u.lastName}`;

// Group consecutive audits by message id. The backend already returns them grouped
// and ordered (most-recently-edited message first, newest snapshot first within a
// group), so we only fold runs here — never sort.
function groupByMessage(audits: AuditFullView[]): AuditFullView[][] {
  const groups: AuditFullView[][] = [];
  for (const a of audits) {
    const current = groups[groups.length - 1];
    if (current && current[0].currentMessageState.id === a.currentMessageState.id) current.push(a);
    else groups.push([a]);
  }
  return groups;
}

// Admin-only — full edit history of every message. Top of the page is the message
// edited most recently; its history is a horizontal carousel (newest edit on the
// left, scroll right to walk back through older versions), then the next message.
export default function AuditsTab() {
  const api = useApi();
  const [groups, setGroups] = useState<AuditFullView[][]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .get<ListOfAudits>('/penpal/admins/audits/messages/all')
      .then((res) => setGroups(groupByMessage(res.auditFullViewList)))
      .catch((e) => setError(errText(e)))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (loading) return <p className="text-sm text-navy-500">Loading audit history…</p>;
  if (error) return <p className="rounded-lg bg-coral-50 p-3 text-sm text-coral-700">{error}</p>;
  if (groups.length === 0) return <p className="text-sm text-navy-500">No message edits recorded yet.</p>;

  return (
    <div className="space-y-8">
      {groups.map((history) => {
        const msg = history[0].currentMessageState;
        const chatLabel = msg.chat.members.map((p) => p.firstName).join(' & ');
        return (
          <section key={msg.id}>
            <div className="mb-2 flex items-baseline justify-between gap-3">
              <h2 className="text-sm font-bold uppercase tracking-wide text-navy-500">
                {chatLabel} · message #{msg.id}
              </h2>
              <span className="shrink-0 text-xs text-navy-400">
                {history.length} {history.length === 1 ? 'version' : 'versions'} · last edited {fmt(history[0].archiveTime)}
              </span>
            </div>

            {/* carousel: newest snapshot first (left); scroll right for older history */}
            <div className="flex snap-x gap-4 overflow-x-auto pb-2">
              {history.map((a, i) => (
                <article
                  key={a.auditId}
                  className="min-w-[18rem] max-w-[18rem] shrink-0 snap-start rounded-2xl border bg-white p-4 shadow-sm"
                >
                  <div className="mb-2 flex items-center justify-between">
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs font-semibold ${
                        i === 0 ? 'bg-teal-100 text-teal-700' : 'bg-navy-100 text-navy-500'
                      }`}
                    >
                      {i === 0 ? 'Latest edit' : `Older · v${history.length - i}`}
                    </span>
                    <span className="text-xs text-navy-400">{fmt(a.archiveTime)}</span>
                  </div>

                  <p className="mb-1 text-xs text-navy-500">
                    From {fullName(a.penpalAuthor)}
                    {a.penpalAuthor.state ? ` · ${a.penpalAuthor.state}` : ''}
                  </p>

                  <NaughtyBox
                    text={a.text}
                    className="mb-3 max-h-40 overflow-y-auto whitespace-pre-wrap rounded-lg bg-navy-50 p-3 text-sm"
                  />

                  <dl className="space-y-1 text-xs text-navy-500">
                    <div className="flex justify-between gap-2">
                      <dt className="text-navy-400">Edited by</dt>
                      <dd className="text-right">{a.editedBy ? fullName(a.editedBy) : '— (original)'}</dd>
                    </div>
                    <div className="flex justify-between gap-2">
                      <dt className="text-navy-400">Sent by</dt>
                      <dd className="text-right">{fullName(a.performedBy)}</dd>
                    </div>
                    <div className="flex justify-between gap-2">
                      <dt className="text-navy-400">Approval</dt>
                      <dd className="text-right">
                        {a.approved == null
                          ? 'Pending'
                          : a.approved
                            ? `Approved${a.approvedBy ? ` by ${fullName(a.approvedBy)}` : ''}`
                            : 'Rejected'}
                      </dd>
                    </div>
                  </dl>
                </article>
              ))}
            </div>
          </section>
        );
      })}
    </div>
  );
}
