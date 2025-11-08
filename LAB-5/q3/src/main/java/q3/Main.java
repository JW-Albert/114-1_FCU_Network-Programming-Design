package q3;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;

public class Main implements Runnable {
    private static final String GEO_API_BASE = "https://wft-geo-db.p.rapidapi.com/v1/geo/cities";
    
    public static void main(String[] args) {
        Main main = new Main();
        main.run();
    }

    @Override
    public void run() {
        try {
            System.out.println("=== 國際城市天氣預報員 ===\n");
            
            // 從 .env 文件讀取 API 配置
            String rapidApiKey = EnvLoader.get("RAPIDAPI_KEY");
            
            if (rapidApiKey == null || rapidApiKey.isEmpty()) {
                System.err.println("錯誤：無法從 .env 文件讀取 RAPIDAPI_KEY");
                return;
            }

            // 步驟 1: 使用 GeoDB Cities API 獲取巴黎的座標
            System.out.println("步驟 1: 查詢巴黎的地理座標...");
            double[] coordinates = getParisCoordinates(rapidApiKey);
            
            if (coordinates == null) {
                System.err.println("無法獲取巴黎的地理座標");
                return;
            }
            
            double latitude = coordinates[0];
            double longitude = coordinates[1];
            System.out.println(String.format("✓ 成功獲取座標：緯度 %.4f, 經度 %.4f\n", latitude, longitude));
            
            // 步驟 2: 使用 Open-Meteo API 查詢天氣
            System.out.println("步驟 2: 查詢巴黎的天氣資訊...");
            getWeatherInfo(latitude, longitude);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Unirest.shutDown();
        }
    }

    /**
     * 獲取巴黎的地理座標
     */
    private double[] getParisCoordinates(String apiKey) {
        try {
            HttpResponse<JsonNode> response = Unirest
                    .get(GEO_API_BASE)
                    .queryString("namePrefix", "Paris")
                    .queryString("countryIds", "FR")
                    .queryString("limit", "1")
                    .header("x-rapidapi-key", apiKey)
                    .header("x-rapidapi-host", "wft-geo-db.p.rapidapi.com")
                    .asJson();

            if (response.getStatus() != 200) {
                System.err.println("GeoDB API 錯誤: HTTP " + response.getStatus());
                return null;
            }

            JSONObject json = response.getBody().getObject();
            
            if (json.has("data") && json.get("data") instanceof JSONArray) {
                JSONArray data = json.getJSONArray("data");
                if (data.length() > 0) {
                    JSONObject city = data.getJSONObject(0);
                    double latitude = city.optDouble("latitude", 0.0);
                    double longitude = city.optDouble("longitude", 0.0);
                    
                    if (latitude != 0.0 && longitude != 0.0) {
                        return new double[]{latitude, longitude};
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("查詢巴黎座標時發生錯誤: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根據座標查詢天氣資訊
     */
    private void getWeatherInfo(double latitude, double longitude) {
        try {
            // 使用 Open-Meteo API (免費，無需 API key)
            HttpResponse<JsonNode> response = Unirest
                    .get("https://api.open-meteo.com/v1/forecast")
                    .queryString("latitude", String.valueOf(latitude))
                    .queryString("longitude", String.valueOf(longitude))
                    .queryString("current", "temperature_2m,weather_code")
                    .queryString("timezone", "auto")
                    .asJson();

            if (response.getStatus() != 200) {
                System.err.println("WeatherAPI 錯誤: HTTP " + response.getStatus());
                JSONObject errorJson = response.getBody().getObject();
                if (errorJson.has("error")) {
                    JSONObject error = errorJson.getJSONObject("error");
                    System.err.println("錯誤訊息: " + error.optString("message", "未知錯誤"));
                } else {
                    System.err.println("響應內容: " + errorJson.toString());
                }
                return;
            }

            JSONObject json = response.getBody().getObject();
            
            // Open-Meteo API 格式
            if (json.has("current")) {
                JSONObject current = json.getJSONObject("current");
                
                // 目前攝氏溫度
                double tempC = current.optDouble("temperature_2m", 0.0);
                
                // 天氣狀況描述（根據 weather_code）
                int weatherCode = current.optInt("weather_code", 0);
                String condition = getWeatherDescription(weatherCode);
                
                // 當地日期與時間
                String timeStr = current.optString("time", "");
                String localTime = timeStr;
                if (!timeStr.isEmpty()) {
                    try {
                        // Open-Meteo 返回的時間已經是當地時區
                        java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(timeStr);
                        localTime = dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } catch (Exception e) {
                        try {
                            // 如果上面失敗，嘗試解析為 Instant 然後轉換
                            java.time.Instant instant = java.time.Instant.parse(timeStr);
                            java.time.ZonedDateTime zonedTime = instant.atZone(java.time.ZoneId.of("Europe/Paris"));
                            localTime = zonedTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        } catch (Exception e2) {
                            // 如果都失敗，使用原始字符串
                        }
                    }
                }
                
                // 顯示結果
                System.out.println("✓ 成功獲取天氣資訊\n");
                System.out.println("=== 巴黎天氣預報 ===");
                System.out.println("目前攝氏溫度: " + String.format("%.1f", tempC) + "°C");
                System.out.println("天氣狀況描述: " + condition);
                System.out.println("當地日期與時間: " + localTime);
            } else {
                System.err.println("無法解析天氣資料");
                System.err.println("響應內容: " + json.toString());
            }
        } catch (Exception e) {
            System.err.println("查詢天氣資訊時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 根據天氣代碼獲取天氣描述
     */
    private String getWeatherDescription(int weatherCode) {
        // WMO Weather interpretation codes (WW)
        switch (weatherCode) {
            case 0: return "晴天";
            case 1: case 2: case 3: return "多雲";
            case 45: case 48: return "有霧";
            case 51: case 53: case 55: return "毛毛雨";
            case 56: case 57: return "凍毛毛雨";
            case 61: case 63: case 65: return "雨";
            case 66: case 67: return "凍雨";
            case 71: case 73: case 75: return "雪";
            case 77: return "雪粒";
            case 80: case 81: case 82: return "陣雨";
            case 85: case 86: return "雪陣";
            case 95: return "雷雨";
            case 96: case 99: return "雷雨帶冰雹";
            default: return "未知";
        }
    }
}
