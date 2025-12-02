package com.example.kotlin.ui.theme

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.kotlin.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

// 資料結構
@Serializable
data class Message(
    val id: Long = 0,
    val created_at: String? = null,
    val user_id: String,
    val user_email: String?,
    val user_nickname: String? = null,
    val content: String,
)

@Composable
fun ChatScreen(onLogout: () -> Unit) {
    val messages = remember { mutableStateListOf<Message>() }
    var newChatMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // 取得 Context 用來顯示 Toast
    val currentUser = supabase.auth.currentUserOrNull()

    // 取得暱稱
    val currentNickname = currentUser?.userMetadata?.get("nickname")?.jsonPrimitive?.content ?: "無名氏"

    // 載入訊息
    LaunchedEffect(Unit) {
        try {
            val data = supabase.postgrest["messages"]
                .select(Columns.ALL) {
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Message>()
            messages.addAll(data)
        } catch (e: Exception) {
            Toast.makeText(context, "讀取失敗: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // 頂部歡迎列
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("歡迎, $currentNickname", style = MaterialTheme.typography.titleSmall)
            Button(onClick = { scope.launch { onLogout() } }) {
                Text("登出")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 訊息列表
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true
        ) {
            items(messages) { message ->
                val isMe = message.user_id == currentUser?.id
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe) Color(0xFF2196F3) else Color.LightGray
                        )
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = message.user_nickname ?: "未知",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isMe) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                            )
                            Text(
                                text = message.content,
                                color = if (isMe) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 底部發送區
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newChatMessage,
                onValueChange = { newChatMessage = it },
                label = { Text("輸入訊息...") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                if (newChatMessage.isNotEmpty() && currentUser != null) {
                    scope.launch {
                        try {
                            // 嘗試寫入資料庫
                            val newMessage = supabase.postgrest["messages"].insert(
                                buildJsonObject {
                                    put("user_id", currentUser.id)
                                    put("user_email", currentUser.email)
                                    put("user_nickname", currentNickname)
                                    put("content", newChatMessage)
                                }
                            ) {
                                select()
                                single()
                            }.decodeAs<Message>()

                            messages.add(0, newMessage)
                            newChatMessage = "" // 成功才清空

                        } catch (e: Exception) {
                            // ★★★ 這裡會顯示錯誤原因 ★★★
                            // 請告訴我畫面上跳出的訊息是什麼
                            e.printStackTrace()
                            Toast.makeText(context, "發送失敗: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "請先輸入文字或確認已登入", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("傳送")
            }
        }
    }
}