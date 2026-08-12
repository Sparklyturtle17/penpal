import type { ReactNode } from 'react';

const ACCENTS = {
  teal: { ring: 'shadow-teal-500/25', title: 'text-teal-700' },
  amber: { ring: 'shadow-coral-500/25', title: 'text-coral-700' },
  navy: { ring: 'shadow-navy-700/25', title: 'text-navy-800' },
} as const;

export default function Modal({
  title,
  onClose,
  children,
  accent,
  wide,
}: {
  title: string;
  onClose: () => void;
  children: ReactNode;
  accent?: keyof typeof ACCENTS;
  wide?: boolean;
}) {
  const a = accent ? ACCENTS[accent] : null;
  return (
    <div
      className="fixed inset-0 z-50 grid place-items-center bg-black/30 p-4"
      onClick={onClose}
    >
      <div
        className={`w-full ${wide ? 'max-w-3xl' : 'max-w-lg'} rounded-2xl border bg-white p-5 shadow-xl ${a ? a.ring : ''}`}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="mb-3 flex items-center justify-between">
          <h2 className={`text-lg font-bold ${a ? a.title : ''}`}>{title}</h2>
          <button onClick={onClose} className="rounded-full px-2 text-navy-400 hover:bg-navy-100">
            ✕
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}
