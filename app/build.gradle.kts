import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.perseverance.pvc"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.perseverance.pvc"
        minSdk = 24
        targetSdk = 36
        versionCode = 14
        versionName = "0.4.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Load environment variables from .env file
        val properties = Properties()
        val envFile = rootProject.file(".env")
        if (envFile.exists()) {
            envFile.inputStream().use { properties.load(it) }
        }

        // Add BuildConfig fields
        buildConfigField("String", "SUPABASE_URL", "\"${properties.getProperty("SUPABASE_URL", "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${properties.getProperty("SUPABASE_ANON_KEY", "")}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${properties.getProperty("GOOGLE_WEB_CLIENT_ID", "")}\"")
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
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    // Firebase BoM
    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:3.0.1")) // Bump to 3.0.1+ if possible, or stick to a known stable for auth-kt
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:auth-kt") // Renamed from gotrue-kt
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.github.jan-tennert.supabase:compose-auth")
    implementation("io.github.jan-tennert.supabase:compose-auth-ui")

    // Ktor (required by Supabase)
    implementation("io.ktor:ktor-client-android:3.0.0")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Material icons (for pause/stop icons)
    implementation("androidx.compose.material:material-icons-extended")
    
    // Video dependencies removed - no longer needed
    
    // ViewModel and Compose integration
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.compose.runtime:runtime-livedata:1.7.2")
    
    // ViewPager for swipe navigation
    implementation("androidx.compose.foundation:foundation:1.7.2")
    implementation("com.google.accompanist:accompanist-pager:0.32.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0")
    
    // DataStore for offline storage
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    
    // Gson for JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}