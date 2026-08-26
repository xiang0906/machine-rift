# Machine Rift 專案現況

> 更新日期：2026-08-26
> 階段：功能完成、進入課程 MVP 收尾與展示階段

## 1. 專案定位

Machine Rift 是一款瀏覽器塔防遊戲。前端使用原生 HTML、CSS、JavaScript 與 Canvas 2D；
Spring Boot 後端負責帳號、玩家進度、關卡內容、防禦塔解鎖、戰績與排行榜；MySQL 保存正式資料，
Flyway 管理資料庫結構與種子資料版本。

目前已完成一條可展示的核心流程：

```text
註冊／登入
  → 遊戲大廳
  → 防禦塔工坊／選擇關卡／戰績中心
  → 任務簡報與塔防戰鬥
  → 儲存勝敗、分數、時間
  → 更新永久戰備金、關卡進度與個人最佳
  → 查詢個人歷史與排行榜
```

專案另有四頁式互動報告 `project-report/index.html`，部署後可由
`https://xiang0906.github.io/machine-rift/project-report/` 瀏覽。報告與遊戲程式分開，不參與 Spring Boot 執行。

## 2. 技術與執行環境

| 層級 | 技術 |
| --- | --- |
| Java | Java 21 |
| 後端 | Spring Boot 3.5.16、Spring MVC、Spring Data JPA |
| 驗證／密碼 | Jakarta Validation、Spring Security Crypto、BCrypt |
| 正式資料庫 | MySQL |
| 測試資料庫 | H2（MySQL mode） |
| 資料庫版本 | Flyway V1～V21 |
| API 文件 | springdoc-openapi 2.8.17、Swagger UI |
| 前端 | HTML、CSS、Vanilla JavaScript、Canvas 2D |
| 建置與測試 | Maven Wrapper、JUnit、MockMvc |
| 文件展示 | Markdown、GitHub Pages、獨立 HTML/CSS/JavaScript 報告 |

Spring Boot 4 升級不在本次課程 MVP 收尾範圍；目前維持 Spring Boot 3.5.16。

## 3. 系統架構

```text
Browser / Canvas UI
        │ REST + Bearer Token
        ▼
Controller：接收 HTTP 請求與回傳狀態
        ▼
Service：帳號、進度、購買、戰績與排行規則
        ▼
Repository：Spring Data JPA 查詢
        ▼
Entity：資料表映射
        ▼
MySQL / H2
```

| 後端套件 | 責任 |
| --- | --- |
| `controller` | 定義 11 支核心 API、驗證輸入、取得登入玩家 |
| `service` | 執行帳號、Session、進度、解鎖、購買與排行榜規則 |
| `repository` | 讀寫玩家、內容與戰績資料；必要時提供排序或資料鎖 |
| `entity` | 映射 9 張業務資料表及其關聯 |
| `dto` | 定義 API 請求與回應，不直接暴露 Entity |
| `mapper` | 將 Entity 轉成前端使用的 DTO，或將 request 轉成 Entity |
| `exception` | 將驗證、登入、找不到資料與衝突統一轉成中文錯誤回應 |
| `config` | 提供 BCrypt `PasswordEncoder` |

## 4. 帳號、Session 與玩家身分

- 首頁是登入頁；第一次遊玩的使用者可前往建立帳號。
- 帳號不分大小寫且不可重複；玩家顯示名稱不分大小寫且不可重複。
- 帳號長度 3～50 字元，可使用特殊符號，但不可包含空白或控制字元。
- 密碼長度 8～72 字元，資料庫只保存 BCrypt 雜湊。
- 登入後產生 30 天有效的 Token，資料庫只保存 Token 的 SHA-256 雜湊。
- 每位玩家只保留一個有效 Session；再次登入會替換舊 Token，登出會清除 Session。
- 私人進度與戰績的玩家身分由 Bearer Token 決定，request 不接受 `playerId`。
- 登入狀態由瀏覽器保存 Token，重新開啟時透過 `GET /api/auth/me` 恢復。

目前採用 Controller 主動呼叫 `AuthService` 驗證 Token，尚未導入完整 Spring Security Filter Chain。

## 5. 玩家進度與永久解鎖

玩家目前保存：

- 永久戰備金 `gold`
- 已完成關卡數 `completedStages`
- 每關是否解鎖、完成次數、最高分與同分時的最短完成時間
- 已永久解鎖的防禦塔及解鎖時間

已移除未實際參與玩法的 `level` 與 `experience`。目前進度直接由永久戰備金、關卡進度、
防禦塔收藏及戰績呈現。

進度規則：

- 新玩家會解鎖第一個可遊玩關卡及最便宜的起始塔。
- 完成關卡後取得該關設定的永久戰備金；首次完成會解鎖下一關。
- 防禦塔不再依關卡自動解鎖，玩家可在工坊自由選擇購買。
- 後端以 transaction 與玩家資料鎖完成購買，防止重複購買或扣款不一致。
- 金幣不足、重複購買或塔不存在時，後端回傳中文業務錯誤。

## 6. 遊戲內容與戰鬥

| 內容 | 數量 |
| --- | ---: |
| 關卡 | 6 |
| 不重複關卡路線 | 6 |
| 路徑節點 | 44 |
| 波次 | 26 |
| 防禦塔 | 6 |
| 敵人種類 | 6 |

關卡依序為：裂隙前線、機械迴廊、核心裂谷、熔火交叉口、量子迷城、機神核心。

防禦塔依序為：脈衝砲塔、離子機槍塔、量子砲塔、電弧砲塔、磁軌砲塔、裂隙重砲塔。

敵人依序為：疾風無人機、偵察機、裝甲機、護盾機兵、裂隙核心、裂隙巨像。

關卡名稱、難度、獎勵、路線、波次、敵人數值與塔數值都由 API／資料庫提供。Canvas 前端負責
實際移動、攻擊、碰撞、建造、繪圖、動畫與 HUD。起始單局金幣 300、基地耐久 10、投射物速度
等即時戰鬥常數仍保存在 `game.js`。

目前戰鬥行為：

- 玩家按下開始波次後，波次會連續進行；前一波敵人全部離場後才切換下一波。
- 敵人生成除了遵守資料庫的 `spawn_interval_ms`，也會檢查最小本體間距，避免互相遮擋。
- 敵人血條固定在本體上方。
- 塔可依傷害或造價排序；選取金幣不足的塔仍會保留選擇，取得足夠金幣後即可建造。
- 建塔後若仍有足夠金幣，會維持同一塔種以便連續放置；金幣不足時也不會強制取消選取。
- 攻擊範圍只在建造預覽或滑鼠移到已建塔時顯示。
- 砲管會朝目標旋轉，並有開火、命中、擊殺金幣、基地受擊、低血量及勝利停頓回饋。
- 戰場路線引導只在開戰前顯示，開始後隱藏。

## 7. 資料庫現況

目前有 9 張業務資料表，另有 Flyway 自動維護的 `flyway_schema_history`：

| 資料表 | 責任 |
| --- | --- |
| `player` | 帳號、密碼雜湊、玩家名稱、永久戰備金、通關數及目前 Session |
| `player_stage_progress` | 關卡解鎖、完成次數、最高分與最佳時間 |
| `player_tower_unlock` | 玩家永久持有的塔與解鎖時間 |
| `stage` | 關卡名稱、難度、通關獎勵及敵人總數 |
| `stage_path` | 每關路徑節點、格線座標及順序 |
| `stage_wave` | 每波敵人種類、數量與生成間隔 |
| `tower` | 塔類型、傷害、攻速、射程、單局造價與永久解鎖價格 |
| `enemy` | 敵人生命、速度與擊殺獎勵 |
| `game_record` | 玩家、關卡、分數、勝敗、遊玩時間與建立日期 |

Migration 重要演進：

- V1～V3：建立核心資料表，加入可遊玩的關卡、敵人、路線與波次。
- V4～V9：加入玩家進度、帳號與 Session，移除舊關卡並確保玩家名稱唯一。
- V10～V13：重新平衡塔，擴充至 6 種塔、6 種敵人及 6 個不重複關卡。
- V14～V17：收斂玩家表、加入戰績日期、恢復玩家與塔關聯、加入永久解鎖價格。
- V18：逐關提高敵人數量與生成壓力，強化高階敵人生命。
- V19：移除 `player.level` 與 `player.experience`。
- V20～V21：提高中後期敵人速度；目前速度依序為 92、72、55、50、48、39。

下一個 migration 版本應從 `V22__...sql` 開始；不可修改已套用的 V1～V21。

## 8. API 現況

| 方法與路徑 | 用途 | 登入 |
| --- | --- | --- |
| `POST /api/auth/register` | 建立玩家帳號並登入 | 否 |
| `POST /api/auth/login` | 驗證帳密並建立 Session | 否 |
| `GET /api/auth/me` | 由 Token 取得目前玩家 | 是 |
| `POST /api/auth/logout` | 撤銷目前 Session | 是 |
| `GET /api/players/me/progress` | 取得本人金幣、關卡與塔進度 | 是 |
| `GET /api/stages` | 取得全部關卡、路徑、波次與敵人 | 否 |
| `GET /api/towers` | 取得全部塔與兩種價格 | 否 |
| `POST /api/towers/{towerId}/unlock` | 以永久戰備金購買塔 | 是 |
| `POST /api/game-records` | 為 Token 所屬玩家儲存戰績 | 是 |
| `GET /api/game-records/me` | 取得本人最近 20 場戰績 | 是 |
| `GET /api/rankings?stageId={id}` | 全部關卡或指定關卡前十名 | 是 |

共 11 支核心 API。排行榜由後端完成每位玩家只取最佳成績、分數高者優先、同分時時間短者優先，
最多回傳前十名。成功與失敗統一使用 `ApiResponse`，驗證與業務錯誤訊息皆為中文。

Swagger UI：`http://localhost:8080/swagger-ui.html`

OpenAPI JSON：`http://localhost:8080/v3/api-docs`

## 9. 前端畫面與導覽

前端是一個以 hash route 導航的單頁應用程式，共 7 個主要畫面：

| 路由 | 畫面 |
| --- | --- |
| `#/login` | 登入首頁與三行世界觀 |
| `#/register` | 建立帳號 |
| `#/lobby` | 遊戲大廳、玩家摘要及功能入口 |
| `#/towers` | 防禦塔工坊與永久購買 |
| `#/stages` | 六關選擇、任務敘事與操作教學 |
| `#/records` | 個人歷史與全部／單關排行榜 |
| `#/game` | Canvas 戰場、HUD 與可捲動塔庫 |

舊的 `#/history`、`#/ranking` 會轉向戰績中心。登入後先進入遊戲大廳；需要登入的路由會在
Session 無效時返回登入。戰鬥狀態只保存在記憶體，因此於 `#/game` 重新整理會安全返回大廳。

前端已拆分為：

- `index.html`：七個畫面與 Overlay 的語意結構。
- `styles.css`：科幻戰術藍圖風格、響應式版面、手機橫向提示與動畫。
- `core.js`：路由、API、Token、全域狀態與玩家內容載入。
- `views.js`：大廳、工坊、戰績、關卡及任務簡報畫面。
- `game.js`：Canvas 塔防引擎、輸入、更新、繪製、HUD 與戰績送出。
- `story.js`：六關任務、勝利與失敗文案。
- `background.js`：登入／一般頁面的動態背景。

## 10. 測試與品質

最近一次完整 Maven 測試共有 **39 項，0 failures、0 errors、0 skipped**：

| 測試類別 | 數量 | 重點 |
| --- | ---: | --- |
| `MachineRiftApplicationTests` | 7 | 啟動、9 表、種子資料、塔平衡、關卡 API、登入恢復、OpenAPI |
| `DatabaseMigrationTest` | 8 | V14～V21 升級與資料保留／平衡結果 |
| `GameRecordControllerTest` | 6 | 本人戰績、中文驗證、歷史及排行榜 API |
| `PlayerControllerTest` | 5 | 本人進度與已移除 API 的 404 |
| `GameRecordServiceTest` | 7 | 儲存、鎖關、歷史、最佳排行及關卡篩選 |
| `TowerServiceTest` | 4 | 購買成功、金幣不足、重複購買、塔不存在 |
| `GameRecordRepositoryTest` | 1 | 排行榜分數與時間排序 |
| `GlobalExceptionHandlerTest` | 1 | 空驗證訊息的中文 fallback |

Maven 測試指令：`./mvnw.cmd test`

完整打包指令：`./mvnw.cmd clean package`

## 11. 已知限制與收尾界線

- 這是單人塔防 MVP，不包含真正的即時多人對戰。
- 遊戲更新與碰撞由前端執行，後端尚未驗證玩家實際戰鬥過程，因此不是防作弊架構。
- 沒有密碼重設、電子郵件驗證、登入限流及失敗鎖定。
- 尚未導入完整 Spring Security Filter Chain。
- JavaScript 已按責任拆成多檔，但仍以傳統 script 順序載入，尚未改為 ES Modules。
- 排行榜目前從符合條件的戰績中於後端彙整；資料量大幅增加後才需要分頁或資料庫端最佳化。
- 即時戰鬥常數與動畫仍在前端，不是全部由資料庫控制。
- 手機提供直式提示並建議橫向遊玩，但不是原生 App，也不會強制旋轉裝置。

## 12. 課程展示建議

目前已符合「能登入、能操作、能存資料、能展示」的課程 MVP 目標，建議收尾聚焦：

1. 使用 `project-report/` 進行四頁式系統報告。
2. 準備 3～5 分鐘 Demo：註冊、工坊購塔、進入關卡、完成戰鬥、查看戰績。
3. 展示 MySQL 的 9 張業務表、Flyway V1～V21 與 Swagger 11 支 API。
4. 展示 `mvnw.cmd test` 的 39 項自動測試結果。
5. 若課程結束前沒有明確需求，以修正錯誤及文件一致性為主，避免再擴大功能範圍。
