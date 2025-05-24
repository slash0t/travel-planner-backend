ALTER TABLE trip_ratings ADD COLUMN comment TEXT;

ALTER TABLE trip_ratings ADD COLUMN is_deleted BOOLEAN DEFAULT FALSE;

DROP TABLE trip_comments;

ALTER TABLE trip_ratings DROP CONSTRAINT IF EXISTS trip_ratings_published_id_user_id_key; 