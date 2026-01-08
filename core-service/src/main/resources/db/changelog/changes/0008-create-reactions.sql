-- Таблица реакций на сообщения
CREATE TABLE uplink.reaction (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES uplink.message(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES uplink.user_profile(id) ON DELETE CASCADE,
    
    -- Реакция
    emoji VARCHAR(100) NOT NULL,
    
    -- Метаданные
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(message_id, user_id, emoji)
);

-- Индексы
CREATE INDEX idx_reaction_message_id ON uplink.reaction(message_id);
CREATE INDEX idx_reaction_user_id ON uplink.reaction(user_id);
CREATE INDEX idx_reaction_emoji ON uplink.reaction(emoji);
CREATE INDEX idx_reaction_created_at ON uplink.reaction(created_at DESC);

-- Таблица кастомных эмодзи
CREATE TABLE uplink.custom_emoji (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id UUID REFERENCES uplink.chat(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    object_key VARCHAR(1024) NOT NULL,
    url VARCHAR(2048),
    mime_type VARCHAR(50) NOT NULL,
    created_by UUID NOT NULL REFERENCES uplink.user_profile(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(chat_id, name)
);

-- Таблица для статистики реакций
CREATE TABLE uplink.message_reaction_summary (
    message_id UUID PRIMARY KEY REFERENCES uplink.message(id) ON DELETE CASCADE,
    reactions JSONB NOT NULL DEFAULT '{}',
    total_count INTEGER NOT NULL DEFAULT 0,
    last_reacted_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);