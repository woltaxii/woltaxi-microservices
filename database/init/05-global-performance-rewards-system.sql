-- WOLTAXI Global Performance Analytics & Rewards System Database Schema
-- Ülkeler Arası Analiz, Kapasite ve Verimlilik Çalışması
-- En İyi Ülke ve Sürücü Ödüllendirme Algoritması
-- Global Performance Comparison & Rewards Management
-- Tarih: 24 Ekim 2025

-- =============================================================================
-- GLOBAL COUNTRY PERFORMANCE - Ülke Bazında Performans Analizi
-- =============================================================================

CREATE TABLE IF NOT EXISTS country_performance_metrics (
    id BIGSERIAL PRIMARY KEY,
    
    -- Country & Period Info
    country_code VARCHAR(3) NOT NULL REFERENCES countries(country_code),
    year INTEGER NOT NULL,
    month INTEGER NOT NULL CHECK (month BETWEEN 1 AND 12),
    
    -- Market Capacity & Coverage
    total_registered_drivers INTEGER DEFAULT 0,
    active_drivers INTEGER DEFAULT 0, -- Drivers who worked this month
    new_driver_registrations INTEGER DEFAULT 0,
    driver_churn_count INTEGER DEFAULT 0, -- Drivers who left
    driver_retention_rate DECIMAL(5, 2) DEFAULT 0, -- % of drivers retained
    
    -- Service Coverage
    total_cities_covered INTEGER DEFAULT 0,
    total_service_areas INTEGER DEFAULT 0,
    population_coverage_percentage DECIMAL(5, 2) DEFAULT 0,
    market_penetration_rate DECIMAL(5, 2) DEFAULT 0, -- % of potential market captured
    
    -- Trip & Demand Analytics
    total_trips_completed INTEGER DEFAULT 0,
    total_trips_requested INTEGER DEFAULT 0,
    trip_fulfillment_rate DECIMAL(5, 2) DEFAULT 0, -- Completed/Requested %
    average_wait_time_minutes DECIMAL(5, 2) DEFAULT 0,
    peak_hour_efficiency DECIMAL(5, 2) DEFAULT 0, -- Performance during peak hours
    
    -- Financial Performance
    total_gross_revenue DECIMAL(15, 2) DEFAULT 0, -- In local currency
    total_gross_revenue_usd DECIMAL(15, 2) DEFAULT 0,
    platform_commission_earned DECIMAL(12, 2) DEFAULT 0,
    driver_total_earnings DECIMAL(15, 2) DEFAULT 0,
    average_trip_value DECIMAL(8, 2) DEFAULT 0,
    revenue_per_driver DECIMAL(10, 2) DEFAULT 0,
    
    -- Operational Efficiency
    average_trips_per_driver DECIMAL(6, 2) DEFAULT 0,
    average_hours_per_driver DECIMAL(6, 2) DEFAULT 0,
    driver_utilization_rate DECIMAL(5, 2) DEFAULT 0, -- Active time / Total time
    fuel_efficiency_average DECIMAL(5, 2) DEFAULT 0, -- km per liter
    operational_cost_ratio DECIMAL(5, 2) DEFAULT 0, -- Costs / Revenue %
    
    -- Customer Satisfaction
    average_customer_rating DECIMAL(3, 2) DEFAULT 0,
    total_customer_ratings INTEGER DEFAULT 0,
    customer_complaint_rate DECIMAL(5, 2) DEFAULT 0, -- Complaints per 1000 trips
    customer_retention_rate DECIMAL(5, 2) DEFAULT 0,
    net_promoter_score INTEGER DEFAULT 0, -- NPS score
    
    -- Quality Metrics
    on_time_arrival_rate DECIMAL(5, 2) DEFAULT 0, -- % of on-time arrivals
    trip_cancellation_rate DECIMAL(5, 2) DEFAULT 0,
    safety_incident_rate DECIMAL(5, 4) DEFAULT 0, -- Incidents per 10,000 trips
    vehicle_quality_score DECIMAL(3, 2) DEFAULT 0, -- 1-5 scale
    
    -- Growth & Trends
    month_over_month_growth DECIMAL(5, 2) DEFAULT 0, -- Revenue growth %
    year_over_year_growth DECIMAL(5, 2) DEFAULT 0,
    seasonal_demand_factor DECIMAL(4, 2) DEFAULT 1.0, -- Seasonal multiplier
    
    -- Competitive Analysis
    market_share_percentage DECIMAL(5, 2) DEFAULT 0,
    competitor_count INTEGER DEFAULT 0,
    competitive_advantage_score DECIMAL(3, 2) DEFAULT 0, -- 1-5 scale
    
    -- Technology & Innovation
    app_download_count INTEGER DEFAULT 0,
    app_rating DECIMAL(3, 2) DEFAULT 0,
    technology_adoption_rate DECIMAL(5, 2) DEFAULT 0, -- % using latest features
    mobile_payment_usage_rate DECIMAL(5, 2) DEFAULT 0,
    
    -- Regulatory & Compliance
    regulatory_compliance_score DECIMAL(3, 2) DEFAULT 0, -- 1-5 scale
    license_compliance_rate DECIMAL(5, 2) DEFAULT 100.0,
    tax_compliance_rate DECIMAL(5, 2) DEFAULT 100.0,
    
    -- Environmental Impact
    average_co2_emissions_per_trip DECIMAL(6, 3) DEFAULT 0, -- kg CO2
    electric_vehicle_percentage DECIMAL(5, 2) DEFAULT 0,
    environmental_score DECIMAL(3, 2) DEFAULT 0, -- 1-5 scale
    
    -- Performance Ranking
    global_ranking INTEGER,
    regional_ranking INTEGER,
    performance_tier VARCHAR(20) DEFAULT 'DEVELOPING' CHECK (performance_tier IN ('PLATINUM', 'GOLD', 'SILVER', 'BRONZE', 'DEVELOPING')),
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints and Indexes
    UNIQUE(country_code, year, month),
    INDEX idx_country_performance_period (year, month),
    INDEX idx_country_performance_ranking (global_ranking),
    INDEX idx_country_performance_revenue (total_gross_revenue_usd DESC)
);

-- =============================================================================
-- GLOBAL DRIVER RANKINGS - Küresel Sürücü Sıralaması
-- =============================================================================

CREATE TABLE IF NOT EXISTS global_driver_rankings (
    id BIGSERIAL PRIMARY KEY,
    
    -- Driver & Location Info
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    country_code VARCHAR(3) NOT NULL,
    city VARCHAR(100),
    
    -- Ranking Period
    ranking_year INTEGER NOT NULL,
    ranking_month INTEGER NOT NULL CHECK (ranking_month BETWEEN 1 AND 12),
    
    -- Performance Metrics
    total_trips_completed INTEGER DEFAULT 0,
    total_distance_km DECIMAL(10, 2) DEFAULT 0,
    total_earnings_local DECIMAL(12, 2) DEFAULT 0,
    total_earnings_usd DECIMAL(12, 2) DEFAULT 0,
    average_customer_rating DECIMAL(3, 2) DEFAULT 0,
    total_customer_ratings INTEGER DEFAULT 0,
    
    -- Efficiency Scores
    trips_per_hour DECIMAL(5, 2) DEFAULT 0,
    earnings_per_hour_usd DECIMAL(8, 2) DEFAULT 0,
    fuel_efficiency_score DECIMAL(5, 2) DEFAULT 0, -- km per liter
    route_optimization_score DECIMAL(5, 2) DEFAULT 0, -- 0-100 scale
    
    -- Quality Scores
    punctuality_score DECIMAL(5, 2) DEFAULT 0, -- % on-time arrivals
    customer_satisfaction_score DECIMAL(5, 2) DEFAULT 0, -- Weighted satisfaction
    vehicle_cleanliness_score DECIMAL(3, 2) DEFAULT 0, -- 1-5 scale
    professionalism_score DECIMAL(3, 2) DEFAULT 0, -- 1-5 scale
    
    -- Reliability Metrics
    attendance_rate DECIMAL(5, 2) DEFAULT 0, -- % of scheduled days worked
    completion_rate DECIMAL(5, 2) DEFAULT 0, -- % of started trips completed
    cancellation_rate DECIMAL(5, 2) DEFAULT 0, -- % of trips cancelled by driver
    availability_hours DECIMAL(6, 2) DEFAULT 0, -- Hours available for rides
    
    -- Innovation & Technology
    app_usage_score DECIMAL(5, 2) DEFAULT 0, -- Feature adoption rate
    navigation_accuracy DECIMAL(5, 2) DEFAULT 0, -- % accurate route following
    communication_score DECIMAL(3, 2) DEFAULT 0, -- Customer communication quality
    
    -- Overall Performance Score (Weighted Algorithm)
    performance_score DECIMAL(8, 4) DEFAULT 0, -- Composite score 0-100
    
    -- Rankings
    global_ranking INTEGER,
    country_ranking INTEGER,
    city_ranking INTEGER,
    category_ranking INTEGER, -- Ranking within subscription tier
    
    -- Achievement Level
    achievement_tier VARCHAR(20) DEFAULT 'BRONZE' CHECK (achievement_tier IN ('DIAMOND', 'PLATINUM', 'GOLD', 'SILVER', 'BRONZE', 'STANDARD')),
    points_earned INTEGER DEFAULT 0, -- Gamification points
    badges_earned TEXT[], -- Array of badge names
    
    -- Improvement Metrics
    performance_trend VARCHAR(20) DEFAULT 'STABLE' CHECK (performance_trend IN ('IMPROVING', 'STABLE', 'DECLINING')),
    month_over_month_improvement DECIMAL(5, 2) DEFAULT 0,
    areas_for_improvement TEXT[], -- Areas needing focus
    
    -- Eligibility for Rewards
    eligible_for_monthly_reward BOOLEAN DEFAULT FALSE,
    eligible_for_quarterly_reward BOOLEAN DEFAULT FALSE,
    eligible_for_annual_reward BOOLEAN DEFAULT FALSE,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints and Indexes
    UNIQUE(driver_id, ranking_year, ranking_month),
    INDEX idx_global_rankings_period (ranking_year, ranking_month),
    INDEX idx_global_rankings_performance (performance_score DESC),
    INDEX idx_global_rankings_global (global_ranking),
    INDEX idx_global_rankings_country (country_code, country_ranking)
);

-- =============================================================================
-- REWARD PROGRAMS - Ödül Programları
-- =============================================================================

CREATE TABLE IF NOT EXISTS reward_programs (
    id BIGSERIAL PRIMARY KEY,
    
    -- Program Details
    program_name VARCHAR(200) NOT NULL,
    program_code VARCHAR(50) NOT NULL UNIQUE,
    program_description TEXT,
    
    -- Program Type & Scope
    program_type VARCHAR(30) NOT NULL CHECK (program_type IN ('MONTHLY', 'QUARTERLY', 'ANNUAL', 'SPECIAL_EVENT', 'MILESTONE')),
    program_scope VARCHAR(20) NOT NULL CHECK (program_scope IN ('GLOBAL', 'COUNTRY', 'CITY', 'CATEGORY')),
    
    -- Eligibility Criteria
    min_trips_required INTEGER DEFAULT 0,
    min_rating_required DECIMAL(3, 2) DEFAULT 0,
    min_earnings_required DECIMAL(10, 2) DEFAULT 0,
    min_hours_worked INTEGER DEFAULT 0,
    max_cancellation_rate DECIMAL(5, 2) DEFAULT 100.0,
    required_badges TEXT[], -- Required badges for eligibility
    
    -- Reward Structure
    reward_type VARCHAR(30) NOT NULL CHECK (reward_type IN ('CASH_BONUS', 'POINTS', 'BADGE', 'SUBSCRIPTION_DISCOUNT', 'GIFT_CARD', 'TROPHY', 'RECOGNITION')),
    reward_value DECIMAL(10, 2) DEFAULT 0, -- Monetary value in USD
    currency_code VARCHAR(3) DEFAULT 'USD',
    
    -- Winner Selection
    max_winners_global INTEGER DEFAULT 1,
    max_winners_per_country INTEGER DEFAULT 1,
    max_winners_per_city INTEGER DEFAULT 1,
    selection_algorithm VARCHAR(50) DEFAULT 'TOP_PERFORMER', -- Selection method
    
    -- Program Period
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    announcement_date DATE,
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    is_recurring BOOLEAN DEFAULT FALSE,
    recurrence_pattern VARCHAR(50), -- 'MONTHLY', 'QUARTERLY', etc.
    
    -- Metrics & Analytics
    total_participants INTEGER DEFAULT 0,
    total_winners INTEGER DEFAULT 0,
    total_rewards_distributed DECIMAL(12, 2) DEFAULT 0,
    engagement_score DECIMAL(5, 2) DEFAULT 0,
    
    -- Display & Marketing
    program_icon VARCHAR(100),
    program_color VARCHAR(7), -- Hex color code
    marketing_message TEXT,
    terms_and_conditions TEXT,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- REWARD WINNERS - Ödül Kazananları
-- =============================================================================

CREATE TABLE IF NOT EXISTS reward_winners (
    id BIGSERIAL PRIMARY KEY,
    
    -- Award Details
    program_id BIGINT NOT NULL REFERENCES reward_programs(id),
    driver_id BIGINT NOT NULL REFERENCES drivers(id),
    
    -- Winner Information
    driver_name VARCHAR(200) NOT NULL,
    country_code VARCHAR(3) NOT NULL,
    city VARCHAR(100),
    winning_performance_score DECIMAL(8, 4),
    
    -- Award Period Performance
    award_year INTEGER NOT NULL,
    award_month INTEGER,
    performance_metrics JSONB, -- Detailed performance data
    
    -- Ranking Information
    global_rank INTEGER,
    country_rank INTEGER,
    city_rank INTEGER,
    total_competitors INTEGER,
    
    -- Reward Details
    reward_amount DECIMAL(10, 2) NOT NULL,
    reward_currency VARCHAR(3) DEFAULT 'USD',
    reward_description TEXT,
    
    -- Award Status
    award_status VARCHAR(20) DEFAULT 'PENDING' CHECK (award_status IN ('PENDING', 'APPROVED', 'DISTRIBUTED', 'CLAIMED', 'EXPIRED')),
    award_date DATE NOT NULL,
    distribution_date DATE,
    claim_date DATE,
    expiry_date DATE,
    
    -- Recognition
    public_recognition BOOLEAN DEFAULT TRUE,
    certificate_url VARCHAR(500),
    badge_earned VARCHAR(100),
    points_awarded INTEGER DEFAULT 0,
    
    -- Additional Benefits
    additional_benefits TEXT[], -- Extra perks or benefits
    subscription_discount_months INTEGER DEFAULT 0,
    priority_support_months INTEGER DEFAULT 0,
    
    -- Communication
    notification_sent BOOLEAN DEFAULT FALSE,
    notification_date TIMESTAMP,
    announcement_made BOOLEAN DEFAULT FALSE,
    announcement_date TIMESTAMP,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_reward_winners_driver (driver_id),
    INDEX idx_reward_winners_program (program_id),
    INDEX idx_reward_winners_period (award_year, award_month),
    INDEX idx_reward_winners_country (country_code)
);

-- =============================================================================
-- PERFORMANCE COMPARISON ANALYTICS - Performans Karşılaştırma
-- =============================================================================

CREATE TABLE IF NOT EXISTS performance_benchmarks (
    id BIGSERIAL PRIMARY KEY,
    
    -- Benchmark Details
    benchmark_name VARCHAR(200) NOT NULL,
    benchmark_category VARCHAR(50) NOT NULL, -- 'EFFICIENCY', 'QUALITY', 'FINANCIAL', etc.
    
    -- Scope & Period
    benchmark_scope VARCHAR(20) NOT NULL CHECK (benchmark_scope IN ('GLOBAL', 'REGIONAL', 'COUNTRY', 'CITY')),
    scope_identifier VARCHAR(100), -- Country code, city name, etc.
    benchmark_year INTEGER NOT NULL,
    benchmark_quarter INTEGER CHECK (benchmark_quarter BETWEEN 1 AND 4),
    
    -- Benchmark Values
    benchmark_value DECIMAL(12, 4) NOT NULL,
    benchmark_unit VARCHAR(50), -- 'USD', 'PERCENTAGE', 'RATING', etc.
    
    -- Statistical Data
    sample_size INTEGER NOT NULL,
    minimum_value DECIMAL(12, 4),
    maximum_value DECIMAL(12, 4),
    median_value DECIMAL(12, 4),
    standard_deviation DECIMAL(12, 4),
    
    -- Performance Tiers
    platinum_threshold DECIMAL(12, 4), -- Top 5%
    gold_threshold DECIMAL(12, 4),     -- Top 15%
    silver_threshold DECIMAL(12, 4),   -- Top 30%
    bronze_threshold DECIMAL(12, 4),   -- Top 50%
    
    -- Trend Analysis
    previous_period_value DECIMAL(12, 4),
    trend_direction VARCHAR(20) CHECK (trend_direction IN ('IMPROVING', 'STABLE', 'DECLINING')),
    trend_percentage DECIMAL(5, 2),
    
    -- Quality & Reliability
    data_quality_score DECIMAL(3, 2) DEFAULT 5.0, -- 1-5 scale
    confidence_level DECIMAL(5, 2) DEFAULT 95.0, -- Statistical confidence
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_benchmarks_scope (benchmark_scope, scope_identifier),
    INDEX idx_benchmarks_category (benchmark_category),
    INDEX idx_benchmarks_period (benchmark_year, benchmark_quarter)
);

-- =============================================================================
-- ALGORITHM CONFIGURATIONS - Algoritma Konfigürasyonları
-- =============================================================================

CREATE TABLE IF NOT EXISTS reward_algorithms (
    id BIGSERIAL PRIMARY KEY,
    
    -- Algorithm Details
    algorithm_name VARCHAR(200) NOT NULL UNIQUE,
    algorithm_version VARCHAR(20) NOT NULL,
    algorithm_description TEXT,
    
    -- Algorithm Type
    algorithm_type VARCHAR(30) NOT NULL CHECK (algorithm_type IN ('PERFORMANCE_SCORING', 'REWARD_DISTRIBUTION', 'RANKING', 'BENCHMARKING')),
    
    -- Scoring Weights (Performance Algorithm)
    customer_rating_weight DECIMAL(4, 3) DEFAULT 0.200, -- 20%
    trip_completion_weight DECIMAL(4, 3) DEFAULT 0.150, -- 15%
    earnings_efficiency_weight DECIMAL(4, 3) DEFAULT 0.150, -- 15%
    punctuality_weight DECIMAL(4, 3) DEFAULT 0.100, -- 10%
    fuel_efficiency_weight DECIMAL(4, 3) DEFAULT 0.100, -- 10%
    customer_service_weight DECIMAL(4, 3) DEFAULT 0.100, -- 10%
    safety_record_weight DECIMAL(4, 3) DEFAULT 0.100, -- 10%
    innovation_adoption_weight DECIMAL(4, 3) DEFAULT 0.050, -- 5%
    attendance_weight DECIMAL(4, 3) DEFAULT 0.050, -- 5%
    
    -- Bonus Multipliers
    consecutive_months_bonus DECIMAL(4, 3) DEFAULT 1.000, -- Multiplier for consistency
    new_driver_bonus DECIMAL(4, 3) DEFAULT 1.100, -- 10% bonus for new drivers
    veteran_driver_bonus DECIMAL(4, 3) DEFAULT 1.050, -- 5% bonus for veterans
    peak_hours_bonus DECIMAL(4, 3) DEFAULT 1.200, -- 20% bonus for peak hour performance
    
    -- Penalties
    cancellation_penalty DECIMAL(4, 3) DEFAULT 0.950, -- 5% penalty for high cancellations
    low_rating_penalty DECIMAL(4, 3) DEFAULT 0.900, -- 10% penalty for ratings below 4.0
    late_arrival_penalty DECIMAL(4, 3) DEFAULT 0.980, -- 2% penalty for frequent lateness
    
    -- Normalization Parameters
    min_score DECIMAL(6, 2) DEFAULT 0.00,
    max_score DECIMAL(6, 2) DEFAULT 100.00,
    normalization_method VARCHAR(20) DEFAULT 'PERCENTILE', -- Method for score normalization
    
    -- Algorithm Status
    is_active BOOLEAN DEFAULT TRUE,
    effective_date DATE NOT NULL,
    expiry_date DATE,
    
    -- Performance Tracking
    calculation_count INTEGER DEFAULT 0,
    average_execution_time_ms INTEGER DEFAULT 0,
    accuracy_score DECIMAL(5, 2) DEFAULT 0, -- Algorithm accuracy measurement
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- INITIAL DATA - Başlangıç Verileri
-- =============================================================================

-- Insert default reward programs
INSERT INTO reward_programs (
    program_name, program_code, program_description, program_type, program_scope,
    min_trips_required, min_rating_required, reward_type, reward_value,
    max_winners_global, max_winners_per_country, start_date, end_date, is_recurring, recurrence_pattern
) VALUES
-- Monthly Programs
('Global Driver of the Month', 'GLOBAL_DOTM', 'Best performing driver worldwide each month', 'MONTHLY', 'GLOBAL', 
 50, 4.8, 'CASH_BONUS', 5000.00, 1, 0, '2025-01-01', '2025-12-31', TRUE, 'MONTHLY'),

('Country Champion', 'COUNTRY_CHAMPION', 'Top driver in each country monthly', 'MONTHLY', 'COUNTRY',
 30, 4.5, 'CASH_BONUS', 1000.00, 0, 1, '2025-01-01', '2025-12-31', TRUE, 'MONTHLY'),

('City Star', 'CITY_STAR', 'Best driver in each major city', 'MONTHLY', 'CITY',
 20, 4.3, 'CASH_BONUS', 500.00, 0, 0, '2025-01-01', '2025-12-31', TRUE, 'MONTHLY'),

-- Quarterly Programs
('Excellence Award', 'EXCELLENCE_Q', 'Quarterly excellence recognition', 'QUARTERLY', 'GLOBAL',
 150, 4.7, 'CASH_BONUS', 10000.00, 5, 1, '2025-01-01', '2025-12-31', TRUE, 'QUARTERLY'),

-- Annual Programs
('WOLTAXI Legend', 'LEGEND_ANNUAL', 'Annual legend status for top performers', 'ANNUAL', 'GLOBAL',
 600, 4.9, 'CASH_BONUS', 50000.00, 3, 0, '2025-01-01', '2025-12-31', TRUE, 'ANNUAL')

ON CONFLICT (program_code) DO NOTHING;

-- Insert default algorithm configuration
INSERT INTO reward_algorithms (
    algorithm_name, algorithm_version, algorithm_description, algorithm_type,
    customer_rating_weight, trip_completion_weight, earnings_efficiency_weight,
    punctuality_weight, fuel_efficiency_weight, customer_service_weight,
    safety_record_weight, innovation_adoption_weight, attendance_weight,
    effective_date
) VALUES (
    'WOLTAXI Standard Performance Algorithm v2.0',
    '2.0',
    'Comprehensive driver performance scoring algorithm with balanced metrics',
    'PERFORMANCE_SCORING',
    0.200, 0.150, 0.150,
    0.100, 0.100, 0.100,
    0.100, 0.050, 0.050,
    '2025-01-01'
) ON CONFLICT (algorithm_name) DO NOTHING;

-- Insert benchmark categories
INSERT INTO performance_benchmarks (
    benchmark_name, benchmark_category, benchmark_scope, benchmark_year,
    benchmark_value, benchmark_unit, sample_size,
    platinum_threshold, gold_threshold, silver_threshold, bronze_threshold
) VALUES 
('Global Average Customer Rating', 'QUALITY', 'GLOBAL', 2025, 4.2, 'RATING', 100000, 4.8, 4.6, 4.4, 4.2),
('Global Average Monthly Earnings', 'FINANCIAL', 'GLOBAL', 2025, 2500.00, 'USD', 100000, 5000.00, 4000.00, 3000.00, 2500.00),
('Global Trip Completion Rate', 'EFFICIENCY', 'GLOBAL', 2025, 92.5, 'PERCENTAGE', 100000, 98.0, 95.0, 93.0, 90.0),
('Global Fuel Efficiency', 'EFFICIENCY', 'GLOBAL', 2025, 12.5, 'KM_PER_LITER', 100000, 18.0, 15.0, 13.0, 11.0)
ON CONFLICT DO NOTHING;

-- =============================================================================
-- VIEWS FOR ANALYTICS - Analiz Görünümleri
-- =============================================================================

-- Top Performing Countries View
CREATE OR REPLACE VIEW top_performing_countries AS
SELECT 
    cpm.country_code,
    c.country_name,
    cpm.year,
    cpm.month,
    cpm.total_gross_revenue_usd,
    cpm.average_customer_rating,
    cpm.trip_fulfillment_rate,
    cpm.driver_utilization_rate,
    cpm.global_ranking,
    cpm.performance_tier,
    -- Composite Performance Score
    (
        (cpm.average_customer_rating / 5.0 * 100) * 0.25 +
        cpm.trip_fulfillment_rate * 0.25 +
        cpm.driver_utilization_rate * 0.25 +
        (cpm.revenue_per_driver / 5000.0 * 100) * 0.25
    ) as composite_score
FROM country_performance_metrics cpm
JOIN countries c ON cpm.country_code = c.country_code
WHERE cpm.year = EXTRACT(YEAR FROM CURRENT_DATE)
  AND cpm.month = EXTRACT(MONTH FROM CURRENT_DATE)
ORDER BY composite_score DESC;

-- Global Driver Leaderboard View
CREATE OR REPLACE VIEW global_driver_leaderboard AS
SELECT 
    gdr.driver_id,
    d.first_name,
    d.last_name,
    gdr.country_code,
    c.country_name,
    gdr.city,
    gdr.performance_score,
    gdr.global_ranking,
    gdr.country_ranking,
    gdr.achievement_tier,
    gdr.total_earnings_usd,
    gdr.average_customer_rating,
    gdr.total_trips_completed,
    gdr.earnings_per_hour_usd,
    gdr.eligible_for_monthly_reward,
    gdr.points_earned,
    gdr.performance_trend
FROM global_driver_rankings gdr
JOIN drivers d ON gdr.driver_id = d.id
JOIN countries c ON gdr.country_code = c.country_code
WHERE gdr.ranking_year = EXTRACT(YEAR FROM CURRENT_DATE)
  AND gdr.ranking_month = EXTRACT(MONTH FROM CURRENT_DATE)
ORDER BY gdr.global_ranking ASC;

-- Country Comparison Dashboard
CREATE OR REPLACE VIEW country_comparison_dashboard AS
SELECT 
    cpm.country_code,
    c.country_name,
    c.continent,
    cpm.active_drivers,
    cpm.total_trips_completed,
    cpm.total_gross_revenue_usd,
    cpm.average_customer_rating,
    cpm.trip_fulfillment_rate,
    cpm.driver_utilization_rate,
    cpm.revenue_per_driver,
    cpm.global_ranking,
    cpm.performance_tier,
    cpm.month_over_month_growth,
    cpm.year_over_year_growth,
    -- Performance vs Global Average
    cpm.average_customer_rating - 4.2 as rating_vs_global_avg,
    cpm.trip_fulfillment_rate - 92.5 as fulfillment_vs_global_avg,
    cpm.driver_utilization_rate - 75.0 as utilization_vs_global_avg
FROM country_performance_metrics cpm
JOIN countries c ON cpm.country_code = c.country_code
WHERE cpm.year = EXTRACT(YEAR FROM CURRENT_DATE)
  AND cpm.month = EXTRACT(MONTH FROM CURRENT_DATE)
ORDER BY cpm.global_ranking ASC;

COMMIT;