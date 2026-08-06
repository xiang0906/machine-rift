package com.machinerift.machine_rift;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.machinerift.machine_rift.repository.EnemyRepository;
import com.machinerift.machine_rift.repository.StagePathRepository;
import com.machinerift.machine_rift.repository.StageRepository;
import com.machinerift.machine_rift.repository.StageWaveRepository;
import com.machinerift.machine_rift.repository.TowerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
class MachineRiftApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private StageRepository stageRepository;

	@Autowired
	private TowerRepository towerRepository;

	@Autowired
	private EnemyRepository enemyRepository;

	@Autowired
	private StagePathRepository stagePathRepository;

	@Autowired
	private StageWaveRepository stageWaveRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
	}

	@Test
	void databaseSchemaUsesNineBusinessTables() {
		Set<String> businessTables = Set.copyOf(jdbcTemplate.queryForList("""
				SELECT LOWER(TABLE_NAME)
				FROM INFORMATION_SCHEMA.TABLES
				WHERE TABLE_SCHEMA = 'PUBLIC'
				  AND TABLE_TYPE = 'BASE TABLE'
				  AND LOWER(TABLE_NAME) <> 'flyway_schema_history'
				""", String.class));

		assertEquals(Set.of(
				"player",
				"player_stage_progress",
				"player_tower_unlock",
				"stage",
				"stage_path",
				"stage_wave",
				"enemy",
				"tower",
				"game_record"), businessTables);
	}

	@Test
	@Transactional
	void flywaySeedsPlayableContent() {
		assertEquals(6, stageRepository.count());
		assertEquals(6, towerRepository.count());
		assertEquals(6, enemyRepository.count());
		assertEquals(44, stagePathRepository.count());
		assertEquals(26, stageWaveRepository.count());
		assertEquals(Set.of("偵察機", "裝甲機", "裂隙核心", "疾風無人機", "護盾機兵", "裂隙巨像"),
				stageWaveRepository.findAll().stream()
						.map(wave -> wave.getEnemy().getEnemyName())
						.collect(Collectors.toSet()));

		var routeSignatures = stageRepository.findAllByOrderByStageIdAsc().stream()
				.map(stage -> stage.getPath().stream()
						.map(point -> point.getGridCol() + "," + point.getGridRow())
						.collect(Collectors.joining("->")))
				.collect(Collectors.toSet());
		assertEquals(6, routeSignatures.size());
	}

	@Test
	void towerPriceTiersIncreaseDamageOutputAndRange() {
		var towers = towerRepository.findAllByOrderByCostAscTowerIdAsc();
		assertEquals(List.of(80, 100, 120, 140, 160, 200),
				towers.stream().map(tower -> tower.getCost()).toList());
		assertEquals(List.of("脈衝砲塔", "離子機槍塔", "量子砲塔", "電弧砲塔", "磁軌砲塔", "裂隙重砲塔"),
				towers.stream().map(tower -> tower.getTowerName()).toList());
		for (int index = 1; index < towers.size(); index++) {
			var previous = towers.get(index - 1);
			var current = towers.get(index);
			assertTrue(previous.getDamage() * previous.getAttackSpeed()
					< current.getDamage() * current.getAttackSpeed());
			assertTrue(previous.getAttackRange() < current.getAttackRange());
		}
	}

	@Test
	void stageApiReturnsDatabaseDrivenPathAndWaves() throws Exception {
		mockMvc.perform(get("/api/stages"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("已取得關卡列表"))
				.andExpect(jsonPath("$.data[0].path.length()").value(4))
				.andExpect(jsonPath("$.data[0].path[0].pointOrder").value(1))
				.andExpect(jsonPath("$.data[0].path[0].gridCol").value(0))
				.andExpect(jsonPath("$.data[0].path[0].gridRow").value(2))
				.andExpect(jsonPath("$.data[0].waves.length()").value(2))
				.andExpect(jsonPath("$.data[0].waves[0].enemy.enemyName").value("偵察機"))
				.andExpect(jsonPath("$.data[0].waves[0].enemy.health").value(40))
				.andExpect(jsonPath("$.data[0].waves[0].spawnIntervalMs").value(900));

		mockMvc.perform(get("/api/towers"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("已取得防禦塔列表"))
				.andExpect(jsonPath("$.data[0].unlockCost").value(0))
				.andExpect(jsonPath("$.data[1].unlockCost").value(250));
	}

	@Test
	void loginRestoresProgressPersonalBestAndUnlocks() throws Exception {
		String registerResponse = mockMvc.perform(post("/api/auth/register")
						.contentType("application/json")
						.content("""
								{
								  "username":"progress.tester+01@example!",
								  "password":"strong-password",
								  "playerName":"Progress Tester"
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.message").value("帳號建立成功"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		JsonNode registerJson = objectMapper.readTree(registerResponse);
		String accessToken = registerJson.path("data").path("accessToken").asText();

		mockMvc.perform(get("/api/auth/me")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("已取得目前玩家資料"));

		mockMvc.perform(get("/api/rankings"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("請先登入"));

		mockMvc.perform(get("/api/rankings")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/auth/register")
						.contentType("application/json")
						.content("""
								{
								  "username":"another_progress_tester",
								  "password":"another-strong-password",
								  "playerName":"progress tester"
								}
								"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("此玩家名稱已被使用"));

		mockMvc.perform(get("/api/players/me/progress")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.experience").value(0))
				.andExpect(jsonPath("$.data.stages[0].unlocked").value(true))
				.andExpect(jsonPath("$.data.stages[1].unlocked").value(false))
				.andExpect(jsonPath("$.data.unlockedTowers.length()").value(1));

		mockMvc.perform(post("/api/game-records")
						.header("Authorization", "Bearer " + accessToken)
						.contentType("application/json")
						.content("""
								{
								  "stageId": 1,
								  "score": 1200,
								  "result": "WIN",
								  "playTime": 50
								}
								"""))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/rankings")
						.param("stageId", "1")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.participantCount").value(1))
				.andExpect(jsonPath("$.data.totalGameCount").value(1))
				.andExpect(jsonPath("$.data.entries[0].stageName").value("裂隙前線"))
				.andExpect(jsonPath("$.data.entries[0].score").value(1200));

		mockMvc.perform(get("/api/rankings")
						.param("stageId", "999")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("找不到指定的關卡，ID：999"));

		String towerListResponse = mockMvc.perform(get("/api/towers"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		long purchasableTowerId = objectMapper.readTree(towerListResponse)
				.path("data").path(1).path("towerId").asLong();
		String towerUnlockPath = "/api/towers/" + purchasableTowerId + "/unlock";

		mockMvc.perform(post(towerUnlockPath)
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("永久戰備金不足，需要 250 G"));

		for (int repeat = 0; repeat < 2; repeat++) {
			mockMvc.perform(post("/api/game-records")
							.header("Authorization", "Bearer " + accessToken)
							.contentType("application/json")
							.content("""
									{
									  "stageId": 1,
									  "score": 0,
									  "result": "WIN",
									  "playTime": 70
									}
									"""))
					.andExpect(status().isCreated());
		}

		mockMvc.perform(post(towerUnlockPath))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post(towerUnlockPath)
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.message").value("防禦塔解鎖成功"))
				.andExpect(jsonPath("$.data.towerName").value("離子機槍塔"))
				.andExpect(jsonPath("$.data.unlockCost").value(250))
				.andExpect(jsonPath("$.data.remainingGold").value(50));

		mockMvc.perform(post(towerUnlockPath)
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("此防禦塔已經解鎖"));

		mockMvc.perform(post("/api/auth/logout")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("登出成功"));

		mockMvc.perform(get("/api/players/me/progress")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isUnauthorized());

		String loginResponse = mockMvc.perform(post("/api/auth/login")
						.contentType("application/json")
						.content("""
								{
								  "username":"PROGRESS.TESTER+01@EXAMPLE!",
								  "password":"strong-password"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("登入成功"))
				.andReturn()
				.getResponse()
				.getContentAsString();
		String restoredToken = objectMapper.readTree(loginResponse)
				.path("data").path("accessToken").asText();

		mockMvc.perform(get("/api/players/me/progress")
						.header("Authorization", "Bearer " + restoredToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.level").value(2))
				.andExpect(jsonPath("$.data.experience").value(1200))
				.andExpect(jsonPath("$.data.gold").value(50))
				.andExpect(jsonPath("$.data.completedStages").value(1))
				.andExpect(jsonPath("$.data.stages[0].bestScore").value(1200))
				.andExpect(jsonPath("$.data.stages[0].bestPlayTime").value(50))
				.andExpect(jsonPath("$.data.stages[1].unlocked").value(true))
				.andExpect(jsonPath("$.data.unlockedTowers.length()").value(2));

		String replacementLoginResponse = mockMvc.perform(post("/api/auth/login")
						.contentType("application/json")
						.content("""
								{
								  "username":"progress.tester+01@example!",
								  "password":"strong-password"
								}
								"""))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		String replacementToken = objectMapper.readTree(replacementLoginResponse)
				.path("data").path("accessToken").asText();

		mockMvc.perform(get("/api/players/me/progress")
						.header("Authorization", "Bearer " + restoredToken))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(get("/api/players/me/progress")
						.header("Authorization", "Bearer " + replacementToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.experience").value(1200));
	}

	@Test
	void openApiDefinitionIsAvailable() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openapi").exists());
	}

}
