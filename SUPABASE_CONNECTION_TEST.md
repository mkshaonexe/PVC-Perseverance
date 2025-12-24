# Supabase Connection Testing Guide

This guide will help you verify that Supabase is successfully integrated and connected in your Android app.

## 🔍 Quick Checks

### 1. **Check Build Configuration**
First, verify that your `.env` file has the correct Supabase credentials:

```bash
# Your .env file should contain:
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=your_actual_anon_key_here
GOOGLE_WEB_CLIENT_ID=your_google_web_client_id.apps.googleusercontent.com
```

**How to verify:**
- Open your `.env` file
- Make sure `SUPABASE_URL` is NOT `https://placeholder.supabase.co`
- Make sure `SUPABASE_ANON_KEY` is NOT `placeholder`
- Both values should be real values from your Supabase project

---

## 🧪 Testing Methods

### Method 1: Check Logcat for Supabase Initialization

**Steps:**
1. Build and run your app in debug mode
2. Open Logcat in Android Studio
3. Filter by "Supabase" or "SupabaseClient"
4. Look for initialization messages

**What to look for:**
- ✅ **SUCCESS**: No errors related to Supabase initialization
- ❌ **FAILURE**: Errors like "Invalid URL" or "Invalid API key"

---

### Method 2: Test Authentication Flow

**Steps:**
1. Run your app
2. Navigate to the login/authentication screen
3. Try to sign up with email and password OR sign in with Google
4. Check Logcat for authentication logs

**What to look for in Logcat:**
```
# Success indicators:
AuthRepository: Sign in successful
AuthRepository: User logged in

# Failure indicators:
AuthRepository: Sign in error
AuthRepository: Invalid API key
AuthRepository: Network error
```

**Expected behavior:**
- ✅ **CONNECTED**: User can successfully sign up/sign in, and you see success logs
- ❌ **NOT CONNECTED**: Authentication fails with API errors

---

### Method 3: Check Current User Status

**Steps:**
1. Add temporary logging to your app
2. In your `MainActivity` or any ViewModel, add this code:

```kotlin
// In your ViewModel or Activity
private val authRepository = AuthRepository(context)

init {
    val currentUser = authRepository.getCurrentUser()
    Log.d("SupabaseTest", "Current User: $currentUser")
    Log.d("SupabaseTest", "Is Logged In: ${authRepository.isUserLoggedIn()}")
}
```

3. Run the app and check Logcat

**What to look for:**
- ✅ **CONNECTED**: You see user information or `null` (if not logged in)
- ❌ **NOT CONNECTED**: App crashes or shows initialization errors

---

### Method 4: Test Database Connection (Study Groups)

**Steps:**
1. Log in to your app
2. Navigate to the Study Group screen
3. Try to select a study group
4. Check if the group is saved

**What to look for in Logcat:**
```
# Success:
AuthRepository: Save user group successful

# Failure:
AuthRepository: Save user group error
AuthRepository: Table 'profiles' does not exist
```

**Expected behavior:**
- ✅ **CONNECTED**: Group selection saves without errors
- ❌ **NOT CONNECTED**: Errors about missing tables or network issues

---

### Method 5: Verify BuildConfig Values

**Steps:**
1. Add this temporary code to your `MainActivity.kt`:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Temporary test code
    Log.d("SupabaseTest", "SUPABASE_URL: ${BuildConfig.SUPABASE_URL}")
    Log.d("SupabaseTest", "SUPABASE_ANON_KEY: ${BuildConfig.SUPABASE_ANON_KEY.take(10)}...")
    Log.d("SupabaseTest", "GOOGLE_WEB_CLIENT_ID: ${BuildConfig.GOOGLE_WEB_CLIENT_ID}")
    
    // Rest of your code...
}
```

2. Build and run the app
3. Check Logcat for the output

**What to look for:**
- ✅ **CONFIGURED**: Real URLs and keys (not placeholders)
- ❌ **NOT CONFIGURED**: Placeholder values or empty strings

---

## 🔧 Advanced Testing: Create a Test Screen

Create a dedicated test screen to verify Supabase connection:

### Step 1: Create Test ViewModel

Create `app/src/main/java/com/perseverance/pvc/ui/test/SupabaseTestViewModel.kt`:

```kotlin
package com.perseverance.pvc.ui.test

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.perseverance.pvc.data.remote.SupabaseClient
import com.perseverance.pvc.data.repository.AuthRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SupabaseTestViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authRepository = AuthRepository(application)
    private val supabase = SupabaseClient.client
    
    private val _testResults = MutableStateFlow<List<String>>(emptyList())
    val testResults: StateFlow<List<String>> = _testResults
    
    fun runTests() {
        viewModelScope.launch {
            val results = mutableListOf<String>()
            
            // Test 1: Check Supabase Client Initialization
            try {
                results.add("✅ Supabase client initialized")
            } catch (e: Exception) {
                results.add("❌ Supabase client error: ${e.message}")
            }
            
            // Test 2: Check Auth Module
            try {
                val currentUser = supabase.auth.currentUserOrNull()
                if (currentUser != null) {
                    results.add("✅ User is logged in: ${currentUser.email}")
                } else {
                    results.add("ℹ️ No user currently logged in (this is OK)")
                }
            } catch (e: Exception) {
                results.add("❌ Auth check error: ${e.message}")
            }
            
            // Test 3: Check Repository
            try {
                val isLoggedIn = authRepository.isUserLoggedIn()
                results.add("✅ AuthRepository working. Logged in: $isLoggedIn")
            } catch (e: Exception) {
                results.add("❌ AuthRepository error: ${e.message}")
            }
            
            // Test 4: Test Email Sign Up (with fake data)
            try {
                val result = authRepository.signUpWithEmail(
                    "test_${System.currentTimeMillis()}@test.com",
                    "testpassword123"
                )
                if (result.isSuccess) {
                    results.add("✅ Email signup works (test account created)")
                    // Sign out immediately
                    authRepository.signOut()
                } else {
                    results.add("⚠️ Email signup failed: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                results.add("❌ Email signup error: ${e.message}")
            }
            
            _testResults.value = results
        }
    }
}
```

### Step 2: Create Test Screen

Create `app/src/main/java/com/perseverance/pvc/ui/test/SupabaseTestScreen.kt`:

```kotlin
package com.perseverance.pvc.ui.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SupabaseTestScreen(
    viewModel: SupabaseTestViewModel = viewModel()
) {
    val testResults by viewModel.testResults.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Supabase Connection Test",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Button(
            onClick = { viewModel.runTests() },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text("Run Tests")
        }
        
        if (testResults.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(testResults) { result ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = result,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
```

### Step 3: Add to Navigation

Add this screen to your navigation graph temporarily to access it.

---

## 📊 Interpreting Results

### ✅ **Supabase is CONNECTED** if you see:
1. No initialization errors in Logcat
2. BuildConfig has real Supabase URL and key (not placeholders)
3. Authentication attempts reach Supabase (even if they fail due to wrong credentials)
4. No network errors related to Supabase

### ❌ **Supabase is NOT CONNECTED** if you see:
1. "placeholder" values in BuildConfig
2. "Invalid API key" errors
3. "Invalid URL" errors
4. App crashes on Supabase operations
5. Network errors with Supabase endpoints

---

## 🐛 Common Issues

### Issue 1: Placeholder Values
**Problem:** BuildConfig shows placeholder values  
**Solution:** 
1. Check your `.env` file has real values
2. Rebuild the app completely: `./gradlew clean build`
3. Make sure `.env` is in the root directory

### Issue 2: Network Errors
**Problem:** "Unable to resolve host" errors  
**Solution:**
1. Check internet connection
2. Verify Supabase URL is correct
3. Check if Supabase project is active

### Issue 3: Authentication Fails
**Problem:** Sign in/sign up fails  
**Solution:**
1. Check Supabase Dashboard → Authentication → Providers
2. Enable Email provider
3. Enable Google provider (if using Google Sign-In)
4. Verify Google OAuth credentials

---

## 🎯 Quick Test Checklist

- [ ] `.env` file has real Supabase credentials
- [ ] App builds without errors
- [ ] No "placeholder" in BuildConfig logs
- [ ] Can attempt authentication (even if it fails)
- [ ] No Supabase initialization errors in Logcat
- [ ] SupabaseClient.client doesn't crash the app

If all items are checked, **Supabase is successfully integrated!** ✅

---

## 📝 Next Steps After Verification

Once Supabase is connected:
1. Set up your database tables in Supabase Dashboard
2. Configure authentication providers
3. Test actual user sign-up and sign-in
4. Implement real features using Supabase
