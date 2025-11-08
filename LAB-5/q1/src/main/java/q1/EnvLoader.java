package q1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 環境變數載入器
 * 
 * 此類別負責從 .env 文件讀取環境變數，提供一個簡單的方式來管理應用程式的配置。
 * 主要功能包括：
 * 1. 自動查找 .env 文件（從當前目錄或類別文件所在目錄向上查找）
 * 2. 解析 .env 文件中的 KEY=VALUE 格式
 * 3. 提供獲取環境變數的方法，優先從系統環境變數讀取
 * 
 * @author 王建葦
 * @version 1.0
 */
public class EnvLoader {
    /**
     * 儲存從 .env 文件讀取的環境變數鍵值對
     * 使用 HashMap 提供快速的鍵值查找
     */
    private static Map<String, String> envMap = new HashMap<>();

    /**
     * 靜態初始化區塊
     * 在類別載入時自動執行，讀取 .env 文件
     */
    static {
        loadEnv();
    }

    /**
     * 載入 .env 文件
     * 
     * 此方法會：
     * 1. 嘗試從當前工作目錄讀取 .env 文件
     * 2. 如果當前目錄沒有，則從類別文件所在目錄向上查找
     * 3. 解析文件內容並存入 envMap
     */
    private static void loadEnv() {
        try {
            // 步驟 1: 嘗試從當前工作目錄讀取 .env 文件
            // 使用 Paths.get() 創建路徑物件
            Path envPath = Paths.get(".env");

            // 步驟 2: 如果當前目錄沒有 .env 文件，嘗試從類別文件所在目錄查找
            if (!envPath.toFile().exists()) {
                // 獲取類別文件所在的路徑
                // 使用反射機制獲取類別載入位置
                String classPath = EnvLoader.class.getProtectionDomain()
                        .getCodeSource().getLocation().getPath();
                
                // 檢查類別路徑是否有效
                if (classPath != null && !classPath.isEmpty()) {
                    // 將字串路徑轉換為 Path 物件
                    Path classDir = Paths.get(classPath);
                    
                    // 如果路徑指向一個文件（例如 JAR 文件），獲取其父目錄
                    if (classDir.toFile().isFile()) {
                        classDir = classDir.getParent();
                    }
                    
                    // 步驟 3: 向上查找包含 .env 的目錄
                    // 從類別文件所在目錄開始，向上查找最多 5 層
                    Path current = classDir;
                    for (int i = 0; i < 5 && current != null; i++) {
                        // 在當前目錄下查找 .env 文件
                        Path testPath = current.resolve(".env");
                        if (testPath.toFile().exists()) {
                            // 找到 .env 文件，記錄路徑並跳出迴圈
                            envPath = testPath;
                            break;
                        }
                        // 向上移動到父目錄
                        current = current.getParent();
                    }
                }
            }

            // 步驟 4: 如果找到 .env 文件，讀取並解析
            if (envPath.toFile().exists()) {
                // 使用 try-with-resources 語法自動關閉文件流
                try (BufferedReader reader = new BufferedReader(new FileReader(envPath.toFile()))) {
                    String line;
                    // 逐行讀取文件內容
                    while ((line = reader.readLine()) != null) {
                        // 移除行首尾的空白字元
                        line = line.trim();
                        
                        // 跳過空行和註釋行（以 # 開頭的行）
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        
                        // 步驟 5: 解析 KEY=VALUE 格式
                        // 查找等號的位置
                        int equalsIndex = line.indexOf('=');
                        if (equalsIndex > 0) {
                            // 提取鍵（等號前的部分）
                            String key = line.substring(0, equalsIndex).trim();
                            // 提取值（等號後的部分）
                            String value = line.substring(equalsIndex + 1).trim();
                            
                            // 步驟 6: 移除值兩側的引號（如果有的話）
                            // 這允許在 .env 文件中使用引號包圍值
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length() - 1);
                            }
                            
                            // 將解析後的鍵值對存入 HashMap
                            envMap.put(key, value);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // 如果讀取文件時發生錯誤，輸出警告訊息
            // 不拋出異常，允許程式繼續執行（可能使用系統環境變數）
            System.err.println("警告：無法讀取 .env 文件: " + e.getMessage());
        }
    }

    /**
     * 獲取環境變數值
     * 
     * 此方法優先從系統環境變數讀取，如果系統環境變數中沒有，
     * 則從 .env 文件讀取。
     * 
     * @param key 環境變數的鍵名
     * @return 環境變數的值，如果不存在則返回 null
     */
    public static String get(String key) {
        // 優先從系統環境變數讀取
        // System.getenv() 返回系統環境變數的值
        String value = System.getenv(key);
        
        // 如果系統環境變數中沒有，則從 .env 文件讀取
        if (value == null || value.isEmpty()) {
            value = envMap.get(key);
        }
        
        return value;
    }

    /**
     * 獲取環境變數值（帶預設值）
     * 
     * 如果環境變數不存在，返回預設值。
     * 
     * @param key 環境變數的鍵名
     * @param defaultValue 如果環境變數不存在時返回的預設值
     * @return 環境變數的值，如果不存在則返回預設值
     */
    public static String get(String key, String defaultValue) {
        // 先嘗試獲取環境變數值
        String value = get(key);
        // 如果值為 null，返回預設值；否則返回實際值
        return value != null ? value : defaultValue;
    }
}