-- Таблица опросов
CREATE TABLE uplink.poll (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES uplink.message(id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    is_anonymous BOOLEAN NOT NULL DEFAULT true,
    is_multiple_choice BOOLEAN NOT NULL DEFAULT false,
    is_quiz BOOLEAN NOT NULL DEFAULT false,
    correct_option_id INTEGER,
    
    -- Настройки
    allows_add_options BOOLEAN NOT NULL DEFAULT false,
    close_date TIMESTAMPTZ,
    is_closed BOOLEAN NOT NULL DEFAULT false,
    
    -- Статистика
    total_voters INTEGER NOT NULL DEFAULT 0,
    
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT question_length CHECK (char_length(question) <= 1000)
);

-- Варианты ответов в опросе
CREATE TABLE uplink.poll_option (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    poll_id UUID NOT NULL REFERENCES uplink.poll(id) ON DELETE CASCADE,
    option_text TEXT NOT NULL,
    option_order INTEGER NOT NULL,
    voter_count INTEGER NOT NULL DEFAULT 0,
    
    UNIQUE(poll_id, option_order),
    CONSTRAINT option_text_length CHECK (char_length(option_text) <= 200)
);

-- Голоса в опросе
CREATE TABLE uplink.poll_vote (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    poll_id UUID NOT NULL REFERENCES uplink.poll(id) ON DELETE CASCADE,
    option_id UUID NOT NULL REFERENCES uplink.poll_option(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES uplink.user_profile(id) ON DELETE CASCADE,
    voted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(poll_id, user_id, option_id)
);

CREATE INDEX idx_poll_vote_poll_id ON uplink.poll_vote(poll_id);
CREATE INDEX idx_poll_vote_user_id ON uplink.poll_vote(user_id);