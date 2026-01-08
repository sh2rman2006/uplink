-- Таблица сообщений
CREATE TABLE uplink.message (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id UUID NOT NULL REFERENCES uplink.chat(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES uplink.user_profile(id),
    
    -- Основное содержимое
    type uplink.message_type NOT NULL DEFAULT 'TEXT',
    text TEXT,
    
    -- Ссылка на родительское сообщение (reply)
    reply_to_message_id UUID REFERENCES uplink.message(id),
    
    -- Для пересланных сообщений
    is_forwarded BOOLEAN NOT NULL DEFAULT false,
    original_sender_id UUID REFERENCES uplink.user_profile(id),
    original_chat_id UUID,
    original_message_id UUID,
    
    -- Для системных сообщений
    system_action VARCHAR(100),
    system_parameters JSONB,
    
    -- Статус
    status uplink.message_status NOT NULL DEFAULT 'SENT',
    error_message TEXT,
    
    -- Метаданные
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMPTZ,
    deleted_by UUID REFERENCES uplink.user_profile(id),
    
    -- Для редактирования
    edit_count INTEGER NOT NULL DEFAULT 0,
    last_edited_at TIMESTAMPTZ,
    
    -- Для голосовых сообщений
    voice_duration INTEGER,
    
    -- Для местоположения
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6),
    location_name VARCHAR(255),
    
    -- Для контактов
    contact_user_id UUID REFERENCES uplink.user_profile(id),
    contact_name VARCHAR(255),
    
    -- Индексы и ограничения
    CONSTRAINT text_length CHECK (char_length(text) <= 10000)
);

-- Индексы для сообщений
CREATE INDEX idx_message_chat_id ON uplink.message(chat_id);
CREATE INDEX idx_message_sender_id ON uplink.message(sender_id);
CREATE INDEX idx_message_created_at ON uplink.message(created_at DESC);
CREATE INDEX idx_message_reply_to ON uplink.message(reply_to_message_id) WHERE reply_to_message_id IS NOT NULL;
CREATE INDEX idx_message_is_forwarded ON uplink.message(is_forwarded) WHERE is_forwarded = true;
CREATE INDEX idx_message_status ON uplink.message(status);
CREATE INDEX idx_message_type ON uplink.message(type);
CREATE INDEX idx_message_deleted_at ON uplink.message(deleted_at) WHERE deleted_at IS NOT NULL;
CREATE INDEX idx_message_chat_created ON uplink.message(chat_id, created_at DESC);

-- Таблица для хранения, кто прочитал сообщение
CREATE TABLE uplink.message_read_receipt (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES uplink.message(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES uplink.user_profile(id) ON DELETE CASCADE,
    read_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(message_id, user_id)
);

CREATE INDEX idx_message_read_receipt_user_id ON uplink.message_read_receipt(user_id);
CREATE INDEX idx_message_read_receipt_read_at ON uplink.message_read_receipt(read_at DESC);