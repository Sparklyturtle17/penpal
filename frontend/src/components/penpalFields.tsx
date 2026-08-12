// Shared option lists + selects for penpal place (State enum) and age, so the
// guardian form and the monitor modals always offer the same valid choices.

// value = backend State enum name, label = human display name. Malawi first
// (primary audience), then US — each alphabetical by label.
const MALAWI: [string, string][] = [
  ['BALAKA', 'Balaka'], ['BLANTYRE', 'Blantyre'], ['CHIKWAWA', 'Chikwawa'], ['CHIRADZULU', 'Chiradzulu'],
  ['CHITIPA', 'Chitipa'], ['DEDZA', 'Dedza'], ['DOWA', 'Dowa'], ['KARONGA', 'Karonga'],
  ['KASUNGU', 'Kasungu'], ['LIKOMA', 'Likoma'], ['LILONGWE', 'Lilongwe'], ['MACHINGA', 'Machinga'],
  ['MANGOCHI', 'Mangochi'], ['MCHINJI', 'Mchinji'], ['MULANJE', 'Mulanje'], ['MWANZA', 'Mwanza'],
  ['MZIMBA', 'Mzimba'], ['NENO', 'Neno'], ['NKHATA_BAY', 'Nkhata Bay'], ['NKHOTAKOTA', 'Nkhotakota'],
  ['NSANJE', 'Nsanje'], ['NTCHEU', 'Ntcheu'], ['NTCHISI', 'Ntchisi'], ['PHALOMBE', 'Phalombe'],
  ['RUMPHI', 'Rumphi'], ['SALIMA', 'Salima'], ['THYOLO', 'Thyolo'], ['ZOMBA', 'Zomba'],
];

const US: [string, string][] = [
  ['AL', 'Alabama'], ['AK', 'Alaska'], ['AZ', 'Arizona'], ['AR', 'Arkansas'], ['CA', 'California'],
  ['CO', 'Colorado'], ['CT', 'Connecticut'], ['DE', 'Delaware'], ['FL', 'Florida'], ['GA', 'Georgia'],
  ['HI', 'Hawaii'], ['ID', 'Idaho'], ['IL', 'Illinois'], ['IN', 'Indiana'], ['IA', 'Iowa'],
  ['KS', 'Kansas'], ['KY', 'Kentucky'], ['LA', 'Louisiana'], ['ME', 'Maine'], ['MD', 'Maryland'],
  ['MA', 'Massachusetts'], ['MI', 'Michigan'], ['MN', 'Minnesota'], ['MS', 'Mississippi'], ['MO', 'Missouri'],
  ['MT', 'Montana'], ['NE', 'Nebraska'], ['NV', 'Nevada'], ['NH', 'New Hampshire'], ['NJ', 'New Jersey'],
  ['NM', 'New Mexico'], ['NY', 'New York'], ['NC', 'North Carolina'], ['ND', 'North Dakota'], ['OH', 'Ohio'],
  ['OK', 'Oklahoma'], ['OR', 'Oregon'], ['PA', 'Pennsylvania'], ['RI', 'Rhode Island'], ['SC', 'South Carolina'],
  ['SD', 'South Dakota'], ['TN', 'Tennessee'], ['TX', 'Texas'], ['UT', 'Utah'], ['VT', 'Vermont'],
  ['VA', 'Virginia'], ['WA', 'Washington'], ['WV', 'West Virginia'], ['WI', 'Wisconsin'], ['WY', 'Wyoming'],
];

const AGES = Array.from({ length: 15 }, (_, i) => i + 4); // 4–18

export function PlaceSelect({
  value, onChange, className,
}: {
  value: string; onChange: (v: string) => void; className?: string;
}) {
  return (
    <select value={value} onChange={(e) => onChange(e.target.value)} className={className}>
      <option value="" disabled>Select a place…</option>
      <optgroup label="Malawi">
        {MALAWI.map(([v, l]) => <option key={v} value={v}>{l}</option>)}
      </optgroup>
      <optgroup label="United States">
        {US.map(([v, l]) => <option key={v} value={v}>{l}</option>)}
      </optgroup>
    </select>
  );
}

export function AgeSelect({
  value, onChange, className,
}: {
  value: string; onChange: (v: string) => void; className?: string;
}) {
  return (
    <select value={value} onChange={(e) => onChange(e.target.value)} className={className}>
      <option value="" disabled>Select age…</option>
      {AGES.map((a) => <option key={a} value={a}>{a}</option>)}
    </select>
  );
}
