CREATE TABLE player_tower_unlock (
  player_tower_unlock_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  player_id BIGINT NOT NULL,
  tower_id BIGINT NOT NULL,
  unlocked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_player_tower_unlock_player
    FOREIGN KEY (player_id) REFERENCES player (player_id) ON DELETE CASCADE,
  CONSTRAINT fk_player_tower_unlock_tower
    FOREIGN KEY (tower_id) REFERENCES tower (tower_id) ON DELETE CASCADE,
  CONSTRAINT uk_player_tower_unlock UNIQUE (player_id, tower_id)
);

CREATE INDEX idx_player_tower_unlock_player
  ON player_tower_unlock (player_id);
CREATE INDEX idx_player_tower_unlock_tower
  ON player_tower_unlock (tower_id);

-- V14 stored only the number of towers unlocked in cost order. Restore the
-- corresponding player-to-tower rows before removing that aggregate column.
INSERT INTO player_tower_unlock (player_id, tower_id, unlocked_at)
SELECT p.player_id, t.tower_id, CURRENT_TIMESTAMP
FROM player p
JOIN tower t
  ON (
    SELECT COUNT(*)
    FROM tower ranked_tower
    WHERE ranked_tower.cost < t.cost
       OR (ranked_tower.cost = t.cost AND ranked_tower.tower_id <= t.tower_id)
  ) <= GREATEST(p.unlocked_tower_count, 1);

ALTER TABLE player DROP COLUMN unlocked_tower_count;
