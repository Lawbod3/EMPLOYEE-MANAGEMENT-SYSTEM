CREATE TABLE employees (
                           employee_id BIGSERIAL PRIMARY KEY,
                           employee_code VARCHAR(50) NOT NULL UNIQUE,
                           user_id BIGINT NOT NULL UNIQUE,
                           first_name VARCHAR(100),
                           last_name VARCHAR(100),
                           email VARCHAR(150),
                           status VARCHAR(30),
                           departments TEXT, -- stored as comma-separated string from StringListConverter
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
