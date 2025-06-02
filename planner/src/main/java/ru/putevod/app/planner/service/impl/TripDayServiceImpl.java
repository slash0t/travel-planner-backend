package ru.putevod.app.planner.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.planner.dto.TripDayDto;
import ru.putevod.app.planner.exception.BadRequestException;
import ru.putevod.app.planner.exception.ResourceNotFoundException;
import ru.putevod.app.planner.mapper.TripDayMapper;
import ru.putevod.app.planner.model.Trip;
import ru.putevod.app.planner.model.TripDay;
import ru.putevod.app.planner.repository.TripDayRepository;
import ru.putevod.app.planner.service.TripDayService;
import ru.putevod.app.planner.service.TripService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripDayServiceImpl implements TripDayService {

    private final TripDayRepository tripDayRepository;
    private final TripService tripService;
    private final TripDayMapper tripDayMapper;

    @Override
    @Transactional
    public TripDayDto createTripDay(Long userId, Long tripId, TripDayDto tripDayDto) {
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

        // Проверяем, что день с таким номером не существует в поездке
        if (tripDayDto.getDayNumber() != null) {
            boolean dayNumberExists = tripDayRepository.findByTripOrderByDayNumberAsc(trip).stream()
                    .anyMatch(day -> tripDayDto.getDayNumber().equals(day.getDayNumber()));

            if (dayNumberExists) {
                throw new BadRequestException("День с указанным номером уже существует в этой поездке");
            }
        } else {
            // Если номер дня не указан, вычисляем его автоматически
            Integer maxDayNumber = tripDayRepository.findByTripOrderByDayNumberAsc(trip).stream()
                    .map(TripDay::getDayNumber)
                    .max(Integer::compareTo)
                    .orElse(0);

            tripDayDto.setDayNumber(maxDayNumber + 1);
        }

        // Проверяем уникальность даты, если она указана
        if (tripDayDto.getDate() != null) {
            boolean dateExists = tripDayRepository.findByTripAndDate(trip, tripDayDto.getDate()).isPresent();

            if (dateExists) {
                throw new BadRequestException("День с указанной датой уже существует в этой поездке");
            }
        }

        TripDay tripDay = tripDayMapper.fromDto(tripDayDto, trip);
        tripDay = tripDayRepository.save(tripDay);

        return tripDayMapper.toDto(tripDay);
    }

    @Override
    @Transactional
    public TripDayDto updateTripDay(Long userId, Long tripId, Long dayId, TripDayDto tripDayDto) {
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

        TripDay tripDay = tripDayRepository.findByTripAndDayId(trip, dayId)
                .orElseThrow(() -> new ResourceNotFoundException("День", "id", dayId));

        // Проверяем, что изменяемый номер дня не конфликтует с существующими
        if (tripDayDto.getDayNumber() != null && !tripDayDto.getDayNumber().equals(tripDay.getDayNumber())) {
            boolean dayNumberExists = tripDayRepository.findByTripOrderByDayNumberAsc(trip).stream()
                    .filter(day -> !day.getDayId().equals(dayId))
                    .anyMatch(day -> tripDayDto.getDayNumber().equals(day.getDayNumber()));

            if (dayNumberExists) {
                throw new BadRequestException("День с указанным номером уже существует в этой поездке");
            }
        }

        // Проверяем уникальность даты, если она изменяется
        if (tripDayDto.getDate() != null && !tripDayDto.getDate().equals(tripDay.getDate())) {
            boolean dateExists = tripDayRepository.findByTripAndDate(trip, tripDayDto.getDate())
                    .map(day -> !day.getDayId().equals(dayId))
                    .orElse(false);

            if (dateExists) {
                throw new BadRequestException("День с указанной датой уже существует в этой поездке");
            }
        }

        tripDayMapper.updateEntityFromDto(tripDayDto, tripDay);
        tripDay = tripDayRepository.save(tripDay);

        return tripDayMapper.toDto(tripDay);
    }

    @Override
    @Transactional(readOnly = true)
    public TripDayDto getTripDay(Long userId, Long tripId, Long dayId) {
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

        TripDay tripDay = tripDayRepository.findByTripAndDayId(trip, dayId)
                .orElseThrow(() -> new ResourceNotFoundException("День", "id", dayId));

        return tripDayMapper.toDto(tripDay);
    }

    @Override
    @Transactional(readOnly = true)
    public TripDayDto getTripDayByDate(Long userId, Long tripId, LocalDate date) {
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

        TripDay tripDay = tripDayRepository.findByTripAndDate(trip, date)
                .orElseThrow(() -> new ResourceNotFoundException("День", "date", date));

        return tripDayMapper.toDto(tripDay);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDayDto> getTripDays(Long userId, Long tripId) {
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

        List<TripDay> tripDays = tripDayRepository.findByTripOrderByDayNumberAsc(trip);

        return tripDays.stream()
                .map(tripDayMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteTripDay(Long userId, Long tripId, Long dayId) {
        Trip trip = tripService.getTripEntityWithAccessCheck(userId, tripId);

        if (tripDayRepository.findByTripAndDayId(trip, dayId).isEmpty()) {
            throw new ResourceNotFoundException("День", "id", dayId);
        }

        tripDayRepository.deleteByTripAndDayId(trip, dayId);
    }
} 