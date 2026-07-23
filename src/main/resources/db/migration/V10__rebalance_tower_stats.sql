-- Establish clear price tiers: higher-cost towers gain both total damage
-- output and range, with a small increase in damage-per-gold efficiency.
UPDATE tower
SET damage = 10,
    attack_speed = 1.8,
    attack_range = 115,
    cost = 80
WHERE tower_name = '脈衝砲塔';

UPDATE tower
SET damage = 26,
    attack_speed = 1.2,
    attack_range = 145,
    cost = 120
WHERE tower_name = '量子砲塔';

UPDATE tower
SET damage = 60,
    attack_speed = 0.7,
    attack_range = 180,
    cost = 160
WHERE tower_name = '磁軌砲塔';
