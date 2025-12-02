package com.example.kotlin.ui.theme

import com.example.kotlin.supabase
import io.github.jan.supabase.auth.auth


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put


@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // 練習題修改：新增暱稱變數
    var nickname by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("註冊", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // 練習題修改：新增暱稱輸入框
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("暱稱 (Nickname)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("電子郵件") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("密碼") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                scope.launch {
                    try {
                        supabase.auth.signUpWith(Email) {
                            this.email = email
                            this.password = password
                            // 練習題修改：將暱稱寫入 Supabase 的使用者 Metadata
                            data = buildJsonObject {
                                put("nickname", nickname)
                            }
                        }
                        onRegisterSuccess()
                    } catch (e: Exception) {
                        errorMessage = "註冊失敗: ${e.message}"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("註冊")
        }
    }
}