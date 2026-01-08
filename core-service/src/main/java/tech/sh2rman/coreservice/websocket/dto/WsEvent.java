package tech.sh2rman.coreservice.websocket.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WsEvent<T>(
        WsEventType type,
        long serverTs,
        T payload
) {
    public static <T> WsEvent<T> of(WsEventType type, T payload) {
        return new WsEvent<>(type, System.currentTimeMillis(), payload);
    }
}