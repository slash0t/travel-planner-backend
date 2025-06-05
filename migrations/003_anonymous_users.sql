-- Создание таблицы анонимных пользователей
CREATE TABLE IF NOT EXISTS anonymous_users (
    anonymous_user_id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(255) NOT NULL UNIQUE,
    device_info TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_migrated BOOLEAN DEFAULT FALSE,
    migrated_to_user_id BIGINT,
    migration_date TIMESTAMP
);

-- Добавление поля роли в таблицу пользователей
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'USER';

-- Добавление поля для отслеживания миграции из анонимного пользователя
ALTER TABLE users ADD COLUMN IF NOT EXISTS migrated_from_anonymous_id BIGINT;

-- Добавление поддержки анонимных создателей в таблицу путешествий
ALTER TABLE trips ALTER COLUMN creator_id DROP NOT NULL;
ALTER TABLE trips ADD COLUMN IF NOT EXISTS anonymous_creator_id BIGINT;

-- Добавление поддержки анонимных пользователей в таблицу todo листов
ALTER TABLE todo_lists ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE todo_lists ADD COLUMN IF NOT EXISTS anonymous_user_id BIGINT;

-- Создание индексов для производительности
CREATE INDEX IF NOT EXISTS idx_anonymous_users_device_id ON anonymous_users(device_id);
CREATE INDEX IF NOT EXISTS idx_anonymous_users_last_activity ON anonymous_users(last_activity);
CREATE INDEX IF NOT EXISTS idx_anonymous_users_migrated ON anonymous_users(is_migrated);
CREATE INDEX IF NOT EXISTS idx_trips_anonymous_creator ON trips(anonymous_creator_id);
CREATE INDEX IF NOT EXISTS idx_todo_lists_anonymous_user ON todo_lists(anonymous_user_id);

-- Добавление ограничений
ALTER TABLE trips ADD CONSTRAINT chk_trip_creator CHECK (
    (creator_id IS NOT NULL AND anonymous_creator_id IS NULL) OR 
    (creator_id IS NULL AND anonymous_creator_id IS NOT NULL)
);

ALTER TABLE todo_lists ADD CONSTRAINT chk_todo_list_owner CHECK (
    (user_id IS NOT NULL AND anonymous_user_id IS NULL) OR 
    (user_id IS NULL AND anonymous_user_id IS NOT NULL)
); 