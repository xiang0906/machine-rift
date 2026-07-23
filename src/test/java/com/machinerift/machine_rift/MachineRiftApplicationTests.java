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

	@Test
	void contextLoads() {
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
		mockMvc.perform(get("/api/stages/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.path.length()").value(4))
				.andExpect(jsonPath("$.data.path[0].pointOrder").value(1))
				.andExpect(jsonPath("$.data.path[0].gridCol").value(0))
				.andExpect(jsonPath("$.data.path[0].gridRow").value(2))
				.andExpect(jsonPath("$.data.waves.length()").value(2))
				.andExpect(jsonPath("$.data.waves[0].enemy.enemyName").value("偵察機"))
				.andExpect(jsonPath("$.data.waves[0].enemy.health").value(40))
				.andExpect(jsonPath("$.data.waves[0].spawnIntervalMs").value(900));
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
				.andReturn()
				.getResponse()
				.getContentAsString();
		JsonNode registerJson = objectMapper.readTree(registerResponse);
		long playerId = registerJson.path("data").path("player").path("playerId").asLong();
		String accessToken = registerJson.path("data").path("accessToken").asText();

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

		mockMvc.perform(get("/api/players/{id}/progress", playerId)
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
								  "playerId": %d,
								  "stageId": 1,
								  "score": 1200,
								  "result": "WIN",
								  "playTime": 50
								}
								""".formatted(playerId)))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/logout")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/players/{id}/progress", playerId)
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
				.andReturn()
				.getResponse()
				.getContentAsString();
		String restoredToken = objectMapper.readTree(loginResponse)
				.path("data").path("accessToken").asText();

		mockMvc.perform(get("/api/players/{id}/progress", playerId)
						.header("Authorization", "Bearer " + restoredToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.level").value(2))
				.andExpect(jsonPath("$.data.experience").value(1200))
				.andExpect(jsonPath("$.data.gold").value(100))
				.andExpect(jsonPath("$.data.completedStages").value(1))
				.andExpect(jsonPath("$.data.stages[0].bestScore").value(1200))
				.andExpect(jsonPath("$.data.stages[0].bestPlayTime").value(50))
				.andExpect(jsonPath("$.data.stages[1].unlocked").value(true))
				.andExpect(jsonPath("$.data.unlockedTowers.length()").value(2));
	}

	@Test
	void openApiDefinitionIsAvailable() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openapi").exists());
	}

}
