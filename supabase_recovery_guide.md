# Supabase Self-Hosted Disaster Recovery Guide

This guide details how to protect, back up, and restore your self-hosted Supabase instance. It is designed to mitigate risks such as accidental server deletion, user data loss, or total system failure.

> [!IMPORTANT]
> **Rule #1 of Database Management**: always assume your server could vanish at any moment. **Backups are not optional.**

---

## 1. Understanding the Architecture & Risks

In a self-hosted Supabase setup (using Docker), your data lives in two main places:
1.  **The Database (PostgreSQL)**: Contains all user data, authentication details (`auth.users`), and app configurations. This is effectively stored in a Docker `volume`.
2.  **The Configuration (`.env`, `docker-compose.yml`)**: Contains your secrets, API keys, and service settings.

### What happens if...?
-   **I accidentally delete the server (VPS/VM)?**
    -   *Result*: If you don't have external backups, **everything is gone**. Docker volumes are usually destroyed with the instance.
    -   *Prevention*: External backups (S3/Drive) + Deletion Protection on the provider side.
-   **I accidentally delete a user in the Dashboard?**
    -   *Result*: The user row is removed from `auth.users`. Any data cascading from that ID (like profiles) might be deleted if you have `ON DELETE CASCADE` set.
    -   *Recovery*: You must restore from a backup taken *before* the deletion.
-   **The Docker container crashes?**
    -   *Result*: Data usually persists in the volume. A restart often fixes it.

---

## 2. Backup Strategy (The "Safety Net")

You need **two** types of backups.

### A. The "Config" Backup (Git)
Your `.env` file and `docker-compose.yml` contain keys that are impossible to recover if lost.
*   **Action**: Keep a copy of your `.env` file in a secure password manager or encrypted storage. **NEVER** commit your production `.env` to a public Git repository.

### B. The "Data" Backup (Automated `pg_dump`)
You need a script that runs daily (or hourly) to dump the database to a file and send it *off-server*.

#### 1. Create a Backup Script
Create a file named `backup.sh` on your server:

```bash
#!/bin/bash

# Configuration
BACKUP_DIR="/var/backups/supabase"
TIMESTAMP=$(date +"%F_%H-%M-%S")
FILENAME="backup_$TIMESTAMP.sql"
CONTAINER_NAME="supabase-db" # Check your actual container name via 'docker ps'

# Ensure backup dir exists
mkdir -p $BACKUP_DIR

# 1. Perform the dump
# We use typical Supabase postgres credentials (user: postgres)
docker exec $CONTAINER_NAME pg_dumpall -U postgres > "$BACKUP_DIR/$FILENAME"

# 2. Compress it (Important for storage)
gzip "$BACKUP_DIR/$FILENAME"

# 3. (CRITICAL) Upload to Cloud Storage
# Example using AWS CLI (S3) - You must install/configure aws-cli first
# aws s3 cp "$BACKUP_DIR/$FILENAME.gz" s3://my-supabase-backups-bucket/

# 4. Clean up old backups (Delete older than 7 days)
find $BACKUP_DIR -type f -name "*.gz" -mtime +7 -delete

echo "Backup $FILENAME.gz completed."
```

#### 2. Automate it (Cron Job)
Run `crontab -e` and add this line to run every day at 3 AM:
```
0 3 * * * /bin/bash /path/to/backup.sh
```

---

## 3. Restoration Scenarios

### Scenario A: "I deleted the server / Total Failure"
**Situation**: The original server is gone. You have a new empty server and your backup file (`backup_date.sql.gz`).

1.  **Setup the New Server**:
    -   Install Docker & Docker Compose.
    -   Copy your safely stored `.env` and `docker-compose.yml` to the new server.
    -   Start Supabase: `docker compose up -d`.
2.  **Prepare for Restore**:
    -   Copy your backup file to the server (e.g., `restore.sql.gz`).
    -   Unzip it: `gunzip restore.sql.gz`.
3.  **Restore the Data**:
    ```bash
    # This overwrites everything with the backup state
    cat restore.sql | docker exec -i supabase-db psql -U postgres
    ```
4.  **Restart Services**:
    -   `docker compose restart`

### Scenario B: "I accidentally deleted a user or table"
**Situation**: The server is fine, but you deleted crucial data 10 minutes ago.

1.  **Locate the latest backup** from *before* the mistake.
2.  **Safety First**:
    -   Do NOT restore directly to production immediately if you can avoid it.
    -   Ideally, spin up a local Docker instance on your computer, restore the backup there, export *just the missing rows*, and insert them back into production.
3.  **Direct Restore (Nuclear Option)**:
    -   If the data loss is catastrophic (e.g., `DROP TABLE users`), use the **Scenario A** restore command. *Warning: This rolls back ALL data to the backup time. New data created since then will be lost.*

---

## 4. Prevention Checklist

- [ ] **Enable "Termination Protection"**: Most cloud providers (AWS, DigitalOcean, Hetzner) have a setting that prevents you from clicking "Delete Server" without extra confirmation. Turn this on.
- [ ] **Separate Storage**: If possible, use a "Managed Database" or mount an external "Block Storage" volume for the `/var/lib/postgresql/data` folder. If the compute server dies, the volume remains safe.
- [ ] **Test Your Backups**: A backup is useless if it doesn't work. Once a month, try restoring your backup to a local version of Supabase to prove it works.

## 5. Summary
1.  **Git** protects your code and config.
2.  **Automated pg_dump** protects your data.
3.  **Off-site storage** (S3/Google Drive) protects you from server deletion.
