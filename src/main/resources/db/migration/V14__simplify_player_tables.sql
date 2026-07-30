-- Consolidate aggregate progress, the latest login session, and sequential
-- tower unlock state into player so the MVP uses eight business tables.
ALTER TABLE player ADD COLUMN experience INT NOT NULL DEFAULT 0;
ALTER TABLE player ADD COLUMN gold INT NOT NULL DEFAULT 0;
ALTER TABLE player ADD COLUMN completed_stages INT NOT NULL DEFAULT 0;
ALTER TABLE player ADD COLUMN unlocked_tower_count INT NOT NULL DEFAULT 0;
ALTER TABLE player ADD COLUMN session_token_hash VARCHAR(64) NULL;
ALTER TABLE player ADD COLUMN session_expires_at TIMESTAMP NULL;
ALTER TABLE player ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Preserve aggregate player progress.
UPDATE player p
SET experience = COALESCE((
      SELECT pp.experience
      FROM player_progress pp
      WHERE pp.player_id = p.player_id
    ), 0),
    gold = COALESCE((
      SELECT pp.gold
      FROM player_progress pp
      WHERE pp.player_id = p.player_id
    ), 0),
    completed_stages = COALESCE((
      SELECT pp.completed_stages
      FROM player_progress pp
      WHERE pp.player_id = p.player_id
    ), 0),
    updated_at = COALESCE((
      SELECT pp.updated_at
      FROM player_progress pp
      WHERE pp.player_id = p.player_id
    ), CURRENT_TIMESTAMP);

-- Current unlock rules always grant towers from cheapest to most expensive,
-- so the number of unlocked towers is sufficient to reproduce the same list.
UPDATE player p
SET unlocked_tower_count = (
  SELECT COUNT(*)
  FROM player_tower_unlock ptu
  WHERE ptu.player_id = p.player_id
);

-- The simplified MVP supports one active session per player. Preserve the
-- newest existing session so most currently logged-in players stay signed in.
UPDATE player p
SET session_token_hash = (
      SELECT ps.token_hash
      FROM player_session ps
      WHERE ps.player_id = p.player_id
      ORDER BY ps.created_at DESC, ps.player_session_id DESC
      LIMIT 1
    ),
    session_expires_at = (
      SELECT ps.expires_at
      FROM player_session ps
      WHERE ps.player_id = p.player_id
      ORDER BY ps.created_at DESC, ps.player_session_id DESC
      LIMIT 1
    );

CREATE UNIQUE INDEX uk_player_session_token_hash
  ON player (session_token_hash);

DROP TABLE player_session;
DROP TABLE player_tower_unlock;
DROP TABLE player_progress;
