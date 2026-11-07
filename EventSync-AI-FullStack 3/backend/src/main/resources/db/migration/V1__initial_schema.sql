CREATE TABLE app_users (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(255) NOT NULL,
  volunteer_id BIGINT,
  token_version INTEGER NOT NULL DEFAULT 0
);
CREATE TABLE events (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  location VARCHAR(255),
  date DATE,
  description VARCHAR(1000),
  status VARCHAR(255)
);
CREATE TABLE volunteers (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  skills VARCHAR(1000),
  experience VARCHAR(255),
  available BOOLEAN NOT NULL DEFAULT TRUE,
  max_workload INTEGER NOT NULL DEFAULT 3
);
CREATE TABLE tasks (
  id BIGSERIAL PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  event_id BIGINT,
  description VARCHAR(1000),
  required_skills VARCHAR(1000),
  workload INTEGER NOT NULL DEFAULT 1,
  status VARCHAR(255)
);
CREATE TABLE assignments (
  id BIGSERIAL PRIMARY KEY,
  event_id BIGINT,
  task_id BIGINT,
  volunteer_id BIGINT,
  status VARCHAR(255),
  assigned_at TIMESTAMP
);
CREATE TABLE notifications (
  id BIGSERIAL PRIMARY KEY,
  message VARCHAR(1000) NOT NULL,
  type VARCHAR(255),
  created_at TIMESTAMP,
  read_flag BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_app_users_email ON app_users (email);
CREATE INDEX idx_assignments_volunteer_status ON assignments (volunteer_id, status);
CREATE INDEX idx_assignments_task_status ON assignments (task_id, status);
CREATE INDEX idx_tasks_event_status ON tasks (event_id, status);
CREATE INDEX idx_notifications_created_at ON notifications (created_at DESC);
