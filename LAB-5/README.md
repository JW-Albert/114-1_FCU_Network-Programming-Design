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
