const SUPABASE_URL = "https://gmrbggyrldkjfkifhwfy.supabase.co";
const SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImdtcmJnZ3lybGRramZraWZod2Z5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMwNzM0NTIsImV4cCI6MjA3ODY0OTQ1Mn0.KAYrujfSAEzGnCaIhciSIhYHDD1ed50-Oq_FLWpXOPs";

const supabaseClient = supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);

const loginBtn = document.getElementById('loginBtn');
const logoutBtn = document.getElementById('logoutBtn');
const messagesDiv = document.getElementById('messages');
const sendForm = document.getElementById('send-form');
const messageInput = document.getElementById('message-input');

let currentUser = null;

loginBtn.addEventListener('click', () => {
    supabaseClient.auth.signInWithOAuth({
        provider: 'google',
    });
});

logoutBtn.addEventListener('click', () => {
    supabaseClient.auth.signOut();
});

supabaseClient.auth.onAuthStateChange((event, session) => {
    if (event === "SIGNED_IN" && session) {
        currentUser = session.user;
        loginBtn.style.display = 'none';
        logoutBtn.style.display = 'block';
        fetchMessages();
        subscribeToMessages();
    } else {
        currentUser = null;
        loginBtn.style.display = 'block';
        logoutBtn.style.display = 'none';
        messagesDiv.innerHTML = '<div>請登入以查看訊息。</div>';
    }
});

async function fetchMessages() {
    const { data: messages, error } = await supabaseClient
        .from('messages')
        .select('*')
        .order('created_at', { ascending: true });

    if (error) {
        console.error('讀取訊息失敗:', error);
        return;
    }

    messagesDiv.innerHTML = '';
    messages.forEach(msg => displayMessage(msg));
}

// 轉成「上午/下午 hh:mm」
function formatTime(timestamp) {
    const date = new Date(timestamp);
    let hours = date.getHours();
    const minutes = date.getMinutes().toString().padStart(2, '0');

    const period = hours >= 12 ? '下午' : '上午';
    hours = hours % 12;
    if (hours === 0) hours = 12;

    return `${period} ${hours.toString().padStart(2, '0')}:${minutes}`;
}

function displayMessage(msg) {
    const msgElement = document.createElement('div');
    msgElement.classList.add('message');

    const timeString = formatTime(msg.created_at);

    msgElement.innerHTML = `<strong>${msg.user_name} (${timeString}):</strong> ${msg.content}`;

    messagesDiv.appendChild(msgElement);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

sendForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    if (!currentUser || !messageInput.value.trim()) return;

    const { error } = await supabaseClient.from('messages').insert({
        content: messageInput.value.trim(),
        user_name: currentUser.user_metadata.full_name || 'Anonymous'
    });

    if (error) {
        console.error('傳送訊息失敗:', error);
    } else {
        messageInput.value = '';
    }
});

function subscribeToMessages() {
    supabaseClient.channel('public:messages')
        .on('postgres_changes', {
            event: 'INSERT',
            schema: 'public',
            table: 'messages'
        }, (payload) => {
            displayMessage(payload.new);
        })
        .subscribe();
}
