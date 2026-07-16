# Machine Rift MVP 後端

## 概述
Machine Rift 是一款塔防遊戲，這個倉庫是它的 MVP 後端服務。遊戲畫面與玩法邏輯規劃在前端進行，後端只負責玩家、關卡、塔、對戰紀錄與排行榜的資料存取。

## 技術棧
- Java 21
- Spring Boot 3.5（Web、Data JPA、Validation）
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
└── exception    # 例外類別與全域錯誤處理
```

## 快速開始

### 前置需求
- JDK 21
- 本機 MySQL（已建立 `machine_rift` 資料庫）

### 設定環境變數
複製 `.env.example` 為 `.env`，並填入本機 MySQL 密碼：

```
cp .env.example .env
```

```
DB_PASSWORD=你的本機MySQL密碼
```

`.env` 已加入 `.gitignore`，不會被提交。

### 啟動服務
```
./mvnw spring-boot:run
```

### 執行測試
```
./mvnw test
```

## 資料庫
- 連線設定在 [src/main/resources/application.properties](src/main/resources/application.properties)，預設連到 `jdbc:mysql://localhost:3306/machine_rift`。
- `spring.jpa.hibernate.ddl-auto=update`，資料表會由 Hibernate 依 entity 定義自動建立/更新。
- [src/main/resources/schema.sql](src/main/resources/schema.sql) 提供對應的建表語法，可作為手動建置資料庫或參考資料表結構之用。
- 核心資料表：`player`、`stage`、`tower`、`game_record`。刪除 `player` 或 `stage` 時，若已有關聯的 `game_record`，會被拒絕（回傳 409 衝突）。

## API 端點

Base path：`/api`

| 資源 | Method | 路徑 | 說明 |
| --- | --- | --- | --- |
| 玩家 | GET | `/players` | 取得所有玩家 |
| 玩家 | GET | `/players/{id}` | 取得單一玩家 |
| 玩家 | POST | `/players` | 建立玩家 |
| 玩家 | PUT | `/players/{id}` | 更新玩家 |
| 玩家 | DELETE | `/players/{id}` | 刪除玩家（有戰績會被拒絕） |
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
| 戰績 | POST | `/game-records` | 儲存一筆對戰紀錄 |
| 戰績 | GET | `/game-records` | 取得所有戰績 |
| 戰績 | GET | `/game-records/{id}` | 取得單一戰績 |
| 排行榜 | GET | `/rankings` | 依分數排序的戰績列表 |

所有回應都包在統一格式中：

```json
{
  "success": true,
  "message": "Players retrieved successfully.",
  "data": [ ... ]
}
```

### 範例：建立玩家
```
POST /api/players
Content-Type: application/json

{
  "playerName": "Ash",
  "level": 1
}
```

## Swagger
啟動服務後，可至 `/swagger-ui.html` 瀏覽並測試所有 API；OpenAPI 原始文件在 `/v3/api-docs`。

## 專案現況
- 後端 API 骨架與資料完整性防護已完成（玩家/關卡刪除保護、集中式例外處理）。
- 玩家、關卡、塔皆已提供完整 CRUD。
- 前端遊戲畫面（Canvas 渲染、遊戲邏輯）尚未開發，目前 `static/index.html` 僅為服務啟動確認頁。
