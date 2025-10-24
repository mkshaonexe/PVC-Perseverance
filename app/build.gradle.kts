plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.perseverance.pvc"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.perseverance.pvc"
        minSdk = 24
        targetSdk = 36
        versionCode = 11
        versionName = "0.2.6"

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
    // Material icons (for pause/stop icons)
    implementation("androidx.compose.material:material-icons-extended")
    
    // Video dependencies removed - no longer needed
    
    // ViewModel and Compose integration
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.5.8")
    
    // ViewPager for swipe navigation
    implementation("androidx.compose.foundation:foundation:1.5.8")
    implementation("com.google.accompanist:accompanist-pager:0.32.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0")
    
    // DataStore for offline storage
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
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