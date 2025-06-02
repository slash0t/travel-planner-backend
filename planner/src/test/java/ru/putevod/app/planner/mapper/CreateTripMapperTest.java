package ru.putevod.app.planner.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.putevod.app.planner.dto.CreateTripDto;
import ru.putevod.app.planner.dto.TripDto;
import ru.putevod.app.planner.model.Trip;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class CreateTripMapperTest {

    @Autowired
    private CreateTripMapper createTripMapper;

    @Test
    void toTripDto_ShouldMapCorrectly() {
        CreateTripDto createTripDto = new CreateTripDto();
        createTripDto.setTitle("Test Trip");
        createTripDto.setDescription("Test Description");
        createTripDto.setStartDate(LocalDate.now());
        createTripDto.setEndDate(LocalDate.now().plusDays(5));
        createTripDto.setCountry("Test Country");
        createTripDto.setCity("Test City");
        createTripDto.setPublished(true);

        TripDto result = createTripMapper.toTripDto(createTripDto);

        assertNotNull(result);
        assertEquals(createTripDto.getTitle(), result.getTitle());
        assertEquals(createTripDto.getDescription(), result.getDescription());
        assertEquals(createTripDto.getStartDate(), result.getStartDate());
        assertEquals(createTripDto.getEndDate(), result.getEndDate());
        assertEquals(createTripDto.getCountry(), result.getCountry());
        assertEquals(createTripDto.getCity(), result.getCity());
        assertEquals(createTripDto.isPublished(), result.isPublished());
    }

    @Test
    void toEntity_ShouldMapCorrectly() {
        CreateTripDto createTripDto = new CreateTripDto();
        createTripDto.setTitle("Test Trip");
        createTripDto.setDescription("Test Description");
        createTripDto.setStartDate(LocalDate.now());
        createTripDto.setEndDate(LocalDate.now().plusDays(5));
        createTripDto.setCountry("Test Country");
        createTripDto.setCity("Test City");
        createTripDto.setPublished(true);

        Trip result = createTripMapper.toEntity(createTripDto);

        assertNotNull(result);
        assertEquals(createTripDto.getTitle(), result.getTitle());
        assertEquals(createTripDto.getDescription(), result.getDescription());
        assertEquals(createTripDto.getStartDate(), result.getStartDate());
        assertEquals(createTripDto.getEndDate(), result.getEndDate());
        assertEquals(createTripDto.getCountry(), result.getCountry());
        assertEquals(createTripDto.getCity(), result.getCity());
        assertEquals(createTripDto.isPublished(), result.isPublished());
        assertFalse(result.isDeleted());
    }
} 