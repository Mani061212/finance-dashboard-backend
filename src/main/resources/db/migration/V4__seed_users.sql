-- Passwords are placeholder — replaced with real BCrypt hashes by DataSeeder.java on first startup.
INSERT INTO users (id, full_name, email, password, role, is_active)
VALUES (gen_random_uuid(), 'System Admin',    'admin@finance.com',   'CHANGEME', 'ADMIN',   TRUE)
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (id, full_name, email, password, role, is_active)
VALUES (gen_random_uuid(), 'Default Analyst', 'analyst@finance.com', 'CHANGEME', 'ANALYST', TRUE)
ON CONFLICT (email) DO NOTHING;