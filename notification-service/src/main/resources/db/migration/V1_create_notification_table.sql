CREATE TABLE notifications (
                               id BIGSERIAL PRIMARY KEY,
                               type VARCHAR(50) NOT NULL,
                               recipient_email VARCHAR(255) NOT NULL,
                               title VARCHAR(255) NOT NULL,
                               message TEXT NOT NULL,
                               sent BOOLEAN DEFAULT FALSE,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               sent_at TIMESTAMP
);

CREATE INDEX idx_notifications_recipient_email ON notifications(recipient_email);
CREATE INDEX idx_notifications_sent ON notifications(sent);