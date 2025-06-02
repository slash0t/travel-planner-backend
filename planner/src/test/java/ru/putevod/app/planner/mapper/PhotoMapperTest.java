package ru.putevod.app.planner.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.putevod.app.planner.dto.PhotoDto;
import ru.putevod.app.planner.model.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class PhotoMapperTest {

    @Autowired
    private PhotoMapper photoMapper;

    @Test
    void toDto_ShouldMapCorrectly() {
        User user = new User();
        user.setUserId(1L);

        Trip trip = new Trip();
        trip.setTripId(1L);

        Place place = new Place();
        place.setPlaceId(1L);

        Event event = new Event();
        event.setEventId(1L);

        File file = new File();
        file.setFileId(1L);

        Photo photo = new Photo();
        photo.setPhotoId(1L);
        photo.setUser(user);
        photo.setTrip(trip);
        photo.setPlace(place);
        photo.setEvent(event);
        photo.setFile(file);
        photo.setCaption("Test photo caption");

        PhotoDto result = photoMapper.toDto(photo);

        assertNotNull(result);
        assertEquals(photo.getPhotoId(), result.getId());
        assertEquals(photo.getUser().getUserId(), result.getUserId());
        assertEquals(photo.getTrip().getTripId(), result.getTripId());
        assertEquals(photo.getPlace().getPlaceId(), result.getPlaceId());
        assertEquals(photo.getEvent().getEventId(), result.getEventId());
        assertEquals(photo.getCaption(), result.getCaption());
    }

    @Test
    void toEntity_ShouldMapCorrectly() {
        PhotoDto photoDto = new PhotoDto();
        photoDto.setId(1L);
        photoDto.setUserId(1L);
        photoDto.setTripId(1L);
        photoDto.setPlaceId(1L);
        photoDto.setEventId(1L);
        photoDto.setCaption("Test photo caption");

        Photo result = photoMapper.toEntity(photoDto);

        assertNotNull(result);
        assertEquals(photoDto.getId(), result.getPhotoId());
        assertEquals(photoDto.getCaption(), result.getCaption());
    }
} 