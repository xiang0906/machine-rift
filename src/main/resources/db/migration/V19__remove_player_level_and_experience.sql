-- Level and experience were derived display-only values and did not affect
-- stages, towers, rewards or rankings. Keep the player aggregate focused on
-- persistent gameplay state that has an actual use.
ALTER TABLE player DROP COLUMN level;
ALTER TABLE player DROP COLUMN experience;
