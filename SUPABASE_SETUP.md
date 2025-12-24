# Supabase Integration Guide for PVC-Perseverance

## 📋 Overview
This guide will help you integrate Supabase into your Android application for backend services and authentication.

## 🚀 Getting Started

### 1. Create a Supabase Project

1. Go to [Supabase Dashboard](https://app.supabase.com)
2. Click "New Project"
3. Fill in your project details:
   - **Project Name**: PVC-Perseverance (or your preferred name)
   - **Database Password**: Create a strong password (save this!)
   - **Region**: Choose the closest region to your users
4. Click "Create new project"
5. Wait for the project to be set up (takes ~2 minutes)

### 2. Get Your Supabase Credentials

Once your project is ready:

1. Go to **Settings** → **API** in your Supabase dashboard
2. You'll find two important values:
   - **Project URL**: `https://your-project-id.supabase.co`
   - **anon/public key**: A long JWT token starting with `eyJ...`

### 3. Configure Your `.env` File

1. Open the `.env` file in your project root
2. Replace the placeholder values:

```env
SUPABASE_URL=https://xyzcompany.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh5emNvbXBhbnkiLCJyb2xlIjoiYW5vbiIsImlhdCI6MTYxNjE2MTYxNiwiZXhwIjoxOTMxNzM3NjE2fQ.example_key_here
```

⚠️ **Important**: Never commit the `.env` file to Git! It's already in `.gitignore`.

## 📦 Next Steps: Add Supabase Dependencies

You'll need to add the Supabase Kotlin client to your `app/build.gradle.kts`:

### Dependencies to Add:

```kotlin
dependencies {
    // ... existing dependencies ...
    
    // Supabase Client
    implementation("io.github.jan-tennert.supabase:postgrest-kt:2.0.0")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.0.0")
    implementation("io.github.jan-tennert.supabase:realtime-kt:2.0.0")
    implementation("io.github.jan-tennert.supabase:storage-kt:2.0.0")
    
    // Ktor Client (required by Supabase)
    implementation("io.ktor:ktor-client-android:2.3.7")
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-utils:2.3.7")
}
```

### Enable BuildConfig for Environment Variables:

Add this to your `android` block in `app/build.gradle.kts`:

```kotlin
android {
    // ... existing config ...
    
    buildFeatures {
        compose = true
        buildConfig = true  // Add this line
    }
    
    defaultConfig {
        // ... existing config ...
        
        // Load environment variables
        val properties = Properties()
        val envFile = rootProject.file(".env")
        if (envFile.exists()) {
            envFile.inputStream().use { properties.load(it) }
        }
        
        buildConfigField("String", "SUPABASE_URL", "\"${properties.getProperty("SUPABASE_URL", "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${properties.getProperty("SUPABASE_ANON_KEY", "")}\"")
    }
}
```

## 🔧 Implementation Examples

### Initialize Supabase Client

Create a file `SupabaseClient.kt`:

```kotlin
package com.perseverance.pvc.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import com.perseverance.pvc.BuildConfig

object SupabaseClient {
    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }
}
```

### Authentication Examples

#### Sign Up with Email

```kotlin
suspend fun signUp(email: String, password: String) {
    try {
        SupabaseClient.client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    } catch (e: Exception) {
        // Handle error
    }
}
```

#### Sign In with Email

```kotlin
suspend fun signIn(email: String, password: String) {
    try {
        SupabaseClient.client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    } catch (e: Exception) {
        // Handle error
    }
}
```

#### Sign In with Google

```kotlin
suspend fun signInWithGoogle() {
    try {
        SupabaseClient.client.auth.signInWith(Google)
    } catch (e: Exception) {
        // Handle error
    }
}
```

#### Sign Out

```kotlin
suspend fun signOut() {
    try {
        SupabaseClient.client.auth.signOut()
    } catch (e: Exception) {
        // Handle error
    }
}
```

#### Get Current User

```kotlin
fun getCurrentUser() = SupabaseClient.client.auth.currentUserOrNull()
```

### Database Operations

#### Insert Data

```kotlin
@Serializable
data class UserProfile(
    val id: String,
    val username: String,
    val email: String,
    val createdAt: String
)

suspend fun createUserProfile(profile: UserProfile) {
    SupabaseClient.client.from("user_profiles").insert(profile)
}
```

#### Query Data

```kotlin
suspend fun getUserProfile(userId: String): UserProfile? {
    return SupabaseClient.client.from("user_profiles")
        .select()
        .eq("id", userId)
        .decodeSingle<UserProfile>()
}
```

#### Update Data

```kotlin
suspend fun updateUsername(userId: String, newUsername: String) {
    SupabaseClient.client.from("user_profiles")
        .update({
            set("username", newUsername)
        }) {
            eq("id", userId)
        }
}
```

## 🗄️ Database Schema Example

Here's a sample schema for user profiles (create this in Supabase SQL Editor):

```sql
-- Create user_profiles table
CREATE TABLE user_profiles (
    id UUID REFERENCES auth.users PRIMARY KEY,
    username TEXT UNIQUE,
    email TEXT,
    avatar_url TEXT,
    rank TEXT DEFAULT 'beginner',
    points INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE user_profiles ENABLE ROW LEVEL SECURITY;

-- Create policies
CREATE POLICY "Users can view their own profile"
    ON user_profiles FOR SELECT
    USING (auth.uid() = id);

CREATE POLICY "Users can update their own profile"
    ON user_profiles FOR UPDATE
    USING (auth.uid() = id);

CREATE POLICY "Users can insert their own profile"
    ON user_profiles FOR INSERT
    WITH CHECK (auth.uid() = id);
```

## 🔐 Security Best Practices

1. **Never commit `.env` file** - It's already in `.gitignore`
2. **Use Row Level Security (RLS)** in Supabase for all tables
3. **Use anon key in client** - Never use service role key in Android app
4. **Validate data** on both client and server side
5. **Use HTTPS only** - Supabase uses HTTPS by default

## 📚 Additional Resources

- [Supabase Documentation](https://supabase.com/docs)
- [Supabase Kotlin Client](https://github.com/supabase-community/supabase-kt)
- [Authentication Guide](https://supabase.com/docs/guides/auth)
- [Database Guide](https://supabase.com/docs/guides/database)

## 🐛 Troubleshooting

### Build Config Not Found
If you get "BuildConfig not found" error:
1. Make sure `buildConfig = true` is in `buildFeatures`
2. Sync Gradle
3. Clean and rebuild project

### Environment Variables Not Loading
1. Check `.env` file exists in project root
2. Verify no syntax errors in `.env`
3. Rebuild project after changing `.env`

### Supabase Connection Issues
1. Verify URL and anon key are correct
2. Check internet connection
3. Ensure Supabase project is active
4. Check Supabase dashboard for any service issues

## ✅ Checklist

- [ ] Create Supabase project
- [ ] Get URL and anon key
- [ ] Update `.env` file with credentials
- [ ] Add Supabase dependencies to `build.gradle.kts`
- [ ] Enable BuildConfig
- [ ] Create SupabaseClient.kt
- [ ] Set up database schema
- [ ] Enable Row Level Security
- [ ] Test authentication
- [ ] Test database operations

---

**Need Help?** Check the [Supabase Discord](https://discord.supabase.com) or [GitHub Discussions](https://github.com/supabase/supabase/discussions)
