-- WOLTAXI Emergency and Safety System Database Schema
-- Acil Durum ve Güvenlik Yönetim Sistemi
-- SOS, Panic Button, Emergency Services Integration
-- Tarih: 24 Ekim 2025

-- =============================================================================
-- EMERGENCY CONTACT TYPES - Acil Durum İletişim Türleri
-- =============================================================================

CREATE TABLE IF NOT EXISTS emergency_contact_types (
    id BIGSERIAL PRIMARY KEY,
    type_name VARCHAR(50) NOT NULL UNIQUE,
    type_description VARCHAR(200),
    priority_level INTEGER DEFAULT 1 CHECK (priority_level BETWEEN 1 AND 5), -- 1: Highest priority
    notification_delay_seconds INTEGER DEFAULT 0, -- Delay before notification
    auto_call_enabled BOOLEAN DEFAULT FALSE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_emergency_contact_types_priority (priority_level)
);

-- Insert default emergency contact types
INSERT INTO emergency_contact_types (type_name, type_description, priority_level, notification_delay_seconds, auto_call_enabled) VALUES
('POLICE', 'Local Police Department', 1, 0, TRUE),
('AMBULANCE', 'Emergency Medical Services', 1, 0, TRUE),
('FAMILY', 'Family Members', 2, 30, FALSE),
('FRIEND', 'Close Friends', 2, 30, FALSE),
('WORKPLACE', 'Workplace Emergency Contact', 3, 60, FALSE),
('PARTNER', 'Life Partner/Spouse', 1, 15, FALSE),
('SECURITY', 'Private Security Services', 2, 0, TRUE),
('WOLTAXI_SUPPORT', 'WOLTAXI 24/7 Support Center', 1, 0, TRUE)
ON CONFLICT (type_name) DO NOTHING;

-- =============================================================================
-- EMERGENCY CONTACTS - Acil Durum İletişim Kişileri
-- =============================================================================

CREATE TABLE IF NOT EXISTS emergency_contacts (
    id BIGSERIAL PRIMARY KEY,
    
    -- User/Driver Reference
    user_id BIGINT REFERENCES users(id),
    driver_id BIGINT REFERENCES drivers(id),
    
    -- Contact Details
    contact_type_id BIGINT NOT NULL REFERENCES emergency_contact_types(id),
    contact_name VARCHAR(200) NOT NULL,
    primary_phone VARCHAR(20) NOT NULL,
    secondary_phone VARCHAR(20),
    email VARCHAR(100),
    
    -- Relationship & Preferences
    relationship VARCHAR(100), -- 'Mother', 'Wife', 'Best Friend', etc.
    preferred_language VARCHAR(10) DEFAULT 'en',
    notification_preferences JSONB, -- SMS, Call, WhatsApp, etc.
    
    -- Location & Availability
    country_code VARCHAR(3),
    city VARCHAR(100),
    timezone VARCHAR(50),
    available_hours JSONB, -- When they can be reached
    
    -- Status & Verification
    is_verified BOOLEAN DEFAULT FALSE,
    verification_code VARCHAR(10),
    verification_attempts INTEGER DEFAULT 0,
    last_verification_at TIMESTAMP,
    
    -- Priority & Activation
    priority_order INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    emergency_keywords TEXT[], -- Keywords that trigger notification to this contact
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT check_user_or_driver CHECK ((user_id IS NOT NULL AND driver_id IS NULL) OR (user_id IS NULL AND driver_id IS NOT NULL)),
    INDEX idx_emergency_contacts_user (user_id),
    INDEX idx_emergency_contacts_driver (driver_id),
    INDEX idx_emergency_contacts_type (contact_type_id),
    INDEX idx_emergency_contacts_active (is_active, priority_order)
);

-- =============================================================================
-- EMERGENCY INCIDENT TYPES - Acil Durum Olay Türleri
-- =============================================================================

CREATE TABLE IF NOT EXISTS emergency_incident_types (
    id BIGSERIAL PRIMARY KEY,
    
    incident_code VARCHAR(20) NOT NULL UNIQUE,
    incident_name VARCHAR(100) NOT NULL,
    incident_description TEXT,
    
    -- Severity & Response
    severity_level INTEGER NOT NULL CHECK (severity_level BETWEEN 1 AND 5), -- 1: Critical, 5: Low
    response_time_seconds INTEGER DEFAULT 300, -- Expected response time
    requires_immediate_action BOOLEAN DEFAULT FALSE,
    
    -- Auto Response Configuration
    auto_call_police BOOLEAN DEFAULT FALSE,
    auto_call_ambulance BOOLEAN DEFAULT FALSE,
    auto_notify_family BOOLEAN DEFAULT TRUE,
    auto_share_location BOOLEAN DEFAULT TRUE,
    auto_record_audio BOOLEAN DEFAULT FALSE,
    auto_record_video BOOLEAN DEFAULT FALSE,
    
    -- Localization
    display_name_translations JSONB, -- Multi-language display names
    instructions_translations JSONB, -- Multi-language emergency instructions
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_emergency_incident_types_severity (severity_level)
);

-- Insert default emergency incident types
INSERT INTO emergency_incident_types (
    incident_code, incident_name, incident_description, severity_level, response_time_seconds,
    requires_immediate_action, auto_call_police, auto_call_ambulance, auto_notify_family,
    auto_share_location, auto_record_audio, auto_record_video,
    display_name_translations, instructions_translations
) VALUES
('PANIC', 'General Panic/Emergency', 'User pressed panic button without specific incident type', 1, 60, TRUE, TRUE, FALSE, TRUE, TRUE, TRUE, FALSE,
 '{"en": "Emergency!", "tr": "Acil Durum!", "es": "¡Emergencia!", "fr": "Urgence!", "de": "Notfall!"}',
 '{"en": "Stay calm. Help is on the way.", "tr": "Sakin kalın. Yardım yolda.", "es": "Mantén la calma. La ayuda está en camino."}'),

('MEDICAL', 'Medical Emergency', 'Medical emergency requiring immediate medical attention', 1, 30, TRUE, FALSE, TRUE, TRUE, TRUE, TRUE, FALSE,
 '{"en": "Medical Emergency", "tr": "Tıbbi Acil", "es": "Emergencia Médica", "fr": "Urgence Médicale"}',
 '{"en": "Medical help is being dispatched.", "tr": "Tıbbi yardım gönderiliyor.", "es": "Se está enviando ayuda médica."}'),

('ACCIDENT', 'Traffic Accident', 'Vehicle accident or collision', 2, 120, TRUE, TRUE, TRUE, TRUE, TRUE, FALSE, TRUE,
 '{"en": "Traffic Accident", "tr": "Trafik Kazası", "es": "Accidente de Tráfico", "fr": "Accident de la Route"}',
 '{"en": "Police and medical services notified.", "tr": "Polis ve tıbbi servisler bilgilendirildi."}'),

('HARASSMENT', 'Harassment/Threat', 'Harassment, threat, or inappropriate behavior', 1, 90, TRUE, TRUE, FALSE, TRUE, TRUE, TRUE, TRUE,
 '{"en": "Harassment/Threat", "tr": "Taciz/Tehdit", "es": "Acoso/Amenaza", "fr": "Harcèlement/Menace"}',
 '{"en": "Security has been alerted.", "tr": "Güvenlik uyarıldı.", "es": "Se ha alertado a la seguridad."}'),

('THEFT', 'Theft/Robbery', 'Theft, robbery, or criminal activity', 1, 60, TRUE, TRUE, FALSE, TRUE, TRUE, TRUE, TRUE,
 '{"en": "Theft/Robbery", "tr": "Hırsızlık/Soygun", "es": "Robo", "fr": "Vol"}',
 '{"en": "Police are being contacted.", "tr": "Polis ile iletişime geçiliyor.", "es": "Se está contactando a la policía."}'),

('BREAKDOWN', 'Vehicle Breakdown', 'Vehicle breakdown or mechanical failure', 4, 600, FALSE, FALSE, FALSE, TRUE, TRUE, FALSE, FALSE,
 '{"en": "Vehicle Breakdown", "tr": "Araç Arızası", "es": "Avería del Vehículo", "fr": "Panne de Véhicule"}',
 '{"en": "Roadside assistance is coming.", "tr": "Yol yardımı geliyor.", "es": "La asistencia en carretera está llegando."}'),

('LOST', 'Lost/Confused', 'Driver or passenger is lost or confused', 3, 300, FALSE, FALSE, FALSE, TRUE, TRUE, FALSE, FALSE,
 '{"en": "Lost/Confused", "tr": "Kaybolmuş/Şaşkın", "es": "Perdido/Confundido", "fr": "Perdu/Confus"}',
 '{"en": "Navigation assistance is available.", "tr": "Navigasyon yardımı mevcut.", "es": "La asistencia de navegación está disponible."}'),

('SUSPICIOUS', 'Suspicious Activity', 'Suspicion of unsafe situation or person', 2, 180, FALSE, FALSE, FALSE, TRUE, TRUE, TRUE, FALSE,
 '{"en": "Suspicious Activity", "tr": "Şüpheli Faaliyet", "es": "Actividad Sospechosa", "fr": "Activité Suspecte"}',
 '{"en": "Situation is being monitored.", "tr": "Durum izleniyor.", "es": "La situación está siendo monitoreada."}')

ON CONFLICT (incident_code) DO NOTHING;

-- =============================================================================
-- EMERGENCY INCIDENTS - Acil Durum Olayları
-- =============================================================================

CREATE TABLE IF NOT EXISTS emergency_incidents (
    id BIGSERIAL PRIMARY KEY,
    
    -- Unique Incident Identifier
    incident_uuid UUID DEFAULT uuid_generate_v4() UNIQUE,
    incident_number VARCHAR(20) NOT NULL UNIQUE, -- Human-readable: EMG-2025-001234
    
    -- References
    incident_type_id BIGINT NOT NULL REFERENCES emergency_incident_types(id),
    user_id BIGINT REFERENCES users(id),
    driver_id BIGINT REFERENCES drivers(id),
    ride_id BIGINT REFERENCES rides(id),
    
    -- Incident Details
    incident_title VARCHAR(200),
    incident_description TEXT,
    severity_assessment INTEGER CHECK (severity_assessment BETWEEN 1 AND 5),
    
    -- Location Information
    incident_latitude DECIMAL(10, 8),
    incident_longitude DECIMAL(11, 8),
    incident_address TEXT,
    incident_country_code VARCHAR(3),
    incident_city VARCHAR(100),
    nearest_landmark VARCHAR(200),
    location_accuracy_meters DECIMAL(8, 2),
    
    -- Timing
    reported_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    occurred_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    response_started_at TIMESTAMP,
    resolved_at TIMESTAMP,
    
    -- Reporter Information
    reported_by_user_id BIGINT REFERENCES users(id),
    reported_by_driver_id BIGINT REFERENCES drivers(id),
    reporting_method VARCHAR(20) DEFAULT 'APP', -- APP, CALL, SMS, WEBSITE
    reporter_phone VARCHAR(20),
    reporter_relationship VARCHAR(50), -- SELF, FAMILY, WITNESS, etc.
    
    -- Status & Progress
    status VARCHAR(20) DEFAULT 'REPORTED' CHECK (status IN (
        'REPORTED', 'ACKNOWLEDGED', 'DISPATCHED', 'IN_PROGRESS', 
        'RESOLVED', 'FALSE_ALARM', 'CANCELLED'
    )),
    resolution_notes TEXT,
    follow_up_required BOOLEAN DEFAULT FALSE,
    
    -- Response Actions Taken
    police_notified BOOLEAN DEFAULT FALSE,
    police_notification_time TIMESTAMP,
    ambulance_called BOOLEAN DEFAULT FALSE,
    ambulance_call_time TIMESTAMP,
    family_notified BOOLEAN DEFAULT FALSE,
    family_notification_time TIMESTAMP,
    security_dispatched BOOLEAN DEFAULT FALSE,
    security_dispatch_time TIMESTAMP,
    
    -- Media & Evidence
    audio_recording_url VARCHAR(500),
    video_recording_url VARCHAR(500),
    photo_urls TEXT[], -- Array of photo URLs
    voice_message_url VARCHAR(500),
    
    -- Additional Data
    weather_conditions VARCHAR(100),
    traffic_conditions VARCHAR(100),
    witnesses_count INTEGER DEFAULT 0,
    additional_metadata JSONB,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT check_user_or_driver_incident CHECK ((user_id IS NOT NULL OR driver_id IS NOT NULL)),
    INDEX idx_emergency_incidents_status (status),
    INDEX idx_emergency_incidents_severity (severity_assessment),
    INDEX idx_emergency_incidents_location (incident_latitude, incident_longitude),
    INDEX idx_emergency_incidents_time (reported_at DESC),
    INDEX idx_emergency_incidents_ride (ride_id),
    INDEX idx_emergency_incidents_type (incident_type_id)
);

-- =============================================================================
-- EMERGENCY RESPONSES - Acil Durum Müdahaleleri
-- =============================================================================

CREATE TABLE IF NOT EXISTS emergency_responses (
    id BIGSERIAL PRIMARY KEY,
    
    incident_id BIGINT NOT NULL REFERENCES emergency_incidents(id),
    response_uuid UUID DEFAULT uuid_generate_v4() UNIQUE,
    
    -- Response Details
    response_type VARCHAR(30) NOT NULL, -- POLICE, AMBULANCE, SECURITY, FAMILY_CONTACT, etc.
    responder_name VARCHAR(200),
    responder_phone VARCHAR(20),
    responder_organization VARCHAR(200),
    
    -- Response Status
    response_status VARCHAR(20) DEFAULT 'PENDING' CHECK (response_status IN (
        'PENDING', 'DISPATCHED', 'EN_ROUTE', 'ON_SCENE', 'COMPLETED', 'CANCELLED'
    )),
    
    -- Timing
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    dispatched_at TIMESTAMP,
    arrived_at TIMESTAMP,
    completed_at TIMESTAMP,
    estimated_arrival_time TIMESTAMP,
    
    -- Location
    responder_latitude DECIMAL(10, 8),
    responder_longitude DECIMAL(11, 8),
    distance_to_incident_km DECIMAL(8, 2),
    
    -- Communication
    contact_attempts INTEGER DEFAULT 0,
    last_contact_at TIMESTAMP,
    communication_log JSONB,
    
    -- Outcome
    response_notes TEXT,
    outcome_status VARCHAR(50),
    effectiveness_rating INTEGER CHECK (effectiveness_rating BETWEEN 1 AND 5),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_emergency_responses_incident (incident_id),
    INDEX idx_emergency_responses_status (response_status),
    INDEX idx_emergency_responses_type (response_type),
    INDEX idx_emergency_responses_time (requested_at DESC)
);

-- =============================================================================
-- EMERGENCY LOCATION TRACKING - Acil Durum Konum Takibi
-- =============================================================================

CREATE TABLE IF NOT EXISTS emergency_location_tracking (
    id BIGSERIAL PRIMARY KEY,
    
    incident_id BIGINT NOT NULL REFERENCES emergency_incidents(id),
    tracking_uuid UUID DEFAULT uuid_generate_v4(),
    
    -- Location Data
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    altitude DECIMAL(8, 2),
    accuracy_meters DECIMAL(8, 2),
    speed_kmh DECIMAL(6, 2),
    heading_degrees DECIMAL(5, 2),
    
    -- Source Information
    location_source VARCHAR(20) DEFAULT 'GPS', -- GPS, NETWORK, PASSIVE, MANUAL
    device_type VARCHAR(30), -- SMARTPHONE, TABLET, VEHICLE_TRACKER
    battery_level INTEGER,
    
    -- Sharing & Privacy
    shared_with TEXT[], -- Array of contact types: ['FAMILY', 'POLICE', 'SECURITY']
    sharing_duration_minutes INTEGER DEFAULT 60,
    sharing_started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sharing_expires_at TIMESTAMP,
    
    -- Real-time Updates
    is_real_time BOOLEAN DEFAULT TRUE,
    update_interval_seconds INTEGER DEFAULT 30,
    last_update_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Metadata
    cell_tower_info JSONB,
    wifi_networks JSONB,
    nearby_bluetooth_devices JSONB,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_emergency_location_incident (incident_id),
    INDEX idx_emergency_location_time (created_at DESC),
    INDEX idx_emergency_location_coords (latitude, longitude),
    INDEX idx_emergency_location_sharing (sharing_expires_at)
);

-- =============================================================================
-- EMERGENCY COMMUNICATION LOG - Acil Durum İletişim Kayıtları
-- =============================================================================

CREATE TABLE IF NOT EXISTS emergency_communication_log (
    id BIGSERIAL PRIMARY KEY,
    
    incident_id BIGINT NOT NULL REFERENCES emergency_incidents(id),
    communication_uuid UUID DEFAULT uuid_generate_v4(),
    
    -- Communication Details
    communication_type VARCHAR(20) NOT NULL, -- SMS, CALL, PUSH, EMAIL, WHATSAPP
    direction VARCHAR(10) NOT NULL CHECK (direction IN ('OUTBOUND', 'INBOUND')),
    
    -- Participants
    sender_type VARCHAR(20), -- USER, DRIVER, SYSTEM, OPERATOR, EMERGENCY_SERVICE
    sender_id BIGINT,
    recipient_type VARCHAR(20),
    recipient_id BIGINT,
    recipient_phone VARCHAR(20),
    recipient_email VARCHAR(100),
    
    -- Message Content
    subject VARCHAR(200),
    message_content TEXT,
    message_language VARCHAR(10) DEFAULT 'en',
    
    -- Delivery Status
    delivery_status VARCHAR(20) DEFAULT 'PENDING' CHECK (delivery_status IN (
        'PENDING', 'SENT', 'DELIVERED', 'READ', 'FAILED', 'BOUNCED'
    )),
    delivery_attempts INTEGER DEFAULT 0,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP,
    
    -- Metadata
    provider_message_id VARCHAR(100),
    provider_status VARCHAR(50),
    cost_amount DECIMAL(8, 4),
    cost_currency VARCHAR(3) DEFAULT 'USD',
    
    -- Media Attachments
    attachment_urls TEXT[],
    attachment_types VARCHAR(20)[], -- PHOTO, VIDEO, AUDIO, DOCUMENT
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_emergency_comm_incident (incident_id),
    INDEX idx_emergency_comm_type (communication_type),
    INDEX idx_emergency_comm_status (delivery_status),
    INDEX idx_emergency_comm_time (created_at DESC)
);

-- =============================================================================
-- EMERGENCY SETTINGS - Kullanıcı Acil Durum Ayarları
-- =============================================================================

CREATE TABLE IF NOT EXISTS emergency_settings (
    id BIGSERIAL PRIMARY KEY,
    
    -- User/Driver Reference
    user_id BIGINT REFERENCES users(id),
    driver_id BIGINT REFERENCES drivers(id),
    
    -- General Emergency Settings
    emergency_enabled BOOLEAN DEFAULT TRUE,
    panic_button_enabled BOOLEAN DEFAULT TRUE,
    auto_share_location BOOLEAN DEFAULT TRUE,
    auto_record_enabled BOOLEAN DEFAULT FALSE,
    
    -- Notification Preferences
    sms_notifications BOOLEAN DEFAULT TRUE,
    call_notifications BOOLEAN DEFAULT TRUE,
    push_notifications BOOLEAN DEFAULT TRUE,
    email_notifications BOOLEAN DEFAULT FALSE,
    whatsapp_notifications BOOLEAN DEFAULT FALSE,
    
    -- Safety Features
    women_safety_mode BOOLEAN DEFAULT FALSE,
    family_safety_mode BOOLEAN DEFAULT FALSE,
    tourist_safety_mode BOOLEAN DEFAULT FALSE,
    
    -- Auto-Response Configuration
    auto_call_delay_seconds INTEGER DEFAULT 60, -- Delay before auto-calling authorities
    auto_share_duration_minutes INTEGER DEFAULT 60,
    fake_call_enabled BOOLEAN DEFAULT TRUE, -- Fake call feature for escaping situations
    
    -- Privacy & Sharing
    share_with_family_default BOOLEAN DEFAULT TRUE,
    share_with_friends_default BOOLEAN DEFAULT FALSE,
    share_location_precision VARCHAR(10) DEFAULT 'HIGH', -- HIGH, MEDIUM, LOW
    
    -- Language & Localization
    emergency_language VARCHAR(10) DEFAULT 'en',
    local_emergency_numbers JSONB, -- Custom emergency numbers by country
    
    -- Device Settings
    shake_to_activate BOOLEAN DEFAULT TRUE,
    volume_key_emergency BOOLEAN DEFAULT FALSE,
    double_tap_emergency BOOLEAN DEFAULT FALSE,
    voice_activation_enabled BOOLEAN DEFAULT FALSE,
    voice_activation_phrase VARCHAR(50) DEFAULT 'WOLTAXI EMERGENCY',
    
    -- Constraints
    CONSTRAINT check_user_or_driver_settings CHECK ((user_id IS NOT NULL AND driver_id IS NULL) OR (user_id IS NULL AND driver_id IS NOT NULL)),
    UNIQUE(user_id),
    UNIQUE(driver_id),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_emergency_settings_user (user_id),
    INDEX idx_emergency_settings_driver (driver_id)
);

-- =============================================================================
-- TRIGGERS FOR AUTOMATIC UPDATES
-- =============================================================================

-- Auto-update incident number
CREATE OR REPLACE FUNCTION generate_incident_number()
RETURNS TRIGGER AS $$
BEGIN
    NEW.incident_number := 'EMG-' || EXTRACT(YEAR FROM CURRENT_TIMESTAMP) || '-' || 
                          LPAD(NEXTVAL('emergency_incidents_id_seq')::text, 6, '0');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_generate_incident_number
    BEFORE INSERT ON emergency_incidents
    FOR EACH ROW
    EXECUTE FUNCTION generate_incident_number();

-- Auto-update timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_emergency_contacts_updated_at
    BEFORE UPDATE ON emergency_contacts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_emergency_incidents_updated_at
    BEFORE UPDATE ON emergency_incidents
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_emergency_responses_updated_at
    BEFORE UPDATE ON emergency_responses
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- VIEWS FOR ANALYTICS AND REPORTING
-- =============================================================================

-- Active Incidents View
CREATE OR REPLACE VIEW active_emergency_incidents AS
SELECT 
    ei.id,
    ei.incident_uuid,
    ei.incident_number,
    eit.incident_name,
    ei.severity_assessment,
    ei.status,
    ei.incident_latitude,
    ei.incident_longitude,
    ei.incident_address,
    ei.reported_at,
    ei.user_id,
    ei.driver_id,
    ei.ride_id,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - ei.reported_at))/60 as minutes_since_reported
FROM emergency_incidents ei
JOIN emergency_incident_types eit ON ei.incident_type_id = eit.id
WHERE ei.status IN ('REPORTED', 'ACKNOWLEDGED', 'DISPATCHED', 'IN_PROGRESS')
ORDER BY ei.severity_assessment ASC, ei.reported_at ASC;

-- Emergency Response Performance View
CREATE OR REPLACE VIEW emergency_response_performance AS
SELECT 
    eit.incident_name,
    COUNT(*) as total_incidents,
    AVG(EXTRACT(EPOCH FROM (er.dispatched_at - ei.reported_at))/60) as avg_dispatch_time_minutes,
    AVG(EXTRACT(EPOCH FROM (er.arrived_at - er.dispatched_at))/60) as avg_arrival_time_minutes,
    COUNT(CASE WHEN ei.status = 'RESOLVED' THEN 1 END) as resolved_count,
    ROUND(COUNT(CASE WHEN ei.status = 'RESOLVED' THEN 1 END) * 100.0 / COUNT(*), 2) as resolution_rate
FROM emergency_incidents ei
JOIN emergency_incident_types eit ON ei.incident_type_id = eit.id
LEFT JOIN emergency_responses er ON ei.id = er.incident_id
WHERE ei.reported_at >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY eit.incident_name
ORDER BY total_incidents DESC;

COMMIT;