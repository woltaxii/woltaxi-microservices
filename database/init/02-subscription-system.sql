-- WOLTAXI Subscription System Database Schema
-- Sürücü Paket Sistemi ve Müşteri Portföyü Yönetimi
-- Tarih: 24 Ekim 2025

-- =============================================================================
-- SUBSCRIPTION PACKAGES - Sürücü Paketleri
-- =============================================================================

CREATE TABLE IF NOT EXISTS subscription_packages (
    id BIGSERIAL PRIMARY KEY,
    package_name VARCHAR(50) NOT NULL UNIQUE, -- BASIC, PREMIUM, GOLD, DIAMOND
    package_code VARCHAR(20) NOT NULL UNIQUE,
    
    -- Package Details
    description TEXT NOT NULL,
    features TEXT[], -- Array of features
    
    -- Pricing
    monthly_price DECIMAL(8, 2) NOT NULL,
    yearly_price DECIMAL(8, 2) NOT NULL,
    yearly_discount_percent DECIMAL(5, 2) DEFAULT 15.00, -- %15 yıllık indirim
    
    -- Limits and Benefits
    max_daily_rides INTEGER DEFAULT -1, -- -1 = unlimited
    max_monthly_rides INTEGER DEFAULT -1,
    priority_level INTEGER DEFAULT 1, -- 1=Normal, 2=High, 3=VIP
    customer_portfolio_limit INTEGER DEFAULT 100,
    commission_rate DECIMAL(5, 4) DEFAULT 0.15, -- Platform komisyonu
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    is_popular BOOLEAN DEFAULT FALSE, -- Popüler paket işareti
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- DRIVER SUBSCRIPTIONS - Sürücü Abonelikleri
-- =============================================================================

CREATE TABLE IF NOT EXISTS driver_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    subscription_uuid UUID DEFAULT uuid_generate_v4() UNIQUE,
    
    -- References
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    package_id BIGINT NOT NULL REFERENCES subscription_packages(id),
    
    -- Subscription Details
    subscription_type VARCHAR(20) NOT NULL CHECK (subscription_type IN ('MONTHLY', 'YEARLY')),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    
    -- Payment
    amount_paid DECIMAL(8, 2) NOT NULL,
    payment_status VARCHAR(20) DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PAID', 'FAILED', 'CANCELLED', 'REFUNDED')),
    payment_method VARCHAR(20) CHECK (payment_method IN ('CARD', 'BANK_TRANSFER', 'MOBILE_PAYMENT')),
    
    -- Status
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED', 'SUSPENDED')),
    auto_renewal BOOLEAN DEFAULT TRUE,
    
    -- Usage Tracking
    rides_used_this_period INTEGER DEFAULT 0,
    last_ride_date TIMESTAMP,
    
    -- Customer Portfolio
    customer_portfolio_count INTEGER DEFAULT 0,
    portfolio_limit INTEGER NOT NULL,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP,
    
    -- Indexes
    INDEX idx_driver_subscription_driver (driver_id),
    INDEX idx_driver_subscription_status (status),
    INDEX idx_driver_subscription_dates (start_date, end_date)
);

-- =============================================================================
-- CUSTOMER PORTFOLIO - Müşteri Portföyü
-- =============================================================================

CREATE TABLE IF NOT EXISTS customer_portfolio (
    id BIGSERIAL PRIMARY KEY,
    portfolio_uuid UUID DEFAULT uuid_generate_v4() UNIQUE,
    
    -- References
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    customer_id BIGINT NOT NULL REFERENCES users(id),
    
    -- Relationship Details
    relationship_type VARCHAR(20) DEFAULT 'REGULAR' CHECK (relationship_type IN ('REGULAR', 'VIP', 'PREMIUM')),
    added_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Customer Preferences
    preferred_pickup_locations TEXT[], -- JSON array of locations
    preferred_times VARCHAR(100), -- "08:00-09:00,17:00-19:00"
    special_notes TEXT,
    
    -- Statistics
    total_rides INTEGER DEFAULT 0,
    total_revenue DECIMAL(10, 2) DEFAULT 0,
    last_ride_date TIMESTAMP,
    average_rating DECIMAL(3, 2) DEFAULT 5.00,
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    is_favorite BOOLEAN DEFAULT FALSE, -- Sürücünün favori müşterisi
    
    -- Communication
    last_contact_date TIMESTAMP,
    next_follow_up TIMESTAMP,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Unique constraint
    UNIQUE(driver_id, customer_id),
    
    -- Indexes
    INDEX idx_portfolio_driver (driver_id),
    INDEX idx_portfolio_customer (customer_id),
    INDEX idx_portfolio_active (is_active),
    INDEX idx_portfolio_favorite (is_favorite)
);

-- =============================================================================
-- SUBSCRIPTION PAYMENTS - Abonelik Ödemeleri
-- =============================================================================

CREATE TABLE IF NOT EXISTS subscription_payments (
    id BIGSERIAL PRIMARY KEY,
    payment_uuid UUID DEFAULT uuid_generate_v4() UNIQUE,
    
    -- References
    subscription_id BIGINT NOT NULL REFERENCES driver_subscriptions(id),
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    package_id BIGINT NOT NULL REFERENCES subscription_packages(id),
    
    -- Payment Details
    amount DECIMAL(8, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'TRY',
    payment_type VARCHAR(20) NOT NULL CHECK (payment_type IN ('INITIAL', 'RENEWAL', 'UPGRADE', 'DOWNGRADE')),
    
    -- Payment Method
    payment_method VARCHAR(20) NOT NULL CHECK (payment_method IN ('CARD', 'BANK_TRANSFER', 'MOBILE_PAYMENT', 'CASH')),
    payment_provider VARCHAR(50), -- Iyzico, PayTR, etc.
    provider_transaction_id VARCHAR(100),
    
    -- Status
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED', 'REFUNDED')),
    
    -- Billing Period
    billing_period_start TIMESTAMP NOT NULL,
    billing_period_end TIMESTAMP NOT NULL,
    
    -- Provider Response
    provider_response TEXT,
    failure_reason TEXT,
    
    -- Timestamps
    processed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_sub_payments_subscription (subscription_id),
    INDEX idx_sub_payments_driver (driver_id),
    INDEX idx_sub_payments_status (status)
);

-- =============================================================================
-- CUSTOMER INTERACTIONS - Müşteri Etkileşimleri
-- =============================================================================

CREATE TABLE IF NOT EXISTS customer_interactions (
    id BIGSERIAL PRIMARY KEY,
    interaction_uuid UUID DEFAULT uuid_generate_v4() UNIQUE,
    
    -- References
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    customer_id BIGINT NOT NULL REFERENCES users(id),
    portfolio_id BIGINT REFERENCES customer_portfolio(id),
    
    -- Interaction Details
    interaction_type VARCHAR(30) NOT NULL CHECK (interaction_type IN ('CALL', 'SMS', 'WHATSAPP', 'IN_APP_MESSAGE', 'RIDE_COMPLETED', 'FOLLOW_UP')),
    subject VARCHAR(200),
    content TEXT,
    
    -- Scheduling
    scheduled_at TIMESTAMP,
    completed_at TIMESTAMP,
    
    -- Status
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'CANCELLED', 'FAILED')),
    
    -- Results
    result VARCHAR(20) CHECK (result IN ('SUCCESSFUL', 'NO_ANSWER', 'BUSY', 'DECLINED', 'SCHEDULED_RIDE')),
    notes TEXT,
    
    -- Next Action
    next_action_type VARCHAR(30),
    next_action_date TIMESTAMP,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_interactions_driver (driver_id),
    INDEX idx_interactions_customer (customer_id),
    INDEX idx_interactions_status (status),
    INDEX idx_interactions_scheduled (scheduled_at)
);

-- =============================================================================
-- MARKETING CAMPAIGNS - Pazarlama Kampanyaları
-- =============================================================================

CREATE TABLE IF NOT EXISTS marketing_campaigns (
    id BIGSERIAL PRIMARY KEY,
    campaign_uuid UUID DEFAULT uuid_generate_v4() UNIQUE,
    
    -- Campaign Details
    name VARCHAR(100) NOT NULL,
    description TEXT,
    campaign_type VARCHAR(30) NOT NULL CHECK (campaign_type IN ('PROMOTION', 'LOYALTY', 'REFERRAL', 'SEASONAL', 'RETENTION')),
    
    -- Target Audience
    target_package_types VARCHAR(100)[], -- ['PREMIUM', 'GOLD']
    target_cities VARCHAR(50)[],
    min_subscription_months INTEGER DEFAULT 0,
    
    -- Campaign Content
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    discount_percent DECIMAL(5, 2),
    discount_amount DECIMAL(8, 2),
    
    -- Validity
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    max_usage_per_driver INTEGER DEFAULT 1,
    total_usage_limit INTEGER,
    
    -- Status
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED')),
    
    -- Statistics
    sent_count INTEGER DEFAULT 0,
    opened_count INTEGER DEFAULT 0,
    clicked_count INTEGER DEFAULT 0,
    converted_count INTEGER DEFAULT 0,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- INITIAL SUBSCRIPTION PACKAGES DATA
-- =============================================================================

INSERT INTO subscription_packages (
    package_name, package_code, description, features,
    monthly_price, yearly_price, yearly_discount_percent,
    max_daily_rides, max_monthly_rides, priority_level, 
    customer_portfolio_limit, commission_rate, is_popular
) VALUES 
(
    'BASIC', 'BSC',
    'Temel sürücü paketi - Günlük sınırlı yolculuk',
    ARRAY['Günde 20 yolculuk', 'Temel destek', '50 müşteri portföyü', 'Standart komisyon'],
    299.00, 2990.00, 15.00,
    20, 600, 1, 50, 0.18, FALSE
),
(
    'PREMIUM', 'PRM', 
    'Orta seviye sürücü paketi - Daha fazla yolculuk ve özellik',
    ARRAY['Günde 50 yolculuk', 'Öncelikli destek', '150 müşteri portföyü', 'İndirimli komisyon', 'Müşteri takip sistemi'],
    599.00, 5990.00, 15.00,
    50, 1500, 2, 150, 0.15, TRUE
),
(
    'GOLD', 'GLD',
    'İleri seviye sürücü paketi - Yüksek kazanç potansiyeli', 
    ARRAY['Günde 100 yolculuk', '7/24 destek', '300 müşteri portföyü', 'Düşük komisyon', 'CRM sistemi', 'Pazarlama araçları'],
    999.00, 9990.00, 15.00,
    100, 3000, 3, 300, 0.12, FALSE
),
(
    'DIAMOND', 'DMD',
    'VIP sürücü paketi - Sınırsız imkanlar',
    ARRAY['Sınırsız yolculuk', 'VIP destek', 'Sınırsız müşteri portföyü', 'Minimum komisyon', 'Gelişmiş CRM', 'Özel pazarlama', 'Kişisel hesap yöneticisi'],
    1999.00, 19990.00, 15.00,
    -1, -1, 4, -1, 0.10, FALSE
)
ON CONFLICT (package_name) DO NOTHING;

-- =============================================================================
-- VIEWS FOR SUBSCRIPTION ANALYTICS
-- =============================================================================

-- Active Subscriptions Summary
CREATE OR REPLACE VIEW active_subscriptions_summary AS
SELECT 
    sp.package_name,
    sp.package_code,
    COUNT(ds.id) as active_subscriptions,
    SUM(ds.amount_paid) as total_revenue,
    AVG(ds.amount_paid) as avg_revenue_per_subscription,
    COUNT(ds.id) FILTER (WHERE ds.subscription_type = 'MONTHLY') as monthly_subs,
    COUNT(ds.id) FILTER (WHERE ds.subscription_type = 'YEARLY') as yearly_subs
FROM subscription_packages sp
LEFT JOIN driver_subscriptions ds ON sp.id = ds.package_id AND ds.status = 'ACTIVE'
GROUP BY sp.id, sp.package_name, sp.package_code
ORDER BY sp.id;

-- Driver Portfolio Performance
CREATE OR REPLACE VIEW driver_portfolio_performance AS
SELECT 
    d.id as driver_id,
    CONCAT(d.first_name, ' ', d.last_name) as driver_name,
    d.city,
    sp.package_name,
    ds.status as subscription_status,
    COUNT(cp.id) as portfolio_size,
    SUM(cp.total_rides) as total_portfolio_rides,
    SUM(cp.total_revenue) as total_portfolio_revenue,
    AVG(cp.average_rating) as avg_customer_rating
FROM drivers d
LEFT JOIN driver_subscriptions ds ON d.id = ds.driver_id AND ds.status = 'ACTIVE'
LEFT JOIN subscription_packages sp ON ds.package_id = sp.id
LEFT JOIN customer_portfolio cp ON d.id = cp.driver_id AND cp.is_active = TRUE
GROUP BY d.id, d.first_name, d.last_name, d.city, sp.package_name, ds.status
ORDER BY total_portfolio_revenue DESC NULLS LAST;

-- Monthly Subscription Revenue
CREATE OR REPLACE VIEW monthly_subscription_revenue AS
SELECT 
    DATE_TRUNC('month', created_at) as month,
    sp.package_name,
    COUNT(*) as new_subscriptions,
    SUM(amount_paid) as revenue,
    COUNT(*) FILTER (WHERE subscription_type = 'YEARLY') as yearly_count,
    COUNT(*) FILTER (WHERE subscription_type = 'MONTHLY') as monthly_count
FROM subscription_payments spp
JOIN driver_subscriptions ds ON spp.subscription_id = ds.id
JOIN subscription_packages sp ON ds.package_id = sp.id
WHERE spp.status = 'COMPLETED'
GROUP BY DATE_TRUNC('month', spp.created_at), sp.package_name
ORDER BY month DESC, sp.package_name;

COMMIT;