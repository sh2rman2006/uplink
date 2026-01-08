-- Закрепленные сообщения в чате
CREATE TABLE uplink.pinned_message (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id UUID NOT NULL REFERENCES uplink.chat(id) ON DELETE CASCADE,
    message_id UUID NOT NULL REFERENCES uplink.message(id) ON DELETE CASCADE,
    pinned_by UUID NOT NULL REFERENCES uplink.user_profile(id),
    pinned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    unpinned_at TIMESTAMPTZ,
    pin_order INTEGER NOT NULL DEFAULT 0,
    note TEXT,
    
    UNIQUE(chat_id, message_id) WHERE unpinned_at IS NULL,
    CONSTRAINT note_length CHECK (char_length(note) <= 500)
);

CREATE INDEX idx_pinned_message_chat_id ON uplink.pinned_message(chat_id);
CREATE INDEX idx_pinned_message_pinned_at ON uplink.pinned_message(pinned_at DESC);

-- Избранные сообщения пользователя
CREATE TABLE uplink.starred_message (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES uplink.user_profile(id) ON DELETE CASCADE,
    message_id UUID NOT NULL REFERENCES uplink.message(id) ON DELETE CASCADE,
    starred_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    folder VARCHAR(100),
    tags TEXT[],
    note TEXT,
    
    UNIQUE(user_id, message_id),
    CONSTRAINT note_length CHECK (char_length(note) <= 1000)
);

CREATE INDEX idx_starred_message_user_id ON uplink.starred_message(user_id);
CREATE INDEX idx_starred_message_starred_at ON uplink.starred_message(starred_at DESC);

-- Папки для избранного
CREATE TABLE uplink.starred_folder (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES uplink.user_profile(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(7),
    icon VARCHAR(50),
    parent_folder_id UUID REFERENCES uplink.starred_folder(id),
    folder_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(user_id, name)
);