package q1;

import kong.unirest.Unirest;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;

public class Main implements Runnable {
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

            HttpResponse<JsonNode> response = Unirest
                    .get("https://quotes15.p.rapidapi.com/quotes/random/?language_code=en")
                    .header("x-rapidapi-key", apiKey)
                    .header("x-rapidapi-host", apiHost)
                    .asJson();

            JSONObject json = response.getBody().getObject();

            // 提取作者名和內容
            String author = "";
            if (json.has("originator") && !json.isNull("originator")) {
                JSONObject originator = json.getJSONObject("originator");
                if (originator.has("name")) {
                    author = originator.getString("name");
                }
            }

            String content = "";
            if (json.has("content")) {
                content = json.getString("content");
            }

            // 格式化輸出
            if (!author.isEmpty() && !content.isEmpty()) {
                System.out.println("智慧箴言：" + author + " 曾說過：\"" + content + "\"。");
            } else {
                System.out.println("無法解析名言資料");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Unirest.shutDown();
        }
    }
}