-- WOLTAXI Global Marketing & Advertisement System Database Schema
-- Tüm Ülkelerde Bağımsız Reklam Altyapısı
-- Multi-Platform Advertisement Management
-- Tarih: 24 Ekim 2025

-- =============================================================================
-- COUNTRIES & MARKETS - Ülke ve Pazar Yönetimi
-- =============================================================================

CREATE TABLE IF NOT EXISTS countries (
    id BIGSERIAL PRIMARY KEY,
    country_code VARCHAR(3) NOT NULL UNIQUE, -- ISO 3166-1 (USA, TUR, DEU)
    country_name VARCHAR(100) NOT NULL,
    continent VARCHAR(50) NOT NULL,
    
    -- Localization
    primary_language VARCHAR(10) NOT NULL, -- en, tr, de, fr, es
    secondary_languages VARCHAR(100)[], -- ['en', 'ar'] for multi-language countries
    currency_code VARCHAR(3) NOT NULL, -- USD, TRY, EUR
    currency_symbol VARCHAR(5) NOT NULL, -- $, ₺, €
    
    -- Market Info
    population BIGINT,
    gdp_per_capita DECIMAL(10, 2),
    internet_penetration DECIMAL(5, 2), -- %85.5
    smartphone_penetration DECIMAL(5, 2), -- %78.2
    
    -- Time & Culture
    timezone VARCHAR(50) NOT NULL, -- Europe/Istanbul, America/New_York
    date_format VARCHAR(20) DEFAULT 'DD/MM/YYYY',
    time_format VARCHAR(10) DEFAULT '24h', -- 24h, 12h
    weekend_days VARCHAR(20) DEFAULT 'saturday,sunday',
    
    -- Business Environment
    market_maturity VARCHAR(20) DEFAULT 'EMERGING' CHECK (market_maturity IN ('EMERGING', 'DEVELOPING', 'MATURE', 'SATURATED')),
    competition_level VARCHAR(20) DEFAULT 'MEDIUM' CHECK (competition_level IN ('LOW', 'MEDIUM', 'HIGH', 'EXTREME')),
    regulatory_complexity VARCHAR(20) DEFAULT 'MEDIUM' CHECK (regulatory_complexity IN ('LOW', 'MEDIUM', 'HIGH', 'COMPLEX')),
    
    -- Marketing Budget & Targeting
    daily_ad_budget_usd DECIMAL(10, 2) DEFAULT 1000.00,
    target_audience_size BIGINT,
    peak_hours VARCHAR(100), -- "08:00-10:00,17:00-19:00"
    cultural_preferences TEXT,
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    launch_date DATE,
    market_priority INTEGER DEFAULT 3, -- 1=High, 2=Medium, 3=Low
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- ADVERTISEMENT PLATFORMS - Reklam Platformları
-- =============================================================================

CREATE TABLE IF NOT EXISTS ad_platforms (
    id BIGSERIAL PRIMARY KEY,
    platform_name VARCHAR(50) NOT NULL UNIQUE,
    platform_code VARCHAR(20) NOT NULL UNIQUE,
    
    -- Platform Details
    platform_type VARCHAR(30) NOT NULL CHECK (platform_type IN ('SOCIAL_MEDIA', 'SEARCH_ENGINE', 'VIDEO', 'DISPLAY', 'MOBILE')),
    api_endpoint VARCHAR(200) NOT NULL,
    api_version VARCHAR(10),
    
    -- Features
    supports_video BOOLEAN DEFAULT FALSE,
    supports_carousel BOOLEAN DEFAULT FALSE,
    supports_stories BOOLEAN DEFAULT FALSE,
    supports_retargeting BOOLEAN DEFAULT FALSE,
    supports_lookalike BOOLEAN DEFAULT FALSE,
    supports_geotargeting BOOLEAN DEFAULT FALSE,
    
    -- Pricing Model
    pricing_models VARCHAR(100)[], -- ['CPC', 'CPM', 'CPA', 'CPV']
    min_daily_budget_usd DECIMAL(8, 2) DEFAULT 5.00,
    avg_cpc_usd DECIMAL(6, 4), -- Average Cost Per Click
    avg_cpm_usd DECIMAL(6, 4), -- Average Cost Per Mille
    
    -- Global Availability
    available_countries VARCHAR(3)[], -- Country codes where platform is available
    restricted_countries VARCHAR(3)[], -- Restricted countries
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    integration_status VARCHAR(20) DEFAULT 'PENDING' CHECK (integration_status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'DEPRECATED')),
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- COUNTRY PLATFORM CONFIGS - Ülke Bazında Platform Ayarları
-- =============================================================================

CREATE TABLE IF NOT EXISTS country_platform_configs (
    id BIGSERIAL PRIMARY KEY,
    country_id BIGINT NOT NULL REFERENCES countries(id),
    platform_id BIGINT NOT NULL REFERENCES ad_platforms(id),
    
    -- Platform Specific Settings
    api_key_encrypted TEXT NOT NULL,
    api_secret_encrypted TEXT,
    access_token_encrypted TEXT,
    account_id VARCHAR(100),
    
    -- Budget & Bidding
    daily_budget_local DECIMAL(10, 2) NOT NULL,
    daily_budget_usd DECIMAL(10, 2) NOT NULL,
    max_cpc_local DECIMAL(6, 4),
    max_cpm_local DECIMAL(6, 4),
    
    -- Targeting Preferences
    preferred_age_groups VARCHAR(100), -- "18-24,25-34,35-44"
    preferred_genders VARCHAR(20), -- "male,female,all"
    preferred_interests TEXT[], -- Array of interests
    preferred_behaviors TEXT[], -- User behaviors
    excluded_audiences TEXT[], -- Excluded segments
    
    -- Creative Preferences
    primary_language VARCHAR(10) NOT NULL,
    ad_tone VARCHAR(20) DEFAULT 'PROFESSIONAL' CHECK (ad_tone IN ('CASUAL', 'PROFESSIONAL', 'FRIENDLY', 'AUTHORITATIVE', 'HUMOROUS')),
    brand_colors VARCHAR(100), -- "#E30613,#FFD700,#FFFFFF"
    logo_variant VARCHAR(50), -- "horizontal", "vertical", "icon"
    
    -- Scheduling
    active_hours VARCHAR(100), -- "00:00-23:59" or specific hours
    active_days VARCHAR(50), -- "monday,tuesday,wednesday,thursday,friday,saturday,sunday"
    timezone_override VARCHAR(50),
    
    -- Performance Settings
    optimization_goal VARCHAR(30) DEFAULT 'CONVERSIONS' CHECK (optimization_goal IN ('REACH', 'IMPRESSIONS', 'CLICKS', 'CONVERSIONS', 'APP_INSTALLS')),
    bid_strategy VARCHAR(30) DEFAULT 'AUTOMATIC' CHECK (bid_strategy IN ('AUTOMATIC', 'MANUAL', 'TARGET_CPA', 'TARGET_ROAS')),
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    auto_optimization BOOLEAN DEFAULT TRUE,
    
    -- Performance Tracking
    last_sync TIMESTAMP,
    sync_status VARCHAR(20) DEFAULT 'PENDING',
    error_count INTEGER DEFAULT 0,
    last_error TEXT,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Unique constraint
    UNIQUE(country_id, platform_id)
);

-- =============================================================================
-- GLOBAL CAMPAIGNS - Küresel Kampanyalar
-- =============================================================================

CREATE TABLE IF NOT EXISTS global_campaigns (
    id BIGSERIAL PRIMARY KEY,
    campaign_uuid UUID DEFAULT uuid_generate_v4() UNIQUE,
    
    -- Campaign Basics
    campaign_name VARCHAR(200) NOT NULL,
    campaign_description TEXT,
    campaign_type VARCHAR(30) NOT NULL CHECK (campaign_type IN ('BRAND_AWARENESS', 'APP_INSTALL', 'LEAD_GENERATION', 'CONVERSION', 'ENGAGEMENT', 'TRAFFIC')),
    
    -- Global Settings
    master_language VARCHAR(10) DEFAULT 'en',
    auto_translate BOOLEAN DEFAULT TRUE,
    auto_localize BOOLEAN DEFAULT TRUE,
    
    -- Targeting
    target_countries VARCHAR(3)[], -- Countries to target
    exclude_countries VARCHAR(3)[], -- Countries to exclude
    target_platforms VARCHAR(20)[], -- Platform codes to use
    
    -- Budget & Timing
    total_budget_usd DECIMAL(12, 2) NOT NULL,
    daily_budget_usd DECIMAL(10, 2) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    
    -- Creative Assets
    primary_headline VARCHAR(200) NOT NULL,
    primary_description TEXT NOT NULL,
    call_to_action VARCHAR(50) NOT NULL,
    landing_page_url VARCHAR(500) NOT NULL,
    
    -- Media Assets
    primary_image_url VARCHAR(500),
    primary_video_url VARCHAR(500),
    logo_url VARCHAR(500),
    additional_assets TEXT[], -- Array of asset URLs
    
    -- Advanced Settings
    frequency_cap INTEGER DEFAULT 3, -- Max impressions per user per day
    audience_network BOOLEAN DEFAULT TRUE,
    instagram_placement BOOLEAN DEFAULT TRUE,
    facebook_placement BOOLEAN DEFAULT TRUE,
    stories_placement BOOLEAN DEFAULT TRUE,
    
    -- AI & Optimization
    use_ai_optimization BOOLEAN DEFAULT TRUE,
    auto_budget_reallocation BOOLEAN DEFAULT TRUE,
    auto_pause_underperforming BOOLEAN DEFAULT TRUE,
    performance_threshold DECIMAL(5, 2) DEFAULT 2.0, -- Min CTR %
    
    -- Status & Control
    status VARCHAR(20) DEFAULT 'DRAFT' CHECK (status IN ('DRAFT', 'PENDING_APPROVAL', 'ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED')),
    approval_status VARCHAR(20) DEFAULT 'PENDING',
    created_by BIGINT, -- User ID
    approved_by BIGINT, -- User ID
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    launched_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- =============================================================================
-- LOCALIZED CAMPAIGNS - Yerelleştirilmiş Kampanyalar
-- =============================================================================

CREATE TABLE IF NOT EXISTS localized_campaigns (
    id BIGSERIAL PRIMARY KEY,
    campaign_uuid UUID DEFAULT uuid_generate_v4() UNIQUE,
    
    -- References
    global_campaign_id BIGINT NOT NULL REFERENCES global_campaigns(id),
    country_id BIGINT NOT NULL REFERENCES countries(id),
    platform_id BIGINT NOT NULL REFERENCES ad_platforms(id),
    
    -- Localized Content
    localized_headline VARCHAR(200) NOT NULL,
    localized_description TEXT NOT NULL,
    localized_cta VARCHAR(50) NOT NULL,
    localized_landing_url VARCHAR(500) NOT NULL,
    
    -- Local Media
    localized_image_url VARCHAR(500),
    localized_video_url VARCHAR(500),
    cultural_adaptations TEXT, -- JSON of cultural modifications
    
    -- Local Budget & Targeting
    local_budget DECIMAL(10, 2) NOT NULL,
    local_currency VARCHAR(3) NOT NULL,
    budget_usd_equivalent DECIMAL(10, 2) NOT NULL,
    
    -- Platform Specific IDs
    platform_campaign_id VARCHAR(100), -- ID from the ad platform
    platform_adset_id VARCHAR(100),
    platform_ad_id VARCHAR(100),
    
    -- Local Performance
    impressions BIGINT DEFAULT 0,
    clicks BIGINT DEFAULT 0,
    conversions BIGINT DEFAULT 0,
    spend_local DECIMAL(10, 2) DEFAULT 0,
    spend_usd DECIMAL(10, 2) DEFAULT 0,
    
    -- Calculated Metrics
    ctr DECIMAL(5, 4) DEFAULT 0, -- Click Through Rate
    cpc_local DECIMAL(8, 4) DEFAULT 0, -- Cost Per Click
    cpm_local DECIMAL(8, 4) DEFAULT 0, -- Cost Per Mille
    conversion_rate DECIMAL(5, 4) DEFAULT 0,
    roas DECIMAL(8, 4) DEFAULT 0, -- Return on Ad Spend
    
    -- Status & Sync
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACTIVE', 'PAUSED', 'COMPLETED', 'FAILED')),
    sync_status VARCHAR(20) DEFAULT 'PENDING',
    last_synced TIMESTAMP,
    sync_errors TEXT,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    launched_at TIMESTAMP,
    
    -- Indexes
    INDEX idx_localized_campaigns_global (global_campaign_id),
    INDEX idx_localized_campaigns_country (country_id),
    INDEX idx_localized_campaigns_platform (platform_id),
    INDEX idx_localized_campaigns_status (status)
);

-- =============================================================================
-- ADVERTISEMENT PERFORMANCE - Reklam Performans Tracking
-- =============================================================================

CREATE TABLE IF NOT EXISTS ad_performance_daily (
    id BIGSERIAL PRIMARY KEY,
    
    -- References
    localized_campaign_id BIGINT NOT NULL REFERENCES localized_campaigns(id),
    country_id BIGINT NOT NULL REFERENCES countries(id),
    platform_id BIGINT NOT NULL REFERENCES ad_platforms(id),
    
    -- Date
    performance_date DATE NOT NULL,
    
    -- Core Metrics
    impressions BIGINT DEFAULT 0,
    clicks BIGINT DEFAULT 0,
    conversions BIGINT DEFAULT 0,
    app_installs BIGINT DEFAULT 0,
    registrations BIGINT DEFAULT 0,
    
    -- Financial Metrics
    spend_local DECIMAL(10, 2) DEFAULT 0,
    spend_usd DECIMAL(10, 2) DEFAULT 0,
    revenue_local DECIMAL(10, 2) DEFAULT 0,
    revenue_usd DECIMAL(10, 2) DEFAULT 0,
    
    -- Calculated KPIs
    ctr DECIMAL(5, 4) DEFAULT 0,
    cpc_local DECIMAL(8, 4) DEFAULT 0,
    cpm_local DECIMAL(8, 4) DEFAULT 0,
    cpa_local DECIMAL(8, 4) DEFAULT 0, -- Cost Per Acquisition
    roas DECIMAL(8, 4) DEFAULT 0,
    conversion_rate DECIMAL(5, 4) DEFAULT 0,
    
    -- Audience Insights
    age_18_24_percent DECIMAL(5, 2) DEFAULT 0,
    age_25_34_percent DECIMAL(5, 2) DEFAULT 0,
    age_35_44_percent DECIMAL(5, 2) DEFAULT 0,
    age_45_plus_percent DECIMAL(5, 2) DEFAULT 0,
    male_percent DECIMAL(5, 2) DEFAULT 0,
    female_percent DECIMAL(5, 2) DEFAULT 0,
    
    -- Device & Placement
    mobile_percent DECIMAL(5, 2) DEFAULT 0,
    desktop_percent DECIMAL(5, 2) DEFAULT 0,
    tablet_percent DECIMAL(5, 2) DEFAULT 0,
    feed_placement_percent DECIMAL(5, 2) DEFAULT 0,
    stories_placement_percent DECIMAL(5, 2) DEFAULT 0,
    
    -- Quality Scores
    relevance_score DECIMAL(3, 2) DEFAULT 0,
    quality_score DECIMAL(3, 2) DEFAULT 0,
    engagement_rate DECIMAL(5, 4) DEFAULT 0,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Unique constraint per day
    UNIQUE(localized_campaign_id, performance_date),
    
    -- Indexes
    INDEX idx_ad_performance_date (performance_date),
    INDEX idx_ad_performance_campaign (localized_campaign_id),
    INDEX idx_ad_performance_country (country_id)
);

-- =============================================================================
-- AI OPTIMIZATION RULES - AI Optimizasyon Kuralları
-- =============================================================================

CREATE TABLE IF NOT EXISTS ai_optimization_rules (
    id BIGSERIAL PRIMARY KEY,
    
    -- Rule Definition
    rule_name VARCHAR(100) NOT NULL,
    rule_description TEXT,
    rule_type VARCHAR(30) NOT NULL CHECK (rule_type IN ('BUDGET_OPTIMIZATION', 'BID_OPTIMIZATION', 'AUDIENCE_OPTIMIZATION', 'CREATIVE_OPTIMIZATION', 'PLACEMENT_OPTIMIZATION')),
    
    -- Conditions
    condition_metric VARCHAR(50) NOT NULL, -- 'ctr', 'cpc', 'roas', 'conversion_rate'
    condition_operator VARCHAR(10) NOT NULL CHECK (condition_operator IN ('>', '<', '>=', '<=', '=', '!=')),
    condition_value DECIMAL(10, 4) NOT NULL,
    condition_timeframe INTEGER DEFAULT 3, -- Days to evaluate
    min_data_points INTEGER DEFAULT 100, -- Minimum impressions/clicks needed
    
    -- Actions
    action_type VARCHAR(30) NOT NULL CHECK (action_type IN ('INCREASE_BUDGET', 'DECREASE_BUDGET', 'INCREASE_BID', 'DECREASE_BID', 'PAUSE_AD', 'CHANGE_AUDIENCE', 'CHANGE_CREATIVE')),
    action_value DECIMAL(10, 4), -- Percentage or absolute value
    action_limit DECIMAL(10, 4), -- Maximum change allowed
    
    -- Applicability
    applicable_countries VARCHAR(3)[], -- If null, applies to all
    applicable_platforms VARCHAR(20)[], -- If null, applies to all
    applicable_campaign_types VARCHAR(30)[], -- If null, applies to all
    
    -- Status & Control
    is_active BOOLEAN DEFAULT TRUE,
    auto_apply BOOLEAN DEFAULT FALSE, -- If true, applies automatically
    requires_approval BOOLEAN DEFAULT TRUE,
    
    -- Performance
    times_triggered INTEGER DEFAULT 0,
    times_applied INTEGER DEFAULT 0,
    avg_improvement DECIMAL(5, 4) DEFAULT 0,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- INITIAL DATA POPULATION
-- =============================================================================

-- Major Countries/Markets
INSERT INTO countries (country_code, country_name, continent, primary_language, currency_code, currency_symbol, timezone, market_maturity, daily_ad_budget_usd, market_priority) VALUES
-- Tier 1 Markets (High Priority)
('USA', 'United States', 'North America', 'en', 'USD', '$', 'America/New_York', 'MATURE', 5000.00, 1),
('GBR', 'United Kingdom', 'Europe', 'en', 'GBP', '£', 'Europe/London', 'MATURE', 3000.00, 1),
('DEU', 'Germany', 'Europe', 'de', 'EUR', '€', 'Europe/Berlin', 'MATURE', 3500.00, 1),
('FRA', 'France', 'Europe', 'fr', 'EUR', '€', 'Europe/Paris', 'MATURE', 3000.00, 1),
('JPN', 'Japan', 'Asia', 'ja', 'JPY', '¥', 'Asia/Tokyo', 'MATURE', 4000.00, 1),

-- Tier 2 Markets (Medium Priority)
('TUR', 'Turkey', 'Europe', 'tr', 'TRY', '₺', 'Europe/Istanbul', 'DEVELOPING', 2000.00, 2),
('ESP', 'Spain', 'Europe', 'es', 'EUR', '€', 'Europe/Madrid', 'MATURE', 2500.00, 2),
('ITA', 'Italy', 'Europe', 'it', 'EUR', '€', 'Europe/Rome', 'MATURE', 2500.00, 2),
('CAN', 'Canada', 'North America', 'en', 'CAD', 'C$', 'America/Toronto', 'MATURE', 2500.00, 2),
('AUS', 'Australia', 'Oceania', 'en', 'AUD', 'A$', 'Australia/Sydney', 'MATURE', 2000.00, 2),

-- Tier 3 Markets (Growth Markets)
('BRA', 'Brazil', 'South America', 'pt', 'BRL', 'R$', 'America/Sao_Paulo', 'EMERGING', 1500.00, 3),
('IND', 'India', 'Asia', 'hi', 'INR', '₹', 'Asia/Kolkata', 'EMERGING', 1000.00, 3),
('MEX', 'Mexico', 'North America', 'es', 'MXN', '$', 'America/Mexico_City', 'EMERGING', 1200.00, 3),
('RUS', 'Russia', 'Europe', 'ru', 'RUB', '₽', 'Europe/Moscow', 'DEVELOPING', 1000.00, 3),
('ZAF', 'South Africa', 'Africa', 'en', 'ZAR', 'R', 'Africa/Johannesburg', 'EMERGING', 800.00, 3)
ON CONFLICT (country_code) DO NOTHING;

-- Advertisement Platforms
INSERT INTO ad_platforms (platform_name, platform_code, platform_type, api_endpoint, supports_video, supports_carousel, supports_stories, supports_retargeting, pricing_models, min_daily_budget_usd, available_countries) VALUES
('Facebook', 'FB', 'SOCIAL_MEDIA', 'https://graph.facebook.com/v18.0', TRUE, TRUE, TRUE, TRUE, ARRAY['CPC', 'CPM', 'CPA'], 5.00, ARRAY['USA', 'GBR', 'DEU', 'FRA', 'TUR', 'ESP', 'ITA', 'CAN', 'AUS', 'BRA', 'IND', 'MEX']),
('Instagram', 'IG', 'SOCIAL_MEDIA', 'https://graph.facebook.com/v18.0', TRUE, TRUE, TRUE, TRUE, ARRAY['CPC', 'CPM', 'CPA'], 5.00, ARRAY['USA', 'GBR', 'DEU', 'FRA', 'TUR', 'ESP', 'ITA', 'CAN', 'AUS', 'BRA', 'IND', 'MEX']),
('Google Ads', 'GOOGLE', 'SEARCH_ENGINE', 'https://googleads.googleapis.com/v14', TRUE, FALSE, FALSE, TRUE, ARRAY['CPC', 'CPM', 'CPA', 'CPV'], 10.00, ARRAY['USA', 'GBR', 'DEU', 'FRA', 'TUR', 'ESP', 'ITA', 'CAN', 'AUS', 'BRA', 'IND', 'MEX', 'JPN', 'RUS', 'ZAF']),
('YouTube', 'YT', 'VIDEO', 'https://googleads.googleapis.com/v14', TRUE, FALSE, FALSE, TRUE, ARRAY['CPV', 'CPM'], 10.00, ARRAY['USA', 'GBR', 'DEU', 'FRA', 'TUR', 'ESP', 'ITA', 'CAN', 'AUS', 'BRA', 'IND', 'MEX', 'JPN']),
('TikTok', 'TT', 'SOCIAL_MEDIA', 'https://business-api.tiktok.com/open_api/v1.3', TRUE, FALSE, FALSE, TRUE, ARRAY['CPC', 'CPM', 'CPA'], 20.00, ARRAY['USA', 'GBR', 'DEU', 'FRA', 'ESP', 'ITA', 'CAN', 'AUS', 'BRA', 'MEX']),
('Twitter', 'TW', 'SOCIAL_MEDIA', 'https://ads-api.twitter.com/12', TRUE, FALSE, FALSE, TRUE, ARRAY['CPC', 'CPM', 'CPA'], 5.00, ARRAY['USA', 'GBR', 'DEU', 'FRA', 'TUR', 'ESP', 'ITA', 'CAN', 'AUS', 'BRA', 'IND', 'MEX', 'JPN']),
('LinkedIn', 'LI', 'SOCIAL_MEDIA', 'https://api.linkedin.com/rest/adAccounts', FALSE, TRUE, FALSE, TRUE, ARRAY['CPC', 'CPM', 'CPA'], 10.00, ARRAY['USA', 'GBR', 'DEU', 'FRA', 'TUR', 'ESP', 'ITA', 'CAN', 'AUS', 'BRA', 'IND', 'MEX', 'JPN']),
('Snapchat', 'SC', 'SOCIAL_MEDIA', 'https://adsapi.snapchat.com/v1', TRUE, FALSE, TRUE, TRUE, ARRAY['CPC', 'CPM', 'CPA'], 5.00, ARRAY['USA', 'GBR', 'DEU', 'FRA', 'ESP', 'CAN', 'AUS'])
ON CONFLICT (platform_code) DO NOTHING;

-- AI Optimization Rules
INSERT INTO ai_optimization_rules (rule_name, rule_description, rule_type, condition_metric, condition_operator, condition_value, action_type, action_value, auto_apply) VALUES
('Low CTR Pause Rule', 'Pause ads with CTR below 1%', 'CREATIVE_OPTIMIZATION', 'ctr', '<', 1.0, 'PAUSE_AD', 0, FALSE),
('High ROAS Budget Increase', 'Increase budget for ads with ROAS > 3x', 'BUDGET_OPTIMIZATION', 'roas', '>', 3.0, 'INCREASE_BUDGET', 20.0, TRUE),
('High CPC Decrease Bid', 'Decrease bid when CPC is too high', 'BID_OPTIMIZATION', 'cpc', '>', 2.0, 'DECREASE_BID', 10.0, TRUE),
('Low Conversion Rate Optimization', 'Optimize audience for low conversion rates', 'AUDIENCE_OPTIMIZATION', 'conversion_rate', '<', 2.0, 'CHANGE_AUDIENCE', 0, FALSE)
ON CONFLICT (rule_name) DO NOTHING;

-- =============================================================================
-- VIEWS FOR ANALYTICS AND REPORTING
-- =============================================================================

-- Global Campaign Performance View
CREATE OR REPLACE VIEW global_campaign_performance AS
SELECT 
    gc.id,
    gc.campaign_name,
    gc.campaign_type,
    gc.status,
    COUNT(DISTINCT lc.country_id) as countries_count,
    COUNT(DISTINCT lc.platform_id) as platforms_count,
    SUM(lc.impressions) as total_impressions,
    SUM(lc.clicks) as total_clicks,
    SUM(lc.conversions) as total_conversions,
    SUM(lc.spend_usd) as total_spend_usd,
    CASE 
        WHEN SUM(lc.impressions) > 0 THEN (SUM(lc.clicks)::DECIMAL / SUM(lc.impressions) * 100)
        ELSE 0 
    END as avg_ctr,
    CASE 
        WHEN SUM(lc.clicks) > 0 THEN (SUM(lc.spend_usd) / SUM(lc.clicks))
        ELSE 0 
    END as avg_cpc_usd,
    CASE 
        WHEN SUM(lc.spend_usd) > 0 THEN (SUM(lc.conversions) * 100.0 / SUM(lc.spend_usd))
        ELSE 0 
    END as roas
FROM global_campaigns gc
LEFT JOIN localized_campaigns lc ON gc.id = lc.global_campaign_id
GROUP BY gc.id, gc.campaign_name, gc.campaign_type, gc.status;

-- Country Performance Summary
CREATE OR REPLACE VIEW country_performance_summary AS
SELECT 
    c.country_code,
    c.country_name,
    c.currency_code,
    COUNT(DISTINCT lc.id) as active_campaigns,
    COUNT(DISTINCT lc.platform_id) as active_platforms,
    SUM(lc.impressions) as total_impressions,
    SUM(lc.clicks) as total_clicks,
    SUM(lc.conversions) as total_conversions,
    SUM(lc.spend_local) as total_spend_local,
    SUM(lc.spend_usd) as total_spend_usd,
    AVG(lc.ctr) as avg_ctr,
    AVG(lc.cpc_local) as avg_cpc_local,
    AVG(lc.conversion_rate) as avg_conversion_rate
FROM countries c
LEFT JOIN localized_campaigns lc ON c.id = lc.country_id AND lc.status = 'ACTIVE'
GROUP BY c.id, c.country_code, c.country_name, c.currency_code
ORDER BY total_spend_usd DESC;

-- Platform Performance Summary
CREATE OR REPLACE VIEW platform_performance_summary AS
SELECT 
    ap.platform_name,
    ap.platform_code,
    ap.platform_type,
    COUNT(DISTINCT lc.country_id) as countries_count,
    COUNT(DISTINCT lc.id) as active_campaigns,
    SUM(lc.impressions) as total_impressions,
    SUM(lc.clicks) as total_clicks,
    SUM(lc.conversions) as total_conversions,
    SUM(lc.spend_usd) as total_spend_usd,
    AVG(lc.ctr) as avg_ctr,
    AVG(lc.cpc_local) as avg_cpc,
    AVG(lc.roas) as avg_roas
FROM ad_platforms ap
LEFT JOIN localized_campaigns lc ON ap.id = lc.platform_id AND lc.status = 'ACTIVE'
GROUP BY ap.id, ap.platform_name, ap.platform_code, ap.platform_type
ORDER BY total_spend_usd DESC;

COMMIT;