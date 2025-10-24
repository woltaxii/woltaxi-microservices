-- WOLTAXI Database Initialization Script
-- Bu script WOLTAXI projesinin tüm tablolarını ve ilişkilerini oluşturur
-- PostgreSQL 15+ uyumludur

-- Database encoding ve collation ayarları
ALTER DATABASE woltaxi SET timezone TO 'Europe/Istanbul';

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";
CREATE EXTENSION IF NOT EXISTS "pg_trgm"; -- For text search

-- =============================================================================
-- USERS SCHEMA - Kullanıcı Yönetimi
-- =============================================================================

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(13) NOT NULL UNIQUE,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    birth_date DATE,
    gender VARCHAR(10),
    profile_photo_url VARCHAR(500),
    
    -- Verification
    is_phone_verified BOOLEAN DEFAULT FALSE,
    is_email_verified BOOLEAN DEFAULT FALSE,
    verification_code VARCHAR(6),
    verification_expires_at TIMESTAMP,
    
    -- Status
    status VARCHAR(20) DEFAULT 'ACTIVE',
    last_login TIMESTAMP,
    
    -- Preferences
    preferred_language VARCHAR(5) DEFAULT 'tr',
    notifications_enabled BOOLEAN DEFAULT TRUE,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- DRIVERS SCHEMA - Sürücü Yönetimi
-- =============================================================================

CREATE TABLE IF NOT EXISTS drivers (
    id BIGSERIAL PRIMARY KEY,
    
    -- Personal Information
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(13) NOT NULL UNIQUE,
    email VARCHAR(100),
    national_id VARCHAR(11) NOT NULL UNIQUE,
    birth_date DATE,
    
    -- License Information
    license_number VARCHAR(20) NOT NULL UNIQUE,
    license_date TIMESTAMP NOT NULL,
    license_class VARCHAR(10),
    license_photo_url VARCHAR(500),
    
    -- Vehicle Information
    vehicle_plate VARCHAR(10) NOT NULL UNIQUE,
    vehicle_brand VARCHAR(30) NOT NULL,
    vehicle_model VARCHAR(30) NOT NULL,
    vehicle_year INTEGER NOT NULL CHECK (vehicle_year >= 2010),
    vehicle_color VARCHAR(20) NOT NULL,
    vehicle_photo_url VARCHAR(500),
    
    -- Location
    city VARCHAR(30) NOT NULL,
    district VARCHAR(30),
    current_latitude DECIMAL(10, 8),
    current_longitude DECIMAL(11, 8),
    last_location_update TIMESTAMP,
    
    -- Status
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'SUSPENDED', 'DEACTIVATED')),
    availability VARCHAR(20) DEFAULT 'OFFLINE' CHECK (availability IN ('ONLINE', 'BUSY', 'OFFLINE', 'BREAK')),
    
    -- Performance Metrics
    rating DECIMAL(3, 2) DEFAULT 5.00 CHECK (rating >= 0 AND rating <= 5),
    total_rides BIGINT DEFAULT 0,
    completed_rides BIGINT DEFAULT 0,
    cancelled_rides BIGINT DEFAULT 0,
    total_earnings DECIMAL(10, 2) DEFAULT 0,
    this_month_earnings DECIMAL(10, 2) DEFAULT 0,
    
    -- Verification
    is_verified BOOLEAN DEFAULT FALSE,
    verification_date TIMESTAMP,
    background_check_status VARCHAR(20) DEFAULT 'PENDING' CHECK (background_check_status IN ('PENDING', 'APPROVED', 'REJECTED', 'EXPIRED')),
    
    -- Photos
    profile_photo_url VARCHAR(500),
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_active TIMESTAMP
);

-- =============================================================================
-- RIDES SCHEMA - Yolculuk Yönetimi
-- =============================================================================

CREATE TABLE IF NOT EXISTS rides (
    id BIGSERIAL PRIMARY KEY,
    ride_uuid UUID DEFAULT uuid_generate_v4() UNIQUE,
    
    -- Participants
    user_id BIGINT NOT NULL REFERENCES users(id),
    driver_id BIGINT REFERENCES drivers(id),
    
    -- Location Information
    pickup_latitude DECIMAL(10, 8) NOT NULL,
    pickup_longitude DECIMAL(11, 8) NOT NULL,
    pickup_address TEXT NOT NULL,
    destination_latitude DECIMAL(10, 8) NOT NULL,
    destination_longitude DECIMAL(11, 8) NOT NULL,
    destination_address TEXT NOT NULL,
    
    -- Trip Details
    estimated_distance DECIMAL(8, 2), -- km
    estimated_duration INTEGER, -- minutes
    estimated_fare DECIMAL(8, 2), -- TL
    
    actual_distance DECIMAL(8, 2), -- km
    actual_duration INTEGER, -- minutes
    final_fare DECIMAL(8, 2), -- TL
    
    -- Status and Timing
    status VARCHAR(20) DEFAULT 'REQUESTED' CHECK (status IN ('REQUESTED', 'ACCEPTED', 'DRIVER_ARRIVING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    
    -- Payment
    payment_method VARCHAR(20) DEFAULT 'CASH' CHECK (payment_method IN ('CASH', 'CARD', 'WALLET')),
    payment_status VARCHAR(20) DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED')),
    
    -- Ratings and Feedback
    user_rating INTEGER CHECK (user_rating >= 1 AND user_rating <= 5),
    driver_rating INTEGER CHECK (driver_rating >= 1 AND driver_rating <= 5),
    user_feedback TEXT,
    driver_feedback TEXT,
    
    -- Special Requests
    special_requests TEXT,
    passenger_count INTEGER DEFAULT 1 CHECK (passenger_count >= 1 AND passenger_count <= 4),
    
    -- Cancellation
    cancelled_by VARCHAR(10) CHECK (cancelled_by IN ('USER', 'DRIVER', 'SYSTEM')),
    cancellation_reason TEXT,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- RIDE_TRACKING SCHEMA - Yolculuk Takibi
-- =============================================================================

CREATE TABLE IF NOT EXISTS ride_tracking (
    id BIGSERIAL PRIMARY KEY,
    ride_id BIGINT NOT NULL REFERENCES rides(id),
    
    -- Location Data
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    speed DECIMAL(5, 2), -- km/h
    heading INTEGER CHECK (heading >= 0 AND heading <= 360), -- degrees
    accuracy DECIMAL(5, 2), -- meters
    
    -- Timestamps
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Index for performance
    INDEX idx_ride_tracking_ride_time (ride_id, recorded_at)
);

-- =============================================================================
-- PAYMENTS SCHEMA - Ödeme Yönetimi
-- =============================================================================

CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    payment_uuid UUID DEFAULT uuid_generate_v4() UNIQUE,
    
    -- References
    ride_id BIGINT NOT NULL REFERENCES rides(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    
    -- Payment Details
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'TRY',
    payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('CASH', 'CARD', 'WALLET', 'BANK_TRANSFER')),
    
    -- Status
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED')),
    
    -- Payment Provider Details
    provider VARCHAR(50), -- Iyzico, PayTR, etc.
    provider_transaction_id VARCHAR(100),
    provider_response TEXT,
    
    -- Commission and Earnings
    platform_commission DECIMAL(10, 2),
    driver_earnings DECIMAL(10, 2),
    
    -- Timestamps
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- NOTIFICATIONS SCHEMA - Bildirim Yönetimi
-- =============================================================================

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    
    -- Target
    user_id BIGINT REFERENCES users(id),
    driver_id BIGINT REFERENCES drivers(id),
    
    -- Content
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL, -- RIDE_REQUEST, PAYMENT_SUCCESS, etc.
    
    -- Status
    is_read BOOLEAN DEFAULT FALSE,
    is_sent BOOLEAN DEFAULT FALSE,
    
    -- Delivery Methods
    push_notification BOOLEAN DEFAULT TRUE,
    sms BOOLEAN DEFAULT FALSE,
    email BOOLEAN DEFAULT FALSE,
    
    -- References
    ride_id BIGINT REFERENCES rides(id),
    payment_id BIGINT REFERENCES payments(id),
    
    -- Timestamps
    sent_at TIMESTAMP,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- CITIES AND PRICING SCHEMA - Şehir ve Fiyatlandırma
-- =============================================================================

CREATE TABLE IF NOT EXISTS cities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    country VARCHAR(50) DEFAULT 'Turkey',
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Pricing
    base_fare DECIMAL(8, 2) NOT NULL,
    per_km_rate DECIMAL(8, 2) NOT NULL,
    per_minute_rate DECIMAL(8, 2) NOT NULL,
    minimum_fare DECIMAL(8, 2) NOT NULL,
    
    -- Commission
    platform_commission_rate DECIMAL(5, 4) DEFAULT 0.15, -- 15%
    
    -- Coordinates for geofencing
    center_latitude DECIMAL(10, 8),
    center_longitude DECIMAL(11, 8),
    service_radius INTEGER, -- km
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- SUPPORT TICKETS SCHEMA - Destek Talepleri
-- =============================================================================

CREATE TABLE IF NOT EXISTS support_tickets (
    id BIGSERIAL PRIMARY KEY,
    ticket_uuid UUID DEFAULT uuid_generate_v4() UNIQUE,
    
    -- Requester
    user_id BIGINT REFERENCES users(id),
    driver_id BIGINT REFERENCES drivers(id),
    
    -- Ticket Details
    subject VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(50) NOT NULL, -- TECHNICAL, PAYMENT, DRIVER_ISSUE, etc.
    priority VARCHAR(20) DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),
    
    -- Status
    status VARCHAR(20) DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'IN_PROGRESS', 'RESOLVED', 'CLOSED')),
    
    -- Assignment
    assigned_to VARCHAR(100), -- Support agent
    
    -- References
    ride_id BIGINT REFERENCES rides(id),
    
    -- Resolution
    resolution TEXT,
    resolved_at TIMESTAMP,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- INDEXES FOR PERFORMANCE
-- =============================================================================

-- Users indexes
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- Drivers indexes
CREATE INDEX IF NOT EXISTS idx_drivers_phone ON drivers(phone);
CREATE INDEX IF NOT EXISTS idx_drivers_license ON drivers(license_number);
CREATE INDEX IF NOT EXISTS idx_drivers_plate ON drivers(vehicle_plate);
CREATE INDEX IF NOT EXISTS idx_drivers_status ON drivers(status);
CREATE INDEX IF NOT EXISTS idx_drivers_availability ON drivers(availability);
CREATE INDEX IF NOT EXISTS idx_drivers_city ON drivers(city);
CREATE INDEX IF NOT EXISTS idx_drivers_location ON drivers(current_latitude, current_longitude);

-- Rides indexes
CREATE INDEX IF NOT EXISTS idx_rides_user ON rides(user_id);
CREATE INDEX IF NOT EXISTS idx_rides_driver ON rides(driver_id);
CREATE INDEX IF NOT EXISTS idx_rides_status ON rides(status);
CREATE INDEX IF NOT EXISTS idx_rides_requested_at ON rides(requested_at);
CREATE INDEX IF NOT EXISTS idx_rides_pickup_location ON rides(pickup_latitude, pickup_longitude);

-- Payments indexes
CREATE INDEX IF NOT EXISTS idx_payments_ride ON payments(ride_id);
CREATE INDEX IF NOT EXISTS idx_payments_user ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_driver ON payments(driver_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);

-- Notifications indexes
CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_driver ON notifications(driver_id);
CREATE INDEX IF NOT EXISTS idx_notifications_unread ON notifications(is_read) WHERE is_read = FALSE;

-- =============================================================================
-- INITIAL DATA
-- =============================================================================

-- Insert major Turkish cities
INSERT INTO cities (name, base_fare, per_km_rate, per_minute_rate, minimum_fare, center_latitude, center_longitude, service_radius) VALUES
('Istanbul', 15.00, 3.50, 0.80, 25.00, 41.0082, 28.9784, 50),
('Ankara', 12.00, 3.00, 0.70, 20.00, 39.9334, 32.8597, 30),
('Izmir', 12.00, 3.00, 0.70, 20.00, 38.4192, 27.1287, 25),
('Bursa', 10.00, 2.80, 0.65, 18.00, 40.1826, 29.0669, 20),
('Antalya', 12.00, 3.20, 0.75, 22.00, 36.8969, 30.7133, 25),
('Adana', 10.00, 2.70, 0.60, 17.00, 37.0000, 35.3213, 20)
ON CONFLICT (name) DO NOTHING;

-- =============================================================================
-- TRIGGERS FOR UPDATED_AT
-- =============================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply to all tables with updated_at column
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_drivers_updated_at BEFORE UPDATE ON drivers FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_rides_updated_at BEFORE UPDATE ON rides FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_cities_updated_at BEFORE UPDATE ON cities FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_support_tickets_updated_at BEFORE UPDATE ON support_tickets FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- VIEWS FOR COMMON QUERIES
-- =============================================================================

-- Active drivers with current location
CREATE OR REPLACE VIEW active_drivers AS
SELECT 
    d.*,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - d.last_location_update))/60 as minutes_since_location_update
FROM drivers d
WHERE d.status = 'APPROVED' 
  AND d.availability = 'ONLINE'
  AND d.is_verified = TRUE
  AND d.last_location_update > CURRENT_TIMESTAMP - INTERVAL '10 minutes';

-- Ride statistics view
CREATE OR REPLACE VIEW ride_statistics AS
SELECT 
    DATE_TRUNC('day', created_at) as date,
    COUNT(*) as total_rides,
    COUNT(*) FILTER (WHERE status = 'COMPLETED') as completed_rides,
    COUNT(*) FILTER (WHERE status = 'CANCELLED') as cancelled_rides,
    AVG(final_fare) FILTER (WHERE status = 'COMPLETED') as avg_fare,
    SUM(final_fare) FILTER (WHERE status = 'COMPLETED') as total_revenue
FROM rides
GROUP BY DATE_TRUNC('day', created_at)
ORDER BY date DESC;

COMMIT;