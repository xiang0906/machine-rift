CREATE TABLE player_progress (
  player_id BIGINT PRIMARY KEY,
  experience INT NOT NULL DEFAULT 0,
  gold INT NOT NULL DEFAULT 0,
  completed_stages INT NOT NULL DEFAULT 0,
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_player_progress_player
    FOREIGN KEY (player_id) REFERENCES player (player_id) ON DELETE CASCADE
);

CREATE TABLE player_stage_progress (
  player_stage_progress_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  player_id BIGINT NOT NULL,
  stage_id BIGINT NOT NULL,
  unlocked BOOLEAN NOT NULL DEFAULT FALSE,
  best_score INT NULL,
  best_play_time INT NULL,
  completion_count INT NOT NULL DEFAULT 0,
  updated_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_player_stage_progress_player
    FOREIGN KEY (player_id) REFERENCES player (player_id) ON DELETE CASCADE,
  CONSTRAINT fk_player_stage_progress_stage
    FOREIGN KEY (stage_id) REFERENCES stage (stage_id) ON DELETE CASCADE,
  CONSTRAINT uk_player_stage_progress UNIQUE (player_id, stage_id)
);

CREATE TABLE player_tower_unlock (
  player_tower_unlock_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  player_id BIGINT NOT NULL,
  tower_id BIGINT NOT NULL,
  unlocked_at TIMESTAMP NOT NULL,
  CONSTRAINT fk_player_tower_unlock_player
    FOREIGN KEY (player_id) REFERENCES player (player_id) ON DELETE CASCADE,
  CONSTRAINT fk_player_tower_unlock_tower
    FOREIGN KEY (tower_id) REFERENCES tower (tower_id) ON DELETE CASCADE,
  CONSTRAINT uk_player_tower_unlock UNIQUE (player_id, tower_id)
);

CREATE INDEX idx_player_stage_progress_player
  ON player_stage_progress (player_id);
CREATE INDEX idx_player_stage_progress_stage
  ON player_stage_progress (stage_id);
CREATE INDEX idx_player_tower_unlock_player
  ON player_tower_unlock (player_id);
CREATE INDEX idx_player_tower_unlock_tower
  ON player_tower_unlock (tower_id);

-- Backfill aggregate progress for players created before V4.
INSERT INTO player_progress (
  player_id, experience, gold, completed_stages, updated_at
)
SELECT
  p.player_id,
  COALESCE(SUM(gr.score), 0),
  COALESCE(SUM(CASE WHEN gr.result = 'WIN' THEN s.reward_gold ELSE 0 END), 0),
  COUNT(DISTINCT CASE WHEN gr.result = 'WIN' THEN gr.stage_id ELSE NULL END),
  CURRENT_TIMESTAMP
FROM player p
LEFT JOIN game_record gr ON gr.player_id = p.player_id
LEFT JOIN stage s ON s.stage_id = gr.stage_id
GROUP BY p.player_id;

-- Backfill each player's personal best for stages they have already played.
INSERT INTO player_stage_progress (
  player_id, stage_id, unlocked, best_score, best_play_time,
  completion_count, updated_at
)
SELECT
  gr.player_id,
  gr.stage_id,
  TRUE,
  MAX(gr.score),
  MIN(CASE
    WHEN gr.score = (
      SELECT MAX(best_gr.score)
      FROM game_record best_gr
      WHERE best_gr.player_id = gr.player_id
        AND best_gr.stage_id = gr.stage_id
    )
    THEN gr.play_time
    ELSE NULL
  END),
  SUM(CASE WHEN gr.result = 'WIN' THEN 1 ELSE 0 END),
  CURRENT_TIMESTAMP
FROM game_record gr
GROUP BY gr.player_id, gr.stage_id;

-- Every player can enter the first stage.
INSERT INTO player_stage_progress (
  player_id, stage_id, unlocked, best_score, best_play_time,
  completion_count, updated_at
)
SELECT
  p.player_id,
  first_stage.stage_id,
  TRUE,
  NULL,
  NULL,
  0,
  CURRENT_TIMESTAMP
FROM player p
JOIN stage first_stage
  ON first_stage.stage_id = (SELECT MIN(stage_id) FROM stage)
WHERE NOT EXISTS (
  SELECT 1
  FROM player_stage_progress psp
  WHERE psp.player_id = p.player_id
    AND psp.stage_id = first_stage.stage_id
);

-- Every player starts with the cheapest tower.
INSERT INTO player_tower_unlock (
  player_id, tower_id, unlocked_at
)
SELECT
  p.player_id,
  starter_tower.tower_id,
  CURRENT_TIMESTAMP
FROM player p
JOIN tower starter_tower
  ON starter_tower.tower_id = (
    SELECT MIN(tower_id)
    FROM tower
    WHERE cost = (SELECT MIN(cost) FROM tower)
  )
WHERE NOT EXISTS (
  SELECT 1
  FROM player_tower_unlock ptu
  WHERE ptu.player_id = p.player_id
    AND ptu.tower_id = starter_tower.tower_id
);
