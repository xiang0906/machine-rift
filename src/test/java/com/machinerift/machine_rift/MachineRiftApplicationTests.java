package com.machinerift.machine_rift;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

	@Test
	void contextLoads() {
	}

	@Test
	void flywaySeedsPlayableContent() {
		assertEquals(3, stageRepository.count());
		assertEquals(3, towerRepository.count());
		assertEquals(3, enemyRepository.count());
		assertEquals(20, stagePathRepository.count());
		assertEquals(9, stageWaveRepository.count());
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
	void openApiDefinitionIsAvailable() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openapi").exists());
	}

}
