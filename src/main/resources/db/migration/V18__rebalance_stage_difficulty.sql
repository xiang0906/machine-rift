-- First difficulty pass: keep stage 1 as the tutorial, then increase pressure
-- gradually. Tower stats, rewards, starting gold and base health stay unchanged.

-- Stage 2: only add one enemy to the final wave.
UPDATE stage_wave
SET enemy_count = 7
WHERE stage_id = (SELECT stage_id FROM stage WHERE stage_name = '機械迴廊')
  AND wave_number = 3;

-- Stage 3: 25 -> 28 enemies (+12%).
UPDATE stage_wave
SET enemy_count = CASE wave_number
  WHEN 1 THEN 8 WHEN 2 THEN 8 WHEN 3 THEN 8 WHEN 4 THEN 4
END
WHERE stage_id = (SELECT stage_id FROM stage WHERE stage_name = '核心裂谷')
  AND wave_number BETWEEN 1 AND 4;

-- Stage 4: 30 -> 35 enemies (+16.7%), with intervals reduced by 10%.
UPDATE stage_wave
SET enemy_count = 7,
    spawn_interval_ms = CASE wave_number
      WHEN 1 THEN 585 WHEN 2 THEN 585 WHEN 3 THEN 648
      WHEN 4 THEN 720 WHEN 5 THEN 810
    END
WHERE stage_id = (SELECT stage_id FROM stage WHERE stage_name = '熔火交叉口')
  AND wave_number BETWEEN 1 AND 5;

-- Stage 5: 35 -> 42 enemies (+20%), with intervals reduced by about 12%.
UPDATE stage_wave
SET enemy_count = CASE wave_number
      WHEN 1 THEN 8 WHEN 2 THEN 8 WHEN 3 THEN 8
      WHEN 4 THEN 7 WHEN 5 THEN 7 WHEN 6 THEN 4
    END,
    spawn_interval_ms = CASE wave_number
      WHEN 1 THEN 510 WHEN 2 THEN 550 WHEN 3 THEN 600
      WHEN 4 THEN 670 WHEN 5 THEN 750 WHEN 6 THEN 970
    END
WHERE stage_id = (SELECT stage_id FROM stage WHERE stage_name = '量子迷城')
  AND wave_number BETWEEN 1 AND 6;

-- Stage 6: 41 -> 51 enemies (+24.4%), with intervals reduced by about 15%.
UPDATE stage_wave
SET enemy_count = CASE wave_number
      WHEN 1 THEN 10 WHEN 2 THEN 10 WHEN 3 THEN 10
      WHEN 4 THEN 9 WHEN 5 THEN 7 WHEN 6 THEN 5
    END,
    spawn_interval_ms = CASE wave_number
      WHEN 1 THEN 430 WHEN 2 THEN 460 WHEN 3 THEN 530
      WHEN 4 THEN 600 WHEN 5 THEN 660 WHEN 6 THEN 850
    END
WHERE stage_id = (SELECT stage_id FROM stage WHERE stage_name = '機神核心')
  AND wave_number BETWEEN 1 AND 6;

-- Health belongs to the enemy archetype in the current model, so this 10%
-- increase applies globally wherever these two high-tier enemies appear.
UPDATE enemy SET health = 165 WHERE enemy_name = '裂隙核心';
UPDATE enemy SET health = 286 WHERE enemy_name = '裂隙巨像';

-- Keep each stage summary synchronized with the adjusted wave rows.
UPDATE stage
SET enemy_count = (
  SELECT SUM(stage_wave.enemy_count)
  FROM stage_wave
  WHERE stage_wave.stage_id = stage.stage_id
)
WHERE stage_name IN ('機械迴廊', '核心裂谷', '熔火交叉口', '量子迷城', '機神核心');
