package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
public class UserFeedEvent {
    private int eventId;
    private int userId;
    private String eventType;
    private String operation;
    private int entityId;
    private long timestamp;
}