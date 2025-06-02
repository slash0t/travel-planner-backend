package ru.putevod.app.planner.mapper;

import org.junit.jupiter.api.Test;
import ru.putevod.app.planner.dto.EventReminderDto;
import ru.putevod.app.planner.model.Event;
import ru.putevod.app.planner.model.EventReminder;
import ru.putevod.app.planner.model.User;

import static org.junit.jupiter.api.Assertions.*;

class EventReminderMapperImplTest {

    private final EventReminderMapperImpl mapper = new EventReminderMapperImpl();

    @Test
    void testToDto() {
        EventReminder eventReminder = new EventReminder();
        eventReminder.setReminderId(1L);
        eventReminder.setRemindAt(null);
        eventReminder.setMinutesBefore(10);
        eventReminder.setSent(false);

        EventReminderDto dto = mapper.toDto(eventReminder);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals(10, dto.getMinutesBefore());
        assertFalse(dto.isSent());
    }

    @Test
    void testToEntity() {
        EventReminderDto dto = new EventReminderDto();
        dto.setId(1L);
        dto.setRemindAt(null);
        dto.setMinutesBefore(10);
        dto.setSent(false);

        EventReminder entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals(1L, entity.getReminderId());
        assertEquals(10, entity.getMinutesBefore());
        assertFalse(entity.isSent());
    }

    @Test
    void testFromDto() {
        EventReminderDto dto = new EventReminderDto();
        dto.setId(1L);
        dto.setRemindAt(null);
        dto.setMinutesBefore(10);
        dto.setSent(false);

        Event event = new Event();
        event.setEventId(2L);

        User user = new User();
        user.setUserId(3L);

        EventReminder entity = mapper.fromDto(dto, event, user);

        assertNotNull(entity);
        assertEquals(1L, entity.getReminderId());
        assertEquals(10, entity.getMinutesBefore());
        assertFalse(entity.isSent());
        assertEquals(2L, entity.getEvent().getEventId());
        assertEquals(3L, entity.getUser().getUserId());
    }
}
