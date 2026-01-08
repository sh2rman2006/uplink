-- Таблица чатов
CREATE TABLE uplink.chat (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    type uplink.chat_type NOT NULL,
    title VARCHAR(255),
    description TEXT,
    avatar_object_key VARCHAR(512),
    avatar_url VARCHAR(1024),
    
    -- Настройки видимости
    is_private BOOLEAN NOT NULL DEFAULT false,
    is_public BOOLEAN NOT NULL DEFAULT false,
    
    -- Инвайт-ссылка
    invite_link VARCHAR(255) UNIQUE,
    invite_link_expires_at TIMESTAMPTZ,
    
    -- Настройки разрешений (для групповых чатов)
    allow_send_messages BOOLEAN NOT NULL DEFAULT true,
    allow_send_media BOOLEAN NOT NULL DEFAULT true,
    allow_add_users BOOLEAN NOT NULL DEFAULT true,
    allow_pin_messages BOOLEAN NOT NULL DEFAULT true,
    allow_change_info BOOLEAN NOT NULL DEFAULT true,
    
    -- Для секретных чатов
    is_encrypted BOOLEAN NOT NULL DEFAULT false,
    
    -- Метаданные
    created_by UUID REFERENCES uplink.user_profile(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_message_at TIMESTAMPTZ,
    last_message_id UUID,
    
    -- Индексы и ограничения
    CONSTRAINT chat_title_length CHECK (char_length(title) <= 255),
    CONSTRAINT chat_description_length CHECK (char_length(description) <= 2000)
);

-- Индексы для чатов
CREATE INDEX idx_chat_type ON uplink.chat(type);
CREATE INDEX idx_chat_created_by ON uplink.chat(created_by);
CREATE INDEX idx_chat_last_message_at ON uplink.chat(last_message_at DESC NULLS LAST);
CREATE INDEX idx_chat_updated_at ON uplink.chat(updated_at DESC);
CREATE INDEX idx_chat_is_public ON uplink.chat(is_public) WHERE is_public = true;
CREATE INDEX idx_chat_is_private ON uplink.chat(is_private) WHERE is_private = true;

-- Таблица настроек чата для пользователя (ИСПРАВЛЕНО)
CREATE TABLE uplink.chat_user_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id UUID NOT NULL REFERENCES uplink.chat(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES uplink.user_profile(id) ON DELETE CASCADE,
    
    -- Настройки уведомлений
    notification_setting uplink.notification_setting NOT NULL DEFAULT 'ALL',
    mute_until TIMESTAMPTZ,
    is_pinned BOOLEAN NOT NULL DEFAULT false,
    is_archived BOOLEAN NOT NULL DEFAULT false,
    
    -- Настройки отображения
    hide_media BOOLEAN NOT NULL DEFAULT false,
    hide_stickers BOOLEAN NOT NULL DEFAULT false,
    hide_links_preview BOOLEAN NOT NULL DEFAULT false,
    
    -- Псевдоним пользователя в этом чате
    custom_nickname VARCHAR(64),
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(chat_id, user_id),
    CONSTRAINT custom_nickname_length CHECK (char_length(custom_nickname) <= 64)
);

CREATE INDEX idx_chat_user_settings_user_id ON uplink.chat_user_settings(user_id);
CREATE INDEX idx_chat_user_settings_pinned ON uplink.chat_user_settings(is_pinned) WHERE is_pinned = true;
CREATE INDEX idx_chat_user_settings_archived ON uplink.chat_user_settings(is_archived) WHERE is_archived = true;