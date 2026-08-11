package com.penpals;

import com.penpals.chat.Chat;
import com.penpals.chat.message.Message;
import com.penpals.chat.message.audit.MessageAudit;
import com.penpals.common.State;
import com.penpals.users.AppUser;
import com.penpals.users.RoleEnum;
import com.penpals.users.dto.CreatePenpalRequest;
import com.penpals.users.dto.PenpalViews.*;
import com.penpals.users.penpal.Penpal;

import java.time.Instant;
import java.util.List;

/** Mirrors data.sql (dev profile) using the real entities, so test and app never diverge. */
public final class SeedData {

	private SeedData() {
	}

	private static AppUser user(long id, String first, String last, String email,
	                            String phone, String whatsapp, RoleEnum role, String authId) {
		AppUser u = new AppUser();
		u.setId(id);
		u.setFirstName(first);
		u.setLastName(last);
		u.setEmail(email);
		u.setPhone(phone);
		u.setWhatsapp(whatsapp);
		u.setRole(role);
		u.setAuthId(authId);
		return u;
	}

	private static Penpal penpal(long id, String first, String last, String email,
	                             String phone, String whatsapp, String authId,
	                             int age, State state, String bio, AppUser guardian) {
		Penpal p = new Penpal();
		p.setId(id);
		p.setFirstName(first);
		p.setLastName(last);
		p.setEmail(email);
		p.setPhone(phone);
		p.setWhatsapp(whatsapp);
		p.setRole(RoleEnum.PENPAL);
		p.setAuthId(authId);
		p.setAge(age);
		p.setState(state);
		p.setBiography(bio);
		p.setParentHelper(guardian);
		return p;
	}

	public static CreatePenpalRequest createRequestFrom(Penpal p) {
		return new CreatePenpalRequest(
			p.getFirstName(),
			p.getLastName(),
			p.getAge(),
			p.getState(),
			p.getBiography(),
			p.getParentHelper() == null ? null : p.getParentHelper().getId(),
			null);
	}

	private static Chat chat(long id, boolean active, Penpal... members) {
		Chat c = new Chat();
		c.setId(id);
		c.setActive(active);
		c.setMembers(List.of(members));
		return c;
	}

	private static Message message(long id, String text, Penpal author, AppUser performedBy,
	                               Instant createTime, Chat chat, Boolean approved,
	                               AppUser approvedBy, Instant approvedTime) {
		Message m = new Message();
		m.setId(id);
		m.setText(text);
		m.setPenpalAuthor(author);
		m.setPerformedBy(performedBy);
		m.setCreateTime(createTime);
		m.setChat(chat);
		m.setApproved(approved);
		m.setApprovedBy(approvedBy);
		m.setApprovedTime(approvedTime);
		return m;
	}

	private static MessageAudit audit(long auditId, Instant archiveTime, AppUser editedBy,
	                                  Message message, String text, Penpal author, AppUser performedBy,
	                                  Instant createTime, Chat chat, Boolean approved,
	                                  AppUser approvedBy, Instant approvedTime) {
		MessageAudit a = new MessageAudit();
		a.setAuditId(auditId);
		a.setArchiveTime(archiveTime);
		a.setEditedBy(editedBy);
		a.setMessage(message);
		a.setText(text);
		a.setPenpalAuthor(author);
		a.setPerformedBy(performedBy);
		a.setCreateTime(createTime);
		a.setChat(chat);
		a.setApproved(approved);
		a.setApprovedBy(approvedBy);
		a.setApprovedTime(approvedTime);
		return a;
	}

	// guardians / monitor / admin
	public static final AppUser HELEN = user(5, "Helen", "Hale",   "helen@example.com", "+14155550005", "+14155550005", RoleEnum.PARENT_HELPER, "parent_helper");
	public static final AppUser HUGO  = user(6, "Hugo",  "Hunt",   "hugo@example.com",  "+14155550006", "+14155550006", RoleEnum.PARENT_HELPER, "parent_helper_2");
	public static final AppUser PAT   = user(8, "Pat",   "Parker", "pat@example.com",   "+14155550008", "+14155550008", RoleEnum.PARENT_HELPER, "parent_helper_3");

	public static final AppUser MONA  = user(7, "Mona",  "Moore",  "mona@example.com",  "+14155550007", "+14155550007", RoleEnum.MONITOR,       "monitor");

	public static final AppUser ADAM  = user(9, "Adam",  "Ash",    "adam@example.com",  "+14155550009", "+14155550009", RoleEnum.ADMIN,         "admin");

	// penpals (parentHelper points at the real guardian entity above)
	public static final Penpal ALICE  = penpal(1, "Alice",  "Anders", "alice@example.com",  "+14155550001", "+14155550001", "penpal", 11, State.CA,       "I like drawing.",  PAT);
	public static final Penpal BOB    = penpal(2, "Bob",    "Brown",  "bob@example.com",    "+14155550002", "+14155550002", null,     12, State.LILONGWE, "I love football.", HELEN);
	public static final Penpal CARLOS = penpal(3, "Carlos", "Cruz",   "carlos@example.com", "+14155550003", "+14155550003", null,     10, State.TX,       "I play guitar.",   HUGO);
	public static final Penpal DIANA  = penpal(4, "Diana",  "Diaz",   "diana@example.com",  "+14155550004", "+14155550004", null,     13, State.BLANTYRE, "I read a lot.",    HUGO);

	// chats: each exactly two penpals
	public static final Chat CHAT_1 = chat(1, true, ALICE, BOB);    // Alice <-> Bob
	public static final Chat CHAT_2 = chat(2, true, CARLOS, DIANA); // Carlos <-> Diana

	// messages: penpalAuthor = authoring penpal, performedBy = the guardian who sent it,
	// approvedBy = the monitor (Mona). MSG_2 is still pending (approved = null).
	public static final Message MSG_1 = message(1, "Hi Bob, nice to meet you!",
		ALICE, PAT, Instant.parse("2026-07-01T09:00:00Z"), CHAT_1, true, MONA, Instant.parse("2026-07-01T09:05:00Z"));
	public static final Message MSG_2 = message(2, "Hey Alice! Where are you from?",
		BOB, HELEN, Instant.parse("2026-07-01T10:00:00Z"), CHAT_1, null, null, null);
	public static final Message MSG_3 = message(3, "Hola Diana, do you like music?",
		CARLOS, HUGO, Instant.parse("2026-07-02T14:00:00Z"), CHAT_2, true, MONA, Instant.parse("2026-07-02T14:10:00Z"));

	// Monitor "blast": one message per active chat. No penpal author; performedBy/approvedBy = the monitor (Mona).
	public static final Message BLAST_1 = message(4, "Reminder: always be kind online!",
		null, MONA, Instant.parse("2026-07-03T12:00:00Z"), CHAT_1, true, MONA, Instant.parse("2026-07-03T12:00:00Z"));
	public static final Message BLAST_2 = message(5, "Reminder: always be kind online!",
		null, MONA, Instant.parse("2026-07-03T12:00:00Z"), CHAT_2, true, MONA, Instant.parse("2026-07-03T12:00:00Z"));

	// message_audit: pre-edit snapshots. editedBy = who edited; message rows copied from the audited message.
	public static final MessageAudit AUDIT_1 = audit(1, Instant.parse("2026-07-01T09:02:00Z"), ALICE,
		MSG_1, "Hi Bob",     ALICE, PAT,   Instant.parse("2026-07-01T09:00:00Z"), CHAT_1, null, null, null);
	public static final MessageAudit AUDIT_2 = audit(2, Instant.parse("2026-07-01T10:05:00Z"), HELEN,
		MSG_2, "Hey Alice",  BOB,   HELEN, Instant.parse("2026-07-01T10:00:00Z"), CHAT_1, null, null, null);
	public static final MessageAudit AUDIT_3 = audit(3, Instant.parse("2026-07-02T14:20:00Z"), MONA,
		MSG_3, "Hola Diana", CARLOS, HUGO, Instant.parse("2026-07-02T14:00:00Z"), CHAT_2, true, MONA, Instant.parse("2026-07-02T14:10:00Z"));
}