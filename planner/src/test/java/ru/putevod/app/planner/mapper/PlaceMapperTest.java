package ru.putevod.app.planner.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.putevod.app.planner.dto.PlaceDto;
import ru.putevod.app.planner.model.Place;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class PlaceMapperTest {

    @Autowired
    private PlaceMapper placeMapper;

    @Test
    void testToDto() {
        Place place = new Place();
        place.setPlaceId(1L);
        place.setName("Test Place");
        place.setLatitude(new BigDecimal("12.34"));
        place.setLongitude(new BigDecimal("56.78"));
        place.setAddress("Test Address");
        place.setPlaceType("Test Type");
        place.setExternalId("external-123");
        place.setPreviewUrl("test-preview-url");
        place.setCreatedAt(LocalDateTime.now());
        place.setUpdatedAt(LocalDateTime.now());
        place.setPhotos(new ArrayList<>());
        place.setEvents(new ArrayList<>());

        PlaceDto placeDto = placeMapper.toDto(place);

        assertNotNull(placeDto);
        assertEquals(place.getPlaceId(), placeDto.getId());
        assertEquals(place.getName(), placeDto.getName());
        assertEquals(place.getLatitude(), placeDto.getLatitude());
        assertEquals(place.getLongitude(), placeDto.getLongitude());
        assertEquals(place.getAddress(), placeDto.getAddress());
        assertEquals(place.getPlaceType(), placeDto.getPlaceType());
        assertEquals(place.getExternalId(), placeDto.getExternalId());
        assertEquals(place.getPreviewUrl(), placeDto.getPreviewUrl());
        assertEquals(place.getCreatedAt(), placeDto.getCreatedAt());
        assertEquals(place.getUpdatedAt(), placeDto.getUpdatedAt());
        assertNotNull(placeDto.getPhotos());
        assertTrue(placeDto.getPhotos().isEmpty());
    }

    @Test
    void testToEntity() {
        PlaceDto placeDto = PlaceDto.builder()
                .id(1L)
                .name("Test Place")                .latitude(new BigDecimal("12.34"))
                .longitude(new BigDecimal("56.78"))
                .address("Test Address")
                .placeType("Test Type")
                .externalId("external-123")
                .previewUrl("test-preview-url")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .photos(new ArrayList<>())
                .build();

        Place place = placeMapper.toEntity(placeDto);

        assertNotNull(place);
        assertEquals(placeDto.getId(), place.getPlaceId());
        assertEquals(placeDto.getName(), place.getName());
        assertEquals(placeDto.getLatitude(), place.getLatitude());
        assertEquals(placeDto.getLongitude(), place.getLongitude());
        assertEquals(placeDto.getAddress(), place.getAddress());
        assertEquals(placeDto.getPlaceType(), place.getPlaceType());
        assertEquals(placeDto.getExternalId(), place.getExternalId());
        assertEquals(placeDto.getPreviewUrl(), place.getPreviewUrl());
        assertNotNull(place.getPhotos());
        assertTrue(place.getPhotos().isEmpty());
    }

    @Test
    void testUpdateEntityFromDto() {
        Place place = new Place();        place.setPlaceId(1L);
        place.setName("Original Name");
        place.setLatitude(new BigDecimal("11.11"));
        place.setLongitude(new BigDecimal("22.22"));

        PlaceDto placeDto = PlaceDto.builder()                .id(1L)
                .name("Updated Name")
                .latitude(new BigDecimal("33.33"))
                .longitude(new BigDecimal("44.44"))
                .address("Updated Address")
                .placeType("Updated Type")
                .externalId("updated-external-id")
                .previewUrl("updated-preview-url")
                .build();

        placeMapper.updateEntityFromDto(placeDto, place);

        assertEquals(placeDto.getName(), place.getName());
        assertEquals(placeDto.getLatitude(), place.getLatitude());
        assertEquals(placeDto.getLongitude(), place.getLongitude());
        assertEquals(placeDto.getAddress(), place.getAddress());
        assertEquals(placeDto.getPlaceType(), place.getPlaceType());
        assertEquals(placeDto.getExternalId(), place.getExternalId());
        assertEquals(placeDto.getPreviewUrl(), place.getPreviewUrl());
    }
}
