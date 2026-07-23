-- Remove the three legacy placeholder stages and their game records. These
-- stages predate database-driven paths and waves and cannot be played.
DELETE FROM game_record
WHERE stage_id IN (
  SELECT stage_id
  FROM stage
  WHERE stage_name IN ('Wave 1', 'Wave 2', 'Wave 3')
);

-- player_stage_progress is removed automatically through ON DELETE CASCADE.
DELETE FROM stage
WHERE stage_name IN ('Wave 1', 'Wave 2', 'Wave 3');

-- Progress is currently derived entirely from game records, so rebuild the
-- aggregate values after removing legacy history.
UPDATE player_progress pp
SET experience = COALESCE((
      SELECT SUM(gr.score)
      FROM game_record gr
      WHERE gr.player_id = pp.player_id
    ), 0),
    gold = COALESCE((
      SELECT SUM(CASE WHEN gr.result = 'WIN' THEN s.reward_gold ELSE 0 END)
      FROM game_record gr
      JOIN stage s ON s.stage_id = gr.stage_id
      WHERE gr.player_id = pp.player_id
    ), 0),
    completed_stages = (
      SELECT COUNT(DISTINCT gr.stage_id)
      FROM game_record gr
      WHERE gr.player_id = pp.player_id
        AND gr.result = 'WIN'
    ),
    updated_at = CURRENT_TIMESTAMP;

UPDATE player p
SET level = 1 + FLOOR(COALESCE((
      SELECT pp.experience
      FROM player_progress pp
      WHERE pp.player_id = p.player_id
    ), 0) / 1000);
