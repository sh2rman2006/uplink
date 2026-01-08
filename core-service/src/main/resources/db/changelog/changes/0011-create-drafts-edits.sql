-- Черновики сообщений
CREATE TABLE uplink.message_draft (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chat_id UUID NOT NULL REFERENCES uplink.chat(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES uplink.user_profile(id) ON DELETE CASCADE,
    
    draft_text TEXT,
    attachments JSONB,
    reply_to_message_id UUID REFERENCES uplink.message(id),
    
    last_edited_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(chat_id, user_id),
    CONSTRAINT draft_text_length CHECK (char_length(draft_text) <= 10000)
);

-- История редактирования сообщений
CREATE TABLE uplink.message_edit_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES uplink.message(id) ON DELETE CASCADE,
    
    old_text TEXT,
    new_text TEXT,
    edited_by UUID NOT NULL REFERENCES uplink.user_profile(id),
    edited_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT old_text_length CHECK (char_length(old_text) <= 10000),
    CONSTRAINT new_text_length CHECK (char_length(new_text) <= 10000)
);

CREATE INDEX idx_message_edit_history_message_id ON uplink.message_edit_history(message_id);
CREATE INDEX idx_message_edit_history_edited_at ON uplink.message_edit_history(edited_at DESC);