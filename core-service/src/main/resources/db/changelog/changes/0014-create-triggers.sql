-- Функция для обновления updated_at
CREATE OR REPLACE FUNCTION uplink.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Применяем триггеры ко всем основным таблицам
CREATE TRIGGER update_user_profile_updated_at 
    BEFORE UPDATE ON uplink.user_profile 
    FOR EACH ROW EXECUTE FUNCTION uplink.update_updated_at_column();

CREATE TRIGGER update_chat_updated_at 
    BEFORE UPDATE ON uplink.chat 
    FOR EACH ROW EXECUTE FUNCTION uplink.update_updated_at_column();

CREATE TRIGGER update_chat_participant_updated_at 
    BEFORE UPDATE ON uplink.chat_participant 
    FOR EACH ROW EXECUTE FUNCTION uplink.update_updated_at_column();

CREATE TRIGGER update_message_updated_at 
    BEFORE UPDATE ON uplink.message 
    FOR EACH ROW EXECUTE FUNCTION uplink.update_updated_at_column();

CREATE TRIGGER update_attachment_updated_at 
    BEFORE UPDATE ON uplink.attachment 
    FOR EACH ROW EXECUTE FUNCTION uplink.update_updated_at_column();

CREATE TRIGGER update_poll_updated_at 
    BEFORE UPDATE ON uplink.poll 
    FOR EACH ROW EXECUTE FUNCTION uplink.update_updated_at_column();

-- Триггер для обновления last_message_at в чате
CREATE OR REPLACE FUNCTION uplink.update_chat_last_message()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE uplink.chat 
    SET last_message_at = NEW.created_at,
        last_message_id = NEW.id,
        updated_at = NOW()
    WHERE id = NEW.chat_id;
    
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_chat_on_new_message
    AFTER INSERT ON uplink.message
    FOR EACH ROW EXECUTE FUNCTION uplink.update_chat_last_message();

-- Триггер для обновления счетчика реакций
CREATE OR REPLACE FUNCTION uplink.update_reaction_summary()
RETURNS TRIGGER AS $$
BEGIN
    -- Обновляем сводку реакций при изменении реакций
    INSERT INTO uplink.message_reaction_summary (message_id, reactions, total_count, last_reacted_at, updated_at)
    SELECT 
        r.message_id,
        jsonb_object_agg(r.emoji, COUNT(*)::int) as reactions,
        COUNT(*) as total_count,
        MAX(r.created_at) as last_reacted_at,
        NOW() as updated_at
    FROM uplink.reaction r
    WHERE r.message_id = NEW.message_id
    GROUP BY r.message_id
    ON CONFLICT (message_id) DO UPDATE SET
        reactions = EXCLUDED.reactions,
        total_count = EXCLUDED.total_count,
        last_reacted_at = EXCLUDED.last_reacted_at,
        updated_at = NOW();
    
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_reaction_summary_trigger
    AFTER INSERT OR DELETE OR UPDATE ON uplink.reaction
    FOR EACH ROW EXECUTE FUNCTION uplink.update_reaction_summary();