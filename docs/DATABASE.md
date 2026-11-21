Connect the Kotlin Android application to a MySQL database through a secure Spring Boot REST API:

-- =========================================================
-- NEURO FOCUS DATABASE
-- Kotlin Android + JSON REST API
-- MySQL 8.0.16+
-- =========================================================

DROP DATABASE IF EXISTS neuro_focus;

CREATE DATABASE neuro_focus
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE neuro_focus;

SET time_zone = '+00:00';

-- =========================================================
-- 1. USERS
-- =========================================================

CREATE TABLE users (
id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
full_name VARCHAR(120) NOT NULL,
email VARCHAR(255) NOT NULL UNIQUE,
password_hash VARCHAR(255) NOT NULL,
avatar_url VARCHAR(500) NULL,
timezone VARCHAR(64) NOT NULL DEFAULT 'UTC',
account_status ENUM('active', 'suspended', 'deleted')
NOT NULL DEFAULT 'active',
email_verified BOOLEAN NOT NULL DEFAULT FALSE,
created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
ON UPDATE CURRENT_TIMESTAMP(3),
deleted_at DATETIME(3) NULL
) ENGINE=InnoDB;

-- =========================================================
-- 2. USER PREFERENCES
-- =========================================================

CREATE TABLE user_preferences (
id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
user_id BIGINT UNSIGNED NOT NULL UNIQUE,

    focus_duration_minutes SMALLINT UNSIGNED NOT NULL DEFAULT 25,
    break_duration_minutes SMALLINT UNSIGNED NOT NULL DEFAULT 5,
    daily_focus_goal_minutes SMALLINT UNSIGNED NOT NULL DEFAULT 120,

    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sound_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    adaptive_recommendations_enabled BOOLEAN NOT NULL DEFAULT TRUE,

    theme ENUM('system', 'light', 'dark') NOT NULL DEFAULT 'system',

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),

    CONSTRAINT fk_preferences_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_focus_duration
        CHECK (focus_duration_minutes BETWEEN 5 AND 180),

    CONSTRAINT chk_break_duration
        CHECK (break_duration_minutes BETWEEN 1 AND 60),

    CONSTRAINT chk_daily_goal
        CHECK (daily_focus_goal_minutes BETWEEN 5 AND 1440)
) ENGINE=InnoDB;

-- =========================================================
-- 3. USER CONSENTS
-- =========================================================

CREATE TABLE user_consents (
id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
user_id BIGINT UNSIGNED NOT NULL,

    consent_type ENUM(
        'camera',
        'signal_processing',
        'analytics',
        'privacy_policy'
    ) NOT NULL,

    consent_version VARCHAR(30) NOT NULL,
    privacy_policy_version VARCHAR(30) NULL,

    is_granted BOOLEAN NOT NULL DEFAULT FALSE,
    granted_at DATETIME(3) NULL,
    revoked_at DATETIME(3) NULL,

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    CONSTRAINT fk_consents_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_consent_dates
        CHECK (
            (is_granted = FALSE)
            OR
            (granted_at IS NOT NULL AND revoked_at IS NULL)
        ),

    INDEX idx_consents_user_type (user_id, consent_type)
) ENGINE=InnoDB;

-- =========================================================
-- 4. FOCUS SESSIONS
-- =========================================================

CREATE TABLE focus_sessions (
id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
user_id BIGINT UNSIGNED NOT NULL,

    title VARCHAR(150) NOT NULL,
    description TEXT NULL,

    session_type ENUM('focus', 'break', 'deep_work')
        NOT NULL DEFAULT 'focus',

    planned_duration_minutes SMALLINT UNSIGNED NOT NULL,
    actual_duration_seconds INT UNSIGNED NOT NULL DEFAULT 0,

    status ENUM(
        'pending',
        'active',
        'completed',
        'cancelled',
        'interrupted'
    ) NOT NULL DEFAULT 'pending',

    started_at DATETIME(3) NULL,
    ended_at DATETIME(3) NULL,

    interruption_count SMALLINT UNSIGNED NOT NULL DEFAULT 0,

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),

    CONSTRAINT fk_sessions_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT chk_planned_duration
        CHECK (planned_duration_minutes BETWEEN 1 AND 1440),

    CONSTRAINT chk_session_dates
        CHECK (
            ended_at IS NULL
            OR started_at IS NULL
            OR ended_at >= started_at
        ),

    UNIQUE KEY uq_session_owner (id, user_id),

    INDEX idx_sessions_user_status (user_id, status),
    INDEX idx_sessions_user_started (user_id, started_at)
) ENGINE=InnoDB;

-- =========================================================
-- 5. SIGNAL MEASUREMENTS
-- =========================================================

CREATE TABLE signal_measurements (
id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT UNSIGNED NOT NULL,
    session_id BIGINT UNSIGNED NOT NULL,

    provider VARCHAR(80) NOT NULL DEFAULT 'Presage SmartSpectra',
    
    signal_type ENUM(
        'heart_rate',
        'breathing_rate',
        'hrv',
        'movement',
        'facial_expression'
    ) NOT NULL,

    signal_value DECIMAL(12,4) NULL,
    unit VARCHAR(30) NULL,

    signal_quality ENUM(
        'low',
        'medium',
        'high',
        'unknown'
    ) NOT NULL DEFAULT 'unknown',

    measured_at DATETIME(3) NOT NULL,
    expires_at DATETIME(3) NULL,

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    CONSTRAINT fk_signals_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_signals_session_owner
        FOREIGN KEY (session_id, user_id)
        REFERENCES focus_sessions(id, user_id)
        ON DELETE CASCADE,

    INDEX idx_signals_session_time (session_id, measured_at),
    INDEX idx_signals_user_type (user_id, signal_type),
    INDEX idx_signals_expiry (expires_at)
) ENGINE=InnoDB;

-- =========================================================
-- 6. RECOMMENDATIONS
-- =========================================================

CREATE TABLE recommendations (
id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT UNSIGNED NOT NULL,
    session_id BIGINT UNSIGNED NOT NULL,

    recommendation_type ENUM(
        'focus',
        'break',
        'breathing',
        'environment',
        'hydration',
        'movement'
    ) NOT NULL,

    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,

    reason VARCHAR(255) NULL,

    priority ENUM('low', 'medium', 'high')
        NOT NULL DEFAULT 'medium',

    status ENUM(
        'pending',
        'accepted',
        'dismissed',
        'completed'
    ) NOT NULL DEFAULT 'pending',

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    responded_at DATETIME(3) NULL,

    CONSTRAINT fk_recommendations_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_recommendations_session_owner
        FOREIGN KEY (session_id, user_id)
        REFERENCES focus_sessions(id, user_id)
        ON DELETE CASCADE,

    INDEX idx_recommendations_user_status (user_id, status),
    INDEX idx_recommendations_session (session_id)
) ENGINE=InnoDB;

-- =========================================================
-- 7. SESSION FEEDBACK
-- =========================================================

CREATE TABLE session_feedback (
id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT UNSIGNED NOT NULL,
    session_id BIGINT UNSIGNED NOT NULL UNIQUE,

    focus_rating TINYINT UNSIGNED NOT NULL,
    mood_rating TINYINT UNSIGNED NULL,
    productivity_rating TINYINT UNSIGNED NULL,

    feedback_text TEXT NULL,

    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    CONSTRAINT fk_feedback_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_feedback_session_owner
        FOREIGN KEY (session_id, user_id)
        REFERENCES focus_sessions(id, user_id)
        ON DELETE CASCADE,

    CONSTRAINT chk_focus_rating
        CHECK (focus_rating BETWEEN 1 AND 5),

    CONSTRAINT chk_mood_rating
        CHECK (mood_rating IS NULL OR mood_rating BETWEEN 1 AND 5),

    CONSTRAINT chk_productivity_rating
        CHECK (
            productivity_rating IS NULL
            OR productivity_rating BETWEEN 1 AND 5
        )
) ENGINE=InnoDB;

-- =========================================================
-- 8. SESSION SUMMARIES
-- =========================================================

CREATE TABLE session_summaries (
id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,

    user_id BIGINT UNSIGNED NOT NULL,
    session_id BIGINT UNSIGNED NOT NULL UNIQUE,

    total_focus_seconds INT UNSIGNED NOT NULL DEFAULT 0,
    total_break_seconds INT UNSIGNED NOT NULL DEFAULT 0,

    average_heart_rate DECIMAL(8,2) NULL,
    average_breathing_rate DECIMAL(8,2) NULL,
    average_hrv DECIMAL(8,2) NULL,

    focus_score DECIMAL(5,2) NULL,

    summary_text TEXT NULL,

    generated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    CONSTRAINT fk_summaries_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_summaries_session_owner
        FOREIGN KEY (session_id, user_id)
        REFERENCES focus_sessions(id, user_id)
        ON DELETE CASCADE,

    CONSTRAINT chk_focus_score
        CHECK (focus_score IS NULL OR focus_score BETWEEN 0 AND 100),

    INDEX idx_summaries_user (user_id, generated_at)
) ENGINE=InnoDB;

-- =========================================================
-- SAMPLE DATA
-- These are fictional development records, not real signals.
-- =========================================================

-- USERS

INSERT INTO users (
id, full_name, email, password_hash,
timezone, account_status, email_verified
) VALUES
(
1, 'Alice', 'alice@example.com',
'$2b$12$REPLACE_WITH_REAL_BCRYPT_HASH',
'Asia/Almaty', 'active', TRUE
),
(
2, 'Demo User', 'demo@example.com',
'$2b$12$REPLACE_WITH_REAL_BCRYPT_HASH',
'Asia/Jakarta', 'active', TRUE
);

-- USER PREFERENCES

INSERT INTO user_preferences (
user_id,
focus_duration_minutes,
break_duration_minutes,
daily_focus_goal_minutes,
notifications_enabled,
sound_enabled,
adaptive_recommendations_enabled,
theme
) VALUES
(1, 25, 5, 120, TRUE, TRUE, TRUE, 'system'),
(2, 45, 10, 180, TRUE, FALSE, TRUE, 'dark');

-- USER CONSENTS

INSERT INTO user_consents (
user_id,
consent_type,
consent_version,
privacy_policy_version,
is_granted,
granted_at
) VALUES
(1, 'camera', '1.0', '1.0', TRUE, UTC_TIMESTAMP(3)),
(1, 'signal_processing', '1.0', '1.0', TRUE, UTC_TIMESTAMP(3)),
(1, 'privacy_policy', '1.0', '1.0', TRUE, UTC_TIMESTAMP(3)),
(2, 'camera', '1.0', '1.0', TRUE, UTC_TIMESTAMP(3));

-- FOCUS SESSIONS

INSERT INTO focus_sessions (
id,
user_id,
title,
description,
session_type,
planned_duration_minutes,
actual_duration_seconds,
status,
started_at,
ended_at,
interruption_count
) VALUES
(
1, 1, 'Morning Deep Work',
'Working on Neuro Focus development',
'deep_work', 25, 1500, 'completed',
DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 120 MINUTE),
DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 95 MINUTE),
0
),
(
2, 1, 'Research Session',
'Researching adaptive productivity systems',
'focus', 45, 2700, 'completed',
DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 180 MINUTE),
DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 135 MINUTE),
1
),
(
3, 1, 'Current Focus Session',
'Testing live signal integration',
'focus', 25, 0, 'active',
DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 10 MINUTE),
NULL,
0
);

-- SIGNAL MEASUREMENTS
-- Fictional test values. Replace with validated SDK output.

INSERT INTO signal_measurements (
user_id,
session_id,
provider,
signal_type,
signal_value,
unit,
signal_quality,
measured_at,
expires_at
) VALUES
(
1, 1, 'Presage SmartSpectra',
'heart_rate', 76.0000, 'bpm', 'high',
DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 115 MINUTE),
DATE_ADD(UTC_TIMESTAMP(3), INTERVAL 1 DAY)
),
(
1, 1, 'Presage SmartSpectra',
'breathing_rate', 15.0000, 'breaths/min', 'high',
DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 110 MINUTE),
DATE_ADD(UTC_TIMESTAMP(3), INTERVAL 1 DAY)
),
(
1, 1, 'Presage SmartSpectra',
'hrv', 48.5000, 'ms', 'medium',
DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 105 MINUTE),
DATE_ADD(UTC_TIMESTAMP(3), INTERVAL 1 DAY)
),
(
1, 2, 'Presage SmartSpectra',
'heart_rate', 82.0000, 'bpm', 'high',
DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 175 MINUTE),
DATE_ADD(UTC_TIMESTAMP(3), INTERVAL 1 DAY)
),
(
1, 3, 'Presage SmartSpectra',
'heart_rate', 79.0000, 'bpm', 'medium',
DATE_SUB(UTC_TIMESTAMP(3), INTERVAL 5 MINUTE),
DATE_ADD(UTC_TIMESTAMP(3), INTERVAL 1 DAY)
);

-- RECOMMENDATIONS

INSERT INTO recommendations (
user_id,
session_id,
recommendation_type,
title,
message,
reason,
priority,
status
) VALUES
(
1, 1, 'break',
'Take a short break',
'Your focus session is complete. Consider taking a five-minute break.',
'Scheduled focus session completed',
'medium',
'completed'
),
(
1, 2, 'movement',
'Try a short movement break',
'Stand up, stretch, or walk for a few minutes if you feel comfortable.',
'A longer focus session has ended',
'low',
'accepted'
),
(
1, 3, 'focus',
'Keep your focus session comfortable',
'Continue working at a comfortable pace. You can pause whenever you need.',
'Active focus session',
'low',
'pending'
);

-- SESSION FEEDBACK

INSERT INTO session_feedback (
user_id,
session_id,
focus_rating,
mood_rating,
productivity_rating,
feedback_text
) VALUES
(
1, 1, 4, 4, 5,
'Good focus session. I completed my planned task.'
),
(
1, 2, 3, 4, 4,
'Some distractions, but overall productive.'
);

-- SESSION SUMMARIES

INSERT INTO session_summaries (
user_id,
session_id,
total_focus_seconds,
total_break_seconds,
average_heart_rate,
average_breathing_rate,
average_hrv,
focus_score,
summary_text
) VALUES
(
1, 1, 1500, 0,
76.00, 15.00, 48.50, 85.00,
'Completed a 25-minute deep work session.'
),
(
1, 2, 2700, 0,
82.00, NULL, NULL, 72.00,
'Completed a 45-minute research session.'
);

-- =========================================================
-- VERIFICATION QUERIES
-- =========================================================

-- 1. Check all database tables
SHOW TABLES;

-- 2. Check users
SELECT id, full_name, email, account_status
FROM users;

-- 3. Check preferences
SELECT *
FROM user_preferences;

-- 4. Check focus sessions
SELECT
id,
user_id,
title,
session_type,
planned_duration_minutes,
status,
started_at,
ended_at
FROM focus_sessions
ORDER BY id;

-- 5. Check signal measurements
SELECT
id,
session_id,
signal_type,
signal_value,
unit,
signal_quality,
measured_at
FROM signal_measurements
ORDER BY measured_at DESC;

-- 6. Check recommendations
SELECT
id,
session_id,
title,
recommendation_type,
priority,
status
FROM recommendations;

-- 7. Check session feedback
SELECT *
FROM session_feedback;

-- 8. Check session summaries
SELECT *
FROM session_summaries;

-- 9. Dashboard overview for user ID 1
SELECT
u.id AS user_id,
u.full_name,
COUNT(fs.id) AS total_sessions,
SUM(
CASE WHEN fs.status = 'completed' THEN 1 ELSE 0 END
) AS completed_sessions,
COALESCE(
SUM(
CASE
WHEN fs.status = 'completed'
THEN fs.actual_duration_seconds
ELSE 0
END
), 0
) AS total_focus_seconds
FROM users u
LEFT JOIN focus_sessions fs
ON fs.user_id = u.id
WHERE u.id = 1
GROUP BY u.id, u.full_name;

-- =========================================================
-- END OF NEURO FOCUS DATABASE SETUP
-- =========================================================