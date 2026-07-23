CREATE TABLE enemy (
  enemy_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  enemy_name VARCHAR(100) NOT NULL,
  health INT NOT NULL,
  speed DOUBLE NOT NULL,
  reward_gold INT NOT NULL,
  CONSTRAINT uk_enemy_name UNIQUE (enemy_name)
);

CREATE TABLE stage_path (
  stage_path_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  stage_id BIGINT NOT NULL,
  point_order INT NOT NULL,
  grid_col INT NOT NULL,
  grid_row INT NOT NULL,
  CONSTRAINT fk_stage_path_stage
    FOREIGN KEY (stage_id) REFERENCES stage (stage_id) ON DELETE CASCADE,
  CONSTRAINT uk_stage_path_order UNIQUE (stage_id, point_order)
);

CREATE TABLE stage_wave (
  stage_wave_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  stage_id BIGINT NOT NULL,
  enemy_id BIGINT NOT NULL,
  wave_number INT NOT NULL,
  enemy_count INT NOT NULL,
  spawn_interval_ms INT NOT NULL,
  CONSTRAINT fk_stage_wave_stage
    FOREIGN KEY (stage_id) REFERENCES stage (stage_id) ON DELETE CASCADE,
  CONSTRAINT fk_stage_wave_enemy
    FOREIGN KEY (enemy_id) REFERENCES enemy (enemy_id) ON DELETE RESTRICT,
  CONSTRAINT uk_stage_wave_number UNIQUE (stage_id, wave_number)
);

CREATE INDEX idx_stage_path_stage_id ON stage_path (stage_id);
CREATE INDEX idx_stage_wave_stage_id ON stage_wave (stage_id);
CREATE INDEX idx_stage_wave_enemy_id ON stage_wave (enemy_id);

INSERT INTO enemy (enemy_name, health, speed, reward_gold)
SELECT '偵察機', 40, 62.0, 10
WHERE NOT EXISTS (SELECT 1 FROM enemy WHERE enemy_name = '偵察機');

INSERT INTO enemy (enemy_name, health, speed, reward_gold)
SELECT '裝甲機', 85, 45.0, 18
WHERE NOT EXISTS (SELECT 1 FROM enemy WHERE enemy_name = '裝甲機');

INSERT INTO enemy (enemy_name, health, speed, reward_gold)
SELECT '裂隙核心', 150, 34.0, 30
WHERE NOT EXISTS (SELECT 1 FROM enemy WHERE enemy_name = '裂隙核心');

-- 裂隙前線
INSERT INTO stage_path (stage_id, point_order, grid_col, grid_row)
SELECT s.stage_id, p.point_order, p.grid_col, p.grid_row
FROM stage s
JOIN (
  SELECT 1 AS point_order, 0 AS grid_col, 2 AS grid_row
  UNION ALL SELECT 2, 6, 2
  UNION ALL SELECT 3, 6, 5
  UNION ALL SELECT 4, 13, 5
) p ON 1 = 1
WHERE s.stage_name = '裂隙前線'
  AND NOT EXISTS (
    SELECT 1 FROM stage_path sp
    WHERE sp.stage_id = s.stage_id AND sp.point_order = p.point_order
  );

-- 機械迴廊
INSERT INTO stage_path (stage_id, point_order, grid_col, grid_row)
SELECT s.stage_id, p.point_order, p.grid_col, p.grid_row
FROM stage s
JOIN (
  SELECT 1 AS point_order, 0 AS grid_col, 1 AS grid_row
  UNION ALL SELECT 2, 4, 1
  UNION ALL SELECT 3, 4, 4
  UNION ALL SELECT 4, 9, 4
  UNION ALL SELECT 5, 9, 6
  UNION ALL SELECT 6, 13, 6
) p ON 1 = 1
WHERE s.stage_name = '機械迴廊'
  AND NOT EXISTS (
    SELECT 1 FROM stage_path sp
    WHERE sp.stage_id = s.stage_id AND sp.point_order = p.point_order
  );

-- 核心裂谷
INSERT INTO stage_path (stage_id, point_order, grid_col, grid_row)
SELECT s.stage_id, p.point_order, p.grid_col, p.grid_row
FROM stage s
JOIN (
  SELECT 1 AS point_order, 0 AS grid_col, 0 AS grid_row
  UNION ALL SELECT 2, 12, 0
  UNION ALL SELECT 3, 12, 2
  UNION ALL SELECT 4, 1, 2
  UNION ALL SELECT 5, 1, 4
  UNION ALL SELECT 6, 12, 4
  UNION ALL SELECT 7, 12, 6
  UNION ALL SELECT 8, 1, 6
  UNION ALL SELECT 9, 1, 7
  UNION ALL SELECT 10, 13, 7
) p ON 1 = 1
WHERE s.stage_name = '核心裂谷'
  AND NOT EXISTS (
    SELECT 1 FROM stage_path sp
    WHERE sp.stage_id = s.stage_id AND sp.point_order = p.point_order
  );

INSERT INTO stage_wave (
  stage_id, enemy_id, wave_number, enemy_count, spawn_interval_ms
)
SELECT s.stage_id, e.enemy_id, 1, 5, 900
FROM stage s JOIN enemy e ON e.enemy_name = '偵察機'
WHERE s.stage_name = '裂隙前線'
  AND NOT EXISTS (
    SELECT 1 FROM stage_wave sw WHERE sw.stage_id = s.stage_id AND sw.wave_number = 1
  );

INSERT INTO stage_wave (
  stage_id, enemy_id, wave_number, enemy_count, spawn_interval_ms
)
SELECT s.stage_id, e.enemy_id, 2, 5, 780
FROM stage s JOIN enemy e ON e.enemy_name = '裝甲機'
WHERE s.stage_name = '裂隙前線'
  AND NOT EXISTS (
    SELECT 1 FROM stage_wave sw WHERE sw.stage_id = s.stage_id AND sw.wave_number = 2
  );

INSERT INTO stage_wave (
  stage_id, enemy_id, wave_number, enemy_count, spawn_interval_ms
)
SELECT s.stage_id, e.enemy_id, 1, 6, 820
FROM stage s JOIN enemy e ON e.enemy_name = '偵察機'
WHERE s.stage_name = '機械迴廊'
  AND NOT EXISTS (
    SELECT 1 FROM stage_wave sw WHERE sw.stage_id = s.stage_id AND sw.wave_number = 1
  );

INSERT INTO stage_wave (
  stage_id, enemy_id, wave_number, enemy_count, spawn_interval_ms
)
SELECT s.stage_id, e.enemy_id, 2, 6, 720
FROM stage s JOIN enemy e ON e.enemy_name = '裝甲機'
WHERE s.stage_name = '機械迴廊'
  AND NOT EXISTS (
    SELECT 1 FROM stage_wave sw WHERE sw.stage_id = s.stage_id AND sw.wave_number = 2
  );

INSERT INTO stage_wave (
  stage_id, enemy_id, wave_number, enemy_count, spawn_interval_ms
)
SELECT s.stage_id, e.enemy_id, 3, 6, 650
FROM stage s JOIN enemy e ON e.enemy_name = '裂隙核心'
WHERE s.stage_name = '機械迴廊'
  AND NOT EXISTS (
    SELECT 1 FROM stage_wave sw WHERE sw.stage_id = s.stage_id AND sw.wave_number = 3
  );

INSERT INTO stage_wave (
  stage_id, enemy_id, wave_number, enemy_count, spawn_interval_ms
)
SELECT s.stage_id, e.enemy_id, 1, 7, 760
FROM stage s JOIN enemy e ON e.enemy_name = '偵察機'
WHERE s.stage_name = '核心裂谷'
  AND NOT EXISTS (
    SELECT 1 FROM stage_wave sw WHERE sw.stage_id = s.stage_id AND sw.wave_number = 1
  );

INSERT INTO stage_wave (
  stage_id, enemy_id, wave_number, enemy_count, spawn_interval_ms
)
SELECT s.stage_id, e.enemy_id, 2, 7, 680
FROM stage s JOIN enemy e ON e.enemy_name = '裝甲機'
WHERE s.stage_name = '核心裂谷'
  AND NOT EXISTS (
    SELECT 1 FROM stage_wave sw WHERE sw.stage_id = s.stage_id AND sw.wave_number = 2
  );

INSERT INTO stage_wave (
  stage_id, enemy_id, wave_number, enemy_count, spawn_interval_ms
)
SELECT s.stage_id, e.enemy_id, 3, 7, 600
FROM stage s JOIN enemy e ON e.enemy_name = '裝甲機'
WHERE s.stage_name = '核心裂谷'
  AND NOT EXISTS (
    SELECT 1 FROM stage_wave sw WHERE sw.stage_id = s.stage_id AND sw.wave_number = 3
  );

INSERT INTO stage_wave (
  stage_id, enemy_id, wave_number, enemy_count, spawn_interval_ms
)
SELECT s.stage_id, e.enemy_id, 4, 7, 520
FROM stage s JOIN enemy e ON e.enemy_name = '裂隙核心'
WHERE s.stage_name = '核心裂谷'
  AND NOT EXISTS (
    SELECT 1 FROM stage_wave sw WHERE sw.stage_id = s.stage_id AND sw.wave_number = 4
  );
