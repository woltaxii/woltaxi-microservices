-- WOLTAXI Driver Analytics & Performance System Database Schema
-- Sürücü Performans ve Kar-Zarar Analiz Sistemi
-- Aylık ve Yıllık Performans Takibi
-- Tarih: 24 Ekim 2025

-- =============================================================================
-- DRIVER PERFORMANCE TRACKING - Sürücü Performans Takibi
-- =============================================================================

CREATE TABLE IF NOT EXISTS driver_daily_performance (
    id BIGSERIAL PRIMARY KEY,
    
    -- Driver Info
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    performance_date DATE NOT NULL,
    
    -- Working Hours
    work_start_time TIMESTAMP,
    work_end_time TIMESTAMP,
    total_work_hours DECIMAL(4, 2) DEFAULT 0, -- 8.50 hours
    active_driving_hours DECIMAL(4, 2) DEFAULT 0, -- Time actually driving
    idle_hours DECIMAL(4, 2) DEFAULT 0, -- Waiting time
    break_hours DECIMAL(4, 2) DEFAULT 0, -- Official breaks
    
    -- Trip Statistics
    total_trips INTEGER DEFAULT 0,
    completed_trips INTEGER DEFAULT 0,
    cancelled_trips INTEGER DEFAULT 0,
    customer_cancelled INTEGER DEFAULT 0,
    driver_cancelled INTEGER DEFAULT 0,
    
    -- Distance & Time
    total_distance_km DECIMAL(8, 2) DEFAULT 0, -- Total kilometers driven
    passenger_distance_km DECIMAL(8, 2) DEFAULT 0, -- With passengers
    empty_distance_km DECIMAL(8, 2) DEFAULT 0, -- Without passengers
    total_trip_time_minutes INTEGER DEFAULT 0, -- Total time in trips
    average_trip_time_minutes DECIMAL(5, 2) DEFAULT 0,
    
    -- Financial Performance
    gross_earnings DECIMAL(10, 2) DEFAULT 0, -- Total earnings before costs
    base_fare_earnings DECIMAL(10, 2) DEFAULT 0, -- From base fares
    distance_earnings DECIMAL(10, 2) DEFAULT 0, -- Per km earnings
    time_earnings DECIMAL(10, 2) DEFAULT 0, -- Per minute earnings
    surge_earnings DECIMAL(10, 2) DEFAULT 0, -- Peak hour bonuses
    tips_received DECIMAL(10, 2) DEFAULT 0, -- Customer tips
    bonuses_earned DECIMAL(10, 2) DEFAULT 0, -- Platform bonuses
    
    -- Costs & Expenses
    fuel_cost DECIMAL(8, 2) DEFAULT 0,
    vehicle_maintenance_cost DECIMAL(8, 2) DEFAULT 0,
    platform_commission DECIMAL(8, 2) DEFAULT 0, -- WOLTAXI commission
    subscription_fee DECIMAL(6, 2) DEFAULT 0, -- Daily subscription cost
    insurance_cost DECIMAL(6, 2) DEFAULT 0,
    parking_fees DECIMAL(6, 2) DEFAULT 0,
    toll_fees DECIMAL(6, 2) DEFAULT 0,
    other_expenses DECIMAL(6, 2) DEFAULT 0,
    
    -- Calculated Metrics
    total_expenses DECIMAL(10, 2) DEFAULT 0,
    net_profit DECIMAL(10, 2) DEFAULT 0, -- Gross - Expenses
    profit_margin DECIMAL(5, 2) DEFAULT 0, -- Net profit / Gross earnings * 100
    earnings_per_hour DECIMAL(8, 2) DEFAULT 0, -- Net profit / work hours
    earnings_per_km DECIMAL(6, 4) DEFAULT 0, -- Net profit / total km
    earnings_per_trip DECIMAL(8, 2) DEFAULT 0, -- Net profit / completed trips
    
    -- Quality Metrics
    customer_rating_average DECIMAL(3, 2) DEFAULT 0, -- 4.85 out of 5
    total_ratings_received INTEGER DEFAULT 0,
    five_star_ratings INTEGER DEFAULT 0,
    four_star_ratings INTEGER DEFAULT 0,
    three_star_ratings INTEGER DEFAULT 0,
    two_star_ratings INTEGER DEFAULT 0,
    one_star_ratings INTEGER DEFAULT 0,
    
    -- Efficiency Metrics
    acceptance_rate DECIMAL(5, 2) DEFAULT 0, -- Accepted / Total requests * 100
    cancellation_rate DECIMAL(5, 2) DEFAULT 0, -- Cancelled / Total trips * 100
    completion_rate DECIMAL(5, 2) DEFAULT 0, -- Completed / Started trips * 100
    utilization_rate DECIMAL(5, 2) DEFAULT 0, -- Passenger time / Total time * 100
    
    -- Location & Area Performance
    primary_work_area VARCHAR(100), -- Beşiktaş, Kadıköy, etc.
    areas_covered TEXT[], -- Array of areas worked in
    peak_hour_earnings DECIMAL(8, 2) DEFAULT 0, -- Earnings during peak hours
    off_peak_earnings DECIMAL(8, 2) DEFAULT 0, -- Earnings during off-peak
    
    -- Weather & External Factors
    weather_condition VARCHAR(50), -- sunny, rainy, snowy
    temperature_celsius INTEGER,
    traffic_level VARCHAR(20) DEFAULT 'NORMAL', -- LOW, NORMAL, HIGH, EXTREME
    special_events TEXT, -- "Football match at Vodafone Arena"
    
    -- Performance Goals & Targets
    daily_earnings_target DECIMAL(8, 2),
    daily_trips_target INTEGER,
    target_achievement_rate DECIMAL(5, 2) DEFAULT 0, -- Achieved / Target * 100
    
    -- Status & Notes
    performance_status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (performance_status IN ('ACTIVE', 'SICK_LEAVE', 'VACATION', 'SUSPENDED', 'MAINTENANCE')),
    notes TEXT,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes and Constraints
    UNIQUE(driver_id, performance_date),
    INDEX idx_driver_performance_date (performance_date),
    INDEX idx_driver_performance_driver (driver_id),
    INDEX idx_driver_performance_profit (net_profit DESC),
    INDEX idx_driver_performance_rating (customer_rating_average DESC)
);

-- =============================================================================
-- MONTHLY PERFORMANCE AGGREGATION - Aylık Performans Özeti
-- =============================================================================

CREATE TABLE IF NOT EXISTS driver_monthly_summary (
    id BIGSERIAL PRIMARY KEY,
    
    -- Driver & Period
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    year INTEGER NOT NULL,
    month INTEGER NOT NULL CHECK (month BETWEEN 1 AND 12),
    
    -- Working Statistics
    total_working_days INTEGER DEFAULT 0,
    total_work_hours DECIMAL(6, 2) DEFAULT 0,
    average_hours_per_day DECIMAL(4, 2) DEFAULT 0,
    
    -- Trip Summary
    total_trips INTEGER DEFAULT 0,
    completed_trips INTEGER DEFAULT 0,
    cancelled_trips INTEGER DEFAULT 0,
    average_trips_per_day DECIMAL(5, 2) DEFAULT 0,
    
    -- Distance Summary
    total_distance_km DECIMAL(10, 2) DEFAULT 0,
    passenger_distance_km DECIMAL(10, 2) DEFAULT 0,
    average_km_per_day DECIMAL(6, 2) DEFAULT 0,
    
    -- Financial Summary
    gross_earnings DECIMAL(12, 2) DEFAULT 0,
    total_expenses DECIMAL(10, 2) DEFAULT 0,
    net_profit DECIMAL(12, 2) DEFAULT 0,
    average_daily_profit DECIMAL(8, 2) DEFAULT 0,
    best_day_earnings DECIMAL(8, 2) DEFAULT 0,
    worst_day_earnings DECIMAL(8, 2) DEFAULT 0,
    
    -- Subscription & Commission
    subscription_package_id BIGINT REFERENCES subscription_packages(id),
    subscription_fee_paid DECIMAL(8, 2) DEFAULT 0,
    commission_paid DECIMAL(10, 2) DEFAULT 0,
    commission_rate DECIMAL(5, 2) DEFAULT 0, -- Average commission rate
    
    -- Performance Metrics
    average_rating DECIMAL(3, 2) DEFAULT 0,
    total_ratings_received INTEGER DEFAULT 0,
    acceptance_rate DECIMAL(5, 2) DEFAULT 0,
    cancellation_rate DECIMAL(5, 2) DEFAULT 0,
    completion_rate DECIMAL(5, 2) DEFAULT 0,
    
    -- Fuel & Vehicle Costs
    total_fuel_cost DECIMAL(8, 2) DEFAULT 0,
    fuel_efficiency_km_per_liter DECIMAL(5, 2) DEFAULT 0,
    maintenance_cost DECIMAL(8, 2) DEFAULT 0,
    
    -- Peak Performance Analysis
    peak_hour_earnings DECIMAL(10, 2) DEFAULT 0,
    peak_hour_percentage DECIMAL(5, 2) DEFAULT 0, -- % of total earnings from peak
    most_profitable_day_of_week VARCHAR(10), -- MONDAY, TUESDAY, etc.
    most_profitable_hour INTEGER, -- 0-23
    
    -- Goal Achievement
    monthly_earnings_target DECIMAL(10, 2),
    target_achievement_rate DECIMAL(5, 2) DEFAULT 0,
    ranking_in_subscription_tier INTEGER, -- Ranking among same subscription tier
    total_drivers_in_tier INTEGER,
    
    -- Growth Metrics
    earnings_growth_rate DECIMAL(5, 2) DEFAULT 0, -- vs previous month
    trips_growth_rate DECIMAL(5, 2) DEFAULT 0,
    rating_improvement DECIMAL(4, 3) DEFAULT 0,
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    UNIQUE(driver_id, year, month),
    INDEX idx_monthly_summary_date (year, month),
    INDEX idx_monthly_summary_profit (net_profit DESC),
    INDEX idx_monthly_summary_driver (driver_id)
);

-- =============================================================================
-- YEARLY PERFORMANCE AGGREGATION - Yıllık Performans Özeti
-- =============================================================================

CREATE TABLE IF NOT EXISTS driver_yearly_summary (
    id BIGSERIAL PRIMARY KEY,
    
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    year INTEGER NOT NULL,
    
    -- Annual Working Statistics
    total_working_days INTEGER DEFAULT 0,
    total_work_hours DECIMAL(8, 2) DEFAULT 0,
    average_hours_per_day DECIMAL(4, 2) DEFAULT 0,
    
    -- Annual Trip Summary
    total_trips INTEGER DEFAULT 0,
    completed_trips INTEGER DEFAULT 0,
    total_distance_km DECIMAL(12, 2) DEFAULT 0,
    
    -- Annual Financial Summary
    gross_earnings DECIMAL(15, 2) DEFAULT 0,
    total_expenses DECIMAL(12, 2) DEFAULT 0,
    net_profit DECIMAL(15, 2) DEFAULT 0,
    
    -- Tax & Legal
    estimated_tax_liability DECIMAL(10, 2) DEFAULT 0,
    social_security_payments DECIMAL(8, 2) DEFAULT 0,
    insurance_payments DECIMAL(8, 2) DEFAULT 0,
    
    -- Performance Achievements
    best_month_earnings DECIMAL(10, 2) DEFAULT 0,
    best_month INTEGER, -- 1-12
    worst_month_earnings DECIMAL(10, 2) DEFAULT 0,
    worst_month INTEGER,
    
    -- Annual Ratings & Quality
    average_rating DECIMAL(3, 2) DEFAULT 0,
    total_ratings_received INTEGER DEFAULT 0,
    customer_compliments INTEGER DEFAULT 0,
    customer_complaints INTEGER DEFAULT 0,
    
    -- Vehicle & Maintenance
    total_fuel_cost DECIMAL(10, 2) DEFAULT 0,
    total_maintenance_cost DECIMAL(10, 2) DEFAULT 0,
    vehicle_depreciation DECIMAL(8, 2) DEFAULT 0,
    
    -- Subscription History
    subscription_changes INTEGER DEFAULT 0, -- How many times changed subscription
    months_premium_subscription INTEGER DEFAULT 0,
    months_basic_subscription INTEGER DEFAULT 0,
    
    -- Growth & Development
    year_over_year_growth DECIMAL(5, 2) DEFAULT 0, -- vs previous year
    earnings_milestone_achieved VARCHAR(50), -- "First 100K TL year"
    achievements TEXT[], -- Array of achievements
    
    -- Rankings & Recognition
    city_ranking INTEGER, -- Ranking in the city
    total_drivers_in_city INTEGER,
    subscription_tier_ranking INTEGER,
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    UNIQUE(driver_id, year),
    INDEX idx_yearly_summary_year (year),
    INDEX idx_yearly_summary_profit (net_profit DESC)
);

-- =============================================================================
-- DRIVER FINANCIAL GOALS - Sürücü Mali Hedefleri
-- =============================================================================

CREATE TABLE IF NOT EXISTS driver_financial_goals (
    id BIGSERIAL PRIMARY KEY,
    
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    
    -- Goal Period
    goal_type VARCHAR(20) NOT NULL CHECK (goal_type IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')),
    target_period_start DATE NOT NULL,
    target_period_end DATE NOT NULL,
    
    -- Financial Targets
    target_gross_earnings DECIMAL(10, 2),
    target_net_profit DECIMAL(10, 2),
    target_trips_count INTEGER,
    target_work_hours INTEGER,
    target_rating DECIMAL(3, 2),
    
    -- Progress Tracking
    current_gross_earnings DECIMAL(10, 2) DEFAULT 0,
    current_net_profit DECIMAL(10, 2) DEFAULT 0,
    current_trips_count INTEGER DEFAULT 0,
    current_work_hours INTEGER DEFAULT 0,
    current_rating DECIMAL(3, 2) DEFAULT 0,
    
    -- Achievement Status
    is_achieved BOOLEAN DEFAULT FALSE,
    achievement_date DATE,
    achievement_percentage DECIMAL(5, 2) DEFAULT 0,
    
    -- Rewards & Incentives
    reward_amount DECIMAL(8, 2), -- Bonus for achieving goal
    reward_type VARCHAR(50), -- "CASH_BONUS", "SUBSCRIPTION_DISCOUNT", "BADGE"
    is_reward_claimed BOOLEAN DEFAULT FALSE,
    
    -- Status
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'EXPIRED', 'CANCELLED')),
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_financial_goals_driver (driver_id),
    INDEX idx_financial_goals_period (target_period_start, target_period_end),
    INDEX idx_financial_goals_type (goal_type)
);

-- =============================================================================
-- DRIVER EXPENSE CATEGORIES - Sürücü Gider Kategorileri
-- =============================================================================

CREATE TABLE IF NOT EXISTS driver_expense_categories (
    id BIGSERIAL PRIMARY KEY,
    
    category_name VARCHAR(50) NOT NULL UNIQUE,
    category_description TEXT,
    is_tax_deductible BOOLEAN DEFAULT FALSE,
    category_icon VARCHAR(50), -- For mobile app
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default expense categories
INSERT INTO driver_expense_categories (category_name, category_description, is_tax_deductible, category_icon, display_order) VALUES
('FUEL', 'Yakıt giderleri', TRUE, 'fuel-pump', 1),
('MAINTENANCE', 'Araç bakım ve onarım', TRUE, 'wrench', 2),
('INSURANCE', 'Araç sigortası', TRUE, 'shield', 3),
('PARKING', 'Park ücretleri', TRUE, 'parking', 4),
('TOLLS', 'Köprü ve otoyol ücretleri', TRUE, 'road', 5),
('CAR_WASH', 'Araç yıkama', TRUE, 'car-wash', 6),
('PHONE_BILL', 'Telefon faturası (iş için)', TRUE, 'phone', 7),
('FOOD_DRINK', 'Yemek ve içecek', FALSE, 'restaurant', 8),
('SUBSCRIPTION', 'WOLTAXI abonelik ücreti', TRUE, 'subscription', 9),
('OTHER', 'Diğer giderler', FALSE, 'receipt', 10)
ON CONFLICT (category_name) DO NOTHING;

-- =============================================================================
-- DRIVER DETAILED EXPENSES - Detaylı Gider Takibi
-- =============================================================================

CREATE TABLE IF NOT EXISTS driver_expenses (
    id BIGSERIAL PRIMARY KEY,
    
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    category_id BIGINT NOT NULL REFERENCES driver_expense_categories(id),
    
    -- Expense Details
    expense_date DATE NOT NULL,
    amount DECIMAL(8, 2) NOT NULL CHECK (amount >= 0),
    currency_code VARCHAR(3) DEFAULT 'TRY',
    
    -- Description & Receipt
    description VARCHAR(500),
    receipt_url VARCHAR(500), -- Photo/scan of receipt
    merchant_name VARCHAR(200),
    location VARCHAR(200),
    
    -- Vehicle Information
    odometer_reading INTEGER, -- Km reading when expense occurred
    
    -- Tax & Accounting
    is_tax_deductible BOOLEAN DEFAULT FALSE,
    tax_amount DECIMAL(6, 2) DEFAULT 0,
    invoice_number VARCHAR(100),
    
    -- Approval & Status
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'REIMBURSED')),
    approved_by BIGINT, -- Admin user ID
    approval_date TIMESTAMP,
    rejection_reason TEXT,
    
    -- Reimbursement (if applicable)
    is_reimbursable BOOLEAN DEFAULT FALSE,
    reimbursement_amount DECIMAL(8, 2) DEFAULT 0,
    reimbursement_date DATE,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_expenses_driver_date (driver_id, expense_date),
    INDEX idx_expenses_category (category_id),
    INDEX idx_expenses_status (status)
);

-- =============================================================================
-- PERFORMANCE ANALYTICS VIEWS - Performans Analiz Görünümleri
-- =============================================================================

-- Top Performing Drivers This Month
CREATE OR REPLACE VIEW top_drivers_this_month AS
SELECT 
    d.id,
    d.first_name,
    d.last_name,
    d.phone_number,
    dms.net_profit,
    dms.total_trips,
    dms.average_rating,
    dms.completion_rate,
    sp.package_name as subscription_package,
    ROW_NUMBER() OVER (ORDER BY dms.net_profit DESC) as ranking
FROM drivers d
JOIN driver_monthly_summary dms ON d.id = dms.driver_id
LEFT JOIN subscription_packages sp ON dms.subscription_package_id = sp.id
WHERE dms.year = EXTRACT(YEAR FROM CURRENT_DATE)
  AND dms.month = EXTRACT(MONTH FROM CURRENT_DATE)
  AND dms.is_active = TRUE
ORDER BY dms.net_profit DESC
LIMIT 50;

-- Monthly Profit Trends
CREATE OR REPLACE VIEW monthly_profit_trends AS
SELECT 
    dms.year,
    dms.month,
    COUNT(*) as active_drivers,
    AVG(dms.net_profit) as avg_monthly_profit,
    SUM(dms.net_profit) as total_profit,
    AVG(dms.average_rating) as avg_rating,
    AVG(dms.completion_rate) as avg_completion_rate,
    AVG(dms.total_trips) as avg_monthly_trips
FROM driver_monthly_summary dms
WHERE dms.is_active = TRUE
GROUP BY dms.year, dms.month
ORDER BY dms.year DESC, dms.month DESC;

-- Driver Performance Dashboard
CREATE OR REPLACE VIEW driver_performance_dashboard AS
SELECT 
    d.id,
    d.first_name,
    d.last_name,
    d.subscription_package_id,
    sp.package_name,
    sp.commission_rate,
    
    -- Current Month Performance
    COALESCE(dms_current.net_profit, 0) as current_month_profit,
    COALESCE(dms_current.total_trips, 0) as current_month_trips,
    COALESCE(dms_current.average_rating, 0) as current_month_rating,
    
    -- Previous Month Comparison
    COALESCE(dms_previous.net_profit, 0) as previous_month_profit,
    COALESCE(dms_current.net_profit - dms_previous.net_profit, 0) as profit_change,
    
    -- Year to Date
    COALESCE(dys.net_profit, 0) as ytd_profit,
    COALESCE(dys.total_trips, 0) as ytd_trips,
    COALESCE(dys.average_rating, 0) as ytd_rating,
    
    -- Rankings
    dms_current.ranking_in_subscription_tier,
    dms_current.total_drivers_in_tier,
    
    -- Status
    d.is_active,
    d.status
    
FROM drivers d
LEFT JOIN subscription_packages sp ON d.subscription_package_id = sp.id
LEFT JOIN driver_monthly_summary dms_current ON d.id = dms_current.driver_id 
    AND dms_current.year = EXTRACT(YEAR FROM CURRENT_DATE)
    AND dms_current.month = EXTRACT(MONTH FROM CURRENT_DATE)
LEFT JOIN driver_monthly_summary dms_previous ON d.id = dms_previous.driver_id 
    AND dms_previous.year = EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '1 month')
    AND dms_previous.month = EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '1 month')
LEFT JOIN driver_yearly_summary dys ON d.id = dys.driver_id 
    AND dys.year = EXTRACT(YEAR FROM CURRENT_DATE)
WHERE d.is_active = TRUE;

-- =============================================================================
-- TRIGGERS FOR AUTOMATIC CALCULATIONS
-- =============================================================================

-- Function to calculate daily performance metrics
CREATE OR REPLACE FUNCTION calculate_daily_performance()
RETURNS TRIGGER AS $$
BEGIN
    -- Calculate total expenses
    NEW.total_expenses = COALESCE(NEW.fuel_cost, 0) + 
                        COALESCE(NEW.vehicle_maintenance_cost, 0) + 
                        COALESCE(NEW.platform_commission, 0) + 
                        COALESCE(NEW.subscription_fee, 0) + 
                        COALESCE(NEW.insurance_cost, 0) + 
                        COALESCE(NEW.parking_fees, 0) + 
                        COALESCE(NEW.toll_fees, 0) + 
                        COALESCE(NEW.other_expenses, 0);
    
    -- Calculate net profit
    NEW.net_profit = COALESCE(NEW.gross_earnings, 0) - NEW.total_expenses;
    
    -- Calculate profit margin
    IF NEW.gross_earnings > 0 THEN
        NEW.profit_margin = (NEW.net_profit / NEW.gross_earnings) * 100;
    ELSE
        NEW.profit_margin = 0;
    END IF;
    
    -- Calculate earnings per hour
    IF NEW.total_work_hours > 0 THEN
        NEW.earnings_per_hour = NEW.net_profit / NEW.total_work_hours;
    ELSE
        NEW.earnings_per_hour = 0;
    END IF;
    
    -- Calculate earnings per km
    IF NEW.total_distance_km > 0 THEN
        NEW.earnings_per_km = NEW.net_profit / NEW.total_distance_km;
    ELSE
        NEW.earnings_per_km = 0;
    END IF;
    
    -- Calculate earnings per trip
    IF NEW.completed_trips > 0 THEN
        NEW.earnings_per_trip = NEW.net_profit / NEW.completed_trips;
    ELSE
        NEW.earnings_per_trip = 0;
    END IF;
    
    -- Calculate rates
    IF NEW.total_trips > 0 THEN
        NEW.cancellation_rate = (NEW.cancelled_trips::DECIMAL / NEW.total_trips) * 100;
        NEW.completion_rate = (NEW.completed_trips::DECIMAL / NEW.total_trips) * 100;
    ELSE
        NEW.cancellation_rate = 0;
        NEW.completion_rate = 0;
    END IF;
    
    -- Calculate target achievement rate
    IF NEW.daily_earnings_target > 0 THEN
        NEW.target_achievement_rate = (NEW.net_profit / NEW.daily_earnings_target) * 100;
    ELSE
        NEW.target_achievement_rate = 0;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger
DROP TRIGGER IF EXISTS trigger_calculate_daily_performance ON driver_daily_performance;
CREATE TRIGGER trigger_calculate_daily_performance
    BEFORE INSERT OR UPDATE ON driver_daily_performance
    FOR EACH ROW
    EXECUTE FUNCTION calculate_daily_performance();

COMMIT;