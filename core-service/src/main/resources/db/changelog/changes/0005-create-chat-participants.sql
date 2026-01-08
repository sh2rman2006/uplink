-- Таблица участников чата
CREATE TABLE uplink.chat_participant (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id UUID NOT NULL REFERENCES uplink.chat(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES uplink.user_profile(id) ON DELETE CASCADE,
    
    -- Роль и статус
    role uplink.chat_role NOT NULL DEFAULT 'MEMBER',
    status uplink.participant_status NOT NULL DEFAULT 'ACTIVE',
    
    -- Разрешения (детализированные, переопределяют настройки чата)
    can_send_messages BOOLEAN NOT NULL DEFAULT true,
    can_send_media BOOLEAN NOT NULL DEFAULT true,
    can_add_users BOOLEAN NOT NULL DEFAULT true,
    can_pin_messages BOOLEAN NOT NULL DEFAULT true,
    can_change_info BOOLEAN NOT NULL DEFAULT true,
    can_delete_messages BOOLEAN NOT NULL DEFAULT true,
    can_ban_users BOOLEAN NOT NULL DEFAULT true,
    
    -- Статистика и метаданные
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    left_at TIMESTAMPTZ,
    invited_by UUID REFERENCES uplink.user_profile(id),
    banned_by UUID REFERENCES uplink.user_profile(id),
    banned_until TIMESTAMPTZ,
    ban_reason TEXT,
    
    -- Последнее прочитанное сообщение
    last_read_message_id UUID,
    last_read_at TIMESTAMPTZ,
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(chat_id, user_id),
    CONSTRAINT ban_reason_length CHECK (char_length(ban_reason) <= 500)
);

-- Индексы
CREATE INDEX idx_chat_participant_chat_id ON uplink.chat_participant(chat_id);
CREATE INDEX idx_chat_participant_user_id ON uplink.chat_participant(user_id);
CREATE INDEX idx_chat_participant_role ON uplink.chat_participant(role);
CREATE INDEX idx_chat_participant_status ON uplink.chat_participant(status);
CREATE INDEX idx_chat_participant_banned_until ON uplink.chat_participant(banned_until) WHERE banned_until IS NOT NULL;

-- Таблица для запросов на вступление в чат
CREATE TABLE uplink.chat_join_request (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id UUID NOT NULL REFERENCES uplink.chat(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES uplink.user_profile(id) ON DELETE CASCADE,
    message TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    reviewed_by UUID REFERENCES uplink.user_profile(id),
    reviewed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(chat_id, user_id),
    CONSTRAINT join_message_length CHECK (char_length(message) <= 500)
);

CREATE INDEX idx_chat_join_request_status ON uplink.chat_join_request(status);
CREATE INDEX idx_chat_join_request_chat_id ON uplink.chat_join_request(chat_id);