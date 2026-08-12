-- Users: 4 penpals, 3 parent-helpers (guardians), 1 monitor, 1 admin.
-- auth_id matches the dev in-memory usernames so /me resolves in dev.
INSERT INTO app_user (id, first_name, last_name, email, phone, whatsapp, role, auth_id) VALUES
(1, 'Alice',  'Anders', 'alice@example.com',  '+14155550001', '+14155550001', 'PENPAL',        'penpal'),
(2, 'Bob',    'Brown',  'bob@example.com',    '+14155550002', '+14155550002', 'PENPAL',        NULL),
(3, 'Carlos', 'Cruz',   'carlos@example.com', '+14155550003', '+14155550003', 'PENPAL',        NULL),
(4, 'Diana',  'Diaz',   'diana@example.com',  '+14155550004', '+14155550004', 'PENPAL',        NULL),
(5, 'Helen',  'Hale',   'helen@example.com',  '+14155550005', '+14155550005', 'PARENT_HELPER', 'parent_helper'),
(6, 'Hugo',   'Hunt',   'hugo@example.com',   '+14155550006', '+14155550006', 'PARENT_HELPER', 'parent_helper_2'),
(7, 'Mona',   'Moore',  'mona@example.com',   '+14155550007', '+14155550007', 'MONITOR',       'monitor'),
(8, 'Pat',    'Parker', 'pat@example.com',    '+14155550008', '+14155550008', 'PARENT_HELPER', 'parent_helper_3'),
(9, 'Adam',   'Ash',    'adam@example.com',   '+14155550009', '+14155550009', 'ADMIN',         'admin'),
-- Extra fixtures for the monitor map "empty state" scenarios:
--   Nia   (10) = a parent/helper with NO penpal (lone-node case).
--   Quinn (11) & Rosa (12) = guardians of two penpals that share no chat yet.
(10, 'Nia',   'Nolan',  'nia@example.com',    '+14155550010', '+14155550010', 'PARENT_HELPER', NULL),
(11, 'Quinn', 'Quill',  'quinn@example.com',  '+14155550011', '+14155550011', 'PARENT_HELPER', 'parent_helper_4'),
(12, 'Rosa',  'Reed',   'rosa@example.com',   '+14155550012', '+14155550012', 'PARENT_HELPER', NULL),
(13, 'Omar',  'Osei',   'omar@example.com',   '+14155550013', '+14155550013', 'PENPAL',        NULL),
(14, 'Priya', 'Patel',  'priya@example.com',  '+14155550014', '+14155550014', 'PENPAL',        NULL);

-- Penpal detail rows (same ids as their user rows). One guardian (parent_helper) each:
--   Alice -> Pat (8), Bob -> Helen (5, the dev 'parent_helper'), Carlos -> Hugo (6), Diana -> Pat (8).
-- A chat's two penpals always have DIFFERENT guardians (a guardian never has both sides of one chat).
-- state = State enum name: US state code (CA, TX) or Malawi district (LILONGWE, BLANTYRE).
INSERT INTO penpal (id, age, state, biography, parent_helper_id) VALUES
(1, 11, 'CA',       'I like drawing. I am in fourth grade. I have a pet dog named Doggo. I love to watch soccer, but not play because I get tired easily. I have a brother Timon who farts a lot! I am reading a book about a boy in Malawi who built his own windmill, that is so cool! One day I want to be an engineer and make my own windmill too. My favorite color is blue, even though my brother says that blue is for boys, I told him "you''re dumb!".', 8),
(2, 12, 'LILONGWE', 'I live in Lilongwe with my mom, my dad, and my two little sisters. My family keeps chickens and one goat named Buttons. I love football more than anything and I want to be a football star one day. Every morning I collect the eggs before I walk to school. My favorite subject is science because I like to learn how things work. I am saving up to buy my very own football.', 5),
(3, 10, 'TX',       'I am from Texas and I am the loudest kid in my whole class! I love music and I am learning to play the guitar from my abuela. My two cats are named Sol and Luna and they follow me everywhere. On Sundays my family eats tacos and sings songs together after church. When I grow up I want to be in a band and travel the world. I also really like soccer and drawing dinosaurs.', 6),
(4, 13, 'BLANTYRE', 'I read a lot, sometimes three whole books in one week! I live in Blantyre with my mother, who is a nurse at the big hospital. When I grow up I want to help people too, maybe as a doctor. I love to draw maps of faraway places that I dream of visiting. My grandmother has a garden where we grow tomatoes and sweet pumpkins. My favorite animal is the elephant because they never forget their friends.', 8),
-- Omar & Priya have different guardians (11, 12) and are in NO chat, so a monitor
-- can pair them into a brand-new chat.
(13, 12, 'TX',       'I collect stamps from every country I can find, and I have almost one hundred! I live in Texas and my favorite thing is learning about faraway places. I have a big map on my wall and I put a pin on every country in my collection. My dream is to be a pilot so I can visit all of them one day. I also love math and puzzles, the harder the better. My little brother tries to steal my stamps, but I hide them in a secret box.', 11),
(14, 11, 'LILONGWE', 'I love to sing more than anything in the whole world! I live in Lilongwe and I sing in the choir at my school. My teacher says I have a big voice for such a small girl. When I grow up I want to be a famous singer and make people happy. I also like to dance, but I am still learning not to trip over my own feet! My favorite song is one my mother taught me when I was very little.', 12);

-- Two chats, each exactly two penpals
INSERT INTO chat (id, active) VALUES
(1, TRUE),
(2, TRUE);

INSERT INTO chat_members (chat_id, members_id) VALUES
(1, 1), (1, 2),   -- Alice <-> Bob
(2, 3), (2, 4);   -- Carlos <-> Diana

-- penpal_author = the authoring penpal; performed_by = the guardian who sent it.
-- approved_by is ALWAYS the monitor (Mona, 7). Pending (approved NULL): 2, 8, 10.
-- Messages 6 and 9 carry an inline monitor edit: "(original)[correction]".
-- Chat 1 = Alice(1)/Pat(8) <-> Bob(2)/Helen(5). Chat 2 = Carlos(3)/Hugo(6) <-> Diana(4)/Pat(8).
INSERT INTO message (id, text, penpal_author_id, performed_by_id, create_time, chat_id, approved, approved_by_id, approved_time) VALUES
(1, 'Hi Bob, it is so nice to meet you! My name is Alice and I am eleven years old. I live in California with my mom, my dad, and my little brother Timon. I love to draw pictures of animals, especially horses and dogs. My dog Doggo likes to sit next to me while I draw. What do you like to do for fun? I really hope that we can be good friends and write many letters to each other. Please tell me all about where you live!', 1, 8, '2026-07-01 09:00:00+00', 1, TRUE, 7, '2026-07-01 09:05:00+00'),
(2, 'I''m just a test, I am only a test. Im sitting here in pending status. Its a long, long journey to approved status. Its a long long wait, while I''m hoping for approval.', 2, 5, '2026-07-01 10:00:00+00', 1, NULL, NULL, NULL),
(6, 'Thank you for your letter Bob, it made me so happy to (recieve)[receive] it! School is going well, but math has been very hard for me this year. My teacher says I am getting a little better every single day. Do you like school? My favorite subject is art, and last week I painted a big picture of the ocean with a whale in it. I hung it on my wall right next to my bed. I hope you have a wonderful day!', 1, 8, '2026-07-04 09:00:00+00', 1, TRUE, 7, '2026-07-04 09:10:00+00'),
(7, 'Hello Alice, thank you for writing back to me so quickly! I live in Lilongwe, which is a big and busy city in Malawi. My family keeps some chickens and one goat named Buttons who likes to eat everything. Every morning I help my father collect the eggs before I walk to school. In the evening when it is cooler, I play football with my friends until it gets dark. Someday I want to be a football star and play for a big team. What sports do you like to play?', 2, 5, '2026-07-05 10:00:00+00', 1, TRUE, 7, '2026-07-05 10:10:00+00'),
(8, 'Dear Bob, I have been laughing about your goat Buttons all week, what a funny name for a goat! Yesterday it rained so hard that we could not go outside, so I stayed in and read three whole books. My favorite one is about a girl who can talk to animals. I really wish I could talk to animals too, especially my dog Doggo. Do you have a favorite book that you like to read? I am also learning to ride my bike without the training wheels. It is a little scary, but it is also very fun!', 1, 8, '2026-07-06 11:00:00+00', 1, NULL, NULL, NULL),
(3, 'Hola Diana, it is very nice to meet you! I am Carlos, I am ten years old, and I live in Texas. I love music more than almost anything, and my abuela is teaching me to play the guitar. Every Sunday after church we sing old songs together in her kitchen. Do you like music too? I also love tacos, soccer, and my two cats named Sol and Luna. Sol is lazy but Luna is very naughty and knocks things off the table. Please write back soon and tell me all about yourself!', 3, 6, '2026-07-02 14:00:00+00', 2, TRUE, 7, '2026-07-02 14:10:00+00'),
(9, 'Hi Carlos, I am so glad that you wrote to me! I live in Blantyre and I love to read books more than (anyting)[anything] else in the world. Right now I am reading a story about a brave girl who sails all the way across the sea. My mother is a nurse and she works very hard at the big hospital in our city. When I grow up I want to help people too, so I dream of becoming a doctor. What do you want to be when you grow up? I also love to draw maps of all the places I hope to visit one day.', 4, 8, '2026-07-04 15:00:00+00', 2, TRUE, 7, '2026-07-04 15:10:00+00'),
(10, 'Dear Diana, being a doctor is a wonderful dream, you must be very smart and kind! This week my guitar teacher showed me a brand new song and I practiced it until my fingers were sore. My cat Luna likes to curl up on my lap while I play, but Sol runs away from all the noise. We had a huge rainstorm here and the streets turned into little rivers. My brother and I made paper boats and raced them down the water until they fell apart. What is the weather like where you live right now?', 3, 6, '2026-07-05 16:00:00+00', 2, NULL, NULL, NULL),
(11, 'Hello Carlos, your paper boat race sounds like the most fun thing ever! The weather here has been very hot and dry, so we drink lots and lots of water. My favorite thing to do after school is help my grandmother in her big garden. We grow tomatoes, beans, and sweet pumpkins that taste absolutely amazing. Sometimes the birds try to eat all of our seeds, so I built a little scarecrow to scare them away. I named him Mr. Sticks and he proudly wears my father''s old straw hat! Do you have a garden where you live?', 4, 8, '2026-07-06 17:00:00+00', 2, TRUE, 7, '2026-07-06 17:10:00+00'),
-- Monitor "blast": one message fanned out to every active chat, auto-approved.
(4, 'Reminder from your monitors: always be kind and respectful in every letter you write. Please never share your home address, your phone number, or any passwords with anyone. If a message ever makes you feel worried or sad, tell a grown-up that you trust right away. We are so happy that you are part of our penpal family. Keep being wonderful!', NULL, 7, '2026-07-03 12:00:00+00', 1, TRUE, 7, '2026-07-03 12:00:00+00'),
(5, 'Reminder from your monitors: always be kind and respectful in every letter you write. Please never share your home address, your phone number, or any passwords with anyone. If a message ever makes you feel worried or sad, tell a grown-up that you trust right away. We are so happy that you are part of our penpal family. Keep being wonderful!', NULL, 7, '2026-07-03 12:00:00+00', 2, TRUE, 7, '2026-07-03 12:00:00+00');

-- Old states preserved before each edit. edited_by = who edited (creator / guardian / monitor).
-- penpal_author + performed_by copied from the message being audited.
INSERT INTO message_audit
    (audit_id, archive_time, edited_by_id, message_id, text, penpal_author_id, performed_by_id, create_time, chat_id, approved, approved_by_id, approved_time) VALUES
(1, '2026-07-01 09:02:00+00', 1, 1, 'Hi Bob',     1, 8, '2026-07-01 09:00:00+00', 1, NULL, NULL, NULL),
(2, '2026-07-01 10:05:00+00', 5, 2, 'Hey Alice',  2, 5, '2026-07-01 10:00:00+00', 1, NULL, NULL, NULL),
(3, '2026-07-02 14:20:00+00', 7, 3, 'Hola Diana', 3, 6, '2026-07-02 14:00:00+00', 2, TRUE, 7,    '2026-07-02 14:10:00+00');

-- Seed rows above use explicit ids; H2 does not advance the IDENTITY counter for
-- explicit inserts, so restart each generated-id table clear of the seeded values.
ALTER TABLE app_user     ALTER COLUMN id       RESTART WITH 100;
ALTER TABLE chat         ALTER COLUMN id       RESTART WITH 100;
ALTER TABLE message      ALTER COLUMN id       RESTART WITH 100;
ALTER TABLE message_audit ALTER COLUMN audit_id RESTART WITH 100;
