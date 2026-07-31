-- FinAI Database Initialization Script
-- This script is automatically executed when MySQL container starts

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS finai_dev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS finai_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Grant privileges
GRANT ALL PRIVILEGES ON finai_dev.* TO 'finai_user'@'%';
GRANT ALL PRIVILEGES ON finai_test.* TO 'finai_user'@'%';
FLUSH PRIVILEGES;

-- Use the development database
USE finai_dev;

-- Create audit log table (example)
CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_name VARCHAR(255) NOT NULL,
    entity_id BIGINT,
    action VARCHAR(50) NOT NULL,
    user_id BIGINT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    details TEXT,
    INDEX idx_entity (entity_name, entity_id),
    INDEX idx_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Log initialization
INSERT INTO audit_log (entity_name, action, details) 
VALUES ('DATABASE', 'INIT', 'Database initialized successfully');
