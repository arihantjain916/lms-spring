-- Messaging (user <-> customer care, student <-> instructor/admin) and in-app notifications.
-- Types match what Hibernate derives from the entities so ddl-auto=update finds nothing to change.

CREATE TABLE IF NOT EXISTS conversations
(
    id              VARCHAR(255) NOT NULL,
    type            VARCHAR(255) NOT NULL,
    initiator_id    VARCHAR(255) NOT NULL,
    -- null on SUPPORT threads: they are answered out of a shared admin queue
    recipient_id    VARCHAR(255),
    course_id       BIGINT,
    subject         TEXT,
    status          VARCHAR(255) NOT NULL,
    last_message_at TIMESTAMP(6),
    created_at      TIMESTAMP(6) NOT NULL,
    updated_at      TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_conversations PRIMARY KEY (id),
    CONSTRAINT fk_conversations_initiator FOREIGN KEY (initiator_id) REFERENCES users (id),
    CONSTRAINT fk_conversations_recipient FOREIGN KEY (recipient_id) REFERENCES users (id),
    CONSTRAINT fk_conversations_course FOREIGN KEY (course_id) REFERENCES courses (id)
);

CREATE TABLE IF NOT EXISTS messages
(
    id              VARCHAR(255) NOT NULL,
    conversation_id VARCHAR(255) NOT NULL,
    sender_id       VARCHAR(255) NOT NULL,
    content         TEXT         NOT NULL,
    read_at         TIMESTAMP(6),
    created_at      TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_messages PRIMARY KEY (id),
    CONSTRAINT fk_messages_conversation FOREIGN KEY (conversation_id) REFERENCES conversations (id),
    CONSTRAINT fk_messages_sender FOREIGN KEY (sender_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS notifications
(
    id           VARCHAR(255) NOT NULL,
    user_id      VARCHAR(255) NOT NULL,
    type         VARCHAR(255) NOT NULL,
    title        TEXT         NOT NULL,
    body         TEXT,
    link         VARCHAR(255),
    reference_id VARCHAR(255),
    is_read      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- inbox lookups: "my threads, newest activity first"
CREATE INDEX IF NOT EXISTS idx_conversations_initiator ON conversations (initiator_id);
CREATE INDEX IF NOT EXISTS idx_conversations_recipient ON conversations (recipient_id);
-- the customer-care queue filters on type (+ status) and sorts by last activity
CREATE INDEX IF NOT EXISTS idx_conversations_type_status ON conversations (type, status, last_message_at DESC);

-- message pagination within a thread, and the unread-count scan
CREATE INDEX IF NOT EXISTS idx_messages_conversation_created ON messages (conversation_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_messages_unread ON messages (conversation_id, sender_id) WHERE read_at IS NULL;

-- notification list and the unread badge count
CREATE INDEX IF NOT EXISTS idx_notifications_user_created ON notifications (user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON notifications (user_id) WHERE is_read = FALSE;
