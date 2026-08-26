# Machine Rift 專案檔案導覽

> 更新日期：2026-08-26
> 對應版本：Spring Boot 3.5.16、Flyway V1～V21、39 項自動測試

這份文件說明每個檔案負責什麼、功能如何跨檔案運作，以及修改需求時應先找哪裡。

```text
瀏覽器 HTML / CSS / JavaScript / Canvas
  → REST API → Controller → Service → Repository → Entity → MySQL
```

## 1. 根目錄與文件

| 路徑 | 功用 |
| --- | --- |
| `README.md` | GitHub 專案首頁；提供功能摘要、環境、啟動、API 與 Demo 說明。GitHub Pages 根網址會將它轉成網頁。 |
| `docs/project-status.md` | 最新系統現況：功能、架構、資料庫、API、測試及限制。 |
| `docs/file-guide.md` | 本文件；程式模組、檔案與修改入口導覽。 |
| `project-report/index.html` | 四頁互動式專案報告入口，不參與 Spring Boot 執行。 |
| `project-report/report.css` | 報告專用版面、卡片、圖表與響應式樣式。 |
| `project-report/report.js` | 報告頁籤切換及互動。 |
| `pom.xml` | Maven 設定與 Spring Boot、JPA、Flyway、MySQL、Swagger、H2、測試依賴。 |
| `mvnw`／`mvnw.cmd` | macOS／Linux 與 Windows 的 Maven Wrapper。 |
| `.mvn/` | Maven Wrapper 版本與啟動所需檔案。 |
| `.env.example` | 本機環境變數範例。 |
| `.env` | 本機資料庫密碼；已忽略，不可提交。 |
| `.gitignore` | 排除 `.env`、`target/` 及本機工具產物。 |
| `.github/` | GitHub 與工具設定；不參與遊戲執行。 |
| `tools/` | 專案輔助腳本；不屬於正式遊戲程式。 |
| `target/` | Maven 編譯、測試與打包產物，可重新產生。 |

## 2. `src` 總覽

```text
src/
├─ main/
│  ├─ java/com/machinerift/machine_rift/
│  │  ├─ config/       共用設定
│  │  ├─ controller/   5 個 API Controller
│  │  ├─ dto/          19 個 API 契約
│  │  ├─ entity/       9 個資料表映射
│  │  ├─ exception/    4 個例外與全域處理
│  │  ├─ mapper/       4 個 Entity/DTO 轉換器
│  │  ├─ repository/   9 個資料存取介面
│  │  ├─ service/      5 個核心業務模組
│  │  └─ MachineRiftApplication.java
│  └─ resources/
│     ├─ db/migration/ V1～V21
│     ├─ static/       遊戲前端
│     └─ application.properties
└─ test/
   ├─ java/            8 個測試類別、39 項測試
   └─ resources/       H2 測試設定
```

## 3. Java 入口與設定

位置：`src/main/java/com/machinerift/machine_rift/`

| 檔案 | 功用 |
| --- | --- |
| `MachineRiftApplication.java` | Spring Boot 主入口；啟動元件掃描、Web、JPA 及 Flyway。 |
| `config/PasswordConfig.java` | 建立 BCrypt `PasswordEncoder`，供 `AuthService` 雜湊與比對密碼。 |

## 4. Controller：11 支 API 的入口

位置：`controller/`。Controller 處理 HTTP、Header、DTO 驗證及狀態碼，主要規則交給 Service。

| 檔案 | 方法／端點 | 功用 |
| --- | --- | --- |
| `AuthController.java` | `register()` → `POST /api/auth/register` | 建立帳號並登入。 |
|  | `login()` → `POST /api/auth/login` | 驗證帳號密碼。 |
|  | `me()` → `GET /api/auth/me` | 由 Token 恢復目前玩家。 |
|  | `logout()` → `POST /api/auth/logout` | 撤銷目前 Session。 |
| `PlayerController.java` | `getPlayerProgress()` → `GET /api/players/me/progress` | 取得本人金幣、關卡及塔進度；不再提供等級／經驗值。 |
| `StageController.java` | `getAllStages()` → `GET /api/stages` | 回傳所有關卡及完整路線、波次與敵人。 |
| `TowerController.java` | `getAllTowers()` → `GET /api/towers` | 回傳塔數值、單局造價與永久價格。 |
|  | `unlockTower()` → `POST /api/towers/{towerId}/unlock` | 驗證登入玩家並購買指定塔。 |
| `GameRecordController.java` | `saveGameRecord()` → `POST /api/game-records` | 為 Token 所屬玩家儲存戰績。 |
|  | `getMyGameRecords()` → `GET /api/game-records/me` | 回傳本人最近 20 場。 |
|  | `getRankings()` → `GET /api/rankings` | 回傳全部或 `stageId` 指定關卡的前十名。 |

## 5. Service：5 個核心功能模組

位置：`service/`。

### `AuthService.java`

- `register()`：標準化帳號、檢查帳號與名稱重複、BCrypt 加密、新增玩家、初始化進度並登入。
- `login()`：比對帳號與 BCrypt 密碼，成功後更新 Session。
- `requirePlayer()`：解析 Bearer Token、SHA-256 雜湊、查詢玩家並檢查過期時間。
- `logout()`：確認 Token 後清除雜湊與期限。
- `createSession()`：產生隨機 Token，資料庫只保存雜湊，回應提供原始 Token。

### `PlayerProgressService.java`

- `initializePlayer()`：新玩家配發第一關、起始塔及對應進度列。
- `getProgress()`：組合永久戰備金、通關數、六關進度與已解鎖塔。
- `isStageUnlocked()`：判斷玩家能否進入指定關卡。
- `recordGameResult()`：勝利時更新通關次數、最佳成績、永久戰備金與下一關。
- `isBetterResult()`：分數較高，或同分但時間較短時更新個人最佳。
- `unlockNextStage()`／`unlockStage()`：完成關卡後建立或更新下一關解鎖資料。
- `unlockTower()`：初始化時建立玩家與起始塔的永久關聯。

此 Service 已不再處理 `level` 或 `experience`。

### `StageService.java`

- `getAllStages()`：依 ID 讀取六關，再用 `StageMapper` 回傳路線、波次及敵人設定。

### `TowerService.java`

- `getAllTowers()`：依單局造價與 ID 排序回傳六座塔。
- `unlockTower()`：在 transaction 中鎖定玩家、檢查塔、重複購買與永久戰備金，扣款後建立解鎖列。

### `GameRecordService.java`

- `saveGameRecord()`：檢查關卡存在且已解鎖，建立戰績並通知進度 Service 更新結果。
- `getPlayerHistory()`：回傳登入玩家最近 20 場戰績。
- `getRankings()`：可篩選關卡；每位玩家只取最佳場，依分數降冪、時間升冪，最多 10 名。

## 6. Repository：資料查詢

位置：`repository/`。這些介面直接繼承 `JpaRepository`，不需額外標示 `@Repository`。

| 檔案 | 主要用途 |
| --- | --- |
| `PlayerRepository.java` | 依 ID、帳號或 Session Token 查玩家；檢查帳號／名稱重複；購塔時鎖定玩家。 |
| `PlayerStageProgressRepository.java` | 依玩家／關卡查進度及列出玩家全部關卡進度。 |
| `PlayerTowerUnlockRepository.java` | 列出玩家持有塔、檢查重複解鎖。 |
| `StageRepository.java` | 依序讀取關卡、尋找第一關及下一關。 |
| `StagePathRepository.java` | `stage_path` 的基本 JPA 操作；主要由 Stage 關聯載入。 |
| `StageWaveRepository.java` | `stage_wave` 的基本 JPA 操作；主要由 Stage 關聯載入。 |
| `EnemyRepository.java` | `enemy` 的基本 JPA 操作。 |
| `TowerRepository.java` | 依單局造價與 ID 排序讀取塔。 |
| `GameRecordRepository.java` | 本人最近 20 場、全體或指定關卡排行榜候選資料。 |

## 7. Entity：9 張業務資料表

位置：`entity/`。

| Entity | 資料表 | 主要欄位與關聯 |
| --- | --- | --- |
| `Player.java` | `player` | 名稱、帳號、密碼雜湊、永久金幣、通關數、Session 雜湊／期限及時間。 |
| `PlayerStageProgress.java` | `player_stage_progress` | Player、Stage、解鎖、最高分、最佳時間及完成次數。 |
| `PlayerTowerUnlock.java` | `player_tower_unlock` | Player、Tower、解鎖時間。 |
| `Stage.java` | `stage` | 名稱、難度、獎勵、敵人總數及一對多 path／waves。 |
| `StagePath.java` | `stage_path` | Stage、節點順序及格線座標。 |
| `StageWave.java` | `stage_wave` | Stage、Enemy、波次、敵人數及生成間隔。 |
| `Tower.java` | `tower` | 名稱、類型、傷害、攻速、射程、單局造價及永久價格。 |
| `Enemy.java` | `enemy` | 名稱、生命、速度及擊殺金幣。 |
| `GameRecord.java` | `game_record` | Player、Stage、分數、結果、遊玩秒數及建立時間。 |

`Player` 不再包含 `level`、`experience` 或 `unlockedTowerCount`。

## 8. DTO：19 個 API 契約

位置：`dto/`。

| 分組 | 檔案 | 功用 |
| --- | --- | --- |
| 共用 | `ApiResponse.java` | 統一 success、message、data、timestamp 外層。 |
| 登入 | `AuthRegisterRequestDto.java`、`AuthLoginRequestDto.java` | 註冊／登入輸入與中文驗證。 |
| 登入 | `AuthResponseDto.java`、`PlayerResponseDto.java` | Token、期限與安全玩家摘要。 |
| 進度 | `PlayerProgressResponseDto.java` | 金幣、通關數、關卡進度與已解鎖塔。 |
| 進度 | `PlayerStageProgressResponseDto.java` | 關卡解鎖、最高分、最佳時間及完成次數。 |
| 塔 | `UnlockedTowerResponseDto.java` | 玩家持有塔的精簡 ID／名稱。 |
| 塔 | `TowerResponseDto.java` | 塔數值、單局 `cost` 與永久 `unlockCost`。 |
| 塔 | `TowerUnlockResponseDto.java` | 購買結果、剩餘金幣及解鎖時間。 |
| 關卡 | `StageResponseDto.java` | 關卡摘要及巢狀路線、波次。 |
| 關卡 | `StagePathResponseDto.java` | 路徑節點順序與格線座標。 |
| 關卡 | `StageWaveResponseDto.java` | 波次、敵人數、間隔及敵人。 |
| 關卡 | `EnemyResponseDto.java` | 敵人生命、速度與獎勵。 |
| 戰績 | `GameRecordRequestDto.java` | 關卡、分數、勝敗與遊玩時間；不接受 playerId。 |
| 戰績 | `GameRecordResponseDto.java` | 戰績新增結果。 |
| 戰績 | `GameRecordHistoryResponseDto.java` | 個人歷史的關卡、成績、時間與日期。 |
| 排行 | `RankingEntryResponseDto.java`、`RankingResponseDto.java` | 前十名項目及參賽／對局統計。 |

## 9. Mapper 與 Exception

### Mapper（`mapper/`）

| 檔案 | 功用 |
| --- | --- |
| `PlayerMapper.java` | Player → 安全玩家摘要。 |
| `StageMapper.java` | Stage → 含路線、波次與敵人的完整關卡 DTO。 |
| `TowerMapper.java` | Tower → 前端塔資料。 |
| `GameRecordMapper.java` | request → GameRecord；戰績 → 新增結果／歷史 DTO。 |

### Exception（`exception/`）

| 檔案 | 功用 |
| --- | --- |
| `AuthenticationException.java` | Token 缺少、無效、過期或帳密錯誤。 |
| `ResourceNotFoundException.java` | 玩家、關卡或塔不存在。 |
| `ResourceConflictException.java` | 重複資料、重複購買、金幣不足等業務衝突。 |
| `GlobalExceptionHandler.java` | 統一中文錯誤回應；欄位訊息為空時使用中文 fallback。 |

## 10. 設定與 Migration

位置：`src/main/resources/`。

| 路徑 | 功用 |
| --- | --- |
| `application.properties` | MySQL、JPA、Flyway、Swagger 與日誌設定。 |
| `db/migration/` | Flyway V1～V21；建立結構、搬移資料、種子內容與平衡。 |
| `static/` | Spring Boot 直接提供的遊戲前端。 |

| Migration | 功用 |
| --- | --- |
| V1～V3 | 核心表、初始關卡／塔、敵人、路線與波次。 |
| V4～V9 | 玩家進度、第一關、舊內容移除、帳號 Session、唯一名稱。 |
| V10～V13 | 塔平衡，擴充至六塔、六敵人、六關及獨立路線。 |
| V14～V17 | 收斂玩家表、戰績日期、恢復玩家塔關聯、永久價格。 |
| V18 | 六關難度與高階敵人生命調整。 |
| V19 | 移除玩家 level／experience。 |
| V20～V21 | 提高中後期敵人速度。 |

已套用的 migration 不應直接修改；下一個版本使用 `V22__描述.sql`。

## 11. 前端檔案與主要函式

位置：`src/main/resources/static/`。

### `index.html` 與 `styles.css`

- `index.html`：登入、註冊、大廳、工坊、關卡、戰績中心、遊戲 7 個主畫面，以及簡報、手機方向、結果與確認 Overlay。
- `styles.css`：科幻戰術藍圖配色、卡片、HUD、塔庫、排行、工坊、動畫、手機提示與響應式配置。

### `js/core.js`

| 函式／資料 | 功用 |
| --- | --- |
| `screens`、`SCREEN_ROUTES` | 畫面與 hash route 對照。 |
| `screenNameFromHash()`／`recordTabFromHash()` | 解析網址。 |
| `showScreen()`／`syncRouteForScreen()` | 切換畫面並同步網址。 |
| `confirmAction()` | 自訂確認 Overlay，取代原生 `confirm()`。 |
| `api()` | 統一 fetch、Bearer Token、JSON 與錯誤。 |
| `state` | 保存玩家、Token、關卡、塔、進度與選關。 |
| `enterGame()`／`loadPlayerContent()` | 登入後載入內容並進入大廳。 |
| `missionNumberForStage()` | 依關卡排序計算任務編號，不使用資料庫 ID。 |

### `js/views.js`

| 函式 | 功用 |
| --- | --- |
| `renderLobby()` | 玩家、戰備金、進度與建議行動。 |
| `renderTowerWorkshop()`／`purchaseTower()` | 工坊塔卡與購買 API。 |
| `openRecordsCenter()`／`selectRecordsTab()` | 戰績中心與頁籤。 |
| `loadHistory()` | 本人最近 20 場及日期格式化。 |
| `loadRanking()` | 全部／單關前三名與前十名。 |
| `renderStageList()` | 六關狀態、文案與個人最佳。 |
| `openMissionBriefing()` | 進場任務簡報。 |
| `completePendingDeployment()` | 手機方向提示後部署。 |
| `applyRouteFromLocation()` | hash 導航、登入保護及舊網址轉向。 |

### `js/game.js`

| 函式／區塊 | 功用 |
| --- | --- |
| `routeForStage()`／`computePathCells()`／`buildPathPoints()` | 將資料庫路徑轉成 Canvas 移動座標。 |
| `startGame()`／`stopCurrentGame()` | 建立或清除一局狀態與動畫。 |
| `sortedTowers()`／`renderTowerPanel()` | 排序並繪製已解鎖塔庫。 |
| `syncSelectedTowerButtons()`／`updateTowerAffordability()` | 選取與金幣回饋；金幣不足仍可選。 |
| `towerPlacementStatus()` | 驗證格線、路徑、重複建塔及金幣。 |
| `spawnEnemy()`／`hasEnoughSpawnClearance()` | 依波次生成並維持本體間距。 |
| `loop()` | 更新敵人、塔、投射物、波次與勝敗。 |
| `draw()` 與各 `draw*()` | 戰場、塔、砲管、敵人、血條、投射物與特效。 |
| `drawTowerPlacementPreview()`／`drawBuiltTowerRange()` | 建造前及 hover 已建塔時顯示射程。 |
| `updateHud()` | 時間、波次、生成、耐久、分數與金幣。 |
| `endGame()` | 送出戰績並顯示結果；儲存失敗不抹除結果。 |

### 其他 JavaScript

- `story.js`：`STAGE_STORIES`、`storyForStage()`、`resultStoryForStage()` 管理六關簡報與勝敗文案。
- `background.js`：繪製非戰場頁面的低亮度動態背景，尊重減少動態效果設定。

## 12. 測試檔案：39 項

位置：`src/test/`。

| 檔案 | 數量 | 驗證內容 |
| --- | ---: | --- |
| `MachineRiftApplicationTests.java` | 7 | Context、9 表、種子、塔層級、關卡 API、登入恢復、OpenAPI。 |
| `DatabaseMigrationTest.java` | 8 | V14～V21 升級與資料保留。 |
| `controller/GameRecordControllerTest.java` | 6 | Token 戰績、中文驗證、歷史、排行榜。 |
| `controller/PlayerControllerTest.java` | 5 | 本人進度及已移除 API 的 404。 |
| `service/GameRecordServiceTest.java` | 7 | 儲存、鎖關、歷史、最佳排行、關卡篩選。 |
| `service/TowerServiceTest.java` | 4 | 購買、金幣不足、重複購買、不存在塔。 |
| `repository/GameRecordRepositoryTest.java` | 1 | 排行分數與時間排序。 |
| `exception/GlobalExceptionHandlerTest.java` | 1 | 空驗證訊息中文 fallback。 |
| `test/resources/application.properties` | — | H2 MySQL mode 與測試 Flyway 設定。 |

## 13. 功能呼叫流程

### 註冊與登入

```text
index.html → core.js api() → AuthController → AuthService
→ PlayerRepository → PlayerProgressService（註冊時）→ player / 進度 / 塔解鎖表
```

### 進入關卡

```text
views.js renderStageList() → openMissionBriefing()
→ game.js startGame() → Canvas 使用 API 載入的 path / waves / enemy
```

### 工坊購塔

```text
views.js purchaseTower() → TowerController → AuthService.requirePlayer()
→ TowerService.unlockTower() → PlayerRepository 鎖定玩家 → PlayerTowerUnlockRepository
```

### 結束戰鬥

```text
game.js endGame() → GameRecordController → AuthService.requirePlayer()
→ GameRecordService.saveGameRecord() → GameRecordRepository
→ PlayerProgressService.recordGameResult() → player + player_stage_progress
```

### 歷史與排行

```text
views.js loadHistory() / loadRanking() → GameRecordController
→ GameRecordService → GameRecordRepository → DTO → 戰績中心
```

## 14. 修改需求時先看哪裡

| 想修改的內容 | 優先檔案 |
| --- | --- |
| 帳號、Token、Session | `AuthController`、`AuthService`、Auth DTO、`Player` |
| 永久金幣與關卡解鎖 | `PlayerProgressService`、`PlayerStageProgress` |
| 工坊購塔 | `TowerService`、`TowerController`、`views.js` |
| 關卡、路線、波次、敵人或塔數值 | 新增 V22 migration；不要改已套用 migration |
| 戰績儲存 | `game.js endGame()`、`GameRecordController`、`GameRecordService` |
| 個人歷史／排行榜 | `views.js`、`GameRecordService`、`GameRecordRepository` |
| 戰鬥、生成、攻擊或建造 | `game.js` |
| 頁面結構與按鈕 | `index.html`、`views.js` |
| 色彩、字級、間距、響應式 | `styles.css` |
| 任務與勝敗敘事 | `story.js` |
| API 回應欄位 | DTO、Mapper、Controller／Service，再調整前端 |
| 資料表結構 | 新 migration、Entity、Repository、Service 與 migration test |
| 專案報告 | `project-report/index.html`、`report.css`、`report.js` |

## 15. 不應直接修改或提交

- `.env`：包含本機資料庫密碼。
- `target/`：Maven 自動產物。
- `flyway_schema_history`：由 Flyway 管理。
- 已套用的 V1～V21 migration：需要變更時新增 V22。
- IDE 暫存與系統檔案。

完成修改後至少執行 `./mvnw.cmd test`；正式交付前建議執行 `./mvnw.cmd clean package`。
