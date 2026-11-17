-- Schema initiala: useri, servere, membri, canale, mesaje, invitatii.
-- VoiceSession NU e aici: sesiunile voice traiesc doar in memorie.

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(32)  NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    avatar_url    VARCHAR(512),
    status        VARCHAR(16)  NOT NULL DEFAULT 'OFFLINE'
        CHECK (status IN ('ONLINE', 'IDLE', 'DND', 'OFFLINE')),
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE servers (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    icon_url   VARCHAR(512),
    owner_id   BIGINT       NOT NULL REFERENCES users (id),
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE memberships (
    id        BIGSERIAL PRIMARY KEY,
    user_id   BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    server_id BIGINT      NOT NULL REFERENCES servers (id) ON DELETE CASCADE,
    role      VARCHAR(16) NOT NULL DEFAULT 'MEMBER'
        CHECK (role IN ('OWNER', 'ADMIN', 'MEMBER')),
    nickname  VARCHAR(32),
    joined_at TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT uq_membership_user_server UNIQUE (user_id, server_id)
);

CREATE TABLE channels (
    id        BIGSERIAL PRIMARY KEY,
    server_id BIGINT       NOT NULL REFERENCES servers (id) ON DELETE CASCADE,
    name      VARCHAR(100) NOT NULL,
    type      VARCHAR(8)   NOT NULL CHECK (type IN ('TEXT', 'VOICE')),
    position  INT          NOT NULL DEFAULT 0
);

CREATE TABLE messages (
    id         BIGSERIAL PRIMARY KEY,
    channel_id BIGINT        NOT NULL REFERENCES channels (id) ON DELETE CASCADE,
    author_id  BIGINT        NOT NULL REFERENCES users (id),
    content    VARCHAR(2000) NOT NULL,
    sent_at    TIMESTAMP     NOT NULL DEFAULT now(),
    edited_at  TIMESTAMP,
    deleted    BOOLEAN       NOT NULL DEFAULT FALSE
);

CREATE TABLE invites (
    code       VARCHAR(16) PRIMARY KEY,
    server_id  BIGINT      NOT NULL REFERENCES servers (id) ON DELETE CASCADE,
    created_by BIGINT      NOT NULL REFERENCES users (id),
    created_at TIMESTAMP   NOT NULL DEFAULT now(),
    expires_at TIMESTAMP,
    max_uses   INT,
    uses       INT         NOT NULL DEFAULT 0,
    status     VARCHAR(16) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'EXPIRED', 'EXHAUSTED', 'REVOKED'))
);

CREATE INDEX idx_memberships_server ON memberships (server_id);
CREATE INDEX idx_channels_server ON channels (server_id);
-- istoricul de mesaje se citeste mereu pe canal, cele mai noi primele
CREATE INDEX idx_messages_channel_sent ON messages (channel_id, sent_at DESC);
CREATE INDEX idx_invites_server ON invites (server_id);
