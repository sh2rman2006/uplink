-- Таблица вложений
CREATE TABLE uplink.attachment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    message_id UUID NOT NULL REFERENCES uplink.message(id) ON DELETE CASCADE,
    
    -- Тип и метаданные файла
    type uplink.attachment_type NOT NULL,
    mime_type VARCHAR(255) NOT NULL,
    file_name VARCHAR(512),
    file_size BIGINT NOT NULL,
    
    -- Хранение в MinIO/S3
    object_key VARCHAR(1024) NOT NULL UNIQUE,
    bucket_name VARCHAR(255) NOT NULL DEFAULT 'uplink-media',
    
    -- URL для доступа
    url VARCHAR(2048),
    thumbnail_url VARCHAR(2048),
    preview_url VARCHAR(2048),
    
    -- Для медиафайлов
    duration INTEGER,
    width INTEGER,
    height INTEGER,
    
    -- Для изображений
    has_thumbnail BOOLEAN NOT NULL DEFAULT false,
    thumbnail_width INTEGER,
    thumbnail_height INTEGER,
    
    -- Для документов
    page_count INTEGER,
    
    -- Для голосовых сообщений
    waveform JSONB,
    
    -- Статус загрузки
    upload_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    upload_progress INTEGER NOT NULL DEFAULT 0,
    uploaded_at TIMESTAMPTZ,
    
    -- Метаданные
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    CONSTRAINT file_size_positive CHECK (file_size > 0),
    CONSTRAINT upload_progress_range CHECK (upload_progress >= 0 AND upload_progress <= 100)
);

-- Индексы
CREATE INDEX idx_attachment_message_id ON uplink.attachment(message_id);
CREATE INDEX idx_attachment_type ON uplink.attachment(type);
CREATE INDEX idx_attachment_upload_status ON uplink.attachment(upload_status);
CREATE INDEX idx_attachment_created_at ON uplink.attachment(created_at DESC);

-- Таблица для миниатюр
CREATE TABLE uplink.attachment_thumbnail (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attachment_id UUID NOT NULL REFERENCES uplink.attachment(id) ON DELETE CASCADE,
    size VARCHAR(20) NOT NULL,
    width INTEGER NOT NULL,
    height INTEGER NOT NULL,
    object_key VARCHAR(1024) NOT NULL,
    url VARCHAR(2048),
    file_size BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(attachment_id, size)
);

-- Таблица для подписей к вложениям
CREATE TABLE uplink.attachment_caption (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attachment_id UUID NOT NULL REFERENCES uplink.attachment(id) ON DELETE CASCADE,
    caption TEXT,
    language VARCHAR(10) NOT NULL DEFAULT 'en',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    
    UNIQUE(attachment_id, language),
    CONSTRAINT caption_length CHECK (char_length(caption) <= 1000)
);