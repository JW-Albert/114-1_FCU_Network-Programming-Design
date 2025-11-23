// supabase/functions/send-notification/index.ts

// Edge Function：收到前端 POST 後，呼叫 OneSignal API 推播

import { serve } from "https://deno.land/std@v1.0.0/http/server.ts";

serve(async (req) => {
  try {
    const { title, messageCount } = await req.json();

    // OneSignal API KEY
    const ONESIGNAL_API_KEY = Deno.env.get("os_v2_app_33at6sskcvg35meltbnyr7gsy7qyzuhsak3uee5h5dhraj4qlhe7yufttfg2x35zhcokcum4jy4uznb6xzz6diiqxyiz7lmkro57msy");

    // OneSignal App ID
    const ONESIGNAL_APP_ID = Deno.env.get("dec13f4a-4a15-4dbe-b08b-985b88fcd2c7");

    if (!ONESIGNAL_API_KEY || !ONESIGNAL_APP_ID) {
      return new Response(
        JSON.stringify({ error: "Missing OneSignal environment variables" }),
        { status: 500 }
      );
    }

    // OneSignal API endpoint
    const url = "https://api.onesignal.com/notifications";

    // API payload
    const body = {
      app_id: ONESIGNAL_APP_ID,
      included_segments: ["All"],   // 發給所有已訂閱的用戶
      headings: { en: title },
      contents: { en: `聊天室目前共有 ${messageCount} 則訊息` },
    };

    const resp = await fetch(url, {
      method: "POST",
      headers: {
        "Authorization": `Basic ${ONESIGNAL_API_KEY}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify(body)
    });

    const data = await resp.json();

    return new Response(JSON.stringify(data), {
      headers: { "Content-Type": "application/json" },
    });

  } catch (err) {
    return new Response(
      JSON.stringify({ error: err.message }),
      { status: 400 }
    );
  }
});
