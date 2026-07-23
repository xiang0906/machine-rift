-- V4 unlocked the lowest stage id. Existing databases can contain an older
-- stage without route or wave data before the seeded playable stages, so make
-- sure every player can enter the first stage that is actually playable.
UPDATE player_stage_progress
SET unlocked = TRUE,
    updated_at = CURRENT_TIMESTAMP
WHERE stage_id = (
  SELECT MIN(s.stage_id)
  FROM stage s
  WHERE EXISTS (
    SELECT 1 FROM stage_path sp WHERE sp.stage_id = s.stage_id
  )
    AND EXISTS (
      SELECT 1 FROM stage_wave sw WHERE sw.stage_id = s.stage_id
    )
);

INSERT INTO player_stage_progress (
  player_id, stage_id, unlocked, best_score, best_play_time,
  completion_count, updated_at
)
SELECT
  p.player_id,
  playable_stage.stage_id,
  TRUE,
  NULL,
  NULL,
  0,
  CURRENT_TIMESTAMP
FROM player p
JOIN stage playable_stage
  ON playable_stage.stage_id = (
    SELECT MIN(s.stage_id)
    FROM stage s
    WHERE EXISTS (
      SELECT 1 FROM stage_path sp WHERE sp.stage_id = s.stage_id
    )
      AND EXISTS (
        SELECT 1 FROM stage_wave sw WHERE sw.stage_id = s.stage_id
      )
  )
WHERE NOT EXISTS (
  SELECT 1
  FROM player_stage_progress psp
  WHERE psp.player_id = p.player_id
    AND psp.stage_id = playable_stage.stage_id
);
