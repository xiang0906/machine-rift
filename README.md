# Machine Rift MVP

## 概述
Machine Rift 是一款塔防遊戲。這個倉庫包含：
- Spring Boot 後端：負責玩家、關卡、塔、對戰紀錄與排行榜的資料存取
- 前端遊戲畫面（`src/main/resources/static/index.html`）：Canvas 繪製的最小可玩塔防切片，實際呼叫後端 API

## 技術棧
- Java 21
- Spring Boot 3.5（Web、Data JPA、Validation、密碼加密）
- MySQL（正式環境）／H2（測試環境）
- Lombok
- springdoc-openapi（Swagger UI）

## 專案架構
分層架構：`Controller -> Service -> Repository -> Entity`，並以 DTO 作為 API 對外契約。

```
src/main/java/com/machinerift/machine_rift/
├── controller   # REST 端點
├── service      # 商業邏輯
├── repository   # Spring Data JPA repository
├── entity       # JPA 實體
├── dto          # 請求/回應資料格式
├── mapper       # entity <-> DTO 轉換
├── exception    # 例外類別與全域錯誤處理
└── config       # 密碼編碼器等應用程式設定
```

## 快速開始

### 前置需求
- JDK 21
- 本機 MySQL（已建立 `machine_rift` 資料庫）

### 設定環境變數
複製 `.env.example` 為 `.env`，並填入本機 MySQL 密碼。

macOS／Linux：

```bash
cp .env.example .env
```

Windows PowerShell：

```powershell
Copy-Item .env.example .env
```

接著設定：

```properties
DB_PASSWORD=你的本機MySQL密碼
```

`.env` 已加入 `.gitignore`，不會被提交。

### 啟動服務

macOS／Linux：

```bash
./mvnw spring-boot:run
```

Windows PowerShell：

```powershell
.\mvnw.cmd spring-boot:run
```

啟動後瀏覽器開啟 `http://localhost:8080/` 即可看到遊戲畫面。

### 執行測試

macOS／Linux：

```bash
./mvnw test
```

Windows PowerShell：

```powershell
.\mvnw.cmd test
```

## 資料庫
- 連線設定在 [src/main/resources/application.properties](src/main/resources/application.properties)，預設連到 `jdbc:mysql://localhost:3306/machine_rift`。
- 資料庫結構與初始內容由 Flyway 管理，migration 位於
  [`src/main/resources/db/migration`](src/main/resources/db/migration)。
- `spring.jpa.hibernate.ddl-auto=validate`，Hibernate 只驗證 entity 與資料表是否一致，
  不會在啟動時自行修改正式資料庫結構。
- 第一次啟動空白資料庫時，Flyway 會建立資料表，並加入六個關卡、六種防塔與六種敵人。
- V3 會建立 `enemy`、`stage_path`、`stage_wave`，並加入三種敵人、
  三條關卡路徑與九個波次設定。
- V4～V6 會建立玩家進度／解鎖資料，並移除舊版 `Wave 1～3` 佔位關卡。
- V7～V9 會為玩家加入唯一帳號、唯一顯示名稱、BCrypt 密碼雜湊與 30 天登入 session。
- V10 會重新平衡既有防塔的造價、傷害、攻速與射程。
- V11 會新增三種防塔，讓防塔總數增加為六種。
- V12 會新增三種敵人、重新配置既有波次，並讓六種敵人都會實際出場。
- V13 會新增三個關卡及各自不重複的路線與波次，讓關卡總數增加為六關。
- 既有 MVP 資料庫若尚未有 Flyway history，啟動時會 baseline 為 V1，再執行後續 migration；
  seed migration 只補上缺少的同名內容，不覆寫既有資料。
- 核心資料表：`player`、`player_session`、`player_progress`、`player_stage_progress`、
  `player_tower_unlock`、`stage`、`stage_path`、`stage_wave`、`tower`、`enemy`、
  `game_record`。刪除 `player` 或 `stage` 時，若已有關聯的 `game_record`，
  會被拒絕（回傳 409 衝突）。

## API 端點

Base path：`/api`

| 資源 | Method | 路徑 | 說明 |
| --- | --- | --- | --- |
| 帳號 | POST | `/auth/register` | 建立帳號並登入 |
| 帳號 | POST | `/auth/login` | 使用帳號密碼登入 |
| 帳號 | GET | `/auth/me` | 以 Bearer token 取得目前玩家 |
| 帳號 | POST | `/auth/logout` | 登出並撤銷目前 token |
| 玩家 | GET | `/players` | 取得所有玩家 |
| 玩家 | GET | `/players/{id}` | 取得單一玩家 |
| 玩家 | GET | `/players/{id}/progress` | 取得本人等級、資產、解鎖內容與各關最佳戰績（需登入） |
| 玩家 | PUT | `/players/{id}` | 更新本人玩家資料（需登入） |
| 玩家 | DELETE | `/players/{id}` | 刪除本人帳號（需登入；有戰績會被拒絕） |
| 關卡 | GET | `/stages` | 取得所有關卡 |
| 關卡 | GET | `/stages/{id}` | 取得單一關卡 |
| 關卡 | POST | `/stages` | 建立關卡 |
| 關卡 | PUT | `/stages/{id}` | 更新關卡 |
| 關卡 | DELETE | `/stages/{id}` | 刪除關卡（有戰績會被拒絕） |
| 塔 | GET | `/towers` | 取得所有塔 |
| 塔 | GET | `/towers/{id}` | 取得單一塔 |
| 塔 | POST | `/towers` | 建立塔 |
| 塔 | PUT | `/towers/{id}` | 更新塔 |
| 塔 | DELETE | `/towers/{id}` | 刪除塔 |
| 戰績 | POST | `/game-records` | 儲存本人的對戰紀錄（需登入） |
| 戰績 | GET | `/game-records` | 取得所有戰績 |
| 戰績 | GET | `/game-records/{id}` | 取得單一戰績 |
| 排行榜 | GET | `/rankings` | 依分數排序的戰績列表（需登入） |

所有回應都包在統一格式中：

```json
{
  "success": true,
  "message": "Players retrieved successfully.",
  "data": [ ... ]
}
```

需登入的 API 使用：

```
Authorization: Bearer <accessToken>
```

### 範例：註冊帳號
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "ash.01+rift!",
  "password": "至少八個字元",
  "playerName": "Ash"
}
```

帳號長度為 3～50 個字元，可使用特殊符號，但不可包含空白。

## Swagger
啟動服務後，可至 `/swagger-ui.html` 瀏覽並測試所有 API；OpenAPI 原始文件在 `/v3/api-docs`。

## 前端遊戲畫面
`src/main/resources/static/index.html` 是純前端單一 HTML 檔（Canvas + Vanilla JS），由 Spring Boot 當靜態資源伺服，實際呼叫後端 API，不使用任何前端框架或建置流程。

遊玩流程：
1. 建立帳號或登入 → `POST /api/auth/register`／`POST /api/auth/login`
2. 前端保存 access token；重新整理或再次開啟時以 `GET /api/auth/me` 恢復同一玩家
3. 選擇關卡 → `GET /api/stages` 讀取關卡清單
4. 建塔 → `GET /api/towers` 讀取塔的造價/傷害/射程，點選塔種後點地圖空格建造
5. 開始波次 → 依資料庫波次與敵人設定生成敵人
6. 結束遊戲 → 帶著登入 token 呼叫 `POST /api/game-records`，將戰績綁定登入玩家
7. 登入後可另外查看排行榜 → `GET /api/rankings` 搭配 `GET /api/players` 顯示玩家名稱

全新資料庫會由 Flyway 自動建立六個關卡、六種防塔、六種敵人、44 個路徑節點
與 26 個波次，因此首次啟動即可遊玩。六條關卡路線使用不同的節點序列。前端會從
`GET /api/stages` 讀取路徑節點、敵人生命、速度、擊殺獎勵、每波數量與生成間隔；
起始金幣、基地血量和投射物速度目前仍是前端常數。

新玩家會自動取得第一關與最便宜的起始防塔。每次儲存戰績時，後端會更新經驗值、
玩家等級、金幣、通關次數及該關最佳分數／時間。首次通關會解鎖下一關；每次勝利
都會嘗試依造價順序解鎖下一座尚未解鎖的防塔。前端會顯示所有關卡，未解鎖關卡
則停用進入按鈕。

遊戲畫面採用左側固定 Canvas 戰場與右側防禦塔庫。桌面版只會捲動塔庫，不會因為
解鎖塔數增加而將戰場推離畫面；窄螢幕則改為橫向滑動塔卡。玩家可依造價或傷害
進行升冪／降冪排序，排序偏好會依玩家保存在瀏覽器。不同防塔使用不同顏色，
不同敵人也會依種類顯示不同顏色與體型。

排行榜只能在登入後查看。前端會彙整每位玩家的最高分，同分時以較短完成時間優先，
並顯示前三名頒獎台與完整前十名列表。

## 專案現況
- 後端 API 骨架與資料完整性防護已完成（玩家/關卡刪除保護、集中式例外處理）。
- 玩家帳號、加密密碼、持久登入、個人進度、解鎖內容與最佳戰績已完成。
- 關卡路徑、波次與敵人設定皆由資料庫及 API 提供。
- 遊戲內容目前包含六個關卡、六種防塔、六種敵人與 26 個波次。
- 前端已有可玩的塔防流程，並串接登入、關卡／塔資料、玩家進度、戰績與排行榜。
- 尚未做的：密碼重設、電子郵件驗證、塔升級、敵人特殊能力與更多後續關卡。
