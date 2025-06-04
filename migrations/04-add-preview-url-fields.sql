-- Добавляем поле preview_url в таблицу trips
ALTER TABLE trips ADD COLUMN IF NOT EXISTS preview_url VARCHAR(500);

-- Добавляем поле preview_url в таблицу published_trips
ALTER TABLE published_trips ADD COLUMN IF NOT EXISTS preview_url VARCHAR(500); 