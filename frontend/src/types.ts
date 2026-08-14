// TypeScript mirrors of the backend view DTOs. Keep in sync with the Java records.

export type Role = 'PENPAL' | 'PARENT_HELPER' | 'MONITOR' | 'ADMIN';

export interface UserFullView {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  whatsapp: string;
  role: Role;
}

export interface PenpalBioView {
  id: number;
  firstName: string;
  age: number;
  state: string;
  biography: string;
}

export interface PenpalAdminView {
  id: number;
  firstName: string;
  lastName: string;
  age: number;
  state: string;
  biography: string;
  parentHelper: UserFullView | null;
}

export interface GuardianMapRelationshipView {
  guardian: UserFullView;
  // penpal + companion are both full monitor views; companion carries its own parentHelper
  penpals: { penpal: PenpalMonitorView; companion: PenpalMonitorView | null }[];
}

// GET /monitors/relations — wrapper around the deduped, guardian-grouped map.
export interface MonitorMapRelationshipView {
  fullMap: GuardianMapRelationshipView[];
}

export interface PenpalMapRelationshipView {
  self: PenpalBioView;
  companion: PenpalBioView | null;
}

export interface MessageSimpleView {
  id: number;
  text: string;
  penpal: PenpalBioView | null; // null-ish for monitor broadcasts (synthesized sender)
  createTime: string; // ISO-8601 instant
  approved: boolean | null; // null = pending (penpal sees their own not-yet-approved)
}

// ---- monitor/admin views ----------------------------------------------------

export interface PenpalMonitorView {
  id: number;
  firstName: string;
  lastName: string;
  age: number | null;
  state: string | null;
  biography: string;
  parentHelper: UserFullView | null;
}

export interface ChatMonitorView {
  id: number;
  members: PenpalMonitorView[];
  active: boolean;
}

// GET /monitors/chats/all — each chat bundled with its messages.
export interface MonitorChatMessageView {
  chatInfo: ChatMonitorView;
  messages: MessageMonitorView[];
}

export interface MessageMonitorView {
  id: number;
  text: string;
  penpalAuthor: PenpalMonitorView; // synthesized "monitor" sender for broadcasts
  createTime: string;
  chat: ChatMonitorView;
  approved: boolean | null; // null = pending
  approvedTime: string | null;
}

// GET /monitors/relationships returns List<GuardianMapRelationshipView> — each
// guardian once, with their penpals + companions, ordered most->fewest (see above type).

// Penpal-facing chat summary (GET /penpals/chats).
export interface ChatSimpleView {
  id: number;
  members: PenpalBioView[];
  active: boolean;
}

// GET /penpals/chats/{id} — a chat bundled with its (penpal-filtered) messages.
export interface SimpleChatMessageView {
  chatInfo: ChatSimpleView;
  messages: MessageSimpleView[];
}

// ---- audit views (GET /admins/audits/*) -------------------------------------
// One historical snapshot of a message. GET /admins/audits/messages/all returns
// the list already grouped by message and ordered by most-recent edit (newest
// group first, newest snapshot first within a group) — the UI groups consecutive
// rows and does not sort client-side.
export interface AuditFullView {
  auditId: number;
  archiveTime: string;                       // when this snapshot was archived (the edit time)
  editedBy: UserFullView | null;             // who made the edit; null for the original create
  currentMessageState: MessageMonitorView;   // the message as it stands now (live)
  text: string;                              // snapshot text at this version
  penpalAuthor: PenpalMonitorView;           // synthesized "~ a monitor" for broadcasts
  performedBy: UserFullView;
  createTime: string;
  chat: ChatMonitorView | null;              // null if the chat was since removed
  approved: boolean | null;
  approvedBy: UserFullView | null;
  approvedTime: string | null;
}

export interface ListOfAudits {
  auditFullViewList: AuditFullView[];
}
