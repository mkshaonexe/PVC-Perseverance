# How to Test Supabase Connection - Quick Guide

## 🚀 Easiest Method: Run Your App

I've added automatic Supabase connection testing to your app. Here's how to check:

### Step 1: Build and Run Your App
```bash
./gradlew assembleDebug
```

### Step 2: Check Logcat
After the app starts, open **Logcat** in Android Studio and filter by:
- `SupabaseConnectionTest` - to see detailed test results
- `MainActivity` - to see the final connection status

### Step 3: Read the Results

You'll see logs like this:

#### ✅ If Supabase is CONNECTED:
```
SupabaseConnectionTest: ========== SUPABASE CONNECTION TEST ==========
SupabaseConnectionTest: Test 1: Checking BuildConfig values...
SupabaseConnectionTest:   SUPABASE_URL: https://yourproject.supabase.co
SupabaseConnectionTest:   SUPABASE_ANON_KEY: eyJhbGciOiJIUzI1NiIsInR...
SupabaseConnectionTest:   ✅ BuildConfig values look valid
SupabaseConnectionTest: Test 2: Checking Supabase client initialization...
SupabaseConnectionTest:   ✅ Supabase client initialized successfully
SupabaseConnectionTest: Test 3: Checking Auth module...
SupabaseConnectionTest:   ✅ Auth module working - No user logged in (this is OK)
SupabaseConnectionTest: ✅ ALL TESTS PASSED - Supabase is CONNECTED!
MainActivity: Supabase Connection Status: true
```

#### ❌ If Supabase is NOT CONNECTED:
```
SupabaseConnectionTest: ========== SUPABASE CONNECTION TEST ==========
SupabaseConnectionTest: Test 1: Checking BuildConfig values...
SupabaseConnectionTest:   SUPABASE_URL: https://placeholder.supabase.co
SupabaseConnectionTest:   SUPABASE_ANON_KEY: placeholder...
SupabaseConnectionTest:   ❌ Using placeholder values - Check your .env file
MainActivity: Supabase Connection Status: false
```

---

## 🔍 What Each Test Checks

1. **BuildConfig Values** - Verifies your `.env` file has real Supabase credentials
2. **Client Initialization** - Confirms Supabase client can be created
3. **Auth Module** - Tests if authentication system is working

---

## 🛠️ If Connection Fails

### Problem: Placeholder Values
**Fix:**
1. Open your `.env` file in the project root
2. Replace placeholder values with real ones from your Supabase project:
   ```
   SUPABASE_URL=https://your-actual-project-id.supabase.co
   SUPABASE_ANON_KEY=your_actual_anon_key_here
   ```
3. Clean and rebuild:
   ```bash
   ./gradlew clean assembleDebug
   ```

### Where to Get Supabase Credentials:
1. Go to [https://supabase.com/dashboard](https://supabase.com/dashboard)
2. Select your project
3. Click **Settings** → **API**
4. Copy:
   - **Project URL** → `SUPABASE_URL`
   - **anon/public key** → `SUPABASE_ANON_KEY`

---

## 📱 Alternative: Manual Testing

### Test Authentication:
1. Run your app
2. Navigate to the Login screen
3. Try to sign up with a test email
4. Check Logcat for `AuthRepository` logs

**Success:** You'll see "Sign up successful" or similar  
**Failure:** You'll see "Invalid API key" or network errors

---

## 📊 Understanding the Results

| Log Message | Meaning |
|------------|---------|
| ✅ ALL TESTS PASSED | Supabase is fully integrated and working |
| ❌ Using placeholder values | Your `.env` file needs real credentials |
| ❌ Failed to initialize client | Check your Supabase URL and key |
| ❌ Auth module error | Supabase Auth might not be enabled |

---

## 🎯 Quick Checklist

- [ ] `.env` file exists in project root
- [ ] `.env` has real Supabase URL (not placeholder)
- [ ] `.env` has real Supabase key (not placeholder)
- [ ] Ran `./gradlew clean assembleDebug`
- [ ] Checked Logcat for test results
- [ ] Saw "✅ ALL TESTS PASSED" message

If all checked, **Supabase is successfully connected!** 🎉

---

## 📚 More Details

For comprehensive testing methods and troubleshooting, see:
- `SUPABASE_CONNECTION_TEST.md` - Full testing guide
- `SupabaseConnectionTester.kt` - Test utility source code
