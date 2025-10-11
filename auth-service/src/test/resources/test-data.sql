INSERT INTO users (id, email, password, first_name, last_name, role, enabled, created_at)
VALUES (1, 'email@email.com', '$2a$10$encodedPassword', 'firstName', 'lastName', 'USER', true, CURRENT_TIMESTAMP);
