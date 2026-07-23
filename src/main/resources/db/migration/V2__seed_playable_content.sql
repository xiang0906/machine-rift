-- On a fresh database these stages receive ids 1-3, matching the three routes
-- currently implemented by the Canvas client. Existing databases keep their
-- ids and content; missing seed rows are appended without overwriting anything.
INSERT INTO stage (stage_name, difficulty, reward_gold, enemy_count)
SELECT '裂隙前線', 'Easy', 100, 10
WHERE NOT EXISTS (
  SELECT 1 FROM stage WHERE stage_name = '裂隙前線'
);

INSERT INTO stage (stage_name, difficulty, reward_gold, enemy_count)
SELECT '機械迴廊', 'Normal', 200, 18
WHERE NOT EXISTS (
  SELECT 1 FROM stage WHERE stage_name = '機械迴廊'
);

INSERT INTO stage (stage_name, difficulty, reward_gold, enemy_count)
SELECT '核心裂谷', 'Hard', 350, 28
WHERE NOT EXISTS (
  SELECT 1 FROM stage WHERE stage_name = '核心裂谷'
);

INSERT INTO tower (
  tower_name, tower_type, damage, attack_speed, attack_range, cost
)
SELECT '脈衝砲塔', 'RAPID', 14, 2.0, 120, 80
WHERE NOT EXISTS (
  SELECT 1 FROM tower WHERE tower_name = '脈衝砲塔'
);

INSERT INTO tower (
  tower_name, tower_type, damage, attack_speed, attack_range, cost
)
SELECT '磁軌砲塔', 'HEAVY', 42, 0.7, 165, 140
WHERE NOT EXISTS (
  SELECT 1 FROM tower WHERE tower_name = '磁軌砲塔'
);

INSERT INTO tower (
  tower_name, tower_type, damage, attack_speed, attack_range, cost
)
SELECT '量子砲塔', 'BALANCED', 24, 1.2, 145, 110
WHERE NOT EXISTS (
  SELECT 1 FROM tower WHERE tower_name = '量子砲塔'
);
