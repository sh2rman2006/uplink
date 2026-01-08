-- Сессии пользователя
CREATE TABLE uplink.user_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES uplink.user_profile(id) ON DELETE CASCADE,
    
    device_id VARCHAR(255) NOT NULL,
    device_name VARCHAR(255),
    device_type VARCHAR(50) NOT NULL,
    device_platform VARCHAR(50),
    user_agent TEXT,
    
    ip_address INET,
    country_code VARCHAR(2),
    city VARCHAR(100),
    
    is_active BOOLEAN NOT NULL DEFAULT true,
    last_activity_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMPTZ,
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(user_id, device_id)
);

-- Лог активности пользователя
CREATE TABLE uplink.user_activity (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES uplink.user_profile(id) ON DELETE CASCADE,
    
    activity_type VARCHAR(100) NOT NULL,
    activity_data JSONB,
    
    chat_id UUID REFERENCES uplink.chat(id),
    message_id UUID REFERENCES uplink.message(id),
    device_id VARCHAR(255),
    ip_address INET,
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_session_user_id ON uplink.user_session(user_id);
CREATE INDEX idx_user_session_is_active ON uplink.user_session(is_active) WHERE is_active = true;
CREATE INDEX idx_user_activity_user_id ON uplink.user_activity(user_id);
CREATE INDEX idx_user_activity_created_at ON uplink.user_activity(created_at DESC);