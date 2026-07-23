# Machine Rift 專案現況

> 更新日期：2026-07-23  
> 階段：可遊玩的全端 MVP

## 1. 專案定位

Machine Rift 是一款以瀏覽器 Canvas 呈現的塔防遊戲。Spring Boot 後端負責帳號、
玩家進度、關卡內容、對戰紀錄與排行榜；MySQL 保存正式資料，Flyway 管理資料庫版本。

目前 MVP 已形成完整循環：

1. 玩家建立帳號或登入。
2. 系統載入該玩家的關卡、塔解鎖與最佳戰績。
3. 玩家選擇已解鎖關卡並進行塔防遊戲。
4. 結束後將勝敗、分數與遊玩時間寫入後端。
5. 後端更新經驗、等級、金幣、個人最佳與解鎖內容。
6. 玩家可在登入後查看排行榜。

## 2. 技術與執行環境

| 層級 | 技術 |
| --- | --- |
| 語言 | Java 21、HTML、CSS、Vanilla JavaScript |
| 後端 | Spring Boot 3.5.16、Spring MVC、Spring Data JPA |
| 驗證 | Jakarta Validation |
| 密碼 | Spring Security Crypto、BCrypt |
| 正式資料庫 | MySQL |
| 測試資料庫 | H2（MySQL mode） |
| Migration | Flyway V1～V13 |
| API 文件 | springdoc-openapi／Swagger UI |
| 前端 | 單一 HTML、Canvas 2D |
| 建置 | Maven Wrapper |

## 3. 系統分層

```text
Browser / Canvas UI
        │ REST + Bearer token
        ▼
Controller
        ▼
Service（驗證、進度、解鎖與刪除規則）
        ▼
Repository
        ▼
JPA Entity
        ▼
MySQL / H2
```

後端套件責任：

| 套件 | 責任 |
| --- | --- |
| `controller` | REST API 與 HTTP 狀態 |
| `service` | 帳號、進度、解鎖與資料完整性規則 |
| `repository` | Spring Data JPA 查詢 |
| `entity` | 資料表映射與關聯 |
| `dto` | API 請求與回應契約 |
| `mapper` | Entity 與 DTO 轉換 |
| `exception` | 統一例外與錯誤回應 |
| `config` | 密碼編碼器等共用設定 |

## 4. 帳號與權限

- 玩家以唯一帳號和密碼登入。
- 玩家顯示名稱不分大小寫且不可重複。
- 帳號長度為 3～50 個字元，可使用特殊符號，但不可包含空白或控制字元。
- 密碼長度為 8～72 個字元，資料庫只保存 BCrypt 雜湊。
- 登入後產生 30 天有效的 Session；資料庫保存 Token 的 SHA-256 雜湊。
- 登出會撤銷目前 Session。
- 玩家進度、玩家資料更新、帳號刪除、戰績寫入與排行榜需要 Bearer Token。
- 玩家不可讀寫其他玩家的私人進度或代替其他玩家寫入戰績。

## 5. 玩家進度

每位玩家保存：

- 等級、經驗、金幣與完成關卡數
- 每關是否解鎖
- 每關完成次數
- 每關最高分與同分時的最短完成時間
- 已解鎖防禦塔

目前解鎖規則：

- 新玩家取得第一個可遊玩關卡與最便宜的塔。
- 首次完成關卡會增加完成關卡數並解鎖下一關。
- 每次勝利都會嘗試解鎖下一座尚未解鎖的塔。
- 分數累加為經驗；每 1000 經驗提升一級。
- 勝利時依關卡設定增加金幣。

## 6. 遊戲內容

| 內容 | 數量 |
| --- | ---: |
| 關卡 | 6 |
| 不重複路線 | 6 |
| 路徑節點 | 44 |
| 波次 | 26 |
| 防禦塔 | 6 |
| 敵人種類 | 6 |

關卡：

1. 裂隙前線
2. 機械迴廊
3. 核心裂谷
4. 熔火交叉口
5. 量子迷城
6. 機神核心

防禦塔：

1. 脈衝砲塔
2. 離子機槍塔
3. 量子砲塔
4. 電弧砲塔
5. 磁軌砲塔
6. 裂隙重砲塔

敵人：

1. 疾風無人機
2. 偵察機
3. 裝甲機
4. 護盾機兵
5. 裂隙核心
6. 裂隙巨像

關卡路線、波次、敵人數值與塔數值都由資料庫提供。起始金幣、基地血量、投射物速度
及目前的攻擊行為仍由前端常數控制。

## 7. 資料庫

專案共有 11 張業務資料表，另有 Flyway 的 `flyway_schema_history`：

| 資料表 | 責任 |
| --- | --- |
| `player` | 帳號、密碼雜湊、顯示名稱與等級 |
| `player_session` | 登入 Token 雜湊與到期時間 |
| `player_progress` | 經驗、金幣與完成關卡數 |
| `player_stage_progress` | 關卡解鎖、完成次數與個人最佳 |
| `player_tower_unlock` | 玩家已解鎖的塔 |
| `stage` | 關卡基本資料 |
| `stage_path` | 關卡路徑節點與順序 |
| `stage_wave` | 波次、敵人、數量與生成間隔 |
| `tower` | 塔的類型、傷害、攻速、射程與造價 |
| `enemy` | 敵人生命、速度與擊殺獎勵 |
| `game_record` | 每場遊戲的分數、勝敗與時間 |

目前表數與功能需求相符。若日後要縮減，可考慮將 `player_progress` 合併進 `player`；
其餘關聯表負責一對多或多對多資料，不建議只為減少表數而合併。

## 8. API 現況

| 範圍 | 端點 |
| --- | --- |
| 帳號 | `POST /api/auth/register`、`POST /api/auth/login` |
| Session | `GET /api/auth/me`、`POST /api/auth/logout` |
| 玩家 | `GET /api/players`、`GET /api/players/{id}` |
| 私人進度 | `GET /api/players/{id}/progress` |
| 玩家維護 | `PUT /api/players/{id}`、`DELETE /api/players/{id}` |
| 關卡 | `/api/stages`、`/api/stages/{id}` |
| 防禦塔 | `/api/towers`、`/api/towers/{id}` |
| 戰績 | `POST /api/game-records`、`GET /api/game-records`、`GET /api/game-records/{id}` |
| 排行榜 | `GET /api/rankings` |

所有回應使用統一的 `ApiResponse` 格式。完整 API 可在 `/swagger-ui.html` 查看。

## 9. 前端現況

- 首頁為登入畫面，首次遊玩的玩家可切換至建立帳號。
- Access Token 保存於瀏覽器，重新開啟時透過 `/api/auth/me` 恢復登入。
- 關卡頁顯示全部六關、鎖定狀態與個人最佳成績。
- 遊戲畫面使用左側固定 Canvas 與右側可捲動塔庫。
- 窄螢幕時塔庫改成橫向滑動，避免塔卡將遊戲畫面推離視窗。
- 塔可依造價或傷害升冪／降冪排序，偏好依玩家保存在瀏覽器。
- 不同塔使用不同顏色；不同敵人使用不同顏色與體型。
- 排行榜只能登入後查看，顯示前三名頒獎台與前十名。

## 10. 測試與品質

目前自動測試共 16 項，涵蓋：

- Spring Boot 與 Flyway 啟動
- 種子資料數量與路線唯一性
- 塔價格、輸出與射程層級
- 帳號註冊、登入、登出與 Session
- 玩家進度、個人最佳與解鎖
- 排行榜登入權限
- 玩家／關卡刪除完整性
- 戰績查詢與更新

最近一次完整測試、JavaScript 語法檢查與 Maven 打包皆通過。

## 11. 已知限制

- 前端集中在單一 `index.html`，已超過 1300 行；功能繼續增加前應拆分 CSS 與 JavaScript。
- 目前沒有完整的 Spring Security Filter Chain，權限由 Controller 呼叫 `AuthService` 驗證。
- `GET /api/players`、`GET /api/game-records` 等讀取 API 目前仍是公開端點。
- 關卡與塔的寫入 API 尚未區分管理員角色。
- 敵人只有生命、速度與獎勵，尚未支援護甲、緩速抗性或特殊技能。
- 防禦塔只有基本單體攻擊，尚未支援升級、範圍傷害、緩速或減益。
- 沒有密碼重設、電子郵件驗證、限流與登入失敗鎖定。
- 排行榜彙整主要在前端完成，資料量增加後應改為後端查詢與分頁。

## 12. 建議後續順序

1. 補上 Spring Security Filter Chain 與管理員權限。
2. 將前端拆分成 `styles.css`、API／狀態模組與遊戲引擎模組。
3. 將排行榜彙整、前十名與分頁移到後端。
4. 增加塔升級、範圍攻擊與敵人特殊能力。
5. 增加 CI，讓 GitHub Push／Pull Request 自動執行測試。
6. 補上密碼重設與登入安全機制。
