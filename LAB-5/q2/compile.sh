#!/bin/bash

# 創建目標目錄
mkdir -p target/classes

# 編譯所有 Java 源代碼
javac -cp "lib/*" src/main/java/q2/*.java -d target/classes

# 檢查編譯是否成功
if [ $? -eq 0 ]; then
    echo "編譯成功！"
    echo "執行方式: java -cp \"lib/*:target/classes\" q2.Main"
else
    echo "編譯失敗！"
    exit 1
fi

