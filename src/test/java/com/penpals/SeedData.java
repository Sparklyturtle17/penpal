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

	// Nia guards no penpal (lone-node case). Quinn & Rosa each guard one of the chatless pair below.
	public static final AppUser NIA   = user(10, "Nia",   "Nolan", "nia@example.com",   "+14155550010", "+14155550010", RoleEnum.PARENT_HELPER, null);
	public static final AppUser QUINN = user(11, "Quinn", "Quill", "quinn@example.com", "+14155550011", "+14155550011", RoleEnum.PARENT_HELPER, "parent_helper_4");
	public static final AppUser ROSA  = user(12, "Rosa",  "Reed",  "rosa@example.com",  "+14155550012", "+14155550012", RoleEnum.PARENT_HELPER, null);

	public static final AppUser MONA  = user(7, "Mona",  "Moore",  "mona@example.com",  "+14155550007", "+14155550007", RoleEnum.MONITOR,       "monitor");

	public static final AppUser ADAM  = user(9, "Adam",  "Ash",    "adam@example.com",  "+14155550009", "+14155550009", RoleEnum.ADMIN,         "admin");

	// penpals (parentHelper points at the real guardian entity above)
	public static final Penpal ALICE  = penpal(1, "Alice",  "Anders", "alice@example.com",  "+14155550001", "+14155550001", "penpal", 11, State.CA,       "I like drawing. I am in fourth grade. I have a pet dog named Doggo. I love to watch soccer, but not play because I get tired easily. I have a brother Timon who farts a lot! I am reading a book about a boy in Malawi who built his own windmill, that is so cool! One day I want to be an engineer and make my own windmill too. My favorite color is blue, even though my brother says that blue is for boys, I told him \"you're dumb!\".", PAT);
	public static final Penpal BOB    = penpal(2, "Bob",    "Brown",  "bob@example.com",    "+14155550002", "+14155550002", null,     12, State.LILONGWE, "I live in Lilongwe with my mom, my dad, and my two little sisters. My family keeps chickens and one goat named Buttons. I love football more than anything and I want to be a football star one day. Every morning I collect the eggs before I walk to school. My favorite subject is science because I like to learn how things work. I am saving up to buy my very own football.", HELEN);
	public static final Penpal CARLOS = penpal(3, "Carlos", "Cruz",   "carlos@example.com", "+14155550003", "+14155550003", null,     10, State.TX,       "I am from Texas and I am the loudest kid in my whole class! I love music and I am learning to play the guitar from my abuela. My two cats are named Sol and Luna and they follow me everywhere. On Sundays my family eats tacos and sings songs together after church. When I grow up I want to be in a band and travel the world. I also really like soccer and drawing dinosaurs.", HUGO);
	public static final Penpal DIANA  = penpal(4, "Diana",  "Diaz",   "diana@example.com",  "+14155550004", "+14155550004", null,     13, State.BLANTYRE, "I read a lot, sometimes three whole books in one week! I live in Blantyre with my mother, who is a nurse at the big hospital. When I grow up I want to help people too, maybe as a doctor. I love to draw maps of faraway places that I dream of visiting. My grandmother has a garden where we grow tomatoes and sweet pumpkins. My favorite animal is the elephant because they never forget their friends.", PAT);

	// Chatless pair: different guardians (Quinn, Rosa), in no chat yet, ready to be linked.
	public static final Penpal OMAR   = penpal(13, "Omar",  "Osei",  "omar@example.com",  "+14155550013", "+14155550013", null, 12, State.TX,       "I collect stamps from every country I can find, and I have almost one hundred! I live in Texas and my favorite thing is learning about faraway places. I have a big map on my wall and I put a pin on every country in my collection. My dream is to be a pilot so I can visit all of them one day. I also love math and puzzles, the harder the better. My little brother tries to steal my stamps, but I hide them in a secret box.", QUINN);
	public static final Penpal PRIYA  = penpal(14, "Priya", "Patel", "priya@example.com", "+14155550014", "+14155550014", null, 11, State.LILONGWE, "I love to sing more than anything in the whole world! I live in Lilongwe and I sing in the choir at my school. My teacher says I have a big voice for such a small girl. When I grow up I want to be a famous singer and make people happy. I also like to dance, but I am still learning not to trip over my own feet! My favorite song is one my mother taught me when I was very little.", ROSA);

	// chats: each exactly two penpals
	public static final Chat CHAT_1 = chat(1, true, ALICE, BOB);    // Alice <-> Bob
	public static final Chat CHAT_2 = chat(2, true, CARLOS, DIANA); // Carlos <-> Diana

	// messages: penpalAuthor = authoring penpal, performedBy = the guardian who sent it,
	// approvedBy = the monitor (Mona). Pending (approved null): MSG_2, MSG_8, MSG_10.
	// MSG_6 and MSG_9 carry an inline monitor edit "(original)[correction]".
	// --- Chat 1 (Alice <-> Bob) ---
	public static final Message MSG_1 = message(1, "Hi Bob, it is so nice to meet you! My name is Alice and I am eleven years old. I live in California with my mom, my dad, and my little brother Timon. I love to draw pictures of animals, especially horses and dogs. My dog Doggo likes to sit next to me while I draw. What do you like to do for fun? I really hope that we can be good friends and write many letters to each other. Please tell me all about where you live!",
		ALICE, PAT, Instant.parse("2026-07-01T09:00:00Z"), CHAT_1, true, MONA, Instant.parse("2026-07-01T09:05:00Z"));
	public static final Message MSG_2 = message(2, "I'm just a test, I am only a test. Im sitting here in pending status. Its a long, long journey to approved status. Its a long long wait, while I'm hoping for approval.",
		BOB, HELEN, Instant.parse("2026-07-01T10:00:00Z"), CHAT_1, null, null, null);
	public static final Message MSG_6 = message(6, "Thank you for your letter Bob, it made me so happy to (recieve)[receive] it! School is going well, but math has been very hard for me this year. My teacher says I am getting a little better every single day. Do you like school? My favorite subject is art, and last week I painted a big picture of the ocean with a whale in it. I hung it on my wall right next to my bed. I hope you have a wonderful day!",
		ALICE, PAT, Instant.parse("2026-07-04T09:00:00Z"), CHAT_1, true, MONA, Instant.parse("2026-07-04T09:10:00Z"));
	public static final Message MSG_7 = message(7, "Hello Alice, thank you for writing back to me so quickly! I live in Lilongwe, which is a big and busy city in Malawi. My family keeps some chickens and one goat named Buttons who likes to eat everything. Every morning I help my father collect the eggs before I walk to school. In the evening when it is cooler, I play football with my friends until it gets dark. Someday I want to be a football star and play for a big team. What sports do you like to play?",
		BOB, HELEN, Instant.parse("2026-07-05T10:00:00Z"), CHAT_1, true, MONA, Instant.parse("2026-07-05T10:10:00Z"));
	public static final Message MSG_8 = message(8, "Dear Bob, I have been laughing about your goat Buttons all week, what a funny name for a goat! Yesterday it rained so hard that we could not go outside, so I stayed in and read three whole books. My favorite one is about a girl who can talk to animals. I really wish I could talk to animals too, especially my dog Doggo. Do you have a favorite book that you like to read? I am also learning to ride my bike without the training wheels. It is a little scary, but it is also very fun!",
		ALICE, PAT, Instant.parse("2026-07-06T11:00:00Z"), CHAT_1, null, null, null);
	// --- Chat 2 (Carlos <-> Diana) ---
	public static final Message MSG_3 = message(3, "Hola Diana, it is very nice to meet you! I am Carlos, I am ten years old, and I live in Texas. I love music more than almost anything, and my abuela is teaching me to play the guitar. Every Sunday after church we sing old songs together in her kitchen. Do you like music too? I also love tacos, soccer, and my two cats named Sol and Luna. Sol is lazy but Luna is very naughty and knocks things off the table. Please write back soon and tell me all about yourself!",
		CARLOS, HUGO, Instant.parse("2026-07-02T14:00:00Z"), CHAT_2, true, MONA, Instant.parse("2026-07-02T14:10:00Z"));
	public static final Message MSG_9 = message(9, "Hi Carlos, I am so glad that you wrote to me! I live in Blantyre and I love to read books more than (anyting)[anything] else in the world. Right now I am reading a story about a brave girl who sails all the way across the sea. My mother is a nurse and she works very hard at the big hospital in our city. When I grow up I want to help people too, so I dream of becoming a doctor. What do you want to be when you grow up? I also love to draw maps of all the places I hope to visit one day.",
		DIANA, PAT, Instant.parse("2026-07-04T15:00:00Z"), CHAT_2, true, MONA, Instant.parse("2026-07-04T15:10:00Z"));
	public static final Message MSG_10 = message(10, "Dear Diana, being a doctor is a wonderful dream, you must be very smart and kind! This week my guitar teacher showed me a brand new song and I practiced it until my fingers were sore. My cat Luna likes to curl up on my lap while I play, but Sol runs away from all the noise. We had a huge rainstorm here and the streets turned into little rivers. My brother and I made paper boats and raced them down the water until they fell apart. What is the weather like where you live right now?",
		CARLOS, HUGO, Instant.parse("2026-07-05T16:00:00Z"), CHAT_2, null, null, null);
	public static final Message MSG_11 = message(11, "Hello Carlos, your paper boat race sounds like the most fun thing ever! The weather here has been very hot and dry, so we drink lots and lots of water. My favorite thing to do after school is help my grandmother in her big garden. We grow tomatoes, beans, and sweet pumpkins that taste absolutely amazing. Sometimes the birds try to eat all of our seeds, so I built a little scarecrow to scare them away. I named him Mr. Sticks and he proudly wears my father's old straw hat! Do you have a garden where you live?",
		DIANA, PAT, Instant.parse("2026-07-06T17:00:00Z"), CHAT_2, true, MONA, Instant.parse("2026-07-06T17:10:00Z"));

	// Monitor "blast": one message per active chat. No penpal author; performedBy/approvedBy = the monitor (Mona).
	private static final String BLAST_TEXT = "Reminder from your monitors: always be kind and respectful in every letter you write. Please never share your home address, your phone number, or any passwords with anyone. If a message ever makes you feel worried or sad, tell a grown-up that you trust right away. We are so happy that you are part of our penpal family. Keep being wonderful!";
	public static final Message BLAST_1 = message(4, BLAST_TEXT,
		null, MONA, Instant.parse("2026-07-03T12:00:00Z"), CHAT_1, true, MONA, Instant.parse("2026-07-03T12:00:00Z"));
	public static final Message BLAST_2 = message(5, BLAST_TEXT,
		null, MONA, Instant.parse("2026-07-03T12:00:00Z"), CHAT_2, true, MONA, Instant.parse("2026-07-03T12:00:00Z"));

	// message_audit: pre-edit snapshots. editedBy = who edited; message rows copied from the audited message.
	public static final MessageAudit AUDIT_1 = audit(1, Instant.parse("2026-07-01T09:02:00Z"), ALICE,
		MSG_1, "Hi Bob",     ALICE, PAT,   Instant.parse("2026-07-01T09:00:00Z"), CHAT_1, null, null, null);
	public static final MessageAudit AUDIT_2 = audit(2, Instant.parse("2026-07-01T10:05:00Z"), HELEN,
		MSG_2, "Hey Alice",  BOB,   HELEN, Instant.parse("2026-07-01T10:00:00Z"), CHAT_1, null, null, null);
	public static final MessageAudit AUDIT_3 = audit(3, Instant.parse("2026-07-02T14:20:00Z"), MONA,
		MSG_3, "Hola Diana", CARLOS, HUGO, Instant.parse("2026-07-02T14:00:00Z"), CHAT_2, true, MONA, Instant.parse("2026-07-02T14:10:00Z"));
}