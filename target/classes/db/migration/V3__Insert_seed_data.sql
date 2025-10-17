-- VIP Guest Memory System - Seed Data
-- This migration inserts initial data for development and testing

-- Insert default staff members (passwords are hashed for 'password123')
INSERT INTO staff (email, password_hash, first_name, last_name, role, active) VALUES
('manager@restaurant.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ycsgj/VGwHyplPrLHpFuDjqfqKu', 'John', 'Manager', 'MANAGER', true),
('host@restaurant.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ycsgj/VGwHyplPrLHpFuDjqfqKu', 'Sarah', 'Johnson', 'HOST', true),
('server1@restaurant.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ycsgj/VGwHyplPrLHpFuDjqfqKu', 'Mike', 'Wilson', 'SERVER', true),
('server2@restaurant.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye.Uo0ycsgj/VGwHyplPrLHpFuDjqfqKu', 'Emma', 'Davis', 'SERVER', true);

-- Insert sample guest data
INSERT INTO guests (first_name, last_name, phone, email, seating_preference, dietary_restrictions, favorite_drinks, birthday, anniversary, notes, created_by) VALUES
('Alice', 'Smith', '+1234567890', 'alice.smith@email.com', 'Window table', ARRAY['Vegetarian'], ARRAY['Red wine', 'Sparkling water'], '1985-06-15', '2010-09-20', 'Prefers quiet atmosphere, celebrates anniversary here annually', 1),
('Bob', 'Johnson', '+1234567891', 'bob.johnson@email.com', 'Booth', ARRAY['Gluten-free'], ARRAY['Beer', 'Coffee'], '1978-03-22', NULL, 'Regular customer, comes for business lunches', 1),
('Carol', 'Williams', '+1234567892', 'carol.williams@email.com', 'Patio', ARRAY['No nuts'], ARRAY['White wine', 'Cocktails'], '1990-11-08', '2015-05-14', 'Loves outdoor seating, allergic to tree nuts', 2),
('David', 'Brown', '+1234567893', NULL, 'Bar seating', ARRAY[], ARRAY['Whiskey', 'Dark beer'], '1982-12-03', NULL, 'Enjoys watching sports at the bar', 2),
('Eva', 'Martinez', '+1234567894', 'eva.martinez@email.com', 'Corner table', ARRAY['Vegan', 'No dairy'], ARRAY['Herbal tea', 'Fresh juice'], '1995-07-19', NULL, 'Strict vegan diet, very health conscious', 3);

-- Insert sample visit data
INSERT INTO visits (guest_id, staff_id, visit_date, visit_time, party_size, table_number, service_notes) VALUES
(1, 3, '2024-01-15', '19:30:00', 2, 'W5', 'Anniversary dinner, provided complimentary dessert'),
(1, 4, '2023-12-20', '18:00:00', 4, 'W3', 'Family dinner, requested high chair'),
(2, 3, '2024-01-10', '12:30:00', 3, 'B2', 'Business lunch, needed quiet table'),
(2, 3, '2024-01-03', '12:00:00', 2, 'B1', 'Regular lunch meeting'),
(3, 4, '2024-01-08', '20:00:00', 2, 'P1', 'Date night, requested patio despite cold weather'),
(4, 3, '2024-01-12', '21:00:00', 1, 'BR3', 'Watched game, stayed until closing'),
(5, 4, '2024-01-05', '11:30:00', 1, 'C4', 'Brunch, special vegan menu preparation');

-- Insert audit log sample (for demonstration)
INSERT INTO audit_log (staff_id, action, table_name, record_id, new_values) VALUES
(1, 'CREATE', 'guests', 1, '{"first_name": "Alice", "last_name": "Smith", "phone": "+1234567890"}'),
(2, 'CREATE', 'guests', 3, '{"first_name": "Carol", "last_name": "Williams", "phone": "+1234567892"}'),
(3, 'CREATE', 'visits', 1, '{"guest_id": 1, "visit_date": "2024-01-15", "party_size": 2}');