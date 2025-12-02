package com.example.kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kotlin.ui.theme.ChatScreen
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import com.example.kotlin.ui.theme.KotlinTheme // 依據你的專案名稱調整
import com.example.kotlin.ui.theme.LoginScreen
import com.example.kotlin.ui.theme.RegisterScreen

// 初始化 Supabase Client
val supabase = createSupabaseClient(
    supabaseUrl = "https://aclchdkvznzmshbudbbp.supabase.co", // 請填入你的 Project URL
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImFjbGNoZGt2em56bXNoYnVkYmJwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjQ0OTY0MzQsImV4cCI6MjA4MDA3MjQzNH0.NF4gfArKFycZaaUoX9PR2QCadW1N56chkMgfustSoSc" // 請填入你的 API Key (anon public)
) {
    install(Auth)
    install(Postgrest)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KotlinTheme { // 你的 Theme 名稱可能不同
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // 登入頁面
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("chatScreen") {
                        popUpTo("login") { inclusive = true } // 清除堆疊，讓使用者按返回鍵不會回到登入頁
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        // 註冊頁面
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // 聊天室頁面
        composable("chatScreen") {
            ChatScreen(
                onLogout = {
                    scope.launch {
                        supabase.auth.signOut() // 登出
                    }
                    navController.navigate("login") {
                        popUpTo("chatScreen") { inclusive = true }
                    }
                }
            )
        }
    }
}