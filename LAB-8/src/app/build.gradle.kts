plugins {
    alias(libs.plugins.android.application)
    // 強制指定 Kotlin 版本為 2.0.21 (最新穩定版)
    id("org.jetbrains.kotlin.android") version "2.0.21"
    // Kotlin 2.0 專用的 Compose Compiler 插件
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    // Serialization 插件
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.example.kotlin"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.kotlin"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    // ★★★ 注意：Kotlin 2.0 已經不需要 composeOptions { kotlinCompilerExtensionVersion ... } 了，所以這裡刪掉了
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // 導航
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // 測試相關
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ============================================================
    //  Supabase & Ktor 設定 (最新版)
    // ============================================================

    // 1. Supabase BOM
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.2"))

    // 2. Supabase 模組
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")

    // 3. Ktor Client (網路引擎) - 使用最新的 3.0.1
    implementation("io.ktor:ktor-client-android:3.0.1")

    // 4. Ktor JSON 解析
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
}