-- VIP Guest Memory System - Initial Database Schema
-- This migration creates the core tables for staff, guests, and visits

-- Staff table for authentication and user management
CREATE TABLE staff (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('HOST', 'SERVER', 'MANAGER')),
    active BOOLEAN DEFAULT true,
    failed_login_attempts INTEGER DEFAULT 0,
    account_locked_until TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Guests table for customer profiles
CREATE TABLE guests (
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
    created_by INTEGER REFERENCES staff(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL -- For soft delete functionality
);

-- Visits table for tracking guest visits
CREATE TABLE visits (
    id SERIAL PRIMARY KEY,
    guest_id INTEGER NOT NULL REFERENCES guests(id) ON DELETE CASCADE,
    staff_id INTEGER NOT NULL REFERENCES staff(id),
    visit_date DATE NOT NULL,
    visit_time TIME NOT NULL,
    party_size INTEGER DEFAULT 1 CHECK (party_size > 0),
    table_number VARCHAR(10),
    service_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit log table for tracking data access and modifications
CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    staff_id INTEGER REFERENCES staff(id),
    action VARCHAR(50) NOT NULL,
    table_name VARCHAR(50) NOT NULL,
    record_id INTEGER,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);