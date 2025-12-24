# 🔐 Google Login Setup Guide with Supabase

## 📋 Overview

This guide will help you implement Google Login using **Supabase** for authentication. Firebase will only be used for Analytics, Crashlytics, and Push Notifications.

**Architecture:**
- ✅ **Supabase**: Authentication (Google Login, Email/Password), Database, Storage
- ✅ **Firebase**: Analytics, Crashlytics, Cloud Messaging (Push Notifications)

---

## 🚀 Part 1: Setup Supabase Project

### Step 1: Create Supabase Project

1. Go to [Supabase Dashboard](https://app.supabase.com)
2. Click **"New Project"**
3. Fill in details:
   - **Organization**: Select or create one
   - **Project Name**: `PVC-Perseverance` (or your choice)
   - **Database Password**: Create a strong password (SAVE THIS!)
   - **Region**: Choose closest to your users (e.g., `Southeast Asia (Singapore)`)
4. Click **"Create new project"**
5. Wait ~2 minutes for setup to complete

### Step 2: Get Supabase Credentials

1. In your Supabase dashboard, go to **Settings** → **API**
2. Copy these values:
   - **Project URL**: `https://xxxxx.supabase.co`
   - **anon public key**: Long JWT token starting with `eyJ...`

### Step 3: Update Your `.env` File

1. Open `.env` file in your project root
2. Add your Supabase credentials:

```env
# Supabase Configuration
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...your_actual_key_here
```

⚠️ **Important**: Never commit `.env` to Git! It's already protected in `.gitignore`.

---

## 🔧 Part 2: Configure Google OAuth in Supabase

### Step 1: Get Google OAuth Credentials

You already have Firebase set up, so we'll use the same Google Cloud project:

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Select your project: **`pvc-study-app`** (the same one used for Firebase)
3. Go to **APIs & Services** → **Credentials**

### Step 2: Create OAuth 2.0 Client ID (if not exists)

1. Click **"+ CREATE CREDENTIALS"** → **"OAuth client ID"**
2. If prompted, configure the OAuth consent screen first:
   - **User Type**: External
   - **App name**: PVC Perseverance
   - **User support email**: Your email
   - **Developer contact**: Your email
   - Click **Save and Continue** through all steps
3. Back to creating OAuth client ID:
   - **Application type**: Web application
   - **Name**: `Supabase Auth`
   - **Authorized JavaScript origins**: Leave empty for now
   - **Authorized redirect URIs**: Add this:
     ```
     https://your-project-id.supabase.co/auth/v1/callback
     ```
     (Replace `your-project-id` with your actual Supabase project ID from your SUPABASE_URL)
4. Click **"CREATE"**
5. **SAVE THESE VALUES**:
   - **Client ID**: `xxxxx.apps.googleusercontent.com`
   - **Client Secret**: `GOCSPX-xxxxx`

### Step 3: Configure Google Provider in Supabase

1. In Supabase dashboard, go to **Authentication** → **Providers**
2. Find **Google** in the list
3. Toggle it **ON**
4. Fill in the credentials from Step 2:
   - **Client ID**: Paste your Google Client ID
   - **Client Secret**: Paste your Google Client Secret
5. Click **"Save"**

### Step 4: Update `.env` File with Google Web Client ID

Add the Google Web Client ID to your `.env`:

```env
# Supabase Configuration
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...your_actual_key_here

# Google OAuth (for Supabase)
GOOGLE_WEB_CLIENT_ID=xxxxx.apps.googleusercontent.com
```

---

## 📦 Part 3: Add Supabase Dependencies

### Step 1: Update `app/build.gradle.kts`

Add Supabase dependencies and BuildConfig support:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    kotlin("plugin.serialization") version "1.9.22" // Add this for Supabase
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
        val properties = java.util.Properties()
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
        buildConfig = true  // Enable BuildConfig
    }
}

dependencies {
    // Existing dependencies...
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    // Firebase (Analytics, Crashlytics, Messaging ONLY)
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-messaging")
    
    // ⚠️ REMOVE Firebase Auth - We're using Supabase instead
    // implementation("com.google.firebase:firebase-auth")  // REMOVE THIS LINE
    // implementation("com.google.firebase:firebase-firestore")  // REMOVE THIS LINE
    
    // Google Sign-In (for Supabase OAuth)
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    
    // Supabase Client
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.1.3")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.1.3")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.1.3")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.1.3")
    implementation("io.github.jan-tennert.supabase:compose-auth:2.1.3")
    implementation("io.github.jan-tennert.supabase:compose-auth-ui:2.1.3")
    
    // Ktor Client (required by Supabase)
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-utils:2.3.7")
    
    // Kotlin Serialization (required by Supabase)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    
    // Material icons
    implementation("androidx.compose.material:material-icons-extended")
    
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
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
```

### Step 2: Update `build.gradle.kts` (Project Level)

Add Kotlin serialization plugin:

```kotlin
plugins {
    // ... existing plugins ...
    kotlin("plugin.serialization") version "1.9.22" apply false
}
```

### Step 3: Sync Gradle

Click **"Sync Now"** in Android Studio to download all dependencies.

---

## 💻 Part 4: Implement Supabase Client

### Step 1: Create `SupabaseClient.kt`

Create file: `app/src/main/java/com/perseverance/pvc/data/remote/SupabaseClient.kt`

```kotlin
package com.perseverance.pvc.data.remote

import com.perseverance.pvc.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.FlowType
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth) {
            flowType = FlowType.PKCE
            scheme = "app"
            host = "supabase.com"
        }
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }
}
```

### Step 2: Create `AuthRepository.kt`

Create file: `app/src/main/java/com/perseverance/pvc/data/repository/AuthRepository.kt`

```kotlin
package com.perseverance.pvc.data.repository

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.perseverance.pvc.BuildConfig
import com.perseverance.pvc.data.remote.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.Google
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) {
    
    private val supabase = SupabaseClient.client
    
    // Get current user
    fun getCurrentUser(): UserInfo? {
        return supabase.auth.currentUserOrNull()
    }
    
    // Check if user is logged in
    fun isUserLoggedIn(): Boolean {
        return supabase.auth.currentUserOrNull() != null
    }
    
    // Sign up with email and password
    suspend fun signUpWithEmail(email: String, password: String): Result<UserInfo> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                val user = supabase.auth.currentUserOrNull()
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Sign up failed"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Sign up error", e)
                Result.failure(e)
            }
        }
    }
    
    // Sign in with email and password
    suspend fun signInWithEmail(email: String, password: String): Result<UserInfo> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                val user = supabase.auth.currentUserOrNull()
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("Sign in failed"))
                }
            } catch (e: Exception) {
                Log.e("AuthRepository", "Sign in error", e)
                Result.failure(e)
            }
        }
    }
    
    // Get Google Sign-In Intent
    fun getGoogleSignInIntent(): android.content.Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .requestEmail()
            .build()
        
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        return googleSignInClient.signInIntent
    }
    
    // Sign in with Google (process the result)
    suspend fun signInWithGoogle(data: android.content.Intent?): Result<UserInfo> {
        return withContext(Dispatchers.IO) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                
                if (idToken != null) {
                    supabase.auth.signInWith(Google) {
                        this.idToken = idToken
                    }
                    val user = supabase.auth.currentUserOrNull()
                    if (user != null) {
                        Result.success(user)
                    } else {
                        Result.failure(Exception("Google sign in failed"))
                    }
                } else {
                    Result.failure(Exception("No ID token received"))
                }
            } catch (e: ApiException) {
                Log.e("AuthRepository", "Google sign in error", e)
                Result.failure(e)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Google sign in error", e)
                Result.failure(e)
            }
        }
    }
    
    // Sign out
    suspend fun signOut(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.auth.signOut()
                
                // Also sign out from Google
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                googleSignInClient.signOut()
                
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Sign out error", e)
                Result.failure(e)
            }
        }
    }
    
    // Reset password
    suspend fun resetPassword(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                supabase.auth.resetPasswordForEmail(email)
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e("AuthRepository", "Reset password error", e)
                Result.failure(e)
            }
        }
    }
}
```

### Step 3: Create `AuthViewModel.kt`

Create file: `app/src/main/java/com/perseverance/pvc/ui/viewmodel/AuthViewModel.kt`

```kotlin
package com.perseverance.pvc.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.perseverance.pvc.data.repository.AuthRepository
import io.github.jan.supabase.gotrue.user.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserInfo) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(context: Context) : ViewModel() {
    
    private val authRepository = AuthRepository(context)
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()
    
    init {
        checkCurrentUser()
    }
    
    private fun checkCurrentUser() {
        _currentUser.value = authRepository.getCurrentUser()
    }
    
    fun isUserLoggedIn(): Boolean {
        return authRepository.isUserLoggedIn()
    }
    
    fun signUpWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signUpWithEmail(email, password)
            _authState.value = if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                AuthState.Success(result.getOrNull()!!)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Sign up failed")
            }
        }
    }
    
    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signInWithEmail(email, password)
            _authState.value = if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                AuthState.Success(result.getOrNull()!!)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Sign in failed")
            }
        }
    }
    
    fun getGoogleSignInIntent(): Intent {
        return authRepository.getGoogleSignInIntent()
    }
    
    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.signInWithGoogle(data)
            _authState.value = if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                AuthState.Success(result.getOrNull()!!)
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Google sign in failed")
            }
        }
    }
    
    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _currentUser.value = null
            _authState.value = AuthState.Idle
        }
    }
    
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.resetPassword(email)
            _authState.value = if (result.isSuccess) {
                AuthState.Idle
            } else {
                AuthState.Error(result.exceptionOrNull()?.message ?: "Reset password failed")
            }
        }
    }
    
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
```

---

## 🎨 Part 5: Create Login UI

### Create `LoginScreen.kt`

Create file: `app/src/main/java/com/perseverance/pvc/ui/screens/LoginScreen.kt`

```kotlin
package com.perseverance.pvc.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.perseverance.pvc.ui.viewmodel.AuthState
import com.perseverance.pvc.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(false) }
    
    val authState by authViewModel.authState.collectAsState()
    
    // Google Sign-In launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            authViewModel.handleGoogleSignInResult(result.data)
        }
    }
    
    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                onLoginSuccess()
                authViewModel.resetAuthState()
            }
            else -> {}
        }
    }
    
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo or Title
            Text(
                text = "Welcome to PVC",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isSignUp) "Create your account" else "Sign in to continue",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sign In/Sign Up Button
            Button(
                onClick = {
                    if (isSignUp) {
                        authViewModel.signUpWithEmail(email, password)
                    } else {
                        authViewModel.signInWithEmail(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = authState !is AuthState.Loading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isSignUp) "Sign Up" else "Sign In")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Toggle Sign In/Sign Up
            TextButton(onClick = { isSignUp = !isSignUp }) {
                Text(
                    text = if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "OR",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Google Sign In Button
            OutlinedButton(
                onClick = {
                    val intent = authViewModel.getGoogleSignInIntent()
                    googleSignInLauncher.launch(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = authState !is AuthState.Loading
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // You can add Google icon here
                    Text("Continue with Google")
                }
            }
            
            // Error message
            if (authState is AuthState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
        }
    }
}
```

---

## 🗄️ Part 6: Setup Database Schema (Optional)

If you want to store user profiles in Supabase:

1. Go to Supabase Dashboard → **SQL Editor**
2. Run this SQL:

```sql
-- Create user_profiles table
CREATE TABLE user_profiles (
    id UUID REFERENCES auth.users PRIMARY KEY,
    email TEXT,
    username TEXT,
    avatar_url TEXT,
    rank TEXT DEFAULT 'beginner',
    points INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE user_profiles ENABLE ROW LEVEL SECURITY;

-- Policies
CREATE POLICY "Users can view their own profile"
    ON user_profiles FOR SELECT
    USING (auth.uid() = id);

CREATE POLICY "Users can update their own profile"
    ON user_profiles FOR UPDATE
    USING (auth.uid() = id);

CREATE POLICY "Users can insert their own profile"
    ON user_profiles FOR INSERT
    WITH CHECK (auth.uid() = id);

-- Function to create profile on signup
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.user_profiles (id, email, username)
    VALUES (
        NEW.id,
        NEW.email,
        COALESCE(NEW.raw_user_meta_data->>'name', NEW.email)
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Trigger to auto-create profile
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();
```

---

## ✅ Testing Checklist

- [ ] Created Supabase project
- [ ] Got Supabase URL and anon key
- [ ] Updated `.env` file with Supabase credentials
- [ ] Created Google OAuth credentials in Google Cloud Console
- [ ] Configured Google provider in Supabase
- [ ] Added Google Web Client ID to `.env`
- [ ] Updated `build.gradle.kts` with Supabase dependencies
- [ ] Removed Firebase Auth dependencies
- [ ] Created `SupabaseClient.kt`
- [ ] Created `AuthRepository.kt`
- [ ] Created `AuthViewModel.kt`
- [ ] Created `LoginScreen.kt`
- [ ] Synced Gradle successfully
- [ ] Built the app successfully
- [ ] Tested email sign up
- [ ] Tested email sign in
- [ ] Tested Google sign in
- [ ] Tested sign out

---

## 🐛 Troubleshooting

### Issue: "BuildConfig not found"
**Solution**: 
1. Make sure `buildConfig = true` is in `buildFeatures`
2. Sync Gradle
3. Clean and rebuild project

### Issue: "Supabase URL or Key is empty"
**Solution**:
1. Check `.env` file exists in project root
2. Verify no syntax errors in `.env`
3. Rebuild project after changing `.env`

### Issue: "Google Sign In fails"
**Solution**:
1. Verify Google Web Client ID is correct in `.env`
2. Check authorized redirect URI in Google Cloud Console
3. Make sure Google provider is enabled in Supabase

### Issue: "Unresolved reference: Supabase"
**Solution**:
1. Add `kotlin("plugin.serialization")` to plugins
2. Sync Gradle
3. Clean and rebuild

---

## 📚 Next Steps

After implementing authentication:

1. **User Profile Management**: Create screens to view/edit user profiles
2. **Protected Routes**: Add authentication checks to your navigation
3. **Session Management**: Handle token refresh and session persistence
4. **Database Integration**: Store app data in Supabase tables
5. **Real-time Features**: Use Supabase Realtime for live updates

---

**Need Help?** 
- [Supabase Documentation](https://supabase.com/docs)
- [Supabase Kotlin Client](https://github.com/supabase-community/supabase-kt)
- [Supabase Discord](https://discord.supabase.com)
