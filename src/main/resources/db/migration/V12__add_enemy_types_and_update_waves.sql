-- Expand the enemy roster from three to six and make every enemy appear in
-- at least one existing stage wave.
INSERT INTO enemy (enemy_name, health, speed, reward_gold)
SELECT '疾風無人機', 30, 92.0, 12
WHERE NOT EXISTS (
  SELECT 1 FROM enemy WHERE enemy_name = '疾風無人機'
);

INSERT INTO enemy (enemy_name, health, speed, reward_gold)
SELECT '護盾機兵', 125, 36.0, 25
WHERE NOT EXISTS (
  SELECT 1 FROM enemy WHERE enemy_name = '護盾機兵'
);

INSERT INTO enemy (enemy_name, health, speed, reward_gold)
SELECT '裂隙巨像', 260, 25.0, 50
WHERE NOT EXISTS (
  SELECT 1 FROM enemy WHERE enemy_name = '裂隙巨像'
);

-- 機械迴廊：先以高速敵人施壓，最後由裂隙核心收尾。
UPDATE stage_wave
SET enemy_id = (SELECT enemy_id FROM enemy WHERE enemy_name = '疾風無人機')
WHERE stage_id = (SELECT stage_id FROM stage WHERE stage_name = '機械迴廊')
  AND wave_number = 1;

UPDATE stage_wave
SET enemy_id = (SELECT enemy_id FROM enemy WHERE enemy_name = '裝甲機')
WHERE stage_id = (SELECT stage_id FROM stage WHERE stage_name = '機械迴廊')
  AND wave_number = 2;

UPDATE stage_wave
SET enemy_id = (SELECT enemy_id FROM enemy WHERE enemy_name = '裂隙核心')
WHERE stage_id = (SELECT stage_id FROM stage WHERE stage_name = '機械迴廊')
  AND wave_number = 3;

-- 核心裂谷：混合高速、偵察與重裝敵人，最終波改為裂隙巨像。
UPDATE stage_wave
SET enemy_id = (SELECT enemy_id FROM enemy WHERE enemy_name = '疾風無人機')
WHERE stage_id = (SELECT stage_id FROM stage WHERE stage_name = '核心裂谷')
  AND wave_number = 1;

UPDATE stage_wave
SET enemy_id = (SELECT enemy_id FROM enemy WHERE enemy_name = '偵察機')
WHERE stage_id = (SELECT stage_id FROM stage WHERE stage_name = '核心裂谷')
  AND wave_number = 2;

UPDATE stage_wave
SET enemy_id = (SELECT enemy_id FROM enemy WHERE enemy_name = '護盾機兵')
WHERE stage_id = (SELECT stage_id FROM stage WHERE stage_name = '核心裂谷')
  AND wave_number = 3;

UPDATE stage_wave
SET enemy_id = (SELECT enemy_id FROM enemy WHERE enemy_name = '裂隙巨像'),
    enemy_count = 4,
    spawn_interval_ms = 950
WHERE stage_id = (SELECT stage_id FROM stage WHERE stage_name = '核心裂谷')
  AND wave_number = 4;

UPDATE stage
SET enemy_count = (
  SELECT SUM(stage_wave.enemy_count)
  FROM stage_wave
  WHERE stage_wave.stage_id = stage.stage_id
)
WHERE stage_name IN ('裂隙前線', '機械迴廊', '核心裂谷');
