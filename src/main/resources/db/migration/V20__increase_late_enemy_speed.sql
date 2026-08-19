-- Keep the early tutorial enemies unchanged while reducing the sluggish feel
-- of the durable enemy types used in later waves.
UPDATE enemy SET speed = 40.0 WHERE enemy_name = '護盾機兵';
UPDATE enemy SET speed = 38.0 WHERE enemy_name = '裂隙核心';
UPDATE enemy SET speed = 29.0 WHERE enemy_name = '裂隙巨像';
