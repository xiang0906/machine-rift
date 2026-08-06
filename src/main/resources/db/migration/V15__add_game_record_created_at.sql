ALTER TABLE game_record
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX idx_game_record_player_created_at
    ON game_record (player_id, created_at);
