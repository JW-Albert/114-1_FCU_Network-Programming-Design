package q1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class EnvLoader {
    private static Map<String, String> envMap = new HashMap<>();

    static {
        loadEnv();
    }

    private static void loadEnv() {
        try {
            // 嘗試從當前工作目錄讀取 .env 文件
            Path envPath = Paths.get(".env");

            // 如果當前目錄沒有，嘗試從類文件所在目錄的父目錄讀取
            if (!envPath.toFile().exists()) {
                String classPath = EnvLoader.class.getProtectionDomain()
                        .getCodeSource().getLocation().getPath();
                if (classPath != null && !classPath.isEmpty()) {
                    Path classDir = Paths.get(classPath);
                    if (classDir.toFile().isFile()) {
                        // 如果是 JAR 文件，獲取其父目錄
                        classDir = classDir.getParent();
                    }
                    // 向上查找包含 .env 的目錄
                    Path current = classDir;
                    for (int i = 0; i < 5 && current != null; i++) {
                        Path testPath = current.resolve(".env");
                        if (testPath.toFile().exists()) {
                            envPath = testPath;
                            break;
                        }
                        current = current.getParent();
                    }
                }
            }

            if (envPath.toFile().exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(envPath.toFile()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        // 跳過空行和註釋
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }
                        // 解析 KEY=VALUE 格式
                        int equalsIndex = line.indexOf('=');
                        if (equalsIndex > 0) {
                            String key = line.substring(0, equalsIndex).trim();
                            String value = line.substring(equalsIndex + 1).trim();
                            // 移除引號（如果有的話）
                            if (value.startsWith("\"") && value.endsWith("\"")) {
                                value = value.substring(1, value.length() - 1);
                            }
                            envMap.put(key, value);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("警告：無法讀取 .env 文件: " + e.getMessage());
        }
    }

    public static String get(String key) {
        // 優先從系統環境變數讀取，如果沒有則從 .env 文件讀取
        String value = System.getenv(key);
        if (value == null || value.isEmpty()) {
            value = envMap.get(key);
        }
        return value;
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }
}
