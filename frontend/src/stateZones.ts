// Mirrors the backend State enum's zones (com.penpals.common.State) so a message's
// time can be shown in its author's local time. All Malawi districts are CAT (Blantyre);
// US states use their predominant zone.
const BLANTYRE = 'Africa/Blantyre';

export const STATE_ZONE: Record<string, string> = {
  // Malawi districts
  CHITIPA: BLANTYRE, KARONGA: BLANTYRE, RUMPHI: BLANTYRE, NKHATA_BAY: BLANTYRE,
  MZIMBA: BLANTYRE, LIKOMA: BLANTYRE, KASUNGU: BLANTYRE, NKHOTAKOTA: BLANTYRE,
  NTCHISI: BLANTYRE, DOWA: BLANTYRE, SALIMA: BLANTYRE, LILONGWE: BLANTYRE,
  MCHINJI: BLANTYRE, DEDZA: BLANTYRE, NTCHEU: BLANTYRE, MANGOCHI: BLANTYRE,
  MACHINGA: BLANTYRE, ZOMBA: BLANTYRE, CHIRADZULU: BLANTYRE, BLANTYRE, MWANZA: BLANTYRE,
  THYOLO: BLANTYRE, MULANJE: BLANTYRE, PHALOMBE: BLANTYRE, CHIKWAWA: BLANTYRE,
  NSANJE: BLANTYRE, BALAKA: BLANTYRE, NENO: BLANTYRE,

  // US states (predominant zone)
  AL: 'America/Chicago', AK: 'America/Anchorage', AZ: 'America/Phoenix', AR: 'America/Chicago',
  CA: 'America/Los_Angeles', CO: 'America/Denver', CT: 'America/New_York', DE: 'America/New_York',
  FL: 'America/New_York', GA: 'America/New_York', HI: 'Pacific/Honolulu', ID: 'America/Boise',
  IL: 'America/Chicago', IN: 'America/Indiana/Indianapolis', IA: 'America/Chicago', KS: 'America/Chicago',
  KY: 'America/New_York', LA: 'America/Chicago', ME: 'America/New_York', MD: 'America/New_York',
  MA: 'America/New_York', MI: 'America/Detroit', MN: 'America/Chicago', MS: 'America/Chicago',
  MO: 'America/Chicago', MT: 'America/Denver', NE: 'America/Chicago', NV: 'America/Los_Angeles',
  NH: 'America/New_York', NJ: 'America/New_York', NM: 'America/Denver', NY: 'America/New_York',
  NC: 'America/New_York', ND: 'America/Chicago', OH: 'America/New_York', OK: 'America/Chicago',
  OR: 'America/Los_Angeles', PA: 'America/New_York', RI: 'America/New_York', SC: 'America/New_York',
  SD: 'America/Chicago', TN: 'America/Chicago', TX: 'America/Chicago', UT: 'America/Denver',
  VT: 'America/New_York', VA: 'America/New_York', WA: 'America/Los_Angeles', WV: 'America/New_York',
  WI: 'America/Chicago', WY: 'America/Denver',
};
