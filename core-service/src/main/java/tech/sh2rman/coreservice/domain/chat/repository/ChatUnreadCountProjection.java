package tech.sh2rman.coreservice.domain.chat.repository;


import java.util.UUID;

public interface ChatUnreadCountProjection {
    UUID getChatId();
    long getUnread();
}
