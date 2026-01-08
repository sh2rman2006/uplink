-- Контакты пользователя
CREATE TABLE uplink.contact (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES uplink.user_profile(id) ON DELETE CASCADE,
    contact_user_id UUID NOT NULL REFERENCES uplink.user_profile(id) ON DELETE CASCADE,
    
    name_in_contacts VARCHAR(255),
    phone_numbers TEXT[],
    emails TEXT[],
    note TEXT,
    
    tags TEXT[],
    is_favorite BOOLEAN NOT NULL DEFAULT false,
    is_hidden BOOLEAN NOT NULL DEFAULT false,
    
    total_messages INTEGER NOT NULL DEFAULT 0,
    last_message_at TIMESTAMPTZ,
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(user_id, contact_user_id),
    CONSTRAINT note_length CHECK (char_length(note) <= 1000)
);

-- Блокировки пользователей
CREATE TABLE uplink.user_block (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    blocker_id UUID NOT NULL REFERENCES uplink.user_profile(id) ON DELETE CASCADE,
    blocked_id UUID NOT NULL REFERENCES uplink.user_profile(id) ON DELETE CASCADE,
    
    reason TEXT,
    blocked_until TIMESTAMPTZ,
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(blocker_id, blocked_id),
    CONSTRAINT reason_length CHECK (char_length(reason) <= 1000)
);

-- Жалобы на пользователей/сообщения
CREATE TABLE uplink.report (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID NOT NULL REFERENCES uplink.user_profile(id) ON DELETE CASCADE,
    
    reported_user_id UUID REFERENCES uplink.user_profile(id),
    reported_message_id UUID REFERENCES uplink.message(id),
    reported_chat_id UUID REFERENCES uplink.chat(id),
    
    report_type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    evidence JSONB,
    
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    assigned_to UUID REFERENCES uplink.user_profile(id),
    resolution TEXT,
    resolved_at TIMESTAMPTZ,
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT description_length CHECK (char_length(description) <= 5000)
);

CREATE INDEX idx_contact_user_id ON uplink.contact(user_id);
CREATE INDEX idx_user_block_blocker_id ON uplink.user_block(blocker_id);
CREATE INDEX idx_report_status ON uplink.report(status);