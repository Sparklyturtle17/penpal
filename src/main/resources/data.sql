-- Users: 4 penpals, 3 parent-helpers (guardians), 1 monitor, 1 admin.
-- auth_id matches the dev in-memory usernames so /me resolves in dev.
INSERT INTO app_user (id, first_name, last_name, email, phone, whatsapp, role, auth_id) VALUES
(1, 'Alice',  'Anders', 'alice@example.com',  '111', '111', 'PENPAL',        'penpal'),
(2, 'Bob',    'Brown',  'bob@example.com',    '222', '222', 'PENPAL',        NULL),
(3, 'Carlos', 'Cruz',   'carlos@example.com', '333', '333', 'PENPAL',        NULL),
(4, 'Diana',  'Diaz',   'diana@example.com',  '444', '444', 'PENPAL',        NULL),
(5, 'Helen',  'Hale',   'helen@example.com',  '555', '555', 'PARENT_HELPER', 'parent_helper'),
(6, 'Hugo',   'Hunt',   'hugo@example.com',   '666', '666', 'PARENT_HELPER', NULL),
(7, 'Mona',   'Moore',  'mona@example.com',   '777', '777', 'MONITOR',       'monitor'),
(8, 'Pat',    'Parker', 'pat@example.com',    '888', '888', 'PARENT_HELPER', NULL),
(9, 'Adam',   'Ash',    'adam@example.com',   '999', '999', 'ADMIN',         'admin');

-- Penpal detail rows (same ids as their user rows). One guardian (parent_helper) each:
--   Alice -> Pat (8), Bob -> Helen (5, the dev 'parent_helper'), Carlos/Diana -> Hugo (6).
-- state = State enum name: US state code (CA, TX) or Malawi district (LILONGWE, BLANTYRE).
INSERT INTO penpal (id, age, state, biography, parent_helper_id) VALUES
(1, 11, 'CA',       'I like drawing.',  8),
(2, 12, 'LILONGWE', 'I love football.', 5),
(3, 10, 'TX',       'I play guitar.',   6),
(4, 13, 'BLANTYRE', 'I read a lot.',    6);

-- Two chats, each exactly two penpals
INSERT INTO chat (id, active) VALUES
(1, TRUE),
(2, TRUE);

INSERT INTO chat_members (chat_id, members_id) VALUES
(1, 1), (1, 2),   -- Alice <-> Bob
(2, 3), (2, 4);   -- Carlos <-> Diana

-- penpal_author = the authoring penpal; performed_by = the guardian who actually sent it.
-- approved_by is ALWAYS the monitor (Mona, 7). Msg 2 still pending.
INSERT INTO message (id, text, penpal_author_id, performed_by_id, create_time, chat_id, approved, approved_by_id, approved_time) VALUES
(1, 'Hi Bob, nice to meet you!',      1, 8, '2026-07-01 09:00:00', 1, TRUE, 7,    '2026-07-01 09:05:00'),
(2, 'Hey Alice! Where are you from?', 2, 5, '2026-07-01 10:00:00', 1, NULL, NULL, NULL),
(3, 'Hola Diana, do you like music?', 3, 6, '2026-07-02 14:00:00', 2, TRUE, 7,    '2026-07-02 14:10:00');

-- Old states preserved before each edit. edited_by = who edited (creator / guardian / monitor).
-- penpal_author + performed_by copied from the message being audited.
INSERT INTO message_audit
    (audit_id, archive_time, edited_by_id, message_id, text, penpal_author_id, performed_by_id, create_time, chat_id, approved, approved_by_id, approved_time) VALUES
(1, '2026-07-01 09:02:00', 1, 1, 'Hi Bob',     1, 8, '2026-07-01 09:00:00', 1, NULL, NULL, NULL),
(2, '2026-07-01 10:05:00', 5, 2, 'Hey Alice',  2, 5, '2026-07-01 10:00:00', 1, NULL, NULL, NULL),
(3, '2026-07-02 14:20:00', 7, 3, 'Hola Diana', 3, 6, '2026-07-02 14:00:00', 2, TRUE, 7,    '2026-07-02 14:10:00');
