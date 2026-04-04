-- ═══════════════════════════════════════════════════════════════
--  StreamVibe — MySQL Schema
--  Run once: mysql -u root -p streamvibe < schema.sql
-- ═══════════════════════════════════════════════════════════════

CREATE DATABASE IF NOT EXISTS streamvibe CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE streamvibe;

-- ─── USERS ──────────────────────────────────────────────────────
-- Stores every registered account.
-- Accounts are private (email/password auth with JWT).
-- Videos/Reels are public — no user_id needed on read.
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    username    VARCHAR(50)   NOT NULL,
    email       VARCHAR(255)  NOT NULL,
    password    VARCHAR(255)  NOT NULL,   -- BCrypt hash, never plain text
    avatar_emoji VARCHAR(10)  DEFAULT '👤',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email    (email),
    UNIQUE KEY uq_users_username (username)
) ENGINE=InnoDB;

-- ─── VIDEOS ─────────────────────────────────────────────────────
-- duration_seconds FLOAT covers 0.5s (min) to 72000s (20 hr max).
-- thumbnail_emoji is our theoretical placeholder for a real thumbnail URL.
-- is_public=TRUE mirrors YouTube: anyone can see the feed without login.
CREATE TABLE IF NOT EXISTS videos (
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    uploader_id       BIGINT         NOT NULL,
    title             VARCHAR(255)   NOT NULL,
    description       TEXT,
    duration_seconds  FLOAT          NOT NULL,
    thumbnail_emoji   VARCHAR(10)    DEFAULT '🎬',
    tags              VARCHAR(500),           -- comma-separated; normalise to tag table if needed
    view_count        BIGINT         NOT NULL DEFAULT 0,
    is_public         TINYINT(1)     NOT NULL DEFAULT 1,
    created_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_videos_uploader FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_video_duration CHECK (duration_seconds >= 0.5 AND duration_seconds <= 72000),
    INDEX idx_videos_uploader   (uploader_id),
    INDEX idx_videos_created_at (created_at)
) ENGINE=InnoDB;

-- ─── REELS ──────────────────────────────────────────────────────
-- Separate entity from videos.
-- duration_seconds FLOAT covers 0.01s to 180s (3 min max).
CREATE TABLE IF NOT EXISTS reels (
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    uploader_id       BIGINT         NOT NULL,
    caption           VARCHAR(500)   NOT NULL,
    duration_seconds  FLOAT          NOT NULL,
    emoji             VARCHAR(10)    DEFAULT '📱',
    tags              VARCHAR(500),
    view_count        BIGINT         NOT NULL DEFAULT 0,
    is_public         TINYINT(1)     NOT NULL DEFAULT 1,
    created_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_reels_uploader FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_reel_duration CHECK (duration_seconds >= 0.01 AND duration_seconds <= 180),
    INDEX idx_reels_uploader   (uploader_id),
    INDEX idx_reels_created_at (created_at)
) ENGINE=InnoDB;

-- ─── FRIENDSHIPS ────────────────────────────────────────────────
-- Models a bidirectional friendship via a directed request.
-- requester_id sent the request; addressee_id received it.
-- status: PENDING → ACCEPTED or DECLINED.
-- The UNIQUE constraint prevents duplicate requests between the same pair.
-- A CHECK ensures nobody sends a request to themselves.
CREATE TABLE IF NOT EXISTS friendships (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    requester_id  BIGINT       NOT NULL,
    addressee_id  BIGINT       NOT NULL,
    status        ENUM('PENDING','ACCEPTED','DECLINED') NOT NULL DEFAULT 'PENDING',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_fs_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_fs_addressee FOREIGN KEY (addressee_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_fs_no_self  CHECK (requester_id <> addressee_id),
    -- Prevent A→B and B→A coexisting (canonical ordering: smaller id = requester)
    UNIQUE KEY uq_friendship_pair (
        LEAST(requester_id, addressee_id),
        GREATEST(requester_id, addressee_id)
    ),
    INDEX idx_fs_requester (requester_id),
    INDEX idx_fs_addressee (addressee_id)
) ENGINE=InnoDB;

-- ─── SHARES ─────────────────────────────────────────────────────
-- Never copies video/reel data — stores only a reference.
-- content_type: 'VIDEO' or 'REEL'.
-- content_id: the PK of the referenced video or reel.
-- is_deleted: soft-delete; hides from receiver's inbox without
--             touching the original content or sender's record.
-- Clicking a shared item on the frontend navigates to
--   /video/{content_id}  or  /reels  using the original route.
CREATE TABLE IF NOT EXISTS shares (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    sender_id     BIGINT       NOT NULL,
    receiver_id   BIGINT       NOT NULL,
    content_type  ENUM('VIDEO','REEL') NOT NULL,
    content_id    BIGINT       NOT NULL,
    is_deleted    TINYINT(1)   NOT NULL DEFAULT 0,
    shared_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_shares_sender   FOREIGN KEY (sender_id)   REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_shares_receiver FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_shares_no_self CHECK (sender_id <> receiver_id),
    INDEX idx_shares_sender   (sender_id),
    INDEX idx_shares_receiver (receiver_id),
    INDEX idx_shares_content  (content_type, content_id)
) ENGINE=InnoDB;

-- ═══════════════════════════════════════════════════════════════
--  SEED DATA (demo accounts only — remove for production)
-- ═══════════════════════════════════════════════════════════════
-- Password hash for 'demo123' via BCrypt (cost 10)
INSERT IGNORE INTO users (id, username, email, password, avatar_emoji) VALUES
(1, 'Alex',  'alex@demo.com',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6.Mhm', '🦊'),
(2, 'Maya',  'maya@demo.com',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6.Mhm', '🌺'),
(3, 'Jake',  'jake@demo.com',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6.Mhm', '⚡'),
(4, 'Priya', 'priya@demo.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6.Mhm', '🎯');

-- Note: The BCrypt hash above is a placeholder — generate real hashes with:
-- spring.security.BCryptPasswordEncoder.encode("demo123")
-- or online at https://bcrypt-generator.com (cost 10)

-- Friendships: Alex↔Maya, Alex↔Jake, Maya↔Priya
INSERT IGNORE INTO friendships (requester_id, addressee_id, status) VALUES
(1, 2, 'ACCEPTED'),
(1, 3, 'ACCEPTED'),
(2, 4, 'ACCEPTED');

-- Sample videos
INSERT IGNORE INTO videos (id, uploader_id, title, description, duration_seconds, thumbnail_emoji, tags, view_count) VALUES
(1, 1, 'Mountain Hike Timelapse',     'Beautiful sunrise captured during a 4am summit hike.',  1800, '🌄', 'nature,travel,hike', 12400),
(2, 2, 'City Skyline at Night',       'Drone footage of the city lights reflecting on the bay.', 543,  '🌃', 'city,drone,night',  8900),
(3, 3, 'Cooking Ramen from Scratch',  'Full recipe walkthrough — dashi broth to toppings.',     2100, '🍜', 'food,cooking',      21300),
(4, 4, 'Piano Cover — Clair de Lune', 'Solo piano performance recorded in one take.',            330,  '🎹', 'music,piano',       5600),
(5, 1, 'Street Photography Tokyo',    '90 minutes walking the alleys of Shimokitazawa.',        5400, '📸', 'photo,japan,street',3200);

-- Sample reels
INSERT IGNORE INTO reels (id, uploader_id, caption, duration_seconds, emoji, tags, view_count) VALUES
(1, 2, 'Sunrise in 15 seconds ✨ #nature #morning', 15,  '✨', 'nature,morning', 45000),
(2, 3, 'Perfect pasta flip 🍝 #cooking #chef',      30,  '🍝', 'cooking,food',   32000),
(3, 4, 'Cat judges my outfit choices 😹 #cats',     22,  '😹', 'cats,funny',     91000),
(4, 1, 'Quick stretching routine 🧘 #fitness',      45,  '🧘', 'fitness,health', 18000),
(5, 2, 'Beach sunset magic 🌅 #travel #ocean',      18,  '🌅', 'travel,ocean',   27000),
(6, 3, 'Latte art tutorial ☕ #coffee #barista',    60,  '☕', 'coffee,art',     14000);


CREATE TABLE conversations (
                               id               BIGINT AUTO_INCREMENT PRIMARY KEY,
                               user_a_id        BIGINT NOT NULL,          -- always the lower user ID
                               user_b_id        BIGINT NOT NULL,          -- always the higher user ID
                               created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               last_activity_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               UNIQUE KEY uq_pair (user_a_id, user_b_id),
                               FOREIGN KEY (user_a_id) REFERENCES users(id) ON DELETE CASCADE,
                               FOREIGN KEY (user_b_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE messages (
                          id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                          conversation_id BIGINT NOT NULL,
                          sender_id       BIGINT NOT NULL,
                          type            ENUM('TEXT','VIDEO_REF','REEL_REF') NOT NULL,
                          text_body       VARCHAR(2000),             -- populated for TEXT
                          content_id      BIGINT,                   -- populated for VIDEO_REF / REEL_REF
                          is_deleted      BOOLEAN DEFAULT FALSE,
                          sent_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
                          FOREIGN KEY (sender_id)       REFERENCES users(id) ON DELETE CASCADE,
                          INDEX idx_conv_sent (conversation_id, sent_at),
                          INDEX idx_sender    (sender_id)
);

CREATE TABLE message_read_receipts (
                                       id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       message_id BIGINT NOT NULL,
                                       reader_id  BIGINT NOT NULL,
                                       read_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       UNIQUE KEY uq_read (message_id, reader_id),
                                       FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
                                       FOREIGN KEY (reader_id)  REFERENCES users(id) ON DELETE CASCADE
);