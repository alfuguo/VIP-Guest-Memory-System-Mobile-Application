-- VIP Guest Memory System - Performance Indexes
-- This migration creates indexes for frequently queried fields

-- Indexes for staff table
CREATE INDEX idx_staff_email ON staff(email);
CREATE INDEX idx_staff_role ON staff(role);
CREATE INDEX idx_staff_active ON staff(active);

-- Indexes for guests table
CREATE INDEX idx_guests_phone ON guests(phone);
CREATE INDEX idx_guests_name ON guests(first_name, last_name);
CREATE INDEX idx_guests_email ON guests(email);
CREATE INDEX idx_guests_created_by ON guests(created_by);
CREATE INDEX idx_guests_birthday ON guests(birthday);
CREATE INDEX idx_guests_anniversary ON guests(anniversary);
CREATE INDEX idx_guests_deleted_at ON guests(deleted_at); -- For soft delete queries
CREATE INDEX idx_guests_active ON guests(id) WHERE deleted_at IS NULL; -- Partial index for active guests

-- Indexes for visits table
CREATE INDEX idx_visits_guest_id ON visits(guest_id);
CREATE INDEX idx_visits_staff_id ON visits(staff_id);
CREATE INDEX idx_visits_guest_date ON visits(guest_id, visit_date DESC);
CREATE INDEX idx_visits_date ON visits(visit_date DESC);
CREATE INDEX idx_visits_date_time ON visits(visit_date, visit_time);

-- Indexes for audit log table
CREATE INDEX idx_audit_staff_id ON audit_log(staff_id);
CREATE INDEX idx_audit_table_record ON audit_log(table_name, record_id);
CREATE INDEX idx_audit_created_at ON audit_log(created_at DESC);
CREATE INDEX idx_audit_action ON audit_log(action);

-- Composite indexes for common query patterns
CREATE INDEX idx_guests_search ON guests(first_name, last_name, phone) WHERE deleted_at IS NULL;
CREATE INDEX idx_visits_recent ON visits(guest_id, visit_date DESC, visit_time DESC);