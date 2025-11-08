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

/**
 * Q2: GeoDB Cities 城市查詢程式
 * 
 * 此程式使用 GeoDB Cities API 查詢城市資訊，實現兩個主要功能：
 * 1. 查詢台灣人口最多的城市
 * 2. 查詢日本所有人口超過 100 萬的城市，按人口數排序
 * 
 * 主要技術特點：
 * - 使用分頁機制處理大量資料
 * - 實現速率限制處理和自動重試機制
 * - 使用自定義 Comparator 進行排序
 * 
 * @author 王建葦
 * @version 1.0
 */
public class Main implements Runnable {
    /**
     * GeoDB Cities API 的基礎 URL
     * 所有城市查詢請求都基於此 URL
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
     * 執行主要程式邏輯：查詢台灣和日本的城市資訊
     */
    @Override
    public void run() {
        try {
            // 步驟 1: 從 .env 文件讀取 API 配置
            // 使用 EnvLoader 工具類讀取環境變數
            String apiKey = EnvLoader.get("RAPIDAPI_KEY");
            String apiHost = EnvLoader.get("RAPIDAPI_HOST");

            // 驗證 API Key 是否成功讀取
            if (apiKey == null || apiKey.isEmpty()) {
                System.err.println("錯誤：無法從 .env 文件讀取 RAPIDAPI_KEY");
                return; // 如果讀取失敗，終止程式執行
            }

            // 驗證 API Host 是否成功讀取
            if (apiHost == null || apiHost.isEmpty()) {
                System.err.println("錯誤：無法從 .env 文件讀取 RAPIDAPI_HOST");
                return; // 如果讀取失敗，終止程式執行
            }

            // 步驟 2: 執行任務 1 - 尋找台灣人口最多的城市
            System.out.println("=== 任務 1: 台灣人口最多的城市 ===\n");
            findLargestCityInTaiwan(apiKey, apiHost);
            
            // 輸出空行分隔兩個任務
            System.out.println("\n");
            
            // 步驟 3: 等待一段時間，避免 API 速率限制
            // 在兩個 API 請求之間添加延遲，避免觸發速率限制
            try {
                Thread.sleep(1000); // 延遲 1 秒
            } catch (InterruptedException e) {
                // 如果執行緒被中斷，恢復中斷狀態
                Thread.currentThread().interrupt();
            }
            
            // 步驟 4: 執行任務 2 - 列出日本所有人口超過 100 萬的城市
            System.out.println("=== 任務 2: 日本人口超過 100 萬的城市（按人口數排序） ===\n");
            listJapaneseCitiesOverOneMillion(apiKey, apiHost);
            
        } catch (Exception e) {
            // 捕獲所有異常並輸出錯誤訊息
            e.printStackTrace();
        } finally {
            // 步驟 5: 清理資源
            // 關閉 Unirest HTTP 客戶端，釋放網路連接資源
            Unirest.shutDown();
        }
    }

    /**
     * 尋找台灣人口最多的城市
     * 
     * 此方法向 GeoDB Cities API 發送請求，查詢台灣所有城市，
     * 按人口數降序排序，並返回第一筆結果（人口最多的城市）。
     * 
     * @param apiKey RapidAPI 的 API 金鑰，用於身份驗證
     * @param apiHost RapidAPI 的主機名稱，用於 HTTP 標頭
     */
    private void findLargestCityInTaiwan(String apiKey, String apiHost) {
        try {
            // 步驟 1: 構建並發送 HTTP GET 請求
            HttpResponse<JsonNode> response = Unirest
                    .get(GEO_API_BASE)
                    // 查詢參數：countryIds=TW 限定查詢台灣的城市
                    .queryString("countryIds", "TW")
                    // 查詢參數：sort=-population 按人口數降序排序（負號表示降序）
                    .queryString("sort", "-population")
                    // 查詢參數：limit=1 只返回第一筆結果
                    .queryString("limit", "1")
                    // HTTP 標頭：設置 RapidAPI 認證資訊
                    .header("x-rapidapi-key", apiKey)
                    .header("x-rapidapi-host", apiHost)
                    // 將回應解析為 JSON 格式
                    .asJson();

            // 步驟 2: 解析 JSON 回應
            JSONObject json = response.getBody().getObject();
            
            // 步驟 3: 檢查回應格式是否正確
            // 檢查 JSON 中是否包含 data 欄位，且為陣列類型
            if (json.has("data") && json.get("data") instanceof JSONArray) {
                // 獲取城市資料陣列
                JSONArray data = json.getJSONArray("data");
                
                // 步驟 4: 檢查是否有資料
                if (data.length() > 0) {
                    // 獲取第一筆城市資料（人口最多的城市）
                    JSONObject city = data.getJSONObject(0);
                    
                    // 步驟 5: 提取城市資訊
                    // 使用 optString 和 optInt 方法安全地提取資料，提供預設值
                    String cityName = city.optString("city", "未知");
                    String region = city.optString("region", "未知");
                    String country = city.optString("country", "未知");
                    int population = city.optInt("population", 0);
                    
                    // 步驟 6: 格式化並輸出結果
                    System.out.println("城市名 (city): " + cityName);
                    System.out.println("所在地區 (region): " + region);
                    System.out.println("國家 (country): " + country);
                    // 使用 String.format("%,d", population) 格式化人口數，添加千位分隔符
                    System.out.println("人口數 (population): " + String.format("%,d", population));
                } else {
                    // 如果沒有資料，輸出提示訊息
                    System.out.println("未找到台灣的城市資料");
                }
            } else {
                // 如果回應格式錯誤，輸出錯誤訊息
                System.out.println("API 回應格式錯誤");
            }
        } catch (Exception e) {
            // 捕獲異常並輸出錯誤訊息
            System.err.println("查詢台灣城市時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 列出日本所有人口超過 100 萬的城市，按人口數由多到少排列
     * 
     * 此方法使用分頁機制獲取所有符合條件的城市，因為 API 有每次請求的資料量限制。
     * 實現了速率限制處理和自動重試機制。
     * 
     * @param apiKey RapidAPI 的 API 金鑰，用於身份驗證
     * @param apiHost RapidAPI 的主機名稱，用於 HTTP 標頭
     */
    private void listJapaneseCitiesOverOneMillion(String apiKey, String apiHost) {
        try {
            // 步驟 1: 初始化資料結構
            // 使用 ArrayList 儲存所有符合條件的城市
            List<CityInfo> cities = new ArrayList<>();
            // offset 用於分頁控制，表示跳過的資料筆數
            int offset = 0;
            // limit 設定每次請求最多返回的資料筆數
            // BASIC 計劃限制每次請求最多 10 筆
            int limit = 10;
            // hasMore 標記是否還有更多資料需要獲取
            boolean hasMore = true;
            
            // 步驟 2: 使用分頁機制獲取所有符合條件的城市
            // 使用 while 迴圈持續請求直到沒有更多資料
            while (hasMore) {
                // 步驟 2.1: 構建並發送 HTTP GET 請求
                HttpResponse<JsonNode> response = Unirest
                        .get(GEO_API_BASE)
                        // 查詢參數：countryIds=JP 限定查詢日本的城市
                        .queryString("countryIds", "JP")
                        // 查詢參數：minPopulation=1000000 設定最小人口數為 100 萬
                        .queryString("minPopulation", "1000000")
                        // 查詢參數：sort=-population 按人口數降序排序
                        .queryString("sort", "-population")
                        // 查詢參數：limit 設定每次請求返回的資料筆數
                        .queryString("limit", String.valueOf(limit))
                        // 查詢參數：offset 設定跳過的資料筆數（用於分頁）
                        .queryString("offset", String.valueOf(offset))
                        // HTTP 標頭：設置 RapidAPI 認證資訊
                        .header("x-rapidapi-key", apiKey)
                        .header("x-rapidapi-host", apiHost)
                        // 將回應解析為 JSON 格式
                        .asJson();

                // 步驟 2.2: 解析 JSON 回應
                JSONObject json = response.getBody().getObject();
                
                // 步驟 2.3: 檢查 HTTP 狀態碼，處理速率限制
                if (response.getStatus() == 429) {
                    // HTTP 429 表示 Too Many Requests（速率限制）
                    // 等待 3 秒後重試當前請求
                    System.out.println("遇到速率限制，等待 3 秒後重試...");
                    try {
                        Thread.sleep(3000); // 延遲 3 秒
                        continue; // 重試當前請求（不更新 offset）
                    } catch (InterruptedException e) {
                        // 如果執行緒被中斷，恢復中斷狀態並終止迴圈
                        Thread.currentThread().interrupt();
                        hasMore = false;
                        break;
                    }
                } else if (response.getStatus() != 200) {
                    // 如果 HTTP 狀態碼不是 200，表示請求失敗
                    // 嘗試解析錯誤訊息
                    if (json.has("errors")) {
                        JSONArray errors = json.getJSONArray("errors");
                        if (errors.length() > 0) {
                            JSONObject error = errors.getJSONObject(0);
                            System.err.println("API 錯誤: " + error.optString("message", "未知錯誤"));
                        }
                    } else {
                        System.err.println("API 錯誤: HTTP " + response.getStatus());
                    }
                    // 終止迴圈
                    hasMore = false;
                    break;
                }
                
                // 步驟 2.4: 檢查回應格式並提取資料
                if (json.has("data") && json.get("data") instanceof JSONArray) {
                    // 獲取城市資料陣列
                    JSONArray data = json.getJSONArray("data");
                    
                    // 如果沒有資料，終止迴圈
                    if (data.length() == 0) {
                        hasMore = false;
                        break;
                    }
                    
                    // 步驟 2.5: 遍歷資料陣列，提取城市資訊
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject city = data.getJSONObject(i);
                        // 提取城市名和人口數
                        String cityName = city.optString("city", "未知");
                        int population = city.optInt("population", 0);
                        
                        // 再次確認人口數是否超過 100 萬（雖然 API 已過濾，但為保險起見）
                        if (population >= 1000000) {
                            // 創建 CityInfo 物件並加入列表
                            cities.add(new CityInfo(cityName, population));
                        }
                    }
                    
                    // 步驟 2.6: 檢查是否還有更多資料需要獲取
                    // 從 metadata 中獲取分頁資訊
                    JSONObject metadata = json.optJSONObject("metadata");
                    if (metadata != null) {
                        // 獲取當前回應的資料筆數和總筆數
                        int currentCount = metadata.optInt("currentCount", 0);
                        int totalCount = metadata.optInt("totalCount", 0);
                        
                        // 如果沒有資料或總筆數為 0，表示沒有更多資料
                        if (currentCount == 0 || totalCount == 0) {
                            hasMore = false;
                        } else {
                            // 更新 offset，準備獲取下一頁資料
                            offset += currentCount;
                            // 檢查是否還有更多資料（offset 小於總筆數）
                            hasMore = offset < totalCount;
                        }
                    } else {
                        // 如果沒有 metadata，根據返回的資料數量判斷
                        // 如果返回的資料數量等於 limit，可能還有更多資料
                        hasMore = data.length() == limit;
                        if (hasMore) {
                            // 更新 offset
                            offset += data.length();
                        }
                    }
                    
                    // 步驟 2.7: 添加延遲以避免速率限制
                    // 如果還有更多資料需要獲取，在下次請求前延遲 2 秒
                    if (hasMore) {
                        try {
                            Thread.sleep(2000); // 延遲 2 秒，避免速率限制
                        } catch (InterruptedException e) {
                            // 如果執行緒被中斷，恢復中斷狀態並終止迴圈
                            Thread.currentThread().interrupt();
                            hasMore = false;
                        }
                    }
                } else {
                    // 如果回應格式錯誤，嘗試解析錯誤訊息
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
                    // 終止迴圈
                    hasMore = false;
                }
            }
            
            // 步驟 3: 按人口數排序（降序）
            // 使用 Collections.sort() 和自定義 Comparator 進行排序
            Collections.sort(cities, new Comparator<CityInfo>() {
                @Override
                public int compare(CityInfo c1, CityInfo c2) {
                    // 按人口數降序排序（c2.population - c1.population）
                    // 使用 Integer.compare() 方法進行比較
                    return Integer.compare(c2.population, c1.population);
                }
            });
            
            // 步驟 4: 格式化並輸出結果
            if (cities.isEmpty()) {
                // 如果沒有找到符合條件的城市，輸出提示訊息
                System.out.println("未找到日本人口超過 100 萬的城市");
            } else {
                // 輸出表格標題
                // 使用 String.format() 創建固定寬度的表格格式
                System.out.println(String.format("%-30s %15s", "城市名", "人口數"));
                System.out.println("-----------------------------------------------");
                
                // 遍歷所有城市，輸出城市名和人口數
                for (CityInfo city : cities) {
                    System.out.println(String.format("%-30s %15s", 
                        city.name, // 城市名（左對齊，寬度 30）
                        String.format("%,d", city.population))); // 人口數（右對齊，寬度 15，帶千位分隔符）
                }
                
                // 輸出總數統計
                System.out.println("\n總共找到 " + cities.size() + " 個城市");
            }
        } catch (Exception e) {
            // 捕獲異常並輸出錯誤訊息
            System.err.println("查詢日本城市時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 城市資訊內部類別
     * 
     * 用於儲存城市的基本資訊，包括城市名和人口數。
     * 這是一個簡單的資料類別（Data Class）。
     */
    private static class CityInfo {
        /** 城市名稱 */
        String name;
        /** 城市人口數 */
        int population;
        
        /**
         * 建構函式
         * 
         * @param name 城市名稱
         * @param population 城市人口數
         */
        CityInfo(String name, int population) {
            this.name = name;
            this.population = population;
        }
    }
}