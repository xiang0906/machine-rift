CREATE TABLE IF NOT EXISTS player (
  player_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  player_name VARCHAR(100) NOT NULL,
  level INT NOT NULL,
  created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS stage (
  stage_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  stage_name VARCHAR(100) NOT NULL,
  difficulty VARCHAR(50) NOT NULL,
  reward_gold INT NOT NULL,
  enemy_count INT NOT NULL
);

CREATE TABLE IF NOT EXISTS tower (
  tower_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tower_name VARCHAR(100) NOT NULL,
  tower_type VARCHAR(50) NOT NULL,
  damage INT NOT NULL,
  attack_speed DOUBLE NOT NULL,
  attack_range INT NOT NULL,
  cost INT NOT NULL
);

CREATE TABLE IF NOT EXISTS game_record (
  record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  player_id BIGINT NOT NULL,
  stage_id BIGINT NOT NULL,
  score INT NOT NULL,
  result VARCHAR(20) NOT NULL,
  play_time INT NOT NULL,
  CONSTRAINT fk_game_record_player FOREIGN KEY (player_id) REFERENCES player (player_id) ON DELETE RESTRICT,
  CONSTRAINT fk_game_record_stage FOREIGN KEY (stage_id) REFERENCES stage (stage_id) ON DELETE RESTRICT
);
