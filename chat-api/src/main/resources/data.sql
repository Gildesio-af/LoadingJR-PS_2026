INSERT INTO chat_user (id, username, email, password, created_at, is_active)
VALUES ('39791f4f-5cfc-4c4d-80f8-147d53746b83', 'user1', 'user1@example.com', '$2a$12$Nrz3AtCHPBriSKxJm4puz..4MFKYKQD4TJy/QpUXBXkjjsc.XSQYC', NOW(), true)
ON CONFLICT (username) DO NOTHING;

INSERT INTO chat_user (id, username, email, password, created_at, is_active)
VALUES ('a3c16b57-3987-4aa1-8a57-15a32c483f13', 'user2', 'user2@example.com', '$2a$12$Nrz3AtCHPBriSKxJm4puz..4MFKYKQD4TJy/QpUXBXkjjsc.XSQYC', NOW(), true)
    ON CONFLICT (username) DO NOTHING;

