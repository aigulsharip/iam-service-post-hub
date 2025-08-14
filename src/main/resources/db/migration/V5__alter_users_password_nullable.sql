ALTER TABLE v1_iam_service.users ALTER COLUMN password DROP NOT NULL;

ALTER TABLE v1_iam_service.users DROP CONSTRAINT IF EXISTS users_username_key;