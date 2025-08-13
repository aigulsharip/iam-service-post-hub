CREATE TABLE email_verification_token (
                                          id SERIAL PRIMARY KEY,
                                          token VARCHAR(255) NOT NULL UNIQUE,
                                          created TIMESTAMP NOT NULL,
                                          expires TIMESTAMP NOT NULL,
                                          user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE
);
