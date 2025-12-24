# 🔐 Quick Reference: .env File for Supabase

## What is the .env file?
The `.env` file stores your **sensitive Supabase credentials** that your Android app needs to connect to your Supabase backend.

## 📝 Demo .env Configuration

```env
# Supabase Configuration
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_ANON_KEY=your_supabase_anon_key_here
```

## 🎯 How to Get Your Credentials

### Step 1: Create Supabase Project
1. Visit: https://app.supabase.com
2. Click "New Project"
3. Fill in project details and create

### Step 2: Get Your Credentials
1. Go to **Settings** → **API** in your Supabase dashboard
2. Copy these two values:
   - **Project URL** → Use for `SUPABASE_URL`
   - **anon public key** → Use for `SUPABASE_ANON_KEY`

### Step 3: Update Your .env File
Replace the placeholder values in your `.env` file with the actual values from Step 2.

## 📋 Example with Real Format

```env
# Real example format (with fake data)
SUPABASE_URL=https://xyzcompany.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh5emNvbXBhbnkiLCJyb2xlIjoiYW5vbiIsImlhdCI6MTYxNjE2MTYxNiwiZXhwIjoxOTMxNzM3NjE2fQ.kCx3gZGN6EXAMPLE_KEY_HERE
```

## ⚠️ Important Security Notes

1. **NEVER commit `.env` to Git** - Already protected in `.gitignore`
2. **NEVER share your `.env` file** - Contains sensitive credentials
3. **Use `.env.example`** - For sharing template with team
4. **Anon key is safe** - Can be used in client apps (has limited permissions)
5. **Service role key** - NEVER use in Android app (server-side only)

## 🔄 What's Next?

After setting up your `.env` file:

1. ✅ Add Supabase dependencies to `app/build.gradle.kts`
2. ✅ Enable BuildConfig in gradle
3. ✅ Create SupabaseClient.kt
4. ✅ Start using Supabase for auth and database

See `SUPABASE_SETUP.md` for complete implementation guide!

## 🆘 Troubleshooting

**Q: Where do I put the .env file?**  
A: In the project root directory (same level as `app/` folder)

**Q: My app can't read the .env values**  
A: Make sure you've:
- Added BuildConfig setup in `build.gradle.kts`
- Synced Gradle
- Rebuilt the project

**Q: Is it safe to use anon key in my app?**  
A: Yes! The anon key is designed for client apps. Use Row Level Security (RLS) in Supabase to protect your data.

**Q: What if I accidentally commit .env?**  
A: 
1. Immediately regenerate your keys in Supabase dashboard
2. Remove .env from Git history
3. Update your local .env with new keys

---

**Ready to integrate?** Follow the complete guide in `SUPABASE_SETUP.md`
