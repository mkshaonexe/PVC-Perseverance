/* Enable Realtime for Group Members */

/* 1. Enable Replication for tables we want to listen to */
alter publication supabase_realtime add table public.group_members;
alter publication supabase_realtime add table public.users;

/* 2. Create Trigger for Member Count Automation */
/* This ensures that when a user joins or leaves, the count is always accurate without client-side race conditions */

create or replace function public.update_group_member_count()
returns trigger as $$
begin
  if (TG_OP = 'INSERT') then
    update public.groups
    set member_count = member_count + 1
    where id = NEW.group_id;
    return NEW;
  elsif (TG_OP = 'DELETE') then
    update public.groups
    set member_count = member_count - 1
    where id = OLD.group_id;
    return OLD;
  end if;
  return null;
end;
$$ language plpgsql security definer;

/* Drop if exists to avoid errors on re-run */
drop trigger if exists trigger_update_member_count on public.group_members;

create trigger trigger_update_member_count
after insert or delete on public.group_members
for each row execute function public.update_group_member_count();

/* 3. Helper View (ensure it exists as per previous setup) */
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
