// ===== Supabase Client =====
const SUPABASE_URL = "https://gmrbggyrldkjfkifhwfy.supabase.co";
const SUPABASE_ANON_KEY =
    "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdtcmJnZ3lybGRramZraWZod2Z5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMwNzM0NTIsImV4cCI6MjA3ODY0OTQ1Mn0.KAYrujfSAEzGnCaIhciSIhYHDD1ed50-Oq_FLWpXOPs";

const supabaseClient = supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);

// ===== OneSignal 前端初始化 =====
window.OneSignal = window.OneSignal || [];
OneSignal.push(function () {
    OneSignal.init({
        appId: "os_v2_app_33at6sskcvg35meltbnyr7gsy7qyzuhsak3uee5h5dhraj4qlhe7yufttfg2x35zhcokcum4jy4uznb6xzz6diiqxyiz7lmkro57msy",  // ← 你要自己填
    });
});

// DOM Elements
const loginBtn = document.getElementById("loginBtn");
const logoutBtn = document.getElementById("logoutBtn");
const messagesDiv = document.getElementById("messages");
const sendForm = document.getElementById("send-form");
const messageInput = document.getElementById("message-input");

let currentUser = null;

// ===== Login =====
loginBtn.addEventListener("click", () => {
    supabaseClient.auth.signInWithOAuth({ provider: "google" });
});

logoutBtn.addEventListener("click", () => {
    supabaseClient.auth.signOut();
});

// ===== Session Check =====
supabaseClient.auth.getSession().then(({ data: { session } }) => {
    if (session) {
        currentUser = session.user;
        loginBtn.style.display = "none";
        logoutBtn.style.display = "block";
        fetchMessages();
        subscribeToMessages();
    } else {
        currentUser = null;
        loginBtn.style.display = "block";
        logoutBtn.style.display = "none";
        messagesDiv.innerHTML = "<div>請登入以查看訊息。</div>";
    }
});

// ===== Session Change =====
supabaseClient.auth.onAuthStateChange((event, session) => {
    if (event === "SIGNED_IN" && session) {
        currentUser = session.user;
        loginBtn.style.display = "none";
        logoutBtn.style.display = "block";
        fetchMessages();
        subscribeToMessages();
    } else if (event === "SIGNED_OUT") {
        currentUser = null;
        loginBtn.style.display = "block";
        logoutBtn.style.display = "none";
        messagesDiv.innerHTML = "<div>請登入以查看訊息。</div>";
    }
});

// ===== Fetch Messages =====
async function fetchMessages() {
    const { data: messages, error } = await supabaseClient
        .from("messages")
        .select("*")
        .order("created_at", { ascending: true });

    if (error) {
        console.error("讀取訊息失敗:", error);
        return;
    }

    messagesDiv.innerHTML = "";
    messages.forEach((msg) => displayMessage(msg));
}

// ===== Time Format =====
function formatTime(timestamp) {
    const date = new Date(timestamp);
    let hours = date.getHours();
    const minutes = date.getMinutes().toString().padStart(2, "0");

    const period = hours >= 12 ? "p.m." : "a.m.";
    hours = hours % 12;
    if (hours === 0) hours = 12;

    return `${period} ${hours.toString().padStart(2, "0")}:${minutes}`;
}

// ===== Render Message =====
function displayMessage(msg) {
    const msgElement = document.createElement("div");
    msgElement.classList.add("message");

    const timeString = formatTime(msg.created_at);

    msgElement.innerHTML = `<strong>${msg.user_name} (${timeString}):</strong> ${msg.content}`;
    messagesDiv.appendChild(msgElement);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

// ===== Send Message =====
sendForm.addEventListener("submit", async (e) => {
    e.preventDefault();

    const { data: { session } } = await supabaseClient.auth.getSession();
    if (!session || !messageInput.value.trim()) {
        console.log("無法傳送：未登入或訊息為空");
        return;
    }

    const content = messageInput.value.trim();

    // 1️⃣ 先插入訊息
    const { error } = await supabaseClient.from("messages").insert({
        content,
        user_name:
            session.user.user_metadata?.full_name ||
            session.user.email ||
            "Anonymous",
    });

    if (error) {
        console.error("傳送訊息失敗:", error);
        alert("傳送訊息失敗: " + error.message);
        return;
    }

    // 2️⃣ 取得最新訊息數量
    const { count } = await supabaseClient
        .from("messages")
        .select("*", { count: "exact", head: true });

    // 3️⃣ 根據關鍵字決定推播標題
    let title = "新訊息提醒";
    if (content.includes("讚")) {
        title = "關鍵訊息";
    }

    // 4️⃣ 呼叫 Supabase Edge Function 送推播
    await fetch(
        `${SUPABASE_URL}/functions/v1/send-notification`,
        {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                title,
                messageCount: count,
            }),
        }
    );

    messageInput.value = "";
});

// ===== Realtime Subscriptions =====
function subscribeToMessages() {
    supabaseClient
        .channel("public:messages")
        .on(
            "postgres_changes",
            { event: "INSERT", schema: "public", table: "messages" },
            (payload) => {
                displayMessage(payload.new);
            }
        )
        .subscribe();
}
