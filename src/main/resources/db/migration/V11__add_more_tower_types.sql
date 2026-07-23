-- Add three additional tower choices while preserving a clear progression:
-- every higher price tier provides more sustained damage and greater range.
INSERT INTO tower (
  tower_name, tower_type, damage, attack_speed, attack_range, cost
)
SELECT '離子機槍塔', 'GATLING', 8, 3.0, 125, 100
WHERE NOT EXISTS (
  SELECT 1 FROM tower WHERE tower_name = '離子機槍塔'
);

INSERT INTO tower (
  tower_name, tower_type, damage, attack_speed, attack_range, cost
)
SELECT '電弧砲塔', 'ARC', 32, 1.1, 160, 140
WHERE NOT EXISTS (
  SELECT 1 FROM tower WHERE tower_name = '電弧砲塔'
);

INSERT INTO tower (
  tower_name, tower_type, damage, attack_speed, attack_range, cost
)
SELECT '裂隙重砲塔', 'SIEGE', 100, 0.5, 210, 200
WHERE NOT EXISTS (
  SELECT 1 FROM tower WHERE tower_name = '裂隙重砲塔'
);
