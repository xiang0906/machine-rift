-- Increase the movement pace of every enemy except the already fast drone.
UPDATE enemy SET speed = 72.0 WHERE enemy_name = '偵察機';
UPDATE enemy SET speed = 55.0 WHERE enemy_name = '裝甲機';
UPDATE enemy SET speed = 50.0 WHERE enemy_name = '護盾機兵';
UPDATE enemy SET speed = 48.0 WHERE enemy_name = '裂隙核心';
UPDATE enemy SET speed = 39.0 WHERE enemy_name = '裂隙巨像';
