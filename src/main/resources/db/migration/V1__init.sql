CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(30) NOT NULL UNIQUE,
                       password VARCHAR(80) NOT NULL,
                       email VARCHAR(50) UNIQUE,
                       created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       registration_status VARCHAR(30) NOT NULL,
                       last_login TIMESTAMP,
                       deleted BOOLEAN NOT NULL DEFAULT false
);

CREATE TABLE posts (
                       id BIGSERIAL PRIMARY KEY,
                       user_id INTEGER NOT NULL ,
                       title VARCHAR(255) NOT NULL,
                       content TEXT NOT NULL,
                       created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       deleted BOOLEAN NOT NULL DEFAULT false,
                       likes INTEGER NOT NULL  DEFAULT 0,
                       created_by VARCHAR(50),
                       image VARCHAR(2048),
                       comments_count INTEGER NOT NULL DEFAULT 0,
                       FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                       UNIQUE (title)
);

CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       name VARCHAR(50) NOT NULL,
                       user_system_role VARCHAR(64) NOT NULL,
                       active BOOLEAN NOT NULL DEFAULT true,
                       created_by VARCHAR(50) NOT NULL
);

CREATE TABLE users_roles (
                       user_id BIGINT NOT NULL,
                       role_id INT NOT NULL,
                       PRIMARY KEY (user_id, role_id),
                       FOREIGN KEY (user_id) REFERENCES users (id),
                       FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE refresh_token (
                       id SERIAL PRIMARY KEY,
                       token VARCHAR(128) NOT NULL,
                       created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       user_id BIGINT NOT NULL,
                       CONSTRAINT FK_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
                       CONSTRAINT refresh_token_UNIQUE UNIQUE (user_id, id)
);

INSERT INTO users (username, password, email, created, updated, registration_status, last_login, deleted) VALUES
                       ('super_admin', '$2a$12$zn0gjTqf7mn8QLt8Nplnl.JkOc9DCcXPGDKcO4Nta6gwYptHLdc2a', 'superadmin@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE', CURRENT_TIMESTAMP, false),
                       ('admin', '$2a$12$zn0gjTqf7mn8QLt8Nplnl.JkOc9DCcXPGDKcO4Nta6gwYptHLdc2a', 'admin@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE', CURRENT_TIMESTAMP, false),
                       ('user', '$2a$12$zn0gjTqf7mn8QLt8Nplnl.JkOc9DCcXPGDKcO4Nta6gwYptHLdc2a', 'user@gmail.com', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'ACTIVE', CURRENT_TIMESTAMP, false);

INSERT INTO posts (user_id, title, content, created, updated, deleted, likes, image) VALUES
                       (1, 'First Post', 'This is content of the first post', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, 6, NULL),
                       (2, 'Second Post', 'This is content of the second post', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false, 3, NULL);

INSERT INTO roles (name, user_system_role, created_by) VALUES
                       ('SUPER_ADMIN', 'SUPER_ADMIN', 'SUPER_ADMIN'),
                       ('ADMIN', 'ADMIN', 'SUPER_ADMIN'),
                       ('USER', 'USER', 'SUPER_ADMIN');

INSERT INTO users_roles (user_id, role_id) VALUES
                       (1, 1),
                       (2, 2),
                       (3, 3);
