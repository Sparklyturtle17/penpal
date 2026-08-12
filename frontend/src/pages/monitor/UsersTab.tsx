import { useEffect, useMemo, useState } from 'react';
import { useApi, ApiError } from '../../api/useApi';
import type { ChatMonitorView, GuardianMapRelationshipView, MonitorChatMessageView, MonitorMapRelationshipView, PenpalMonitorView, UserFullView } from '../../types';
import Modal from '../../components/Modal';
import { PlaceSelect, AgeSelect } from '../../components/penpalFields';
import { NaughtyText, HighlightTextarea } from '../../naughty';

const errText = (e: unknown) => `${(e as ApiError).status}: ${(e as ApiError).message}`;
const initials = (a: string, b: string) => `${a[0] ?? ''}${b[0] ?? ''}`.toUpperCase();

type Selected =
  | { kind: 'penpal'; data: PenpalMonitorView }
  | { kind: 'guardian'; data: UserFullView }
  | { kind: 'staff'; data: UserFullView };

// Tab 2 — relationship map (grouped by guardian on the backend) + unconnected staff.
export default function UsersTab() {
  const api = useApi();
  const [groups, setGroups] = useState<GuardianMapRelationshipView[]>([]);
  const [staff, setStaff] = useState<UserFullView[]>([]);
  const [guardians, setGuardians] = useState<UserFullView[]>([]);
  const [chats, setChats] = useState<ChatMonitorView[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [selected, setSelected] = useState<Selected | null>(null);
  const [adding, setAdding] = useState<UserFullView | null>(null);
  const [addingGuardian, setAddingGuardian] = useState(false);
  // line/edge interactions
  const [deactivating, setDeactivating] = useState<{ id: number; a: string; b: string } | null>(null);
  const [reassigning, setReassigning] = useState<{ penpal: PenpalMonitorView; excludeIds: number[] } | null>(null);
  const [chatting, setChatting] = useState<PenpalMonitorView | null>(null);

  const load = async () => {
    try {
      const [map, all, chatList] = await Promise.all([
        api.get<MonitorMapRelationshipView>('/penpal/monitors/relations'),
        api.get<UserFullView[]>('/penpal/monitors/all-users'),
        api.get<MonitorChatMessageView[]>('/penpal/monitors/chats/all'),
      ]);
      setGroups(map.fullMap); // deduped + ordered most->fewest by the backend
      setStaff(all.filter((u) => u.role === 'MONITOR' || u.role === 'ADMIN'));
      setGuardians(all.filter((u) => u.role === 'PARENT_HELPER'));
      setChats(chatList.map((c) => c.chatInfo)); // we only need the chat, not its messages, here
    } catch (e) {
      setError(errText(e));
    }
  };

  // every penpal currently shown with no chat partner (companion === null)
  const chatless = useMemo(
    () => groups.flatMap((g) => g.penpals).filter((pc) => !pc.companion).map((pc) => pc.penpal),
    [groups],
  );

  // the active chat linking two penpals, if any (needed to deactivate it)
  const activeChatId = (a: number, b: number) =>
    chats.find((c) => c.active && c.members.some((m) => m.id === a) && c.members.some((m) => m.id === b))?.id;

  const startDeactivate = (a: PenpalMonitorView, b: PenpalMonitorView) => {
    const id = activeChatId(a.id, b.id);
    if (id != null) setDeactivating({ id, a: `${a.firstName} ${a.lastName}`, b: `${b.firstName} ${b.lastName}` });
  };

  // reassign this penpal — never to its own guardian, nor its chat-partner's guardian
  const startReassign = (penpal: PenpalMonitorView, partnerGuardianId: number | null | undefined) => {
    const excludeIds = [penpal.parentHelper?.id, partnerGuardianId].filter((x): x is number => x != null);
    setReassigning({ penpal, excludeIds });
  };

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (error) return <p className="rounded-lg bg-coral-50 p-3 text-sm text-coral-700">{error}</p>;

  return (
    <div className="space-y-8">
      <section>
        <h2 className="mb-3 text-sm font-bold uppercase tracking-wide text-navy-500">Relationships</h2>
        <div className="overflow-x-auto rounded-3xl border border-navy-100 bg-gradient-to-br from-navy-50/40 to-white p-4 pb-2">
          <div className="min-w-[680px] space-y-8">
            {groups.map((g) => (
              <GuardianGroup
                key={g.guardian.id}
                group={g}
                onSelect={setSelected}
                onAdd={setAdding}
                onDeactivate={startDeactivate}
                onReassign={startReassign}
                onCreateChat={setChatting}
              />
            ))}
            {/* new parent/helper starts life here as a lone node on the left */}
            <button
              onClick={() => setAddingGuardian(true)}
              className="group flex items-center gap-2"
            >
              <span className="grid h-9 w-9 place-items-center rounded-full border-2 border-dashed border-navy-300 text-navy-400 transition group-hover:border-teal-400 group-hover:text-teal-600">
                <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth={2.5} strokeLinecap="round" aria-hidden>
                  <path d="M12 5v14M5 12h14" />
                </svg>
              </span>
              <span className="text-xs font-semibold text-navy-400 group-hover:text-teal-600">add parent / helper</span>
            </button>
          </div>
        </div>
      </section>

      <section>
        <h2 className="mb-2 text-sm font-bold uppercase tracking-wide text-navy-500">Monitors &amp; Admins</h2>
        <ul className="divide-y rounded-2xl border bg-white">
          {staff.map((u) => (
            <li key={u.id}>
              <button
                onClick={() => setSelected({ kind: 'staff', data: u })}
                className="flex w-full items-center gap-3 px-4 py-2.5 text-left text-sm hover:bg-navy-50"
              >
                <Avatar text={initials(u.firstName, u.lastName)} tone="slate" />
                <span className="flex-1">{u.firstName} {u.lastName}</span>
                <span className="text-xs text-navy-500">{u.role}</span>
              </button>
            </li>
          ))}
        </ul>
      </section>

      {selected && (
        <UserDetail
          selected={selected}
          onClose={() => setSelected(null)}
          onSaved={() => { setSelected(null); void load(); }}
        />
      )}

      {adding && (
        <CreatePenpalModal
          guardian={adding}
          api={api}
          onClose={() => setAdding(null)}
          onDone={() => { setAdding(null); void load(); }}
        />
      )}

      {addingGuardian && (
        <CreateParentHelperModal
          api={api}
          onClose={() => setAddingGuardian(false)}
          onDone={() => { setAddingGuardian(false); void load(); }}
        />
      )}

      {deactivating && (
        <ConfirmDeactivateModal
          chat={deactivating}
          api={api}
          onClose={() => setDeactivating(null)}
          onDone={() => { setDeactivating(null); void load(); }}
        />
      )}

      {reassigning && (
        <ReassignModal
          penpal={reassigning.penpal}
          guardians={guardians.filter((g) => !reassigning.excludeIds.includes(g.id))}
          api={api}
          onClose={() => setReassigning(null)}
          onDone={() => { setReassigning(null); void load(); }}
        />
      )}

      {chatting && (
        <CreateChatModal
          penpal={chatting}
          // partners must be chatless too AND under a different guardian
          options={chatless.filter((p) => p.id !== chatting.id && p.parentHelper?.id !== chatting.parentHelper?.id)}
          api={api}
          onClose={() => setChatting(null)}
          onDone={() => { setChatting(null); void load(); }}
        />
      )}
    </div>
  );
}

// ---- the branching map ------------------------------------------------------

const ROW_H = 84;
const BRANCH_W = 22;

function GuardianGroup({
  group,
  onSelect,
  onAdd,
  onDeactivate,
  onReassign,
  onCreateChat,
}: {
  group: GuardianMapRelationshipView;
  onSelect: (s: Selected) => void;
  onAdd: (g: UserFullView) => void;
  onDeactivate: (a: PenpalMonitorView, b: PenpalMonitorView) => void;
  onReassign: (penpal: PenpalMonitorView, partnerGuardianId: number | null | undefined) => void;
  onCreateChat: (penpal: PenpalMonitorView) => void;
}) {
  const rows = group.penpals;
  const h = Math.max(rows.length, 1) * ROW_H;

  return (
    <div className="flex w-full items-stretch">
      {/* one guardian bubble, centered against its penpals; its own "+" sits on
          the right edge, right where the branches fan out */}
      <div className="flex items-center">
        <span className="relative inline-flex">
          <AddDot
            side="right"
            tooltip={`add penpal for ${group.guardian.firstName} ${group.guardian.lastName}`}
            onClick={() => onAdd(group.guardian)}
          />
          <Card
            role="Parent / Helper"
            name={`${group.guardian.firstName} ${group.guardian.lastName}`}
            tone="amber"
            onClick={() => onSelect({ kind: 'guardian', data: group.guardian })}
          />
        </span>
      </div>

      {/* curved branches to each penpal */}
      {rows.length > 0 && (
        <svg width={BRANCH_W} height={h} className="shrink-0 self-start" aria-hidden>
          {rows.map((_, i) => {
            const y = i * ROW_H + ROW_H / 2;
            return (
              <path
                key={i}
                d={`M0,${h / 2} C${BRANCH_W / 2},${h / 2} ${BRANCH_W / 2},${y} ${BRANCH_W},${y}`}
                fill="none"
                stroke="#cbd5e1"
                strokeWidth={2}
              />
            );
          })}
        </svg>
      )}

      <div className="flex flex-1 flex-col">
        {rows.map(({ penpal, companion }, i) => (
          <div key={i} style={{ height: ROW_H }} className="flex w-full items-center gap-1">
            {/* the branch from this penpal to its own guardian (on the left) == reassign */}
            <ConnectorLine
              grow={!!companion}
              tone="coral"
              label={`reassign ${penpal.firstName}`}
              onClick={() => onReassign(penpal, companion?.parentHelper?.id)}
            />
            {/* a chatless penpal gets a "+" on its right edge to start a new chat */}
            <span className="relative inline-flex">
              {!companion && (
                <AddDot
                  side="right"
                  tooltip={`new chat for ${penpal.firstName}`}
                  onClick={() => onCreateChat(penpal)}
                />
              )}
              <Card
                role="Penpal"
                name={`${penpal.firstName} ${penpal.lastName}`}
                sub={penpal.state}
                tone="teal"
                onClick={() => onSelect({ kind: 'penpal', data: penpal })}
              />
            </span>
            {companion && (
              <>
                {/* line between two penpals == their chat */}
                <ConnectorLine
                  tone="coral"
                  label="deactivate chat"
                  onClick={() => onDeactivate(penpal, companion)}
                />
                <Card
                  role="Penpal"
                  name={`${companion.firstName} ${companion.lastName}`}
                  sub={companion.state}
                  tone="teal"
                  onClick={() => onSelect({ kind: 'penpal', data: companion })}
                />
                {companion.parentHelper && (
                  <>
                    {/* line to the companion's guardian == reassign that penpal */}
                    <ConnectorLine
                      tone="coral"
                      label={`reassign ${companion.firstName}`}
                      onClick={() => onReassign(companion, penpal.parentHelper?.id)}
                    />
                    <span className="relative inline-flex">
                      <AddDot
                        tooltip={`add penpal for ${companion.parentHelper.firstName} ${companion.parentHelper.lastName}`}
                        onClick={() => onAdd(companion.parentHelper!)}
                      />
                      <Card
                        role="Parent / Helper"
                        name={`${companion.parentHelper.firstName} ${companion.parentHelper.lastName}`}
                        tone="amber"
                        onClick={() => onSelect({ kind: 'guardian', data: companion.parentHelper! })}
                      />
                    </span>
                  </>
                )}
              </>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

// compact "+" sitting on an edge of a card, right where its connector meets it
function AddDot({ onClick, side = 'left', tooltip }: { onClick: () => void; side?: 'left' | 'right'; tooltip: string }) {
  return (
    <button
      onClick={onClick}
      aria-label={tooltip}
      className={`group/dot absolute ${side === 'right' ? '-right-3' : '-left-3'} top-1/2 z-10 grid h-6 w-6 -translate-y-1/2 place-items-center rounded-full border-2 border-dashed border-navy-300 bg-white text-navy-400 transition hover:border-teal-400 hover:text-teal-600`}
    >
      <svg viewBox="0 0 24 24" className="h-3 w-3" fill="none" stroke="currentColor" strokeWidth={3} strokeLinecap="round" aria-hidden>
        <path d="M12 5v14M5 12h14" />
      </svg>
      <span className="pointer-events-none absolute bottom-full left-1/2 z-20 mb-1.5 -translate-x-1/2 whitespace-nowrap rounded-md bg-navy-900 px-2 py-1 text-[11px] font-semibold text-white opacity-0 shadow-sm transition group-hover/dot:opacity-100">
        {tooltip}
      </span>
    </button>
  );
}

// interactive connector between two cards: darkens + goes dashed on hover and
// surfaces its action label. tone rose == destructive (deactivate), teal == reassign.
function ConnectorLine({ label, tone, onClick, grow = true }: { label: string; tone: 'coral' | 'teal'; onClick: () => void; grow?: boolean }) {
  const hover =
    tone === 'coral'
      ? 'group-hover/line:border-coral-400 group-hover/line:border-dashed'
      : 'group-hover/line:border-teal-400 group-hover/line:border-dashed';
  const bubble = tone === 'coral' ? 'bg-coral-600' : 'bg-teal-600';
  return (
    <button onClick={onClick} aria-label={label} className={`group/line relative flex h-9 items-center justify-center ${grow ? 'min-w-[2.5rem] flex-1' : 'w-8 shrink-0'}`}>
      <span className={`w-full border-t-2 border-navy-300 transition-colors ${hover}`} />
      <span className={`pointer-events-none absolute left-1/2 top-1/2 z-20 -translate-x-1/2 -translate-y-1/2 whitespace-nowrap rounded-md px-2 py-1 text-[11px] font-semibold text-white opacity-0 shadow-sm transition group-hover/line:opacity-100 ${bubble}`}>
        {label}
      </span>
    </button>
  );
}

const TONES = {
  teal: { fill: 'text-teal-500', label: 'text-teal-600', hover: 'hover:border-teal-300 hover:ring-teal-100' },
  amber: { fill: 'text-coral-500', label: 'text-coral-600', hover: 'hover:border-coral-300 hover:ring-coral-100' },
  slate: { fill: 'text-navy-700', label: 'text-navy-700', hover: 'hover:border-navy-300 hover:ring-navy-100' },
} as const;

function Card({
  role, name, tone, onClick, muted, sub,
}: {
  role: string;
  name: string;
  tone: keyof typeof TONES;
  onClick?: () => void;
  muted?: boolean;
  sub?: string | null;
}) {
  const t = TONES[tone];
  const [a, b] = name.split(' ');
  const body = (
    <>
      <Avatar text={initials(a ?? '', b ?? '')} tone={tone} />
      <span className="text-left">
        <span className={`block text-[10px] font-bold uppercase tracking-wide ${t.label}`}>{role}</span>
        <span className="block text-sm font-semibold text-navy-800">{name}</span>
        {sub && <span className="block text-[11px] text-navy-400">{sub}</span>}
      </span>
    </>
  );

  const base = 'flex shrink-0 items-center gap-2.5 rounded-2xl border border-navy-200 bg-white px-3 py-2 shadow-sm';
  if (!onClick) {
    return <div className={`${base} ${muted ? 'opacity-70' : ''}`}>{body}</div>;
  }
  return (
    <button onClick={onClick} className={`${base} ring-4 ring-transparent transition hover:-translate-y-0.5 hover:shadow-md ${t.hover}`}>
      {body}
    </button>
  );
}

// symmetrical heart with the initials layered on top (like the logo circle)
function Avatar({ text, tone }: { text: string; tone: keyof typeof TONES }) {
  return (
    <span className="relative grid h-9 w-9 shrink-0 place-items-center">
      <svg viewBox="0 0 24 24" className={`absolute h-9 w-9 ${TONES[tone].fill}`} fill="currentColor" aria-hidden>
        <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
      </svg>
      <span className="relative -mt-0.5 text-xs font-black text-white">{text}</span>
    </span>
  );
}

// ---- detail + edit ----------------------------------------------------------

function UserDetail({
  selected, onClose, onSaved,
}: {
  selected: Selected;
  onClose: () => void;
  onSaved: () => void;
}) {
  const api = useApi();
  const [editing, setEditing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const title = `${selected.data.firstName} ${selected.data.lastName}`;
  // green penpals, orange parent/helpers, navy monitors & admins
  const accent = selected.kind === 'penpal' ? 'teal' : selected.kind === 'guardian' ? 'amber' : 'navy';

  return (
    <Modal title={title} onClose={onClose} accent={accent}>
      {error && <p className="mb-3 rounded bg-coral-50 p-2 text-sm text-coral-700">{error}</p>}

      {!editing && selected.kind === 'penpal' && <PenpalDetail p={selected.data} />}
      {!editing && selected.kind !== 'penpal' && <UserDetailFields u={selected.data} />}

      {editing && selected.kind === 'penpal' && (
        <PenpalEditForm p={selected.data} onError={setError} onDone={onSaved} onCancel={() => setEditing(false)} api={api} />
      )}
      {editing && selected.kind === 'guardian' && (
        <GuardianEditForm u={selected.data} onError={setError} onDone={onSaved} onCancel={() => setEditing(false)} api={api} />
      )}

      {!editing && (
        <div className="mt-4 flex justify-end">
          {selected.kind === 'staff' ? (
            <span className="text-xs text-navy-400">Editing monitors/admins isn’t available yet.</span>
          ) : (
            <button onClick={() => setEditing(true)} className="rounded-full bg-teal-600 px-4 py-1.5 text-sm font-semibold text-white hover:bg-teal-700">
              ✎ Edit
            </button>
          )}
        </div>
      )}
    </Modal>
  );
}

function PenpalDetail({ p }: { p: PenpalMonitorView }) {
  return (
    <dl className="space-y-1 text-sm">
      <Field label="Age" value={p.age != null ? String(p.age) : '—'} />
      <Field label="State" value={p.state ?? '—'} />
      <div className="flex justify-between gap-4">
        <dt className="text-navy-500">Biography</dt>
        <dd className="text-right"><NaughtyText text={p.biography} /></dd>
      </div>
      <Field label="Parent / Helper" value={p.parentHelper ? `${p.parentHelper.firstName} ${p.parentHelper.lastName}` : '—'} />
    </dl>
  );
}

function UserDetailFields({ u }: { u: UserFullView }) {
  return (
    <dl className="space-y-1 text-sm">
      <Field label="Role" value={u.role} />
      <Field label="Email" value={u.email} />
      <Field label="Phone" value={u.phone} />
      <Field label="WhatsApp" value={u.whatsapp} />
    </dl>
  );
}

function Field({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-4">
      <dt className="text-navy-500">{label}</dt>
      <dd className="text-right">{value}</dd>
    </div>
  );
}

type Api = ReturnType<typeof useApi>;

function PenpalEditForm({
  p, api, onError, onDone, onCancel,
}: {
  p: PenpalMonitorView; api: Api; onError: (m: string) => void; onDone: () => void; onCancel: () => void;
}) {
  const [firstName, setFirst] = useState(p.firstName);
  const [lastName, setLast] = useState(p.lastName);
  const [age, setAge] = useState(p.age != null ? String(p.age) : '');
  const [state, setState] = useState(p.state ?? '');
  const [biography, setBio] = useState(p.biography);

  async function save() {
    try {
      // keep the penpal's existing guardian — a monitor edit here isn't a reassign
      await api.put(`/penpal/monitors/penpals/${p.id}`, {
        firstName, lastName, age: Number(age), state, biography,
        parentHelperId: p.parentHelper?.id ?? null, parentHelper: null,
      });
      onDone();
    } catch (e) { onError(errText(e)); }
  }

  return (
    <div className="space-y-2">
      <Input label="First name" value={firstName} onChange={setFirst} />
      <Input label="Last name" value={lastName} onChange={setLast} />
      <label className="block text-sm">
        <span className="text-navy-500">Age</span>
        <AgeSelect value={age} onChange={setAge} className="mt-1 w-full rounded-lg border bg-white px-3 py-2 text-sm" />
      </label>
      <label className="block text-sm">
        <span className="text-navy-500">Place</span>
        <PlaceSelect value={state} onChange={setState} className="mt-1 w-full rounded-lg border bg-white px-3 py-2 text-sm" />
      </label>
      <label className="block text-sm">
        <span className="text-navy-500">Biography</span>
        <div className="mt-1"><HighlightTextarea value={biography} onChange={setBio} rows={3} className="rounded-lg border px-3 py-2 text-sm" /></div>
      </label>
      <FormActions onCancel={onCancel} onSave={save} />
    </div>
  );
}

function GuardianEditForm({
  u, api, onError, onDone, onCancel,
}: {
  u: UserFullView; api: Api; onError: (m: string) => void; onDone: () => void; onCancel: () => void;
}) {
  const [firstName, setFirst] = useState(u.firstName);
  const [lastName, setLast] = useState(u.lastName);
  const [email, setEmail] = useState(u.email);
  const [phone, setPhone] = useState(u.phone);
  const [whatsapp, setWhatsapp] = useState(u.whatsapp);

  async function save() {
    try {
      await api.put(`/penpal/monitors/parent-helpers/${u.id}`, { firstName, lastName, email, phone, whatsapp });
      onDone();
    } catch (e) { onError(errText(e)); }
  }

  return (
    <div className="space-y-2">
      <Input label="First name" value={firstName} onChange={setFirst} />
      <Input label="Last name" value={lastName} onChange={setLast} />
      <Input label="Email" value={email} onChange={setEmail} />
      <Input label="Phone" value={phone} onChange={setPhone} />
      <Input label="WhatsApp (+countrycode)" value={whatsapp} onChange={setWhatsapp} />
      <FormActions onCancel={onCancel} onSave={save} />
    </div>
  );
}

function Input({ label, value, onChange }: { label: string; value: string; onChange: (v: string) => void }) {
  return (
    <label className="block text-sm">
      <span className="text-navy-500">{label}</span>
      <input value={value} onChange={(e) => onChange(e.target.value)} className="mt-1 w-full rounded-lg border px-3 py-2 text-sm" />
    </label>
  );
}

function FormActions({ onCancel, onSave }: { onCancel: () => void; onSave: () => void }) {
  return (
    <div className="mt-3 flex justify-end gap-2">
      <button onClick={onCancel} className="rounded-full border px-4 py-1.5 text-sm">Cancel</button>
      <button onClick={onSave} className="rounded-full bg-teal-600 px-4 py-1.5 text-sm font-semibold text-white">Save</button>
    </div>
  );
}

function CreatePenpalModal({
  guardian, api, onClose, onDone,
}: {
  guardian: UserFullView; api: Api; onClose: () => void; onDone: () => void;
}) {
  const [firstName, setFirst] = useState('');
  const [lastName, setLast] = useState('');
  const [age, setAge] = useState('');
  const [state, setState] = useState('');
  const [biography, setBio] = useState('');
  const [error, setError] = useState<string | null>(null);

  async function save() {
    try {
      // POST /monitors/penpals — parentHelperId ties the new penpal to this guardian
      await api.post('/penpal/monitors/penpals', {
        firstName, lastName, age: Number(age), state, biography,
        parentHelperId: guardian.id, parentHelper: null,
      });
      onDone();
    } catch (e) {
      setError(errText(e));
    }
  }

  return (
    <Modal title={`New penpal for ${guardian.firstName} ${guardian.lastName}`} onClose={onClose}>
      {error && <p className="mb-3 rounded bg-coral-50 p-2 text-sm text-coral-700">{error}</p>}
      <div className="space-y-2">
        <Input label="First name" value={firstName} onChange={setFirst} />
        <Input label="Last name" value={lastName} onChange={setLast} />
        <label className="block text-sm">
          <span className="text-navy-500">Age</span>
          <AgeSelect value={age} onChange={setAge} className="mt-1 w-full rounded-lg border bg-white px-3 py-2 text-sm" />
        </label>
        <label className="block text-sm">
          <span className="text-navy-500">Place</span>
          <PlaceSelect value={state} onChange={setState} className="mt-1 w-full rounded-lg border bg-white px-3 py-2 text-sm" />
        </label>
        <label className="block text-sm">
          <span className="text-navy-500">Biography</span>
          <div className="mt-1"><HighlightTextarea value={biography} onChange={setBio} rows={3} className="rounded-lg border px-3 py-2 text-sm" /></div>
        </label>
        <FormActions onCancel={onClose} onSave={save} />
      </div>
    </Modal>
  );
}

function CreateParentHelperModal({
  api, onClose, onDone,
}: {
  api: Api; onClose: () => void; onDone: () => void;
}) {
  const [firstName, setFirst] = useState('');
  const [lastName, setLast] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [whatsapp, setWhatsapp] = useState('');
  const [error, setError] = useState<string | null>(null);

  async function save() {
    try {
      await api.post('/penpal/monitors/parent-helpers', { firstName, lastName, email, phone, whatsapp });
      onDone();
    } catch (e) {
      setError(errText(e));
    }
  }

  return (
    <Modal title="New parent / helper" onClose={onClose}>
      {error && <p className="mb-3 rounded bg-coral-50 p-2 text-sm text-coral-700">{error}</p>}
      <div className="space-y-2">
        <Input label="First name" value={firstName} onChange={setFirst} />
        <Input label="Last name" value={lastName} onChange={setLast} />
        <Input label="Email" value={email} onChange={setEmail} />
        <Input label="Phone" value={phone} onChange={setPhone} />
        <Input label="WhatsApp (+countrycode)" value={whatsapp} onChange={setWhatsapp} />
        <p className="text-xs text-navy-400">Provide at least an email or a WhatsApp number.</p>
        <FormActions onCancel={onClose} onSave={save} />
      </div>
    </Modal>
  );
}

function ConfirmDeactivateModal({
  chat, api, onClose, onDone,
}: {
  chat: { id: number; a: string; b: string }; api: Api; onClose: () => void; onDone: () => void;
}) {
  const [error, setError] = useState<string | null>(null);

  async function deactivate() {
    try {
      // `active` is a query param on the endpoint, so it rides in the URL
      await api.patch(`/penpal/monitors/chats/${chat.id}/activation?active=false`, {});
      onDone();
    } catch (e) {
      setError(errText(e));
    }
  }

  return (
    <Modal title="Deactivate chat?" onClose={onClose}>
      {error && <p className="mb-3 rounded bg-coral-50 p-2 text-sm text-coral-700">{error}</p>}
      <p className="text-sm text-navy-600">
        This will deactivate the chat between <span className="font-semibold">{chat.a}</span> and{' '}
        <span className="font-semibold">{chat.b}</span>. They will no longer be able to exchange messages.
      </p>
      <div className="mt-4 flex justify-end gap-2">
        <button onClick={onClose} className="rounded-full border px-4 py-1.5 text-sm">Cancel</button>
        <button onClick={deactivate} className="rounded-full bg-coral-600 px-4 py-1.5 text-sm font-semibold text-white hover:bg-coral-700">
          Deactivate
        </button>
      </div>
    </Modal>
  );
}

function ReassignModal({
  penpal, guardians, api, onClose, onDone,
}: {
  penpal: PenpalMonitorView; guardians: UserFullView[]; api: Api; onClose: () => void; onDone: () => void;
}) {
  const [parentHelperId, setId] = useState('');
  const [error, setError] = useState<string | null>(null);

  async function save() {
    try {
      // reassign resends the penpal's own fields (all @NotBlank/@NotNull) + the new guardian
      await api.put(`/penpal/monitors/reassign-penpal/${penpal.id}`, {
        firstName: penpal.firstName,
        lastName: penpal.lastName,
        age: penpal.age,
        state: penpal.state,
        biography: penpal.biography,
        parentHelperId: Number(parentHelperId),
        parentHelper: null,
      });
      onDone();
    } catch (e) {
      setError(errText(e));
    }
  }

  return (
    <Modal title={`Reassign ${penpal.firstName} ${penpal.lastName}`} onClose={onClose}>
      {error && <p className="mb-3 rounded bg-coral-50 p-2 text-sm text-coral-700">{error}</p>}
      <label className="block text-sm">
        <span className="text-navy-500">New parent / helper</span>
        <select
          value={parentHelperId}
          onChange={(e) => setId(e.target.value)}
          className="mt-1 w-full rounded-lg border bg-white px-3 py-2 text-sm"
        >
          <option value="" disabled>Select a parent / helper…</option>
          {guardians.map((g) => (
            <option key={g.id} value={g.id}>{g.firstName} {g.lastName}</option>
          ))}
        </select>
      </label>
      {guardians.length === 0 && (
        <p className="mt-2 text-xs text-navy-400">No eligible parent / helper — every other guardian is already tied to this chat.</p>
      )}
      <div className="mt-4 flex justify-end gap-2">
        <button onClick={onClose} className="rounded-full border px-4 py-1.5 text-sm">Cancel</button>
        <button
          onClick={save}
          disabled={!parentHelperId}
          className="rounded-full bg-teal-600 px-4 py-1.5 text-sm font-semibold text-white hover:bg-teal-700 disabled:opacity-40"
        >
          Reassign
        </button>
      </div>
    </Modal>
  );
}

function CreateChatModal({
  penpal, options, api, onClose, onDone,
}: {
  penpal: PenpalMonitorView; options: PenpalMonitorView[]; api: Api; onClose: () => void; onDone: () => void;
}) {
  const [partnerId, setPartnerId] = useState('');
  const [error, setError] = useState<string | null>(null);

  async function save() {
    try {
      await api.post('/penpal/monitors/chats', {
        memberIds: [penpal.id, Number(partnerId)],
        active: true,
      });
      onDone();
    } catch (e) {
      setError(errText(e));
    }
  }

  return (
    <Modal title={`New chat for ${penpal.firstName} ${penpal.lastName}`} onClose={onClose}>
      {error && <p className="mb-3 rounded bg-coral-50 p-2 text-sm text-coral-700">{error}</p>}
      <label className="block text-sm">
        <span className="text-navy-500">Pair with</span>
        <select
          value={partnerId}
          onChange={(e) => setPartnerId(e.target.value)}
          className="mt-1 w-full rounded-lg border bg-white px-3 py-2 text-sm"
        >
          <option value="" disabled>Select a penpal…</option>
          {options.map((p) => (
            <option key={p.id} value={p.id}>{p.firstName} {p.lastName}{p.state ? ` · ${p.state}` : ''}</option>
          ))}
        </select>
      </label>
      <p className="mt-2 text-xs text-navy-400">Only penpals without an active chat and under a different guardian are listed.</p>
      <div className="mt-4 flex justify-end gap-2">
        <button onClick={onClose} className="rounded-full border px-4 py-1.5 text-sm">Cancel</button>
        <button
          onClick={save}
          disabled={!partnerId}
          className="rounded-full bg-teal-600 px-4 py-1.5 text-sm font-semibold text-white hover:bg-teal-700 disabled:opacity-40"
        >
          Create chat
        </button>
      </div>
    </Modal>
  );
}
