# LAB-5

## 學生資料

- **課程** 1141 網路程式設計實習(資訊三合)[1402]
- **班級** 資訊三乙
- **姓名** 王建葦
- **學號** D1210799

## 環境需求

- Java 版本：JAVA 21 (OpenJDK)
- 經過測試在 DietPi v9.18.1 上可以運行。

## 專案結構

```
LAB-5/
├── q1/                          # 題目一：名言查詢程式
│   ├── src/main/java/q1/
│   │   ├── Main.java           # 主程式：使用 Quotes API 查詢隨機名言
│   │   └── EnvLoader.java      # 環境變數讀取工具類
│   ├── lib/                     # 依賴項 JAR 文件
│   ├── pom.xml                  # Maven 專案配置
│   ├── .env.example             # 環境變數範例文件
│   ├── compile.sh               # 編譯腳本
│   └── run.sh                   # 執行腳本
│
├── q2/                          # 題目二：GeoDB Cities 城市查詢
│   ├── src/main/java/q2/
│   │   ├── Main.java           # 主程式：查詢台灣和日本城市資訊
│   │   └── EnvLoader.java      # 環境變數讀取工具類
│   ├── lib/                     # 依賴項 JAR 文件
│   ├── pom.xml                  # Maven 專案配置
│   ├── .env.example             # 環境變數範例文件
│   ├── compile.sh               # 編譯腳本
│   └── run.sh                   # 執行腳本
│
└── q3/                          # 題目三：國際城市天氣預報員
    ├── src/main/java/q3/
    │   ├── Main.java           # 主程式：結合 GeoDB 和天氣 API
    │   └── EnvLoader.java      # 環境變數讀取工具類
    ├── lib/                     # 依賴項 JAR 文件
    ├── pom.xml                  # Maven 專案配置
    ├── .env.example             # 環境變數範例文件
    ├── compile.sh               # 編譯腳本
    └── run.sh                   # 執行腳本
```

## 題目說明

### Q1: 名言查詢程式

使用 Quotes API 查詢隨機名言，並格式化輸出作者和內容。

**功能：**
- 從 Quotes API 獲取隨機名言
- 解析 JSON 回應
- 格式化輸出：`智慧箴言：[作者名] 曾說過："[內容]"。`

**執行方式：**
```bash
cd q1
./compile.sh
./run.sh
```

### Q2: GeoDB Cities 城市查詢

使用 GeoDB Cities API 查詢城市資訊。

**功能：**
1. 尋找台灣人口最多的城市，顯示城市名、地區、國家和人口數
2. 列出日本所有人口超過 100 萬的城市，按人口數由多到少排列

**執行方式：**
```bash
cd q2
./compile.sh
./run.sh
```

**注意事項：**
- 由於 API 速率限制，查詢日本城市時會自動添加延遲
- BASIC 計劃限制每次請求最多 10 筆資料

### Q3: 國際城市天氣預報員

結合 GeoDB Cities API 和 Open-Meteo API，實現城市天氣查詢功能。

**功能：**
1. 使用 GeoDB Cities API 獲取巴黎的地理座標（緯度和經度）
2. 使用 Open-Meteo API 根據座標查詢天氣資訊
3. 顯示目前攝氏溫度、天氣狀況描述和當地日期與時間

**執行方式：**
```bash
cd q3
./compile.sh
./run.sh
```

**輸出範例：**
```
=== 國際城市天氣預報員 ===

步驟 1: 查詢巴黎的地理座標...
✓ 成功獲取座標：緯度 48.8709, 經度 2.3561

步驟 2: 查詢巴黎的天氣資訊...
✓ 成功獲取天氣資訊

=== 巴黎天氣預報 ===
目前攝氏溫度: 14.5°C
天氣狀況描述: 多雲
當地日期與時間: 2025-11-08 16:00:00
```

## 環境變數配置

所有題目都需要配置 `.env` 文件（請參考各目錄下的 `.env.example`）。

**重要：** `.env` 文件包含敏感資訊（API keys），已被加入 `.gitignore`，不會被上傳到版本控制系統。

### Q1 環境變數
```bash
RAPIDAPI_KEY=your_rapidapi_key_here
RAPIDAPI_HOST=quotes15.p.rapidapi.com
```

### Q2 環境變數
```bash
RAPIDAPI_KEY=your_rapidapi_key_here
RAPIDAPI_HOST=wft-geo-db.p.rapidapi.com
```

### Q3 環境變數
```bash
RAPIDAPI_KEY=your_rapidapi_key_here
```

## 編譯與執行

### 編譯所有題目
```bash
# 編譯 Q1
cd q1 && ./compile.sh && cd ..

# 編譯 Q2
cd q2 && ./compile.sh && cd ..

# 編譯 Q3
cd q3 && ./compile.sh && cd ..
```

### 執行所有題目
```bash
# 執行 Q1
cd q1 && ./run.sh && cd ..

# 執行 Q2
cd q2 && ./run.sh && cd ..

# 執行 Q3
cd q3 && ./run.sh && cd ..
```

## 技術特點

### 使用的 API
- **Quotes API** (RapidAPI): 提供隨機名言
- **GeoDB Cities API** (RapidAPI): 提供全球城市地理資訊
- **Open-Meteo API**: 提供免費天氣資訊（無需 API key）

### 程式設計特點
- 使用 `kong.unirest` 進行 HTTP 請求
- 實現 `.env` 文件讀取功能，保護 API keys
- 完整的錯誤處理機制
- 支援分頁查詢（Q2）
- 天氣代碼轉換為中文描述（Q3）

### 依賴項
- `unirest-java` 3.14.5
- `gson` 2.10.1
- 以及相關的傳遞依賴項

## 注意事項

1. **API 速率限制**：部分 API 有速率限制，程式已實現自動延遲和重試機制
2. **環境變數**：請確保每個題目的 `.env` 文件已正確配置
3. **Java 版本**：需要 Java 21 或更高版本
4. **網路連線**：所有程式都需要網路連線以訪問 API

## 開發環境

- **作業系統**: DietPi v9.18.1 (Debian-based)
- **Java 版本**: OpenJDK 21
- **編譯工具**: javac
- **依賴管理**: Maven (pom.xml) + 手動管理 JAR 文件

## 程式碼運作說明

### Q1: 名言查詢程式運作流程

#### 主要類別說明

**Main.java** 是程式的主入口點，實現了 `Runnable` 介面。

1. **程式初始化**
   - `main` 方法創建 `Main` 實例並調用 `run` 方法
   - `run` 方法負責執行主要邏輯

2. **環境變數讀取**
   - 使用 `EnvLoader.get()` 方法從 `.env` 文件讀取 `RAPIDAPI_KEY` 和 `RAPIDAPI_HOST`
   - 如果讀取失敗，程式會輸出錯誤訊息並終止

3. **API 請求發送**
   - 使用 `Unirest.get()` 方法向 Quotes API 發送 GET 請求
   - 請求 URL: `https://quotes15.p.rapidapi.com/quotes/random/?language_code=en`
   - 在 HTTP 標頭中添加 `x-rapidapi-key` 和 `x-rapidapi-host` 進行身份驗證
   - 使用 `.asJson()` 方法將回應解析為 JSON 格式

4. **JSON 資料解析**
   - 從回應的 JSON 物件中提取 `originator` 物件（包含作者資訊）
   - 從 `originator` 物件中提取 `name` 欄位作為作者名
   - 從 JSON 物件中提取 `content` 欄位作為名言內容

5. **結果格式化輸出**
   - 檢查作者名和內容是否為空
   - 如果都有值，則格式化輸出為：`智慧箴言：[作者名] 曾說過："[內容]"。`
   - 如果缺少資料，則輸出錯誤訊息

6. **資源清理**
   - 在 `finally` 區塊中調用 `Unirest.shutDown()` 關閉 HTTP 客戶端連接

**EnvLoader.java** 負責讀取和管理環境變數。

1. **靜態初始化**
   - 使用靜態區塊在類別載入時自動調用 `loadEnv()` 方法
   - 創建一個 `HashMap` 來儲存環境變數的鍵值對

2. **.env 文件查找**
   - 首先嘗試從當前工作目錄讀取 `.env` 文件
   - 如果當前目錄沒有，則從類別文件所在目錄開始向上查找
   - 最多向上查找 5 層目錄，直到找到 `.env` 文件或到達根目錄

3. **文件解析**
   - 使用 `BufferedReader` 逐行讀取 `.env` 文件
   - 跳過空行和以 `#` 開頭的註釋行
   - 解析 `KEY=VALUE` 格式的每一行
   - 自動移除值兩側的引號（如果有的話）
   - 將解析後的鍵值對存入 `envMap`

4. **環境變數獲取**
   - `get(String key)` 方法優先從系統環境變數讀取
   - 如果系統環境變數中沒有，則從 `.env` 文件讀取
   - `get(String key, String defaultValue)` 方法提供預設值功能

### Q2: GeoDB Cities 城市查詢運作流程

#### 主要類別說明

**Main.java** 實現兩個主要功能：查詢台灣最大城市和查詢日本大城市。

1. **程式初始化**
   - 定義 `GEO_API_BASE` 常數為 GeoDB Cities API 的基礎 URL
   - `main` 方法創建實例並執行 `run` 方法

2. **任務 1: 查詢台灣人口最多的城市**
   - `findLargestCityInTaiwan()` 方法實現此功能
   - 發送 GET 請求到 GeoDB Cities API，查詢參數包括：
     - `countryIds=TW`：限定查詢台灣的城市
     - `sort=-population`：按人口數降序排序（負號表示降序）
     - `limit=1`：只返回第一筆結果（人口最多的城市）
   - 解析回應的 JSON 資料，提取城市名、地區、國家和人口數
   - 使用 `String.format("%,d", population)` 格式化人口數，添加千位分隔符

3. **任務 2: 查詢日本人口超過 100 萬的城市**
   - `listJapaneseCitiesOverOneMillion()` 方法實現此功能
   - 使用分頁機制獲取所有符合條件的城市：
     - 每次請求最多 10 筆資料（BASIC 計劃限制）
     - 使用 `offset` 參數控制分頁
     - 使用 `while` 迴圈持續請求直到沒有更多資料
   - 速率限制處理：
     - 檢查 HTTP 狀態碼，如果是 429（Too Many Requests），等待 3 秒後重試
     - 每次請求之間延遲 2 秒，避免觸發速率限制
   - 資料收集：
     - 創建 `CityInfo` 物件儲存城市名和人口數
     - 將所有符合條件的城市加入 `ArrayList`
   - 排序：
     - 使用 `Collections.sort()` 和自定義 `Comparator` 按人口數降序排序
   - 格式化輸出：
     - 使用 `String.format()` 創建表格格式的輸出
     - 顯示城市名和格式化後的人口數

4. **CityInfo 內部類別**
   - 用於儲存城市資訊的簡單資料類別
   - 包含 `name`（城市名）和 `population`（人口數）兩個欄位

### Q3: 國際城市天氣預報員運作流程

#### 主要類別說明

**Main.java** 結合兩個 API 實現天氣查詢功能。

1. **程式初始化**
   - 定義 `GEO_API_BASE` 常數為 GeoDB Cities API 的基礎 URL
   - `main` 方法創建實例並執行 `run` 方法

2. **步驟 1: 獲取城市座標**
   - `getParisCoordinates()` 方法實現此功能
   - 發送 GET 請求到 GeoDB Cities API，查詢參數包括：
     - `namePrefix=Paris`：搜尋城市名以 "Paris" 開頭的城市
     - `countryIds=FR`：限定查詢法國的城市
     - `limit=1`：只返回第一筆結果
   - 解析回應的 JSON 資料，提取 `latitude`（緯度）和 `longitude`（經度）
   - 驗證座標是否有效（不為 0.0）
   - 返回包含緯度和經度的 `double` 陣列

3. **步驟 2: 查詢天氣資訊**
   - `getWeatherInfo()` 方法實現此功能
   - 使用 Open-Meteo API（免費，無需 API key）：
     - 請求 URL: `https://api.open-meteo.com/v1/forecast`
     - 查詢參數包括：
       - `latitude` 和 `longitude`：從步驟 1 獲取的座標
       - `current=temperature_2m,weather_code`：請求當前溫度和天氣代碼
       - `timezone=auto`：自動使用當地時區
   - 解析回應的 JSON 資料：
     - 從 `current` 物件中提取 `temperature_2m`（攝氏溫度）
     - 從 `current` 物件中提取 `weather_code`（天氣代碼）
     - 從 `current` 物件中提取 `time`（時間字串）
   - 天氣代碼轉換：
     - 調用 `getWeatherDescription()` 方法將 WMO 天氣代碼轉換為中文描述
   - 時間格式化：
     - 嘗試將時間字串解析為 `LocalDateTime`
     - 如果失敗，嘗試解析為 `Instant` 並轉換為巴黎時區
     - 格式化為 `yyyy-MM-dd HH:mm:ss` 格式
   - 格式化輸出所有天氣資訊

4. **天氣代碼轉換方法**
   - `getWeatherDescription()` 方法實現 WMO 天氣代碼到中文描述的轉換
   - 使用 `switch` 語句處理不同的天氣代碼
   - 支援的天氣類型包括：晴天、多雲、有霧、毛毛雨、雨、雪、陣雨、雷雨等

### 共同組件：EnvLoader 類別

所有三個題目都使用相同的 `EnvLoader` 類別來讀取環境變數。其運作方式已在 Q1 的說明中詳細描述。

### 錯誤處理機制

所有程式都實現了完整的錯誤處理：

1. **API 請求錯誤**
   - 檢查 HTTP 狀態碼，如果不是 200，輸出錯誤訊息
   - 嘗試解析錯誤回應的 JSON 並顯示詳細錯誤訊息

2. **資料解析錯誤**
   - 使用 `optString()` 和 `optInt()` 等方法安全地提取 JSON 資料
   - 提供預設值，避免空指標異常

3. **異常處理**
   - 使用 `try-catch` 區塊捕獲所有異常
   - 輸出錯誤訊息和堆疊追蹤，方便除錯

4. **資源清理**
   - 在 `finally` 區塊中確保 `Unirest.shutDown()` 被調用
   - 釋放 HTTP 客戶端資源

### 網路請求流程

1. **請求準備**
   - 從環境變數讀取 API key 和 host
   - 構建請求 URL 和查詢參數
   - 設置必要的 HTTP 標頭

2. **請求發送**
   - 使用 `Unirest` 庫發送 HTTP GET 請求
   - 等待伺服器回應

3. **回應處理**
   - 檢查 HTTP 狀態碼
   - 將回應解析為 JSON 格式
   - 提取所需的資料欄位

4. **結果輸出**
   - 格式化資料
   - 輸出到標準輸出流

### 資料流程圖

**Q1 資料流程：**
```
.env 文件 → EnvLoader → API Key/Host
    ↓
Main.run() → HTTP 請求 → Quotes API
    ↓
JSON 回應 → 解析資料 → 格式化輸出
```

**Q2 資料流程：**
```
.env 文件 → EnvLoader → API Key/Host
    ↓
Main.run() → HTTP 請求 → GeoDB Cities API
    ↓
JSON 回應 → 解析資料 → 排序處理 → 格式化輸出
```

**Q3 資料流程：**
```
.env 文件 → EnvLoader → API Key
    ↓
Main.run() → HTTP 請求1 → GeoDB Cities API → 獲取座標
    ↓
HTTP 請求2 → Open-Meteo API → 獲取天氣
    ↓
解析資料 → 代碼轉換 → 格式化輸出
```