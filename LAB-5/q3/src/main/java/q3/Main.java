package q3;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

/**
 * Q3: 國際城市天氣預報員
 * 
 * 此程式結合 GeoDB Cities API 和 Open-Meteo API，實現城市天氣查詢功能。
 * 主要功能包括：
 * 1. 使用 GeoDB Cities API 獲取指定城市的地理座標（緯度和經度）
 * 2. 使用 Open-Meteo API 根據座標查詢該城市的即時天氣資訊
 * 3. 顯示目前攝氏溫度、天氣狀況描述和當地日期與時間
 * 
 * 主要技術特點：
 * - 結合兩個不同的 API 實現完整功能
 * - 實現 WMO 天氣代碼到中文描述的轉換
 * - 處理時區轉換和時間格式化
 * 
 * @author 王建葦
 * @version 1.0
 */
public class Main implements Runnable {
    /**
     * GeoDB Cities API 的基礎 URL
     * 用於查詢城市的地理座標資訊
     */
    private static final String GEO_API_BASE = "https://wft-geo-db.p.rapidapi.com/v1/geo/cities";
    
    /**
     * 程式入口點
     * 創建 Main 實例並執行 run 方法
     * 
     * @param args 命令列參數（此程式不使用）
     */
    public static void main(String[] args) {
        Main main = new Main();
        main.run();
    }

    /**
     * 實現 Runnable 介面的 run 方法
     * 執行主要程式邏輯：獲取城市座標並查詢天氣資訊
     */
    @Override
    public void run() {
        try {
            // 輸出程式標題
            System.out.println("=== 國際城市天氣預報員 ===\n");
            
            // 步驟 1: 從 .env 文件讀取 API 配置
            // 使用 EnvLoader 工具類讀取 RapidAPI 金鑰
            String rapidApiKey = EnvLoader.get("RAPIDAPI_KEY");
            
            // 驗證 API Key 是否成功讀取
            if (rapidApiKey == null || rapidApiKey.isEmpty()) {
                System.err.println("錯誤：無法從 .env 文件讀取 RAPIDAPI_KEY");
                return; // 如果讀取失敗，終止程式執行
            }

            // 步驟 2: 使用 GeoDB Cities API 獲取巴黎的地理座標
            System.out.println("步驟 1: 查詢巴黎的地理座標...");
            // 調用 getParisCoordinates 方法獲取座標
            double[] coordinates = getParisCoordinates(rapidApiKey);
            
            // 檢查是否成功獲取座標
            if (coordinates == null) {
                System.err.println("無法獲取巴黎的地理座標");
                return; // 如果獲取失敗，終止程式執行
            }
            
            // 從座標陣列中提取緯度和經度
            double latitude = coordinates[0];   // 緯度
            double longitude = coordinates[1]; // 經度
            
            // 輸出成功訊息和座標資訊
            System.out.println(String.format("成功獲取座標：緯度 %.4f, 經度 %.4f\n", latitude, longitude));
            
            // 步驟 3: 使用 Open-Meteo API 查詢天氣資訊
            System.out.println("步驟 2: 查詢巴黎的天氣資訊...");
            // 調用 getWeatherInfo 方法查詢天氣
            getWeatherInfo(latitude, longitude);
            
        } catch (Exception e) {
            // 捕獲所有異常並輸出錯誤訊息
            e.printStackTrace();
        } finally {
            // 步驟 4: 清理資源
            // 關閉 Unirest HTTP 客戶端，釋放網路連接資源
            Unirest.shutDown();
        }
    }

    /**
     * 獲取巴黎的地理座標
     * 
     * 此方法向 GeoDB Cities API 發送請求，查詢法國巴黎的城市資訊，
     * 並從回應中提取緯度和經度座標。
     * 
     * @param apiKey RapidAPI 的 API 金鑰，用於身份驗證
     * @return 包含緯度和經度的 double 陣列，[0] 為緯度，[1] 為經度
     *         如果查詢失敗則返回 null
     */
    private double[] getParisCoordinates(String apiKey) {
        try {
            // 步驟 1: 構建並發送 HTTP GET 請求
            HttpResponse<JsonNode> response = Unirest
                    .get(GEO_API_BASE)
                    // 查詢參數：namePrefix=Paris 搜尋城市名以 "Paris" 開頭的城市
                    .queryString("namePrefix", "Paris")
                    // 查詢參數：countryIds=FR 限定查詢法國的城市
                    .queryString("countryIds", "FR")
                    // 查詢參數：limit=1 只返回第一筆結果
                    .queryString("limit", "1")
                    // HTTP 標頭：設置 RapidAPI 認證資訊
                    .header("x-rapidapi-key", apiKey)
                    .header("x-rapidapi-host", "wft-geo-db.p.rapidapi.com")
                    // 將回應解析為 JSON 格式
                    .asJson();

            // 步驟 2: 檢查 HTTP 狀態碼
            if (response.getStatus() != 200) {
                // 如果狀態碼不是 200，輸出錯誤訊息並返回 null
                System.err.println("GeoDB API 錯誤: HTTP " + response.getStatus());
                return null;
            }

            // 步驟 3: 解析 JSON 回應
            JSONObject json = response.getBody().getObject();
            
            // 步驟 4: 檢查回應格式並提取座標
            // 檢查 JSON 中是否包含 data 欄位，且為陣列類型
            if (json.has("data") && json.get("data") instanceof JSONArray) {
                // 獲取城市資料陣列
                JSONArray data = json.getJSONArray("data");
                
                // 檢查是否有資料
                if (data.length() > 0) {
                    // 獲取第一筆城市資料（應該是巴黎）
                    JSONObject city = data.getJSONObject(0);
                    
                    // 提取緯度和經度
                    // 使用 optDouble 方法安全地提取資料，提供預設值 0.0
                    double latitude = city.optDouble("latitude", 0.0);
                    double longitude = city.optDouble("longitude", 0.0);
                    
                    // 步驟 5: 驗證座標是否有效
                    // 檢查座標是否不為 0.0（0.0 通常表示無效座標）
                    if (latitude != 0.0 && longitude != 0.0) {
                        // 返回包含緯度和經度的陣列
                        return new double[]{latitude, longitude};
                    }
                }
            }
            
            // 如果無法提取有效座標，返回 null
            return null;
        } catch (Exception e) {
            // 捕獲異常並輸出錯誤訊息
            System.err.println("查詢巴黎座標時發生錯誤: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根據座標查詢天氣資訊
     * 
     * 此方法使用 Open-Meteo API（免費，無需 API key）根據地理座標查詢天氣資訊。
     * 查詢結果包括目前攝氏溫度、天氣狀況描述和當地日期與時間。
     * 
     * @param latitude 緯度座標
     * @param longitude 經度座標
     */
    private void getWeatherInfo(double latitude, double longitude) {
        try {
            // 步驟 1: 構建並發送 HTTP GET 請求到 Open-Meteo API
            // Open-Meteo 是一個免費的天氣 API，無需 API key
            HttpResponse<JsonNode> response = Unirest
                    .get("https://api.open-meteo.com/v1/forecast")
                    // 查詢參數：latitude 設定緯度座標
                    .queryString("latitude", String.valueOf(latitude))
                    // 查詢參數：longitude 設定經度座標
                    .queryString("longitude", String.valueOf(longitude))
                    // 查詢參數：current 指定要獲取的當前天氣資料
                    // temperature_2m 表示 2 米高度的溫度，weather_code 表示天氣代碼
                    .queryString("current", "temperature_2m,weather_code")
                    // 查詢參數：timezone=auto 自動使用當地時區
                    .queryString("timezone", "auto")
                    // 將回應解析為 JSON 格式
                    .asJson();

            // 步驟 2: 檢查 HTTP 狀態碼
            if (response.getStatus() != 200) {
                // 如果狀態碼不是 200，嘗試解析錯誤訊息
                System.err.println("WeatherAPI 錯誤: HTTP " + response.getStatus());
                JSONObject errorJson = response.getBody().getObject();
                if (errorJson.has("error")) {
                    // 如果回應中包含 error 物件，提取錯誤訊息
                    JSONObject error = errorJson.getJSONObject("error");
                    System.err.println("錯誤訊息: " + error.optString("message", "未知錯誤"));
                } else {
                    // 否則輸出整個回應內容
                    System.err.println("響應內容: " + errorJson.toString());
                }
                return; // 終止方法執行
            }

            // 步驟 3: 解析 JSON 回應
            JSONObject json = response.getBody().getObject();
            
            // 步驟 4: 檢查回應格式並提取天氣資料
            // Open-Meteo API 的格式包含 current 物件
            if (json.has("current")) {
                // 獲取 current 物件（包含當前天氣資訊）
                JSONObject current = json.getJSONObject("current");
                
                // 步驟 4.1: 提取目前攝氏溫度
                // temperature_2m 已經是攝氏溫度，無需轉換
                double tempC = current.optDouble("temperature_2m", 0.0);
                
                // 步驟 4.2: 提取天氣代碼並轉換為中文描述
                // weather_code 是 WMO（世界氣象組織）天氣代碼
                int weatherCode = current.optInt("weather_code", 0);
                // 調用 getWeatherDescription 方法將代碼轉換為中文描述
                String condition = getWeatherDescription(weatherCode);
                
                // 步驟 4.3: 提取並格式化當地日期與時間
                // time 欄位包含時間字串
                String timeStr = current.optString("time", "");
                String localTime = timeStr; // 預設使用原始字串
                
                // 如果時間字串不為空，嘗試格式化
                if (!timeStr.isEmpty()) {
                    try {
                        // 方法 1: 嘗試直接解析為 LocalDateTime
                        // Open-Meteo 返回的時間已經是當地時區
                        java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(timeStr);
                        // 格式化為 yyyy-MM-dd HH:mm:ss 格式
                        localTime = dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } catch (Exception e) {
                        // 如果方法 1 失敗，嘗試方法 2
                        try {
                            // 方法 2: 嘗試解析為 Instant 然後轉換為巴黎時區
                            java.time.Instant instant = java.time.Instant.parse(timeStr);
                            // 轉換為巴黎時區（Europe/Paris）
                            java.time.ZonedDateTime zonedTime = instant.atZone(java.time.ZoneId.of("Europe/Paris"));
                            // 格式化為 yyyy-MM-dd HH:mm:ss 格式
                            localTime = zonedTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        } catch (Exception e2) {
                            // 如果兩種方法都失敗，使用原始字符串
                            // 不輸出錯誤，保持程式繼續執行
                        }
                    }
                }
                
                // 步驟 5: 格式化並輸出結果
                System.out.println("成功獲取天氣資訊\n");
                System.out.println("=== 巴黎天氣預報 ===");
                // 輸出目前攝氏溫度，保留一位小數
                System.out.println("目前攝氏溫度: " + String.format("%.1f", tempC) + "°C");
                // 輸出天氣狀況描述
                System.out.println("天氣狀況描述: " + condition);
                // 輸出當地日期與時間
                System.out.println("當地日期與時間: " + localTime);
            } else {
                // 如果回應格式不符合預期，輸出錯誤訊息
                System.err.println("無法解析天氣資料");
                System.err.println("響應內容: " + json.toString());
            }
        } catch (Exception e) {
            // 捕獲異常並輸出錯誤訊息
            System.err.println("查詢天氣資訊時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 根據天氣代碼獲取天氣描述
     * 
     * 此方法將 WMO（世界氣象組織）天氣代碼轉換為中文描述。
     * WMO 天氣代碼是一個標準化的天氣狀況分類系統。
     * 
     * @param weatherCode WMO 天氣代碼（0-99 的整數）
     * @return 對應的中文天氣描述
     */
    private String getWeatherDescription(int weatherCode) {
        // 使用 switch 語句根據天氣代碼返回對應的描述
        // WMO Weather interpretation codes (WW) 標準
        switch (weatherCode) {
            case 0: 
                return "晴天";
            case 1: case 2: case 3: 
                // 代碼 1-3 表示不同程度的雲量
                return "多雲";
            case 45: case 48: 
                // 代碼 45, 48 表示霧
                return "有霧";
            case 51: case 53: case 55: 
                // 代碼 51, 53, 55 表示不同程度的毛毛雨
                return "毛毛雨";
            case 56: case 57: 
                // 代碼 56, 57 表示凍毛毛雨
                return "凍毛毛雨";
            case 61: case 63: case 65: 
                // 代碼 61, 63, 65 表示不同程度的雨
                return "雨";
            case 66: case 67: 
                // 代碼 66, 67 表示凍雨
                return "凍雨";
            case 71: case 73: case 75: 
                // 代碼 71, 73, 75 表示不同程度的雪
                return "雪";
            case 77: 
                // 代碼 77 表示雪粒
                return "雪粒";
            case 80: case 81: case 82: 
                // 代碼 80, 81, 82 表示不同程度的陣雨
                return "陣雨";
            case 85: case 86: 
                // 代碼 85, 86 表示雪陣
                return "雪陣";
            case 95: 
                // 代碼 95 表示雷雨
                return "雷雨";
            case 96: case 99: 
                // 代碼 96, 99 表示雷雨帶冰雹
                return "雷雨帶冰雹";
            default: 
                // 如果代碼不在已知範圍內，返回"未知"
                return "未知";
        }
    }
}