package tech.sh2rman.coreservice.websocket.dto;

public enum WsEventType {
    MESSAGE_CREATED,
    MESSAGE_EDITED,
    MESSAGE_DELETED,

    MESSAGE_DELIVERED,
    MESSAGE_READ,

    USER_ONLINE,
    USER_OFFLINE,
    USER_TYPING,
    USER_STOP_TYPING
}