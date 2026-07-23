ALTER TABLE player ADD COLUMN username VARCHAR(50) NOT NULL DEFAULT 'legacy';
ALTER TABLE player ADD COLUMN password_hash VARCHAR(100) NOT NULL DEFAULT 'legacy-disabled';

-- Existing players remain attached to their progress and records. Their
-- generated legacy accounts cannot be used until an account recovery flow is
-- introduced; all newly registered players receive a BCrypt password hash.
UPDATE player
SET username = CONCAT('legacy_', player_id),
    password_hash = CONCAT('legacy-disabled-', player_id);

CREATE UNIQUE INDEX uk_player_username ON player (username);

CREATE TABLE player_session (
  player_session_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  player_id BIGINT NOT NULL,
  token_hash VARCHAR(64) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  CONSTRAINT uk_player_session_token_hash UNIQUE (token_hash),
  CONSTRAINT fk_player_session_player
    FOREIGN KEY (player_id) REFERENCES player (player_id) ON DELETE CASCADE
);

CREATE INDEX idx_player_session_player_id ON player_session (player_id);
CREATE INDEX idx_player_session_expires_at ON player_session (expires_at);
