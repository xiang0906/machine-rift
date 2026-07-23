-- Preserve the oldest player name and disambiguate older anonymous duplicates
-- without deleting their progress or game records.
UPDATE player
SET player_name = CONCAT(SUBSTRING(player_name, 1, 80), ' #', player_id)
WHERE player_id IN (
  SELECT duplicate_player_id
  FROM (
    SELECT player_id AS duplicate_player_id,
           ROW_NUMBER() OVER (
             PARTITION BY LOWER(TRIM(player_name))
             ORDER BY player_id
           ) AS name_order
    FROM player
  ) ranked_names
  WHERE name_order > 1
);

CREATE UNIQUE INDEX uk_player_player_name ON player (player_name);
