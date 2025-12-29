-- Optimizing Group Member Data Fetching

-- 1. Create a View to easily get group members with their full user profile and status
-- This avoids doing multiple joins on the client side
create or replace view view_group_members_status as
select
  gm.group_id,
  gm.joined_at,
  u.id as user_id,
  u.display_name,
  u.avatar_url,
  u.status,
  u.study_start_time,
  u.study_duration,
  u.last_active,
  u.current_subject,
  u.username
from
  public.group_members gm
  join public.users u on gm.user_id = u.id;

-- 2. Policy to allow reading group members (if not already public)
-- Assuming RLS is enabled, we need to ensure users can read this view.
-- Since views inherit permissions from underlying tables, ensure select is allowed on users and group_members.
-- (No specific command needed here if tables are public readable, but good to keep in mind)

-- 3. Trigger or Function to Auto-Update Member Count (Optional optimization)
-- It's often better to count on read or use a trigger than client-side increment.
-- Here is a trigger example if you want to enforce strict counts:

create or replace function update_group_member_count()
returns trigger as $$
begin
  if (TG_OP = 'INSERT') then
    update public.groups
    set member_count = member_count + 1
    where id = NEW.group_id;
  elsif (TG_OP = 'DELETE') then
    update public.groups
    set member_count = member_count - 1
    where id = OLD.group_id;
  end if;
  return null;
end;
$$ language plpgsql;

-- Apply the trigger (Uncomment if you want to use this instead of client-side logic)
-- create trigger trigger_update_member_count
-- after insert or delete on public.group_members
-- for each row execute function update_group_member_count();

-- 4. Instructions
-- Run this script in the Supabase SQL Editor.
-- The view `view_group_members_status` can be effectively used by the client for faster reads.
