-- Представление для получения списка чатов пользователя с последним сообщением
CREATE OR REPLACE VIEW uplink.v_user_chats AS
SELECT 
    cp.chat_id,
    cp.user_id,
    c.type AS chat_type,
    c.title,
    c.avatar_url,
    c.last_message_at,
    m.text AS last_message_text,
    m.sender_id AS last_message_sender_id,
    m.type AS last_message_type,
    u.username AS last_message_sender_name,
    m.created_at AS last_message_created_at,
    
    -- Количество непрочитанных сообщений
    (SELECT COUNT(*) 
     FROM uplink.message m2 
     WHERE m2.chat_id = cp.chat_id 
       AND m2.created_at > COALESCE(cp.last_read_at, '1970-01-01'::timestamptz)
       AND m2.sender_id != cp.user_id
       AND m2.deleted_at IS NULL) AS unread_count,
    
    -- Участники (для групповых чатов)
    (SELECT JSON_AGG(JSON_BUILD_OBJECT(
        'id', u2.id,
        'username', u2.username,
        'avatar_url', u2.avatar_url
     ))
     FROM uplink.chat_participant cp2
     JOIN uplink.user_profile u2 ON u2.id = cp2.user_id
     WHERE cp2.chat_id = cp.chat_id
       AND cp2.status = 'ACTIVE'
       AND cp2.user_id != cp.user_id
     LIMIT 5) AS other_participants,
    
    cp.role,
    cp.joined_at,
    c.created_at AS chat_created_at,
    cs.is_pinned,
    cs.notification_setting
    
FROM uplink.chat_participant cp
JOIN uplink.chat c ON c.id = cp.chat_id
LEFT JOIN uplink.message m ON m.id = c.last_message_id
LEFT JOIN uplink.user_profile u ON u.id = m.sender_id
LEFT JOIN uplink.chat_user_settings cs ON cs.chat_id = cp.chat_id AND cs.user_id = cp.user_id
WHERE cp.status = 'ACTIVE'
  AND c.deleted_at IS NULL;

-- Представление для статистики чатов
CREATE OR REPLACE VIEW uplink.v_chat_statistics AS
SELECT 
    c.id AS chat_id,
    c.type AS chat_type,
    c.title,
    COUNT(DISTINCT cp.user_id) AS member_count,
    COUNT(m.id) AS total_messages,
    COUNT(DISTINCT m.sender_id) AS active_senders,
    MIN(m.created_at) AS first_message_at,
    MAX(m.created_at) AS last_message_at,
    AVG(CASE WHEN m.type = 'TEXT' THEN char_length(m.text) END) AS avg_message_length,
    COUNT(DISTINCT a.id) AS total_attachments,
    COUNT(DISTINCT r.id) AS total_reactions
FROM uplink.chat c
LEFT JOIN uplink.chat_participant cp ON cp.chat_id = c.id AND cp.status = 'ACTIVE'
LEFT JOIN uplink.message m ON m.chat_id = c.id AND m.deleted_at IS NULL
LEFT JOIN uplink.attachment a ON a.message_id = m.id
LEFT JOIN uplink.reaction r ON r.message_id = m.id
GROUP BY c.id, c.type, c.title;

-- Представление для статистики пользователя
CREATE OR REPLACE VIEW uplink.v_user_statistics AS
SELECT 
    u.id AS user_id,
    u.username,
    u.created_at AS registered_at,
    u.last_seen_at,
    
    COUNT(DISTINCT cp.chat_id) AS total_chats,
    COUNT(DISTINCT CASE WHEN c.type = 'PRIVATE' THEN cp.chat_id END) AS private_chats,
    COUNT(DISTINCT CASE WHEN c.type = 'GROUP' THEN cp.chat_id END) AS group_chats,
    
    COUNT(DISTINCT m.id) AS total_messages_sent,
    COUNT(DISTINCT CASE WHEN m.type = 'TEXT' THEN m.id END) AS text_messages,
    COUNT(DISTINCT CASE WHEN m.type = 'IMAGE' THEN m.id END) AS image_messages,
    COUNT(DISTINCT CASE WHEN m.type = 'VIDEO' THEN m.id END) AS video_messages,
    
    MAX(m.created_at) AS last_message_at,
    AVG(char_length(m.text)) AS avg_message_length,
    
    COUNT(DISTINCT r.id) AS total_reactions_given,
    (SELECT COUNT(DISTINCT r2.id) 
     FROM uplink.reaction r2
     JOIN uplink.message m2 ON m2.id = r2.message_id
     WHERE m2.sender_id = u.id) AS total_reactions_received
    
FROM uplink.user_profile u
LEFT JOIN uplink.chat_participant cp ON cp.user_id = u.id AND cp.status = 'ACTIVE'
LEFT JOIN uplink.chat c ON c.id = cp.chat_id
LEFT JOIN uplink.message m ON m.sender_id = u.id AND m.deleted_at IS NULL
LEFT JOIN uplink.reaction r ON r.user_id = u.id
GROUP BY u.id, u.username, u.created_at, u.last_seen_at;