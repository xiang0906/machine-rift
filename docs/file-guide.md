# Machine Rift 專案檔案導覽

這份文件用來回答兩個問題：

1. 專案中的各個檔案負責什麼？
2. 想修改某項功能時，應該從哪些檔案開始看？

目前專案採用 Spring Boot 分層架構，前端則是由 HTML、CSS、Vanilla JavaScript 與
Canvas 組成的單頁應用程式。主要資料流如下：

```text
瀏覽器畫面
  → JavaScript 呼叫 API
  → Controller 接收請求
  → Service 執行規則
  → Repository 存取資料
  → Entity 對應 MySQL 資料表
```

## 1. 專案根目錄

| 檔案或目錄 | 功用 |
| --- | --- |
| `README.md` | 專案首頁，包含技術棧、啟動方法、API、遊玩流程與目前功能摘要。 |
| `docs/project-status.md` | 完整系統現況，說明架構、資料表、API、前端功能、測試與已知限制。 |
| `docs/file-guide.md` | 本文件，負責程式碼與檔案導覽。 |
| `pom.xml` | Maven 專案設定；管理 Spring Boot、JPA、MySQL、Flyway、Swagger、H2、測試等依賴。 |
| `mvnw` | Linux／macOS 使用的 Maven Wrapper 啟動腳本。 |
| `mvnw.cmd` | Windows 使用的 Maven Wrapper 啟動腳本。 |
| `.mvn/wrapper/maven-wrapper.properties` | Maven Wrapper 版本與下載來源設定。 |
| `.env.example` | 本機環境變數範例；複製成 `.env` 後填入 MySQL 密碼。 |
| `.env` | 本機資料庫密碼，已由 Git 忽略，不應提交到 GitHub。 |
| `.gitignore` | 指定 `.env`、`target/`、IDE 設定等不應提交的檔案。 |
| `.gitattributes` | Git 的文字檔與行尾處理設定。 |
| `.github/modernize/` | Java 升級輔助工具留下的設定，不參與遊戲執行流程。 |
| `target/` | Maven 編譯、測試與打包產物，可刪除後重新建置，不是原始碼。 |

## 2. Java 後端入口

Java 原始碼根目錄：

```text
src/main/java/com/machinerift/machine_rift/
```

| 檔案 | 功用 |
| --- | --- |
| `MachineRiftApplication.java` | Spring Boot 主程式入口。啟動後掃描 Controller、Service、Repository、Entity 與設定類別。 |
| `config/PasswordConfig.java` | 建立 BCrypt `PasswordEncoder`，供註冊與登入時加密、比對密碼。 |

## 3. Controller：API 入口

位置：`src/main/java/com/machinerift/machine_rift/controller/`

Controller 只負責接收 HTTP 請求、讀取 Token、驗證 DTO，並呼叫 Service；遊戲規則不應直接寫在這一層。

| 檔案 | 負責端點與功能 |
| --- | --- |
| `AuthController.java` | `POST /api/auth/register`、`POST /api/auth/login`、`GET /api/auth/me`、`POST /api/auth/logout`。 |
| `PlayerController.java` | `GET /api/players/me/progress`，取得 Token 所屬玩家的等級、金幣、關卡與塔解鎖進度。 |
| `StageController.java` | `GET /api/stages`，回傳所有關卡、路線、波次與敵人設定。 |
| `TowerController.java` | `GET /api/towers` 取得塔資料；`POST /api/towers/{towerId}/unlock` 永久解鎖防禦塔。 |
| `GameRecordController.java` | 儲存本人戰績、取得個人最近 20 場紀錄，以及查詢全部或指定關卡排行榜。 |

## 4. Service：核心業務規則

位置：`src/main/java/com/machinerift/machine_rift/service/`

如果要理解「系統實際怎麼判斷」，通常先看這一層。

| 檔案 | 功用 |
| --- | --- |
| `AuthService.java` | 註冊、帳號與玩家名稱重複檢查、BCrypt 密碼驗證、Session Token 建立／雜湊／過期驗證與登出。 |
| `PlayerProgressService.java` | 初始化新玩家、配發第一關與起始塔、讀取玩家進度、判斷關卡是否解鎖，以及勝敗後更新經驗、等級、永久戰備金、下一關與最佳戰績。 |
| `StageService.java` | 依順序讀取所有關卡，交由 `StageMapper` 組成前端可使用的完整關卡資料。 |
| `TowerService.java` | 讀取塔清單；以交易及玩家資料鎖處理解鎖購買、重複購買與金幣不足。 |
| `GameRecordService.java` | 儲存遊戲結果、取得個人歷史，以及計算全部關卡或指定關卡的每位玩家最佳成績與前十名。 |

## 5. Repository：資料庫查詢

位置：`src/main/java/com/machinerift/machine_rift/repository/`

Repository 使用 Spring Data JPA。多數方法名稱本身就是查詢條件，不需要手寫 SQL。

| 檔案 | 功用 |
| --- | --- |
| `PlayerRepository.java` | 依帳號或 Session Token 查玩家、檢查帳號／名稱重複；購塔時以 pessimistic lock 鎖定玩家。 |
| `PlayerStageProgressRepository.java` | 查詢玩家每關解鎖狀態、完成次數、最佳分數與最佳時間。 |
| `PlayerTowerUnlockRepository.java` | 查詢玩家永久持有的塔，以及判斷是否已購買指定塔。 |
| `StageRepository.java` | 依關卡順序讀取、取得第一關或下一關。 |
| `StagePathRepository.java` | 對應關卡路徑節點資料的基本 CRUD。 |
| `StageWaveRepository.java` | 對應關卡波次資料的基本 CRUD。 |
| `EnemyRepository.java` | 對應敵人資料的基本 CRUD。 |
| `TowerRepository.java` | 依戰場造價與 ID 排序讀取防禦塔。 |
| `GameRecordRepository.java` | 查詢個人最近 20 場、全關卡排行及指定關卡排行。 |

## 6. Entity：資料表對應

位置：`src/main/java/com/machinerift/machine_rift/entity/`

每個 Entity 代表一張主要業務資料表或關聯表。

| 檔案 | 對應資料與責任 |
| --- | --- |
| `Player.java` | `player`：帳號、密碼雜湊、玩家名稱、等級、經驗、永久戰備金、通關數與 Session。 |
| `PlayerStageProgress.java` | `player_stage_progress`：玩家與關卡的解鎖狀態、完成次數、最佳分數及最佳時間。 |
| `PlayerTowerUnlock.java` | `player_tower_unlock`：玩家永久解鎖的防禦塔與解鎖時間。 |
| `Stage.java` | `stage`：關卡名稱、難度、獎勵金幣、敵人總數，並關聯路徑與波次。 |
| `StagePath.java` | `stage_path`：路徑節點順序與 Canvas 格線座標。 |
| `StageWave.java` | `stage_wave`：波次編號、敵人種類、數量與生成間隔。 |
| `Enemy.java` | `enemy`：敵人名稱、生命、速度與擊殺獎勵。 |
| `Tower.java` | `tower`：塔名稱、類型、傷害、攻速、射程、單局造價與永久解鎖價格。 |
| `GameRecord.java` | `game_record`：每一局的玩家、關卡、分數、結果、遊戲時間與建立日期。 |

## 7. DTO：API 請求與回應格式

位置：`src/main/java/com/machinerift/machine_rift/dto/`

DTO 是前後端的資料契約。Entity 不直接回傳給前端，避免公開不必要欄位與資料庫關聯。

### 共用與登入

| 檔案 | 功用 |
| --- | --- |
| `ApiResponse.java` | 統一 API 格式：`success`、`message`、`data`。 |
| `AuthRegisterRequestDto.java` | 註冊輸入：顯示名稱、帳號、密碼及中文驗證訊息。 |
| `AuthLoginRequestDto.java` | 登入輸入：帳號與密碼。 |
| `AuthResponseDto.java` | 註冊／登入成功後回傳 Token 與玩家資料。 |
| `PlayerResponseDto.java` | 對外公開的玩家基本資料，不包含密碼或 Session 雜湊。 |

### 玩家進度

| 檔案 | 功用 |
| --- | --- |
| `PlayerProgressResponseDto.java` | 玩家總進度、永久戰備金、關卡進度及已解鎖塔。 |
| `PlayerStageProgressResponseDto.java` | 單一關卡的解鎖、完成次數與個人最佳。 |
| `UnlockedTowerResponseDto.java` | 玩家已永久解鎖的塔摘要。 |

### 關卡與遊戲內容

| 檔案 | 功用 |
| --- | --- |
| `StageResponseDto.java` | 完整關卡資料，包含路徑與波次。 |
| `StagePathResponseDto.java` | 單一路徑節點的順序與座標。 |
| `StageWaveResponseDto.java` | 單一波次的敵人、數量及生成間隔。 |
| `EnemyResponseDto.java` | 波次中使用的敵人生命、速度與獎勵。 |
| `TowerResponseDto.java` | 塔的戰鬥數值、單局造價及永久解鎖價格。 |
| `TowerUnlockResponseDto.java` | 購塔成功後回傳塔、花費與剩餘戰備金。 |

### 戰績與排行榜

| 檔案 | 功用 |
| --- | --- |
| `GameRecordRequestDto.java` | 遊戲結束時送出的關卡、分數、勝敗與遊戲時間；玩家身分由 Token 決定。 |
| `GameRecordResponseDto.java` | 戰績成功寫入後的回應。 |
| `GameRecordHistoryResponseDto.java` | 個人歷史列出的關卡、結果、分數、時間與日期。 |
| `RankingEntryResponseDto.java` | 排行榜單一名次的玩家、關卡、分數、時間與結果。 |
| `RankingResponseDto.java` | 排行榜統計與最多十筆名次。 |

## 8. Mapper：Entity 與 DTO 轉換

位置：`src/main/java/com/machinerift/machine_rift/mapper/`

| 檔案 | 功用 |
| --- | --- |
| `PlayerMapper.java` | `Player` 轉成安全的玩家回應 DTO。 |
| `StageMapper.java` | 將關卡、路徑、波次與敵人組成巢狀的 `StageResponseDto`。 |
| `TowerMapper.java` | `Tower` 轉成包含解鎖價格的塔回應 DTO。 |
| `GameRecordMapper.java` | 遊戲結果 DTO、`GameRecord` Entity、歷史回應之間的轉換。 |

## 9. 例外與中文錯誤處理

位置：`src/main/java/com/machinerift/machine_rift/exception/`

| 檔案 | 功用 |
| --- | --- |
| `AuthenticationException.java` | 未登入、Token 無效或過期。 |
| `ResourceNotFoundException.java` | 找不到指定玩家、關卡或防禦塔。 |
| `ResourceConflictException.java` | 名稱重複、關卡未解鎖、金幣不足或重複購塔等業務衝突。 |
| `GlobalExceptionHandler.java` | 將驗證、404、409、401 與未預期例外統一轉成中文 `ApiResponse`。 |

## 10. 後端設定與資料庫 Migration

### 設定檔

| 檔案 | 功用 |
| --- | --- |
| `src/main/resources/application.properties` | MySQL、JPA、Flyway、UTF-8、Swagger 與 `.env` 載入設定。 |
| `src/test/resources/application.properties` | 測試時改用記憶體 H2（MySQL mode），避免動到本機 MySQL。 |

### Flyway Migration

位置：`src/main/resources/db/migration/`

Flyway 會依版本從 V1 執行到 V17。已執行過的 migration 不應直接修改；新的資料庫變更應新增下一版檔案。

| 檔案 | 功用 |
| --- | --- |
| `V1__create_core_schema.sql` | 建立玩家、關卡、塔與戰績的第一版核心表。 |
| `V2__seed_playable_content.sql` | 建立最初三關與三座塔的種子資料。 |
| `V3__update_seed_content.sql` | 新增敵人、關卡路徑與波次表，補上可玩的資料庫驅動內容。 |
| `V4__add_player_progress_and_unlocks.sql` | 新增玩家總進度、關卡進度與塔解鎖關聯。 |
| `V5__unlock_first_playable_stage.sql` | 修正新舊玩家的第一個可遊玩關卡解鎖狀態。 |
| `V6__remove_legacy_wave_stages.sql` | 移除早期重複的 wave 關卡與相關孤兒資料。 |
| `V7__add_player_accounts_and_sessions.sql` | 加入帳號、密碼與登入 Session 表。 |
| `V8__remove_account_column_defaults.sql` | 移除帳號與密碼欄位的臨時預設值。 |
| `V9__make_player_names_unique.sql` | 清理重複玩家名稱並加入唯一限制。 |
| `V10__rebalance_tower_stats.sql` | 重新平衡前三座塔的造價與戰鬥數值。 |
| `V11__add_more_tower_types.sql` | 將防禦塔擴充到六種。 |
| `V12__add_enemy_types_and_update_waves.sql` | 將敵人擴充到六種並調整前三關波次。 |
| `V13__add_three_stages_with_unique_routes.sql` | 新增三關及不重複路線，總關卡數達六關。 |
| `V14__simplify_player_tables.sql` | 將總進度與 Session 合併進 `player`，移除當時多餘的三張表。 |
| `V15__add_game_record_created_at.sql` | 為戰績加入建立日期與查詢索引。 |
| `V16__restore_player_tower_unlocks.sql` | 恢復玩家與塔的逐筆解鎖關聯，並搬移既有解鎖數量。 |
| `V17__add_tower_unlock_cost.sql` | 新增永久解鎖價格，與單局建造造價分開。 |

## 11. 前端檔案

位置：`src/main/resources/static/`

Spring Boot 直接提供這些靜態資源，不需要 npm 或額外前端建置工具。

| 檔案 | 功用 |
| --- | --- |
| `index.html` | 七個主要畫面的 HTML 結構：登入、註冊、大廳、工坊、關卡、戰績中心、遊戲，以及簡報、結果、確認對話框。 |
| `styles.css` | 全站視覺、響應式版面、大廳、工坊、關卡卡片、戰績中心、排行榜、遊戲 HUD 與自訂對話框樣式。 |
| `js/background.js` | 登入頁背景 Canvas 粒子與連線動畫，只負責裝飾。 |
| `js/story.js` | 世界觀、各關任務描述、任務簡報及勝敗文案。 |
| `js/core.js` | 全域狀態、hash 路由、API 包裝、Token、Session 恢復、玩家內容載入與共用確認視窗。 |
| `js/views.js` | 登入、註冊、大廳、工坊、關卡選擇、個人歷史、每關排行榜與畫面事件。 |
| `js/game.js` | Canvas 塔防引擎：路線、敵人、波次、建塔、攻擊、砲管旋轉、特效、HUD、勝敗與戰績送出。 |

JavaScript 使用傳統 `<script defer>` 依序載入，順序定義在 `index.html` 底部：

```text
background.js → story.js → core.js → game.js → views.js
```

這些檔案會共享全域函式與狀態，所以調整載入順序前必須先確認相依關係。

## 12. 測試檔案

位置：`src/test/`

| 檔案 | 功用 |
| --- | --- |
| `MachineRiftApplicationTests.java` | 完整整合測試：啟動、登入、Token、進度、戰績、購塔、排行榜及重新登入後資料保存。 |
| `DatabaseMigrationTest.java` | 驗證 V1～V17、種子數量、六條不同路線、塔平衡、資料表收斂與 migration 升級結果。 |
| `controller/GameRecordControllerTest.java` | 測試戰績、個人歷史、全部／指定關卡排行榜的 HTTP 回應與錯誤保護。 |
| `controller/PlayerControllerTest.java` | 測試 `/players/me/progress` 的 Token 身分與舊 ID 路徑停用。 |
| `service/GameRecordServiceTest.java` | 測試戰績儲存、關卡鎖定、個人歷史、最佳成績、同分排序、指定關卡及前十名。 |
| `service/TowerServiceTest.java` | 測試購塔成功、金幣不足、重複購買與找不到塔。 |
| `repository/GameRecordRepositoryTest.java` | 使用 H2 驗證排行榜的分數降冪與時間升冪查詢順序。 |
| `resources/application.properties` | 測試專用 H2 與 Flyway 設定。 |

完整驗證指令：

```powershell
.\mvnw.cmd verify
```

## 13. 依功能追查程式碼

### 註冊與登入

```text
index.html 登入表單
→ views.js
→ core.js api()
→ AuthController
→ AuthService
→ PlayerRepository
→ Player / PlayerMapper / AuthResponseDto
```

### 玩家進度與關卡解鎖

```text
core.js loadPlayerContent()
→ PlayerController
→ PlayerProgressService
→ PlayerStageProgressRepository / PlayerTowerUnlockRepository
→ PlayerProgressResponseDto
```

### 關卡路線、波次與敵人

```text
views.js 關卡選擇 + game.js 遊戲引擎
→ StageController
→ StageService
→ StageRepository
→ Stage / StagePath / StageWave / Enemy
→ StageMapper
```

### 防禦塔工坊

```text
index.html 工坊畫面
→ views.js purchaseTower()
→ TowerController
→ TowerService
→ PlayerRepository 鎖定玩家
→ TowerRepository / PlayerTowerUnlockRepository
```

### 遊戲與戰績保存

```text
game.js endGame()
→ GameRecordController
→ GameRecordService
→ GameRecordRepository
→ PlayerProgressService 更新玩家與關卡進度
```

### 個人歷史與每關排行榜

```text
views.js 戰績中心
→ GameRecordController
→ GameRecordService
→ GameRecordRepository
→ GameRecordHistoryResponseDto / RankingResponseDto
```

## 14. 修改需求時先看哪裡

| 想修改的內容 | 建議先看 |
| --- | --- |
| 登入、註冊、Token | `AuthController`、`AuthService`、`views.js`、`core.js` |
| 玩家金幣、等級、關卡解鎖 | `PlayerProgressService` |
| 塔永久價格或購買規則 | `TowerService`、`TowerController`、`V17`、`views.js` |
| 塔傷害、射程、造價 | `tower` seed／新 migration、`Tower`、`game.js` |
| 敵人生命、速度、獎勵 | `enemy` seed／新 migration、`Enemy`、`game.js` |
| 關卡路線或波次 | `stage_path`／`stage_wave` 的新 migration、`StageMapper`、`game.js` |
| 遊戲畫面或特效 | `index.html`、`styles.css`、`game.js` |
| 大廳、工坊、戰績中心版面 | `index.html`、`styles.css`、`views.js` |
| 個人歷史內容 | `GameRecordController`、`GameRecordService`、`GameRecordMapper`、`views.js` |
| 排行榜規則 | `GameRecordService`、`GameRecordRepository`、`views.js` |
| API 中文錯誤 | DTO 驗證訊息、Service 例外、`GlobalExceptionHandler` |
| 資料表結構 | 新增下一版 Flyway migration，不要直接修改已套用的舊版本 |
| 自動測試 | 依層級查看 `controller/`、`service/`、`repository/` 或整合測試 |

## 15. 不應直接修改或提交的內容

- `.env`：包含本機資料庫密碼。
- `target/`：Maven 產物，重新執行建置即可產生。
- 已經套用到資料庫的舊 migration：應新增 V18 之後的新檔案。
- IDE 個人設定，例如 `.vscode/`、`.idea/`。
- Entity 中的密碼雜湊與 Session 欄位，不應直接加入 API 回應。

閱讀專案時，建議先看 `README.md` 理解操作，再看 `docs/project-status.md` 理解系統，最後依本文件的
功能追查路線進入實際程式碼。
