package q2;

import kong.unirest.Unirest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONArray;
import kong.unirest.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main implements Runnable {
    private static final String GEO_API_BASE = "https://wft-geo-db.p.rapidapi.com/v1/geo/cities";
    
    public static void main(String[] args) {
        Main main = new Main();
        main.run();
    }

    @Override
    public void run() {
        try {
            // 從 .env 文件讀取 API 配置
            String apiKey = EnvLoader.get("RAPIDAPI_KEY");
            String apiHost = EnvLoader.get("RAPIDAPI_HOST");

            if (apiKey == null || apiKey.isEmpty()) {
                System.err.println("錯誤：無法從 .env 文件讀取 RAPIDAPI_KEY");
                return;
            }

            if (apiHost == null || apiHost.isEmpty()) {
                System.err.println("錯誤：無法從 .env 文件讀取 RAPIDAPI_HOST");
                return;
            }

            // 任務 1: 尋找台灣人口最多的城市
            System.out.println("=== 任務 1: 台灣人口最多的城市 ===\n");
            findLargestCityInTaiwan(apiKey, apiHost);
            
            System.out.println("\n");
            
            // 等待一下，避免速率限制
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // 任務 2: 列出日本所有人口超過 100 萬的城市
            System.out.println("=== 任務 2: 日本人口超過 100 萬的城市（按人口數排序） ===\n");
            listJapaneseCitiesOverOneMillion(apiKey, apiHost);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Unirest.shutDown();
        }
    }

    /**
     * 尋找台灣人口最多的城市
     */
    private void findLargestCityInTaiwan(String apiKey, String apiHost) {
        try {
            HttpResponse<JsonNode> response = Unirest
                    .get(GEO_API_BASE)
                    .queryString("countryIds", "TW")
                    .queryString("sort", "-population")
                    .queryString("limit", "1")
                    .header("x-rapidapi-key", apiKey)
                    .header("x-rapidapi-host", apiHost)
                    .asJson();

            JSONObject json = response.getBody().getObject();
            
            if (json.has("data") && json.get("data") instanceof JSONArray) {
                JSONArray data = json.getJSONArray("data");
                if (data.length() > 0) {
                    JSONObject city = data.getJSONObject(0);
                    String cityName = city.optString("city", "未知");
                    String region = city.optString("region", "未知");
                    String country = city.optString("country", "未知");
                    int population = city.optInt("population", 0);
                    
                    System.out.println("城市名 (city): " + cityName);
                    System.out.println("所在地區 (region): " + region);
                    System.out.println("國家 (country): " + country);
                    System.out.println("人口數 (population): " + String.format("%,d", population));
                } else {
                    System.out.println("未找到台灣的城市資料");
                }
            } else {
                System.out.println("API 回應格式錯誤");
            }
        } catch (Exception e) {
            System.err.println("查詢台灣城市時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 列出日本所有人口超過 100 萬的城市，按人口數由多到少排列
     */
    private void listJapaneseCitiesOverOneMillion(String apiKey, String apiHost) {
        try {
            List<CityInfo> cities = new ArrayList<>();
            int offset = 0;
            int limit = 10; // BASIC 計劃限制，每次請求最多 10 筆
            boolean hasMore = true;
            
            // 使用分頁獲取所有符合條件的城市
            while (hasMore) {
                HttpResponse<JsonNode> response = Unirest
                        .get(GEO_API_BASE)
                        .queryString("countryIds", "JP")
                        .queryString("minPopulation", "1000000")
                        .queryString("sort", "-population")
                        .queryString("limit", String.valueOf(limit))
                        .queryString("offset", String.valueOf(offset))
                        .header("x-rapidapi-key", apiKey)
                        .header("x-rapidapi-host", apiHost)
                        .asJson();

                JSONObject json = response.getBody().getObject();
                
                // 檢查響應狀態
                if (response.getStatus() == 429) {
                    // 速率限制，等待後重試
                    System.out.println("遇到速率限制，等待 3 秒後重試...");
                    try {
                        Thread.sleep(3000);
                        continue; // 重試當前請求
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        hasMore = false;
                        break;
                    }
                } else if (response.getStatus() != 200) {
                    if (json.has("errors")) {
                        JSONArray errors = json.getJSONArray("errors");
                        if (errors.length() > 0) {
                            JSONObject error = errors.getJSONObject(0);
                            System.err.println("API 錯誤: " + error.optString("message", "未知錯誤"));
                        }
                    } else {
                        System.err.println("API 錯誤: HTTP " + response.getStatus());
                    }
                    hasMore = false;
                    break;
                }
                
                if (json.has("data") && json.get("data") instanceof JSONArray) {
                    JSONArray data = json.getJSONArray("data");
                    
                    if (data.length() == 0) {
                        hasMore = false;
                        break;
                    }
                    
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject city = data.getJSONObject(i);
                        String cityName = city.optString("city", "未知");
                        int population = city.optInt("population", 0);
                        
                        if (population >= 1000000) {
                            cities.add(new CityInfo(cityName, population));
                        }
                    }
                    
                    // 檢查是否還有更多資料
                    JSONObject metadata = json.optJSONObject("metadata");
                    if (metadata != null) {
                        int currentCount = metadata.optInt("currentCount", 0);
                        int totalCount = metadata.optInt("totalCount", 0);
                        
                        if (currentCount == 0 || totalCount == 0) {
                            hasMore = false;
                        } else {
                            offset += currentCount;
                            hasMore = offset < totalCount;
                        }
                    } else {
                        // 如果沒有 metadata，根據返回的資料數量判斷
                        hasMore = data.length() == limit;
                        if (hasMore) {
                            offset += data.length();
                        }
                    }
                    
                    // 添加延遲以避免速率限制
                    if (hasMore) {
                        try {
                            Thread.sleep(2000); // 延遲 2 秒，避免速率限制
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            hasMore = false;
                        }
                    }
                } else {
                    // 如果沒有 data 欄位，可能是錯誤響應
                    if (json.has("message")) {
                        String errorMsg = json.optString("message", "");
                        System.err.println("API 錯誤: " + errorMsg);
                        // 如果是速率限制錯誤，等待後重試
                        if (errorMsg.contains("rate limit")) {
                            try {
                                System.out.println("等待 2 秒後重試...");
                                Thread.sleep(2000);
                                continue; // 重試當前請求
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                    hasMore = false;
                }
            }
            
            // 按人口數排序（降序）
            Collections.sort(cities, new Comparator<CityInfo>() {
                @Override
                public int compare(CityInfo c1, CityInfo c2) {
                    return Integer.compare(c2.population, c1.population);
                }
            });
            
            // 輸出結果
            if (cities.isEmpty()) {
                System.out.println("未找到日本人口超過 100 萬的城市");
            } else {
                System.out.println(String.format("%-30s %15s", "城市名", "人口數"));
                System.out.println("-----------------------------------------------");
                for (CityInfo city : cities) {
                    System.out.println(String.format("%-30s %15s", 
                        city.name, 
                        String.format("%,d", city.population)));
                }
                System.out.println("\n總共找到 " + cities.size() + " 個城市");
            }
        } catch (Exception e) {
            System.err.println("查詢日本城市時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 城市資訊類別
     */
    private static class CityInfo {
        String name;
        int population;
        
        CityInfo(String name, int population) {
            this.name = name;
            this.population = population;
        }
    }
}