CREATE TABLE users
(
    user_id             BIGSERIAL PRIMARY KEY,
    username            VARCHAR(50) UNIQUE  NOT NULL,
    email               VARCHAR(100) UNIQUE NOT NULL,
    password_hash       VARCHAR(255)        NOT NULL,
    is_admin            BOOLEAN   DEFAULT FALSE,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login          TIMESTAMP,
    profile_picture_url TEXT,
    is_verified         BOOLEAN   DEFAULT FALSE
);

CREATE TABLE trips
(
    trip_id     BIGSERIAL PRIMARY KEY,
    creator_id  BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    title       VARCHAR(100) NOT NULL,
    description TEXT,
    start_date  DATE,
    end_date    DATE,
    country     VARCHAR(100),
    city        VARCHAR(100),
    is_public   BOOLEAN   DEFAULT FALSE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted  BOOLEAN   DEFAULT FALSE
);

CREATE TABLE trip_days
(
    day_id     BIGSERIAL PRIMARY KEY,
    trip_id    BIGINT REFERENCES trips (trip_id) ON DELETE CASCADE,
    day_number INTEGER NOT NULL,
    date       DATE,
    note       TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE places
(
    place_id    BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150)   NOT NULL,
    latitude    DECIMAL(10, 8) NOT NULL,
    longitude   DECIMAL(11, 8) NOT NULL,
    address     TEXT,
    place_type  VARCHAR(50),
    external_id VARCHAR(100),
    preview_url TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE events
(
    event_id          BIGSERIAL PRIMARY KEY,
    day_id            BIGINT REFERENCES trip_days (day_id) ON DELETE CASCADE,
    place_id          BIGINT REFERENCES places (place_id) ON DELETE SET NULL,
    title             VARCHAR(100) NOT NULL,
    description       TEXT,
    start_time        TIME,
    end_time          TIME,
    has_specific_time BOOLEAN   DEFAULT TRUE,
    notes             TEXT,
    order_position    INTEGER      NOT NULL,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE trip_access
(
    access_id         BIGSERIAL PRIMARY KEY,
    trip_id           BIGINT REFERENCES trips (trip_id) ON DELETE CASCADE,
    user_id           BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    access_level      VARCHAR(20) NOT NULL,
    invitation_status VARCHAR(20) DEFAULT 'pending',
    created_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (trip_id, user_id)
);

CREATE TABLE trip_invitations
(
    invitation_id    BIGSERIAL PRIMARY KEY,
    trip_id          BIGINT REFERENCES trips (trip_id) ON DELETE CASCADE,
    inviter_id       BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    email            VARCHAR(100),
    username         VARCHAR(50),
    access_level     VARCHAR(20)         NOT NULL,
    invitation_token VARCHAR(255) UNIQUE NOT NULL,
    is_used          BOOLEAN   DEFAULT FALSE,
    expires_at       TIMESTAMP           NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE todo_lists
(
    list_id     BIGSERIAL PRIMARY KEY,
    user_id     BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    trip_id     BIGINT REFERENCES trips (trip_id) ON DELETE CASCADE,
    title       VARCHAR(100) NOT NULL,
    description TEXT,
    list_type   VARCHAR(50),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE todo_items
(
    item_id        BIGSERIAL PRIMARY KEY,
    list_id        BIGINT REFERENCES todo_lists (list_id) ON DELETE CASCADE,
    content        TEXT    NOT NULL,
    is_completed   BOOLEAN   DEFAULT FALSE,
    order_position INTEGER NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE todo_templates
(
    template_id BIGSERIAL PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    description TEXT,
    category    VARCHAR(50)  NOT NULL,
    is_system   BOOLEAN   DEFAULT FALSE,
    created_by  BIGINT REFERENCES users (user_id) ON DELETE SET NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE template_items
(
    item_id        BIGSERIAL PRIMARY KEY,
    template_id    BIGINT REFERENCES todo_templates (template_id) ON DELETE CASCADE,
    content        TEXT    NOT NULL,
    order_position INTEGER NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE files
(
    file_id    BIGSERIAL PRIMARY KEY,
    user_id    BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    file_name  VARCHAR(255) NOT NULL,
    file_path  TEXT         NOT NULL,
    file_type  VARCHAR(50)  NOT NULL,
    file_size  INTEGER      NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE trip_files
(
    trip_file_id BIGSERIAL PRIMARY KEY,
    trip_id      BIGINT REFERENCES trips (trip_id) ON DELETE CASCADE,
    file_id      BIGINT REFERENCES files (file_id) ON DELETE CASCADE,
    description  TEXT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (trip_id, file_id)
);

CREATE TABLE event_files
(
    event_file_id BIGSERIAL PRIMARY KEY,
    event_id      BIGINT REFERENCES events (event_id) ON DELETE CASCADE,
    file_id       BIGINT REFERENCES files (file_id) ON DELETE CASCADE,
    description   TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (event_id, file_id)
);

CREATE TABLE photos
(
    photo_id   BIGSERIAL PRIMARY KEY,
    user_id    BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    place_id   BIGINT REFERENCES places (place_id) ON DELETE SET NULL,
    event_id   BIGINT REFERENCES events (event_id) ON DELETE SET NULL,
    trip_id    BIGINT REFERENCES trips (trip_id) ON DELETE SET NULL,
    file_id    BIGINT REFERENCES files (file_id) ON DELETE CASCADE,
    caption    TEXT,
    taken_at   TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE published_trips
(
    published_id   BIGSERIAL PRIMARY KEY,
    trip_id        BIGINT REFERENCES trips (trip_id) ON DELETE CASCADE,
    user_id        BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    title          VARCHAR(100) NOT NULL,
    description    TEXT,
    country        VARCHAR(100),
    city           VARCHAR(100),
    duration_days  INTEGER,
    cover_photo_id BIGINT REFERENCES files (file_id) ON DELETE SET NULL,
    tags           TEXT[],
    is_approved    BOOLEAN   DEFAULT TRUE,
    view_count     INTEGER   DEFAULT 0,
    published_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE trip_ratings
(
    rating_id    BIGSERIAL PRIMARY KEY,
    published_id BIGINT REFERENCES published_trips (published_id) ON DELETE CASCADE,
    user_id      BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    rating       INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (published_id, user_id)
);

CREATE TABLE trip_comments
(
    comment_id   BIGSERIAL PRIMARY KEY,
    published_id BIGINT REFERENCES published_trips (published_id) ON DELETE CASCADE,
    user_id      BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    content      TEXT NOT NULL,
    is_deleted   BOOLEAN   DEFAULT FALSE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifications
(
    notification_id BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    type            VARCHAR(50) NOT NULL,
    content         TEXT        NOT NULL,
    related_id      BIGINT,
    is_read         BOOLEAN   DEFAULT FALSE,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE event_reminders
(
    reminder_id    BIGSERIAL PRIMARY KEY,
    event_id       BIGINT REFERENCES events (event_id) ON DELETE CASCADE,
    user_id        BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    remind_at      TIMESTAMP NOT NULL,
    minutes_before INTEGER   NOT NULL,
    is_sent        BOOLEAN   DEFAULT FALSE,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE password_reset_tokens
(
    token_id   BIGSERIAL PRIMARY KEY,
    user_id    BIGINT                   NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    token      VARCHAR(255)             NOT NULL UNIQUE,
    reset_code VARCHAR(10)              NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_used    BOOLEAN                  DEFAULT FALSE
);

CREATE TABLE email_verification_tokens
(
    token_id   BIGSERIAL PRIMARY KEY,
    user_id    BIGINT                   NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
    token      VARCHAR(255)             NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE user_sessions
(
    session_id    BIGSERIAL PRIMARY KEY,
    user_id       BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    token         VARCHAR(255) UNIQUE NOT NULL,
    device_info   TEXT,
    ip_address    VARCHAR(45),
    expires_at    TIMESTAMP           NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sync_status
(
    sync_id           BIGSERIAL PRIMARY KEY,
    user_id           BIGINT REFERENCES users (user_id) ON DELETE CASCADE,
    entity_type       VARCHAR(50) NOT NULL,
    entity_id         BIGINT      NOT NULL,
    last_synced_at    TIMESTAMP,
    local_updated_at  TIMESTAMP,
    server_updated_at TIMESTAMP,
    sync_status       VARCHAR(20) DEFAULT 'pending',
    created_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);


-- User search
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_email ON users (email);

-- Trip search
CREATE INDEX idx_trips_creator ON trips (creator_id);
CREATE INDEX idx_trips_date_range ON trips (start_date, end_date);
CREATE INDEX idx_trips_location ON trips (country, city);

-- Event search
CREATE INDEX idx_events_day ON events (day_id);
CREATE INDEX idx_events_place ON events (place_id);
CREATE INDEX idx_events_time ON events (start_time, end_time);

-- Place search
CREATE INDEX idx_places_coordinates ON places (latitude, longitude);
CREATE INDEX idx_places_type ON places (place_type);

-- Todo list search
CREATE INDEX idx_todo_lists_user ON todo_lists (user_id);
CREATE INDEX idx_todo_lists_trip ON todo_lists (trip_id);

-- Published trips search
CREATE INDEX idx_published_trips_location ON published_trips (country, city);
CREATE INDEX idx_published_trips_duration ON published_trips (duration_days);
CREATE INDEX idx_published_trips_tags ON published_trips USING GIN(tags);

-- File search
CREATE INDEX idx_files_user ON files (user_id);
CREATE INDEX idx_files_type ON files (file_type);

-- Reminder search
CREATE INDEX idx_event_reminders_time ON event_reminders (remind_at);
CREATE INDEX idx_event_reminders_user ON event_reminders (user_id);

-- Notification search
CREATE INDEX idx_notifications_user ON notifications (user_id);
CREATE INDEX idx_notifications_read ON notifications (user_id, is_read);