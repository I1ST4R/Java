-- Create database if not exists
CREATE DATABASE IF NOT EXISTS my_app;
USE my_app;

-- Drop tables if they exist (in correct order to handle foreign keys)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS trainer_skills;
DROP TABLE IF EXISTS trainers;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
SET FOREIGN_KEY_CHECKS = 1;

-- Create tables
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    trainer_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE trainers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    hourly_rate DOUBLE NOT NULL,
    experience_years INT NOT NULL,
    description TEXT,
    photo_path VARCHAR(255)
);

CREATE TABLE trainer_skills (
    trainer_id BIGINT NOT NULL,
    skill VARCHAR(100) NOT NULL,
    PRIMARY KEY (trainer_id, skill),
    FOREIGN KEY (trainer_id) REFERENCES trainers(id)
);

-- Insert default roles
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_TRAINER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

-- Insert admin user (password: root)
INSERT INTO users (username, email, password, created_at, updated_at)
VALUES ('root', 'root@example.com', '$2a$10$wGlFMge3Mv6v.EYcGnfJX.xh19Xk9ai988dk78w504HywyNJmCLz2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Assign admin role to root user
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'root' AND r.name = 'ROLE_ADMIN'; 