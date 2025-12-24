# Supabase Database Setup

This document contains the SQL scripts needed to set up the Supabase database for the Study Friends feature.

## Prerequisites

1. Log in to your Supabase project: https://whsozcurdtbcwpsohxok.supabase.co
2. Go to the SQL Editor
3. Run the following SQL scripts in order

---

## 1. Create Users Table

```sql
CREATE TABLE IF NOT EXISTS users (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  firebase_uid TEXT UNIQUE NOT NULL,
  email TEXT UNIQUE NOT NULL,
  display_name TEXT NOT NULL,
  photo_url TEXT,
  status TEXT DEFAULT 'IDLE', -- 'IDLE' | 'STUDYING'
  current_subject TEXT,
  last_active TIMESTAMP DEFAULT NOW(),
  created_at TIMESTAMP DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Policy: Users can read all user profiles
CREATE POLICY "Users can read all profiles"
  ON users FOR SELECT
  USING (true);

-- Policy: Users can update their own profile
CREATE POLICY "Users can update own profile"
  ON users FOR UPDATE
  USING (auth.uid()::text = firebase_uid);

-- Policy: Users can insert their own profile
CREATE POLICY "Users can insert own profile"
  ON users FOR INSERT
  WITH CHECK (auth.uid()::text = firebase_uid);
```

---

## 2. Create Friends Table

```sql
CREATE TABLE IF NOT EXISTS friends (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  friend_id UUID REFERENCES users(id) ON DELETE CASCADE,
  created_at TIMESTAMP DEFAULT NOW(),
  UNIQUE(user_id, friend_id)
);

-- Enable Row Level Security
ALTER TABLE friends ENABLE ROW LEVEL SECURITY;

-- Policy: Users can read their own friendships
CREATE POLICY "Users can read own friendships"
  ON friends FOR SELECT
  USING (
    user_id IN (SELECT id FROM users WHERE firebase_uid = auth.uid()::text)
    OR friend_id IN (SELECT id FROM users WHERE firebase_uid = auth.uid()::text)
  );

-- Policy: Users can insert friendships
CREATE POLICY "Users can insert friendships"
  ON friends FOR INSERT
  WITH CHECK (
    user_id IN (SELECT id FROM users WHERE firebase_uid = auth.uid()::text)
  );

-- Policy: Users can delete their own friendships
CREATE POLICY "Users can delete own friendships"
  ON friends FOR DELETE
  USING (
    user_id IN (SELECT id FROM users WHERE firebase_uid = auth.uid()::text)
  );
```

---

## 3. Create Friend Requests Table

```sql
CREATE TABLE IF NOT EXISTS friend_requests (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  from_user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  to_user_email TEXT NOT NULL,
  status TEXT DEFAULT 'PENDING', -- 'PENDING' | 'ACCEPTED' | 'REJECTED'
  created_at TIMESTAMP DEFAULT NOW()
);

-- Enable Row Level Security
ALTER TABLE friend_requests ENABLE ROW LEVEL SECURITY;

-- Policy: Users can read requests they sent or received
CREATE POLICY "Users can read own requests"
  ON friend_requests FOR SELECT
  USING (
    from_user_id IN (SELECT id FROM users WHERE firebase_uid = auth.uid()::text)
    OR to_user_email IN (SELECT email FROM users WHERE firebase_uid = auth.uid()::text)
  );

-- Policy: Users can insert friend requests
CREATE POLICY "Users can insert requests"
  ON friend_requests FOR INSERT
  WITH CHECK (
    from_user_id IN (SELECT id FROM users WHERE firebase_uid = auth.uid()::text)
  );

-- Policy: Users can update requests sent to them
CREATE POLICY "Users can update received requests"
  ON friend_requests FOR UPDATE
  USING (
    to_user_email IN (SELECT email FROM users WHERE firebase_uid = auth.uid()::text)
  );
```

---

## 4. Create Indexes for Performance

```sql
-- Index on firebase_uid for faster lookups
CREATE INDEX IF NOT EXISTS idx_users_firebase_uid ON users(firebase_uid);

-- Index on email for faster friend request lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Index on user_id for faster friend queries
CREATE INDEX IF NOT EXISTS idx_friends_user_id ON friends(user_id);

-- Index on friend_id for faster reverse lookups
CREATE INDEX IF NOT EXISTS idx_friends_friend_id ON friends(friend_id);

-- Index on from_user_id for faster request queries
CREATE INDEX IF NOT EXISTS idx_friend_requests_from_user_id ON friend_requests(from_user_id);

-- Index on to_user_email for faster request lookups
CREATE INDEX IF NOT EXISTS idx_friend_requests_to_user_email ON friend_requests(to_user_email);
```

---

## 5. Enable Realtime (Optional)

If you want real-time updates for friend status changes:

```sql
-- Enable realtime for the users table
ALTER PUBLICATION supabase_realtime ADD TABLE users;
```

---

## Verification

After running all the scripts, verify the setup:

1. Go to **Table Editor** in Supabase
2. You should see three tables: `users`, `friends`, `friend_requests`
3. Go to **Authentication** → **Policies**
4. Verify that RLS is enabled and policies are created for each table

---

## Testing

1. Install the app and sign in with Google
2. Check the `users` table - you should see your profile created
3. Try adding a friend by email
4. Check the `friend_requests` table - you should see the request

---

## Troubleshooting

### Issue: "new row violates row-level security policy"
- **Solution**: Make sure you're signed in to Supabase with Google (the app does this automatically)
- Check that the policies are created correctly

### Issue: "relation 'users' does not exist"
- **Solution**: Run the CREATE TABLE scripts in the SQL Editor

### Issue: Friend requests not working
- **Solution**: Make sure both users have signed in at least once so their profiles exist in the `users` table
