import { createContext, useContext, useEffect, useRef, useState, type ReactNode } from 'react';
import { useApi } from './api/useApi';

// The word list lives on the backend and is served only to monitors/admins, so it
// never reaches a penpal's or guardian's browser. Loaded once into this context.
const NaughtyContext = createContext<Set<string>>(new Set());

export function NaughtyProvider({ children }: { children: ReactNode }) {
  const api = useApi();
  const [words, setWords] = useState<Set<string>>(new Set());

  useEffect(() => {
    api.get<string[]>('/penpal/monitors/naughty-words')
      .then((list) => setWords(new Set(list.map((w) => w.trim().toLowerCase()).filter(Boolean))))
      .catch(() => { /* if it fails, highlighting simply stays off */ });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return <NaughtyContext.Provider value={words}>{children}</NaughtyContext.Provider>;
}

export function useNaughty() {
  return useContext(NaughtyContext);
}

// Renders text with any naughty word colored orange (monitor-facing views only).
// Whole-word match only — a naughty word sitting inside a legitimate word is ignored.
export function NaughtyText({ text }: { text: string }) {
  const words = useNaughty();
  if (words.size === 0) return <>{text}</>;

  const parts = text.split(/([A-Za-z']+)/);
  return (
    <>
      {parts.map((p, i) =>
        words.has(p.toLowerCase())
          ? <span key={i} className="font-bold text-coral-700">{p}</span>
          : <span key={i}>{p}</span>,
      )}
    </>
  );
}

// An editable textarea that still shows naughty words in orange, via a highlighted
// backdrop behind a transparent-text textarea. `className` must set the box (padding,
// border, font-size) — it's applied to both layers so they line up exactly.
export function HighlightTextarea({
  value, onChange, rows, className = '', placeholder,
}: {
  value: string; onChange: (v: string) => void; rows?: number; className?: string; placeholder?: string;
}) {
  const taRef = useRef<HTMLTextAreaElement>(null);
  const backRef = useRef<HTMLDivElement>(null);

  const syncScroll = () => {
    if (backRef.current && taRef.current) backRef.current.scrollTop = taRef.current.scrollTop;
  };
  useEffect(syncScroll, [value]);

  const box = `${className} w-full whitespace-pre-wrap break-words`;
  return (
    <div className="relative">
      <div ref={backRef} aria-hidden className={`${box} pointer-events-none absolute inset-0 overflow-hidden bg-white text-navy-800`}>
        <NaughtyText text={value} />{'​'}
      </div>
      <textarea
        ref={taRef}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        onScroll={syncScroll}
        rows={rows}
        placeholder={placeholder}
        className={`${box} relative resize-none bg-transparent text-transparent caret-navy-800 placeholder:text-navy-400`}
      />
    </div>
  );
}
