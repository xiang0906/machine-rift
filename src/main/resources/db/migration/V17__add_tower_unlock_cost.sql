ALTER TABLE tower
  ADD COLUMN unlock_cost INT NOT NULL DEFAULT 0;

UPDATE tower
SET unlock_cost = CASE tower_name
  WHEN '脈衝砲塔' THEN 0
  WHEN '離子機槍塔' THEN 250
  WHEN '量子砲塔' THEN 450
  WHEN '電弧砲塔' THEN 650
  WHEN '磁軌砲塔' THEN 900
  WHEN '裂隙重砲塔' THEN 1200
  ELSE cost * 5
END;
