import { useEffect, useState } from 'react';
import { useApi, ApiError } from '../api/useApi';
import type { PenpalMonitorView, UserFullView } from '../types';
import PageShell from '../components/PageShell';
import Modal from '../components/Modal';
import PenpalMode from './PenpalMode';
import { PlaceSelect, AgeSelect } from '../components/penpalFields';

const errText = (e: unknown) => `${(e as ApiError).status}: ${(e as ApiError).message}`;

// Parent/helper landing — deliberately tiny and mobile-first (low data, low
// tech-literacy): just their penpals, with Open (penpal mode), Edit, and Add.
export default function GuardianHome({ me }: { me: UserFullView }) {
  const api = useApi();
  const [penpals, setPenpals] = useState<PenpalMonitorView[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [adding, setAdding] = useState(false);
  const [editing, setEditing] = useState<PenpalMonitorView | null>(null);
  const [actingAs, setActingAs] = useState<PenpalMonitorView | null>(null);

  const load = async () => {
    try {
      // just this guardian's own penpals — no companions (those show in penpal mode)
      const list = await api.get<PenpalMonitorView[]>('/penpal/parent-helpers/my-penpals');
      setPenpals(list);
    } catch (e) {
      setError(errText(e));
    }
  };

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // penpal mode is full-screen with its own (green) chrome — no PageShell
  if (actingAs) {
    return <PenpalMode penpal={actingAs} onExit={() => setActingAs(null)} />;
  }

  return (
    <PageShell title="My penpals" user={me}>
      <div className="mx-auto max-w-md">
        {error && <p className="mb-4 rounded-lg bg-coral-50 p-3 text-sm text-coral-700">{error}</p>}

        <button
          onClick={() => setAdding(true)}
          className="mb-4 flex w-full items-center justify-center gap-2 rounded-2xl bg-teal-600 py-4 text-base font-bold text-white active:bg-teal-700"
        >
          <svg viewBox="0 0 24 24" className="h-6 w-6" fill="none" stroke="currentColor" strokeWidth={2.5} strokeLinecap="round" aria-hidden>
            <path d="M12 5v14M5 12h14" />
          </svg>
          Add a penpal
        </button>

        {!penpals ? (
          <p className="text-center text-base text-navy-500">Loading…</p>
        ) : penpals.length === 0 ? (
          <p className="text-center text-base text-navy-500">No penpals yet. Tap “Add a penpal” to start.</p>
        ) : (
          <ul className="space-y-3">
            {penpals.map((p) => (
              <li key={p.id} className="rounded-2xl border bg-white p-4">
                <p className="text-lg font-bold">{p.firstName} {p.lastName}</p>
                <p className="mb-3 text-sm text-navy-500">
                  {p.age != null ? `${p.age}` : ''}{p.age != null && p.state ? ' · ' : ''}{p.state ?? ''}
                </p>
                <div className="flex gap-2">
                  <button
                    onClick={() => setActingAs(p)}
                    className="flex-1 rounded-xl bg-teal-600 py-3 text-base font-bold text-white active:bg-teal-700"
                  >
                    Open
                  </button>
                  <button
                    onClick={() => setEditing(p)}
                    className="flex-1 rounded-xl border-2 border-navy-200 py-3 text-base font-bold text-navy-700 active:bg-navy-50"
                  >
                    Edit
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>

      {adding && (
        <PenpalForm
          title="Add a penpal"
          onClose={() => setAdding(false)}
          onSubmit={async (body) => { await api.post('/penpal/parent-helpers/my-penpals', body); setAdding(false); void load(); }}
        />
      )}

      {editing && (
        <PenpalForm
          title="Edit penpal"
          initial={editing}
          onClose={() => setEditing(null)}
          onSubmit={async (body) => { await api.put(`/penpal/parent-helpers/my-penpals/${editing.id}`, body); setEditing(null); void load(); }}
        />
      )}
    </PageShell>
  );
}

type PenpalBody = {
  firstName: string;
  lastName: string;
  age: number;
  state: string;
  biography: string;
  parentHelperId: null;
  parentHelper: null;
};

function PenpalForm({
  title, initial, onClose, onSubmit,
}: {
  title: string;
  initial?: PenpalMonitorView;
  onClose: () => void;
  onSubmit: (body: PenpalBody) => Promise<void>;
}) {
  const [firstName, setFirst] = useState(initial?.firstName ?? '');
  const [lastName, setLast] = useState(initial?.lastName ?? '');
  const [age, setAge] = useState(initial?.age != null ? String(initial.age) : '');
  const [state, setState] = useState(initial?.state ?? '');
  const [biography, setBio] = useState(initial?.biography ?? '');
  const [error, setError] = useState<string | null>(null);

  async function submit() {
    try {
      await onSubmit({
        firstName, lastName, age: Number(age), state, biography,
        parentHelperId: null, parentHelper: null,
      });
    } catch (e) {
      setError(errText(e));
    }
  }

  return (
    <Modal title={title} onClose={onClose}>
      {error && <p className="mb-3 rounded bg-coral-50 p-2 text-sm text-coral-700">{error}</p>}
      <div className="space-y-3">
        <Field label="First name" value={firstName} onChange={setFirst} />
        <Field label="Last name" value={lastName} onChange={setLast} />
        <label className="block">
          <span className="text-sm text-navy-500">Age</span>
          <AgeSelect value={age} onChange={setAge} className="mt-1 w-full rounded-xl border bg-white px-3 py-2 text-base" />
        </label>
        <label className="block">
          <span className="text-sm text-navy-500">Place</span>
          <PlaceSelect value={state} onChange={setState} className="mt-1 w-full rounded-xl border bg-white px-3 py-2 text-base" />
        </label>
        <label className="block">
          <span className="text-sm text-navy-500">About</span>
          <textarea value={biography} onChange={(e) => setBio(e.target.value)} rows={3} className="mt-1 w-full rounded-xl border px-3 py-2 text-base" />
        </label>
        <button onClick={submit} className="w-full rounded-xl bg-teal-600 py-3 text-base font-bold text-white active:bg-teal-700">
          Save
        </button>
      </div>
    </Modal>
  );
}

function Field({
  label, value, onChange, inputMode,
}: {
  label: string; value: string; onChange: (v: string) => void; inputMode?: 'numeric';
}) {
  return (
    <label className="block">
      <span className="text-sm text-navy-500">{label}</span>
      <input
        value={value}
        inputMode={inputMode}
        onChange={(e) => onChange(e.target.value)}
        className="mt-1 w-full rounded-xl border px-3 py-2 text-base"
      />
    </label>
  );
}
