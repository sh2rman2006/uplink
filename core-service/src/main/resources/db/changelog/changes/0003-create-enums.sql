-- Типы чатов
CREATE TYPE uplink.chat_type AS ENUM (
    'PRIVATE',
    'GROUP',
    'CHANNEL',
    'SECRET'
);

-- Типы сообщений
CREATE TYPE uplink.message_type AS ENUM (
    'TEXT',
    'IMAGE',
    'VIDEO',
    'AUDIO',
    'VOICE',
    'FILE',
    'POLL',
    'LOCATION',
    'CONTACT',
    'STICKER',
    'SYSTEM'
);

-- Статусы сообщений
CREATE TYPE uplink.message_status AS ENUM (
    'SENDING',
    'SENT',
    'DELIVERED',
    'READ',
    'FAILED',
    'EDITED',
    'DELETED'
);

-- Роли в чатах
CREATE TYPE uplink.chat_role AS ENUM (
    'OWNER',
    'ADMIN',
    'MEMBER',
    'READER',
    'BANNED'
);

-- Типы вложений
CREATE TYPE uplink.attachment_type AS ENUM (
    'IMAGE',
    'VIDEO',
    'AUDIO',
    'VOICE_MESSAGE',
    'DOCUMENT',
    'ARCHIVE',
    'OTHER'
);

-- Статусы участников
CREATE TYPE uplink.participant_status AS ENUM (
    'ACTIVE',
    'MUTED',
    'LEFT',
    'KICKED',
    'BANNED'
);

-- Уровни разрешений
CREATE TYPE uplink.permission_level AS ENUM (
    'NONE',
    'READ',
    'WRITE',
    'MANAGE',
    'ADMIN'
);

-- Настройки уведомлений
CREATE TYPE uplink.notification_setting AS ENUM (
    'ALL',
    'MENTIONS_ONLY',
    'NONE'
);