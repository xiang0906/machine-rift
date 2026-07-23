-- Add stages 4-6. Each route uses a distinct ordered set of grid nodes.
INSERT INTO stage (stage_name, difficulty, reward_gold, enemy_count)
SELECT '熔火交叉口', 'Expert', 500, 30
WHERE NOT EXISTS (
  SELECT 1 FROM stage WHERE stage_name = '熔火交叉口'
);

INSERT INTO stage (stage_name, difficulty, reward_gold, enemy_count)
SELECT '量子迷城', 'Nightmare', 700, 35
WHERE NOT EXISTS (
  SELECT 1 FROM stage WHERE stage_name = '量子迷城'
);

INSERT INTO stage (stage_name, difficulty, reward_gold, enemy_count)
SELECT '機神核心', 'Inferno', 1000, 41
WHERE NOT EXISTS (
  SELECT 1 FROM stage WHERE stage_name = '機神核心'
);

-- 熔火交叉口：由地圖下方進入，向上折返後穿越中央區域。
INSERT INTO stage_path (stage_id, point_order, grid_col, grid_row)
SELECT s.stage_id, p.point_order, p.grid_col, p.grid_row
FROM stage s
JOIN (
  SELECT 1 AS point_order, 0 AS grid_col, 7 AS grid_row
  UNION ALL SELECT 2, 3, 7
  UNION ALL SELECT 3, 3, 1
  UNION ALL SELECT 4, 7, 1
  UNION ALL SELECT 5, 7, 5
  UNION ALL SELECT 6, 10, 5
  UNION ALL SELECT 7, 10, 2
  UNION ALL SELECT 8, 13, 2
) p ON 1 = 1
WHERE s.stage_name = '熔火交叉口'
  AND NOT EXISTS (
    SELECT 1 FROM stage_path sp
    WHERE sp.stage_id = s.stage_id AND sp.point_order = p.point_order
  );

-- 量子迷城：從中央進入，先繞行上緣，再從下方離開。
INSERT INTO stage_path (stage_id, point_order, grid_col, grid_row)
SELECT s.stage_id, p.point_order, p.grid_col, p.grid_row
FROM stage s
JOIN (
  SELECT 1 AS point_order, 0 AS grid_col, 4 AS grid_row
  UNION ALL SELECT 2, 2, 4
  UNION ALL SELECT 3, 2, 0
  UNION ALL SELECT 4, 11, 0
  UNION ALL SELECT 5, 11, 2
  UNION ALL SELECT 6, 4, 2
  UNION ALL SELECT 7, 4, 6
  UNION ALL SELECT 8, 13, 6
) p ON 1 = 1
WHERE s.stage_name = '量子迷城'
  AND NOT EXISTS (
    SELECT 1 FROM stage_path sp
    WHERE sp.stage_id = s.stage_id AND sp.point_order = p.point_order
  );

-- 機神核心：以三條長縱向通道組成不重疊的蛇形路線。
INSERT INTO stage_path (stage_id, point_order, grid_col, grid_row)
SELECT s.stage_id, p.point_order, p.grid_col, p.grid_row
FROM stage s
JOIN (
  SELECT 1 AS point_order, 0 AS grid_col, 0 AS grid_row
  UNION ALL SELECT 2, 3, 0
  UNION ALL SELECT 3, 3, 6
  UNION ALL SELECT 4, 6, 6
  UNION ALL SELECT 5, 6, 1
  UNION ALL SELECT 6, 9, 1
  UNION ALL SELECT 7, 9, 7
  UNION ALL SELECT 8, 13, 7
) p ON 1 = 1
WHERE s.stage_name = '機神核心'
  AND NOT EXISTS (
    SELECT 1 FROM stage_path sp
    WHERE sp.stage_id = s.stage_id AND sp.point_order = p.point_order
  );

-- Stage 4 waves: 30 enemies across five waves.
INSERT INTO stage_wave (
  stage_id, enemy_id, wave_number, enemy_count, spawn_interval_ms
)
SELECT s.stage_id, e.enemy_id, w.wave_number, w.enemy_count, w.spawn_interval_ms
FROM stage s
JOIN (
  SELECT 1 AS wave_number, '疾風無人機' AS enemy_name, 6 AS enemy_count, 650 AS spawn_interval_ms
  UNION ALL SELECT 2, '偵察機', 6, 650
  UNION ALL SELECT 3, '裝甲機', 6, 720
  UNION ALL SELECT 4, '護盾機兵', 6, 800
  UNION ALL SELECT 5, '裂隙核心', 6, 900
) w ON 1 = 1
JOIN enemy e ON e.enemy_name = w.enemy_name
WHERE s.stage_name = '熔火交叉口'
  AND NOT EXISTS (
    SELECT 1 FROM stage_wave sw
    WHERE sw.stage_id = s.stage_id AND sw.wave_number = w.wave_number
  );

-- Stage 5 waves: 35 enemies across six waves.
INSERT INTO stage_wave (
  stage_id, enemy_id, wave_number, enemy_count, spawn_interval_ms
)
SELECT s.stage_id, e.enemy_id, w.wave_number, w.enemy_count, w.spawn_interval_ms
FROM stage s
JOIN (
  SELECT 1 AS wave_number, '疾風無人機' AS enemy_name, 7 AS enemy_count, 580 AS spawn_interval_ms
  UNION ALL SELECT 2, '偵察機', 7, 620
  UNION ALL SELECT 3, '裝甲機', 7, 680
  UNION ALL SELECT 4, '護盾機兵', 6, 760
  UNION ALL SELECT 5, '裂隙核心', 5, 850
  UNION ALL SELECT 6, '裂隙巨像', 3, 1100
) w ON 1 = 1
JOIN enemy e ON e.enemy_name = w.enemy_name
WHERE s.stage_name = '量子迷城'
  AND NOT EXISTS (
    SELECT 1 FROM stage_wave sw
    WHERE sw.stage_id = s.stage_id AND sw.wave_number = w.wave_number
  );

-- Stage 6 waves: 41 enemies across six waves.
INSERT INTO stage_wave (
  stage_id, enemy_id, wave_number, enemy_count, spawn_interval_ms
)
SELECT s.stage_id, e.enemy_id, w.wave_number, w.enemy_count, w.spawn_interval_ms
FROM stage s
JOIN (
  SELECT 1 AS wave_number, '疾風無人機' AS enemy_name, 8 AS enemy_count, 500 AS spawn_interval_ms
  UNION ALL SELECT 2, '偵察機', 8, 540
  UNION ALL SELECT 3, '裝甲機', 8, 620
  UNION ALL SELECT 4, '護盾機兵', 7, 700
  UNION ALL SELECT 5, '裂隙核心', 6, 780
  UNION ALL SELECT 6, '裂隙巨像', 4, 1000
) w ON 1 = 1
JOIN enemy e ON e.enemy_name = w.enemy_name
WHERE s.stage_name = '機神核心'
  AND NOT EXISTS (
    SELECT 1 FROM stage_wave sw
    WHERE sw.stage_id = s.stage_id AND sw.wave_number = w.wave_number
  );
