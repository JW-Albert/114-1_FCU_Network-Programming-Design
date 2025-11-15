# LAB-6

## 學生資料

- **課程** 1141 網路程式設計實習(資訊三合)[1402]
- **班級** 資訊三乙
- **姓名** 王建葦
- **學號** D1210799

## 環境需求

- 現代網頁瀏覽器（Chrome、Firefox、Edge 等）
- 網際網路連線（用於連接 Supabase 服務）
- 本地網頁伺服器（可選，用於開發測試）

## 專案結構

```
LAB-6/
├── src/                          # 原始碼目錄
│   ├── index.html               # 主頁面 HTML 檔案
│   └── js/
│       └── main.js              # 主要 JavaScript 邏輯
│
└── Demo/                        # 示範截圖
    ├── Unlogin.png             # 未登入狀態截圖
    ├── LoginPage.png          # 登入頁面截圖
    └── SentMsg.png            # 傳送訊息截圖
```

## 題目說明

### Supabase 即時聊天室

使用 Supabase 建立一個即時聊天室應用程式，支援 Google OAuth 身份驗證和即時訊息傳送功能。

**功能：**
1. **Google OAuth 身份驗證**
   - 使用 Google 帳號登入
   - 自動管理登入狀態
   - 支援登出功能

2. **即時訊息功能**
   - 顯示所有歷史訊息
   - 傳送新訊息
   - 即時接收其他使用者的訊息（無需重新整理頁面）
   - 訊息顯示格式：`使用者名稱 (時間): 訊息內容`

3. **使用者介面**
   - 簡潔的聊天室介面
   - 訊息區域自動捲動至最新訊息
   - 登入/登出按鈕動態顯示

**執行方式：**

### 方法一：使用本地網頁伺服器（推薦）

```bash
# 使用 Python 內建伺服器
cd LAB-6/src
python3 -m http.server 8000

# 或使用 Node.js http-server
npx http-server -p 8000
```

然後在瀏覽器中開啟 `http://localhost:8000`

### 方法二：直接開啟 HTML 檔案

直接在瀏覽器中開啟 `src/index.html` 檔案（某些瀏覽器可能因安全限制無法正常運作）

## 環境變數配置

本專案使用 Supabase 服務，需要在 `src/js/main.js` 中配置 Supabase 連線資訊：

```javascript
const SUPABASE_URL = "your_supabase_project_url";
const SUPABASE_ANON_KEY = "your_supabase_anon_key";
```

**重要：** 如果將程式碼上傳到公開儲存庫，建議使用環境變數或配置檔案來管理敏感資訊。

## Supabase 資料庫設定

### 建立 messages 資料表

在 Supabase 資料庫中建立 `messages` 資料表，包含以下欄位：

```sql
CREATE TABLE messages (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    content TEXT NOT NULL,
    user_name TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

### 設定 Row Level Security (RLS)

啟用 RLS 並設定適當的政策：

```sql
-- 啟用 RLS
ALTER TABLE messages ENABLE ROW LEVEL SECURITY;

-- 允許所有人讀取訊息
CREATE POLICY "任何人都可以讀取訊息"
ON messages FOR SELECT
USING (true);

-- 允許已登入使用者插入訊息
CREATE POLICY "已登入使用者可以插入訊息"
ON messages FOR INSERT
WITH CHECK (auth.role() = 'authenticated');
```

### 設定 Google OAuth

1. 在 Supabase 專案設定中啟用 Google OAuth
2. 配置 Google OAuth 憑證（Client ID 和 Client Secret）
3. 設定重定向 URL

## 編譯與執行

本專案為前端網頁應用程式，無需編譯步驟。只需：

1. 確保 Supabase 專案已正確設定
2. 在 `main.js` 中配置 Supabase URL 和 Anon Key
3. 使用網頁伺服器開啟 `index.html` 或直接開啟檔案

## 技術特點

### 使用的技術

- **Supabase**: 後端即服務（BaaS），提供：
  - PostgreSQL 資料庫
  - 身份驗證服務（Google OAuth）
  - 即時訂閱功能（Realtime）
- **Supabase JavaScript Client**: 官方 JavaScript SDK
- **原生 JavaScript**: 無需額外框架
- **HTML5/CSS3**: 現代網頁標準

### 程式設計特點

- **即時訊息同步**: 使用 Supabase Realtime 訂閱功能，自動接收新訊息
- **身份驗證整合**: 完整的 Google OAuth 登入流程
- **狀態管理**: 自動管理使用者登入狀態和 UI 更新
- **時間格式化**: 將時間戳記轉換為易讀的「上午/下午 hh:mm」格式
- **錯誤處理**: 完整的錯誤處理機制

### 依賴項

- `@supabase/supabase-js@2`: Supabase JavaScript 客戶端庫（透過 CDN 載入）

## 注意事項

1. **Supabase 設定**: 確保 Supabase 專案已正確設定，包括：
   - 資料表結構
   - Row Level Security 政策
   - Google OAuth 設定
   - Realtime 功能已啟用

2. **CORS 設定**: 如果使用本地開發伺服器，確保 Supabase 專案允許的來源包含你的開發 URL

3. **安全性**: 
   - 使用 Anon Key 是安全的，因為有 RLS 政策保護
   - 不要將 Service Role Key 暴露在前端程式碼中

4. **瀏覽器相容性**: 需要支援 ES6+ 語法和 async/await 的現代瀏覽器

5. **網路連線**: 應用程式需要網際網路連線以連接 Supabase 服務

## 開發環境

- **作業系統**: Linux (DietPi v9.18.1)
- **瀏覽器**: Chrome、Firefox 或 Edge（最新版本）
- **網頁伺服器**: Python http.server 或 Node.js http-server（開發用）
- **Supabase**: 雲端服務（無需本地安裝）

## 程式碼運作說明

### 主要檔案說明

**index.html** 是應用程式的主頁面，包含：

1. **HTML 結構**
   - 標題和登入/登出按鈕
   - 訊息顯示區域（`#messages`）
   - 訊息輸入表單（`#send-form`）

2. **樣式定義**
   - 簡潔的 CSS 樣式
   - 訊息區域的捲動設定
   - 使用者名稱的顏色標示

3. **腳本載入**
   - Supabase JavaScript SDK（透過 CDN）
   - 本地 `main.js` 檔案

**main.js** 包含所有應用程式邏輯：

### 初始化階段

1. **Supabase 客戶端建立**
   ```javascript
   const supabaseClient = supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
   ```
   - 使用專案 URL 和 Anon Key 建立 Supabase 客戶端實例

2. **DOM 元素選取**
   - 選取所有需要的 HTML 元素（按鈕、表單、訊息區域等）

3. **初始會話檢查**
   - 使用 `getSession()` 檢查使用者是否已登入
   - 根據登入狀態更新 UI 和載入訊息

### 身份驗證流程

1. **Google 登入**
   ```javascript
   supabaseClient.auth.signInWithOAuth({ provider: 'google' });
   ```
   - 觸發 Google OAuth 登入流程
   - 使用者會被重定向到 Google 登入頁面
   - 登入成功後自動重定向回應用程式

2. **登出功能**
   ```javascript
   supabaseClient.auth.signOut();
   ```
   - 清除使用者會話
   - 更新 UI 狀態

3. **身份驗證狀態監聽**
   ```javascript
   supabaseClient.auth.onAuthStateChange((event, session) => { ... });
   ```
   - 監聽登入/登出事件
   - 自動更新 UI 和載入/清除訊息

### 訊息功能

1. **載入歷史訊息**
   ```javascript
   async function fetchMessages() {
       const { data: messages, error } = await supabaseClient
           .from('messages')
           .select('*')
           .order('created_at', { ascending: true });
       // ...
   }
   ```
   - 從 `messages` 資料表查詢所有訊息
   - 按建立時間升序排列
   - 顯示在訊息區域中

2. **傳送訊息**
   ```javascript
   await supabaseClient.from('messages').insert({
       content: messageInput.value.trim(),
       user_name: session.user.user_metadata?.full_name || session.user.email || 'Anonymous'
   });
   ```
   - 插入新訊息到資料庫
   - 使用使用者名稱（從 Google 帳號取得）或電子郵件
   - 自動包含時間戳記（由資料庫處理）

3. **即時訊息訂閱**
   ```javascript
   supabaseClient.channel('public:messages')
       .on('postgres_changes', {
           event: 'INSERT',
           schema: 'public',
           table: 'messages'
       }, (payload) => {
           displayMessage(payload.new);
       })
       .subscribe();
   ```
   - 訂閱 `messages` 資料表的 INSERT 事件
   - 當有新訊息插入時，自動呼叫 `displayMessage()` 顯示

### 訊息顯示

1. **時間格式化**
   ```javascript
   function formatTime(timestamp) {
       const date = new Date(timestamp);
       let hours = date.getHours();
       const minutes = date.getMinutes().toString().padStart(2, '0');
       const period = hours >= 12 ? 'p.m.' : 'a.m.';
       hours = hours % 12;
       if (hours === 0) hours = 12;
       return `${period} ${hours.toString().padStart(2, '0')}:${minutes}`;
   }
   ```
   - 將 ISO 時間戳記轉換為「上午/下午 hh:mm」格式
   - 處理 12 小時制轉換

2. **訊息顯示**
   ```javascript
   function displayMessage(msg) {
       const msgElement = document.createElement('div');
       msgElement.classList.add('message');
       const timeString = formatTime(msg.created_at);
       msgElement.innerHTML = `<strong>${msg.user_name} (${timeString}):</strong> ${msg.content}`;
       messagesDiv.appendChild(msgElement);
       messagesDiv.scrollTop = messagesDiv.scrollHeight;
   }
   ```
   - 建立訊息 DOM 元素
   - 格式化並顯示使用者名稱、時間和內容
   - 自動捲動至最新訊息

### 資料流程

**登入流程：**
```
使用者點擊登入按鈕
    ↓
Supabase OAuth 流程
    ↓
Google 登入頁面
    ↓
重定向回應用程式
    ↓
onAuthStateChange 事件觸發
    ↓
載入歷史訊息 + 訂閱即時更新
```

**訊息傳送流程：**
```
使用者輸入訊息並提交
    ↓
檢查登入狀態
    ↓
插入訊息到 Supabase 資料庫
    ↓
資料庫觸發 INSERT 事件
    ↓
Realtime 訂閱接收事件
    ↓
自動顯示新訊息（所有連線的使用者）
```

**即時同步流程：**
```
使用者 A 傳送訊息
    ↓
訊息插入到資料庫
    ↓
Supabase Realtime 廣播事件
    ↓
所有訂閱的使用者（A、B、C...）收到事件
    ↓
自動更新各自的訊息顯示區域
```

### 錯誤處理機制

1. **API 錯誤處理**
   - 所有 Supabase 操作都檢查 `error` 物件
   - 錯誤訊息輸出到控制台
   - 使用者可見的錯誤顯示警告訊息

2. **狀態驗證**
   - 傳送訊息前檢查登入狀態
   - 驗證訊息內容不為空
   - 處理未登入狀態的 UI 顯示

3. **網路錯誤處理**
   - Supabase 客戶端自動處理網路錯誤
   - 提供重試機制（內建）

### 安全性考量

1. **Row Level Security (RLS)**
   - 資料庫層面的安全政策
   - 控制誰可以讀取/寫入訊息

2. **Anon Key 使用**
   - 使用 Anon Key 而非 Service Role Key
   - RLS 政策確保資料安全

3. **身份驗證要求**
   - 只有已登入使用者可以傳送訊息
   - 使用 Supabase 內建的身份驗證機制

## 示範截圖

- **Unlogin.png**: 顯示未登入狀態的介面
- **LoginPage.png**: 顯示登入頁面
- **SentMsg.png**: 顯示傳送訊息後的介面

## 參考資源

- [Supabase 官方文件](https://supabase.com/docs)
- [Supabase JavaScript 客戶端文件](https://supabase.com/docs/reference/javascript/introduction)
- [Supabase Realtime 文件](https://supabase.com/docs/guides/realtime)
- [Supabase 身份驗證文件](https://supabase.com/docs/guides/auth)

