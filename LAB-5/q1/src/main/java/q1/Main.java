package q1;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;

/**
 * Q1: 名言查詢程式
 * 
 * 此程式使用 Quotes API 查詢隨機名言，並格式化輸出作者和內容。
 * 主要功能包括：
 * 1. 從 .env 文件讀取 API 配置
 * 2. 發送 HTTP 請求到 Quotes API
 * 3. 解析 JSON 回應並提取作者和內容
 * 4. 格式化輸出結果
 * 
 * @author 王建葦
 * @version 1.0
 */
public class Main implements Runnable {
    
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
     * 執行主要程式邏輯：查詢名言並輸出結果
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

            // 步驟 2: 發送 HTTP GET 請求到 Quotes API
            // 使用 Unirest 庫構建 HTTP 請求
            HttpResponse<JsonNode> response = Unirest
                    .get("https://quotes15.p.rapidapi.com/quotes/random/?language_code=en")
                    // 設置 RapidAPI 認證標頭
                    .header("x-rapidapi-key", apiKey)
                    .header("x-rapidapi-host", apiHost)
                    // 將回應解析為 JSON 格式
                    .asJson();

            // 步驟 3: 解析 JSON 回應
            // 從 HttpResponse 中獲取 JSON 物件
            JSONObject json = response.getBody().getObject();

            // 步驟 4: 提取作者名
            // 初始化作者名字串
            String author = "";
            // 檢查 JSON 中是否包含 originator 物件
            if (json.has("originator") && !json.isNull("originator")) {
                // 獲取 originator 物件（包含作者資訊）
                JSONObject originator = json.getJSONObject("originator");
                // 檢查 originator 物件中是否包含 name 欄位
                if (originator.has("name")) {
                    // 提取作者名
                    author = originator.getString("name");
                }
            }

            // 步驟 5: 提取名言內容
            // 初始化內容字串
            String content = "";
            // 檢查 JSON 中是否包含 content 欄位
            if (json.has("content")) {
                // 提取名言內容
                content = json.getString("content");
            }

            // 步驟 6: 格式化並輸出結果
            // 檢查作者名和內容是否都有值
            if (!author.isEmpty() && !content.isEmpty()) {
                // 格式化輸出：智慧箴言：[作者名] 曾說過："[內容]"。
                System.out.println("智慧箴言：" + author + " 曾說過：\"" + content + "\"。");
            } else {
                // 如果缺少資料，輸出錯誤訊息
                System.out.println("無法解析名言資料");
            }
        } catch (Exception e) {
            // 捕獲所有異常並輸出錯誤訊息
            // 這包括網路錯誤、JSON 解析錯誤等
            e.printStackTrace();
        } finally {
            // 步驟 7: 清理資源
            // 關閉 Unirest HTTP 客戶端，釋放網路連接資源
            Unirest.shutDown();
        }
    }
}