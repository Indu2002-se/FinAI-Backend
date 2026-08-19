-- Add profile_complete column to users table
ALTER TABLE users 
ADD COLUMN profile_complete BOOLEAN NOT NULL DEFAULT FALSE;

-- Update existing users to have profile_complete as false
UPDATE users SET profile_complete = FALSE WHERE profile_complete IS NULL;
