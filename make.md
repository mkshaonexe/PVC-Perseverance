# 🚀 PVC-Perseverance Backend Documentation

This document explains how the **Supabase** backend and **Local Persistence** work together in the app. Since you used AI to help build this, this guide will help you understand the "magic" happening behind the scenes.

---

## 🛠 1. The Database Architecture (Supabase)

The app uses **Supabase (PostgreSQL)** for social features and **Android DataStore** for personal study data.

### 📊 Supabase Tables

| Table Name | Purpose | Key Fields |
| :--- | :--- | :--- |
| **`users`** | Stores user profiles & global status. | `id`, `display_name`, `status` (IDLE/STUDYING), `study_start_time`, `last_active`. |
| **`groups`** | Stores study group information. | `id`, `name`, `description`, `member_count`, `image_url`. |
| **`group_members`** | Tracks who is in which group. | `user_id`, `group_id`, `joined_at`. |
| **`friend_requests`**| Handles pending friend requests. | `from_user_id`, `to_user_id`, `status`. |
| **`friends`** | Stores mutual friendships. | `user_id`, `friend_id`. |

### 🔍 SQL Views
- **`view_group_members_status`**: This is a "Virtual Table" that combines `group_members` and `users`. It allows the app to fetch a list of people in a group along with their names and current study status in a single request.

---

## ⚡ 2. Real-Time System (How it feels "Live")

The app uses **Supabase Realtime** to sync data between users instantly.

### 🟢 Group Joining & Leaving
1. When you click **Join**, the app adds a row to `group_members`.
2. A **Supabase Trigger** (`update_group_member_count`) automatically updates the member count in the `groups` table.
3. Other users in that group have a **Realtime Listener** (`subscribeToGroupUpdates`) that detects this change and refreshes their screen immediately.

### ⏱ Real-Time Study Timers
You might wonder: *Does it write to the database every second to update the timer?*
**No.** That would be slow and expensive.
- When you start studying, the app saves the **`study_start_time`** (e.g., 2:00 PM) to Supabase.
- The app then calculates the difference locally: `Current Time - Start Time = 00:05:23`.
- This calculation happens **every second on your phone**, but the database is only updated once at the start and once at the end.

### ❤️ Heartbeat (Presence)
- To show who is "Online", the app sends a **Heartbeat** every 60 seconds (`updateHeartbeat`).
- It updates the `last_active` timestamp. If a user hasn't sent a heartbeat in over 2 minutes, the app considers them "Offline".

---

## 💾 3. Personal Data (Local vs. Remote)

| Feature | Stored in Supabase (Cloud) | Stored in DataStore (Local) |
| :--- | :---: | :---: |
| **Active Study Status** | ✅ Yes | ❌ No |
| **Group Memberships** | ✅ Yes | ❌ No |
| **Actual Study Sessions** | ❌ No | ✅ Yes |
| **Custom Missions** | ❌ No | ✅ Yes |
| **App Settings** | ❌ No | ✅ Yes |

**Why?**
- **Supabase** is used for things **other people need to see** (like your status in a group).
- **Local DataStore** is used for things **only you need** (like your history and settings). This makes the app work offline and feel very fast.

---

## 🔧 4. How to Manage in Supabase Dashboard

If you want to see the data directly:

1. **Table Editor**: Go here to see `users`, `groups`, and `group_members`. You can manually edit names or delete people from groups.
2. **Authentication**: Go here to see all users who have signed up with Google.
3. **Storage**: Go here (specifically the `avatars` bucket) to see profile pictures uploaded by users.
4. **SQL Editor**: This is where the "Triggers" (automatic code) and "Views" are kept.

> [!TIP]
> **Group Joining System**: If a user is not appearing in a group, check the `group_members` table to see if their `user_id` is correctly linked to the `group_id`.

---

## 📝 5. Summary of Data Flow
When you join a group and start studying:
1. **Supabase** gets a "Join" request $\rightarrow$ updates `group_members`.
2. **Supabase** gets a "Start Study" request $\rightarrow$ updates `users.status`.
3. **Local Phone** starts a timer $\rightarrow$ updates UI every 1s.
4. **Other Users** receive a Realtime event $\rightarrow$ see you as "Studying" instantly.
5. **Local Phone** saves the session $\rightarrow$ updates your local history charts.
