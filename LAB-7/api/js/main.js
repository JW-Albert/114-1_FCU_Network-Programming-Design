const ONESIGNAL_APP_ID = '';
const SUPABASE_URL = '';
const SUPABASE_ANON_KEY = '';
const supabaseClient = supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
const loginBtn = document.getElementById('loginBtn');
const logoutBtn = document.getElementById('logoutBtn');
const messagesDiv = document.getElementById('messages');
const sendForm = document.getElementById('send-form');
const messageInput = document.getElementById('message-input');
let currentUser = null;
(function(i,s,o,g,r,a,m){i['OneSignal']=i['OneSignal']||[];i['OneSignal'].push(function(){
  i['OneSignal'].init({
    appId: ONESIGNAL_APP_ID,
    safari_web_id: 'web.onesignal.auto.42caa6a9-1a36-4188-9a18-8fba4e08de54',
    welcomeNotification: {
      disable: false
    },
    allowLocalhostAsSecureOrigin: true 
   });
});
var a=s.createElement('script');a.async=true;a.src=o;a.onload=function(){
  g(a);
};
var m=s.getElementsByTagName('script')[0];m.parentNode.insertBefore(a,m);
})(window,document,'https://cdn.onesignal.com/sdks/OneSignalSDK.js',function(a){},null,null,null);
loginBtn.addEventListener('click', () => {
    supabaseClient.auth.signInWithOAuth({ provider: 'google' });
});
logoutBtn.addEventListener('click', () => {
    supabaseClient.auth.signOut();
});
supabaseClient.auth.onAuthStateChange((event, session) => {
    if (event === 'SIGNED_IN' && session) {
        currentUser = session.user;
        loginBtn.style.display = 'none';
        logoutBtn.style.display = 'block';
        fetchMessages(); 
        subscribeToMessages();
    } else {
        currentUser = null;
        loginBtn.style.display = 'block';
        logoutBtn.style.display = 'none';
        messagesDiv.innerHTML = '<div>請先登入以查看訊息。</div>';
    }
});
async function fetchMessages() {
    const { data: messages, error } = await supabaseClient.from('messages').select('*').order('created_at', { ascending: true }); 
    if (error) {
        console.error('讀取訊息失敗:', error);
        return;
    }
    messagesDiv.innerHTML = ''; 
    messages.forEach(msg => displayMessage(msg));
}
function displayMessage(msg) {
    const msgElement = document.createElement('div');
    msgElement.classList.add('message');
    msgElement.innerHTML = `<strong>${msg.user_name}:</strong> ${msg.content}`;
    messagesDiv.appendChild(msgElement);
    messagesDiv.scrollTop = messagesDiv.scrollHeight; 
}
sendForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const messageContent = messageInput.value.trim();
    if (!messageContent) return;
    const { data: { user } } = await supabaseClient.auth.getUser();
    if (!user) {
        alert('您似乎已登出，請重新整理頁面再試一次。');
        return;
    }
    const { error } = await supabaseClient.from('messages').insert({
        content: messageContent,
        user_name: user.user_metadata.full_name || 'Anonymous'
    });
    if (error) {
        console.error('傳送訊息失敗:', error);
    } else {
        messageInput.value = ''; 
        try {
            await supabaseClient.functions.invoke('send-notification', {
                body: JSON.stringify({
                    title: `來自 ${user.user_metadata.full_name || '新訊息'}`,
                    body: messageContent
                })
            });
        } catch (invokeError) {
            console.error('呼叫推播功能失敗:', invokeError);
        }
    }
});
function subscribeToMessages() {
    supabaseClient.channel('public:messages')
        .on('postgres_changes', { event: 'INSERT', schema: 'public', table: 'messages' }, (payload) => {
            displayMessage(payload.new);
        })
        .subscribe();
}