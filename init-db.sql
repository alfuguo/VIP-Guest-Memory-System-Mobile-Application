-- Database initialization script for VIP Guest Memory System
-- This script creates the initial database schema and default data

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create staff table
CREATE TABLE IF NOT EXISTS staff (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('HOST', 'SERVER', 'MANAGER')),
    active BOOLEAN DEFAULT true,
    failed_login_attempts INTEGER DEFAULT 0,
    locked_until TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create guests table
CREATE TABLE IF NOT EXISTS guests (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    phone VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(255),
    photo_url VARCHAR(500),
    seating_preference VARCHAR(100),
    dietary_restrictions TEXT[],
    favorite_drinks TEXT[],
    birthday DATE,
    anniversary DATE,
    notes TEXT,
    active BOOLEAN DEFAULT true,
    created_by INTEGER REFERENCES staff(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create visits table
CREATE TABLE IF NOT EXISTS visits (
    id SERIAL PRIMARY KEY,
    guest_id INTEGER REFERENCES guests(id) ON DELETE CASCADE,
    staff_id INTEGER REFERENCES staff(id),
    visit_date DATE NOT NULL,
    visit_time TIME NOT NULL,
    party_size INTEGER DEFAULT 1,
    table_number VARCHAR(10),
    service_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create audit_logs table for security tracking
CREATE TABLE IF NOT EXISTS audit_logs (
    id SERIAL PRIMARY KEY,
    staff_id INTEGER REFERENCES staff(id),
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id INTEGER,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_guests_phone ON guests(phone);
CREATE INDEX IF NOT EXISTS idx_guests_name ON guests(first_name, last_name);
CREATE INDEX IF NOT EXISTS idx_guests_active ON guests(active);
CREATE INDEX IF NOT EXISTS idx_visits_guest_date ON visits(guest_id, visit_date DESC);
CREATE INDEX IF NOT EXISTS idx_visits_date ON visits(visit_date DESC);
CREATE INDEX IF NOT EXISTS idx_staff_email ON staff(email);
CREATE INDEX IF NOT EXISTS idx_staff_active ON staff(active);
CREATE INDEX IF NOT EXISTS idx_audit_logs_staff_date ON audit_logs(staff_id, created_at DESC);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at
DROP TRIGGER IF EXISTS update_staff_updated_at ON staff;
CREATE TRIGGER update_staff_updated_at
    BEFORE UPDATE ON staff
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_guests_updated_at ON guests;
CREATE TRIGGER update_guests_updated_at
    BEFORE UPDATE ON guests
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_visits_updated_at ON visits;
CREATE TRIGGER update_visits_updated_at
    BEFORE UPDATE ON visits
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Insert default admin user (password: "admin123" - CHANGE IN PRODUCTION!)
INSERT INTO staff (email, password_hash, first_name, last_name, role, active) 
VALUES (
    'admin@restaurant.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    'System',
    'Administrator',
    'MANAGER',
    true
) ON CONFLICT (email) DO NOTHING;

-- Insert sample staff accounts for testing (password: "password" for all)
INSERT INTO staff (email, password_hash, first_name, last_name, role, active) 
VALUES 
    ('manager@restaurant.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Test', 'Manager', 'MANAGER', true),
    ('server@restaurant.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Test', 'Server', 'SERVER', true),
    ('host@restaurant.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Test', 'Host', 'HOST', true)
ON CONFLICT (email) DO NOTHING;

-- Create view for guest statistics
CREATE OR REPLACE VIEW guest_statistics AS
SELECT 
    g.id,
    g.first_name,
    g.last_name,
    g.phone,
    COUNT(v.id) as visit_count,
    MAX(v.visit_date) as last_visit_date,
    MIN(v.visit_date) as first_visit_date,
    AVG(v.party_size) as avg_party_size,
    CASE 
        WHEN MAX(v.visit_date) < CURRENT_DATE - INTERVAL '6 months' THEN true
        ELSE false
    END as is_returning_guest,
    CASE 
        WHEN g.birthday IS NOT NULL AND 
             DATE_PART('month', g.birthday) = DATE_PART('month', CURRENT_DATE) AND
             DATE_PART('day', g.birthday) BETWEEN DATE_PART('day', CURRENT_DATE) AND DATE_PART('day', CURRENT_DATE + INTERVAL '30 days')
        THEN true
        ELSE false
    END as has_upcoming_birthday,
    CASE 
        WHEN g.anniversary IS NOT NULL AND 
             DATE_PART('month', g.anniversary) = DATE_PART('month', CURRENT_DATE) AND
             DATE_PART('day', g.anniversary) BETWEEN DATE_PART('day', CURRENT_DATE) AND DATE_PART('day', CURRENT_DATE + INTERVAL '30 days')
        THEN true
        ELSE false
    END as has_upcoming_anniversary
FROM guests g
LEFT JOIN visits v ON g.id = v.guest_id
WHERE g.active = true
GROUP BY g.id, g.first_name, g.last_name, g.phone, g.birthday, g.anniversary;

-- Grant permissions to application user (will be created by Docker)
-- Note: This will only work if the user exists, otherwise it will be ignored
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'vip_user') THEN
        GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO vip_user;
        GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO vip_user;
        GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO vip_user;
    END IF;
END
$$;