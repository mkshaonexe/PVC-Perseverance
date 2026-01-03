-- DANGER: This script will remove ALL objects from the public schema.
-- Run this only if you want to completely reset your database.

DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO anon;
GRANT ALL ON SCHEMA public TO authenticated;
GRANT ALL ON SCHEMA public TO service_role;
COMMENT ON SCHEMA public IS 'standard public schema';
