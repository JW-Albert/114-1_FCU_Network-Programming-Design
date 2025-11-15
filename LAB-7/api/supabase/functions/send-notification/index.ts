import { serve } from "https://deno.land/std@0.177.0/http/server.ts";
const ONESIGNAL_API_KEY = Deno.env.get("ONESIGNAL_API_KEY");
const ONESIGNAL_APP_ID = "7ea24596-8ddb-4120-b4b1-382f8643e845";
const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-client-info, apikey, content-type",
};
serve(async (req) => {
  if (req.method === "OPTIONS") {
    return new Response("ok", { headers: corsHeaders });
  }
  try {
    const { title, body } = await req.json();
    console.log("偵錯: 即將發送的 OneSignal 資料", JSON.stringify({
      app_id: ONESIGNAL_APP_ID,
      included_segments: ["All"],
      contents: { "en": body },
      headings: { "en": title }
    }));
    const response = await fetch("https://onesignal.com/api/v1/notifications", {
      method: "POST",
      headers: {
        "Authorization": `Basic ${ONESIGNAL_API_KEY}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        app_id: ONESIGNAL_APP_ID,
        included_segments: ["All"],
        contents: { "en": body },
        headings: { "en": title },
      }),
    });
    const responseBody = await response.text();
    if (!response.ok) {
      console.error('OneSignal API 錯誤回應:', responseBody);
      throw new Error(`Failed to send notification via OneSignal: ${responseBody}`);
    }
    console.log('OneSignal API 成功回應:', responseBody);
    console.log("推播發送成功！");
    return new Response(JSON.stringify({ message: "Notification dispatch completed." }), {
      status: 200,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (err) {
    const message = err instanceof Error ? err.message : String(err);
    console.error('函式執行錯誤:', err);
    return new Response(JSON.stringify({ error: message }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  }
});