ALTER TABLE users
ADD COLUMN IF NOT EXISTS provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL';

ALTER TABLE evaluation_assignments
ADD COLUMN IF NOT EXISTS archived_by_evaluator BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE evaluation_assignments
ADD COLUMN IF NOT EXISTS archived_by_evaluatee BOOLEAN NOT NULL DEFAULT FALSE;

-- Ensure user_notification_preferences table and all columns exist
CREATE TABLE IF NOT EXISTS user_notification_preferences (
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT NOT NULL UNIQUE REFERENCES users(id),
    evaluation_assigned  BOOLEAN NOT NULL DEFAULT TRUE,
    deadline_reminder    BOOLEAN NOT NULL DEFAULT TRUE,
    results_published    BOOLEAN NOT NULL DEFAULT TRUE,
    form_created         BOOLEAN NOT NULL DEFAULT TRUE,
    submission_received  BOOLEAN NOT NULL DEFAULT TRUE,
    system_announcements BOOLEAN NOT NULL DEFAULT TRUE
);

-- Add any missing columns to existing table (safe to run multiple times)
ALTER TABLE user_notification_preferences ADD COLUMN IF NOT EXISTS evaluation_assigned  BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE user_notification_preferences ADD COLUMN IF NOT EXISTS deadline_reminder    BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE user_notification_preferences ADD COLUMN IF NOT EXISTS results_published    BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE user_notification_preferences ADD COLUMN IF NOT EXISTS form_created         BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE user_notification_preferences ADD COLUMN IF NOT EXISTS submission_received  BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE user_notification_preferences ADD COLUMN IF NOT EXISTS system_announcements BOOLEAN NOT NULL DEFAULT TRUE;

-- BR-001: Self-Evaluation Prevention
ALTER TABLE evaluation_assignments
DROP CONSTRAINT IF EXISTS chk_evaluator_evaluatee_diff;

ALTER TABLE evaluation_assignments
ADD CONSTRAINT chk_evaluator_evaluatee_diff
CHECK (evaluator_id <> evaluatee_id);

-- BR-003: Response Retention & Soft Delete
ALTER TABLE evaluations
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS permanently_closed BOOLEAN DEFAULT FALSE;
