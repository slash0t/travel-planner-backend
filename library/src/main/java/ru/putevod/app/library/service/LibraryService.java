package ru.putevod.app.library.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.putevod.app.library.exception.ResourceNotFoundException;
import ru.putevod.app.library.dto.PublicRouteDetailDto;
import ru.putevod.app.library.dto.PublicRouteDto;
import ru.putevod.app.library.dto.RoutePreviewDto;
import ru.putevod.app.library.entity.PublishedRoute;
import ru.putevod.app.library.entity.Trip;
import ru.putevod.app.library.entity.User;
import ru.putevod.app.library.repository.PublishedRouteRepository;
import ru.putevod.app.library.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LibraryService {

    private final PublishedRouteRepository publishedRouteRepository;
    private final UserRepository userRepository;
    private final MapperService mapperService;

    @Transactional(readOnly = true)
    public Page<RoutePreviewDto> getPublishedRoutes(Pageable pageable) {
        return publishedRouteRepository.findAllApproved(pageable)
                .map(mapperService::toRoutePreviewDto);
    }

    @Transactional(readOnly = true)
    public Page<RoutePreviewDto> getPendingRoutes(Pageable pageable) {
        return publishedRouteRepository.findAllPendingApproval(pageable)
                .map(mapperService::toRoutePreviewDto);
    }

    @Transactional(readOnly = true)
    public Page<RoutePreviewDto> searchRoutes(String query, Pageable pageable) {
        return publishedRouteRepository.searchByQuery(query, pageable)
                .map(mapperService::toRoutePreviewDto);
    }

    @Transactional(readOnly = true)
    public Page<RoutePreviewDto> getFilteredRoutes(String country, String city, Integer durationMin, 
                                                Integer durationMax, String tag, Pageable pageable) {
        return publishedRouteRepository.findWithFilters(country, city, durationMin, durationMax, tag, pageable)
                .map(mapperService::toRoutePreviewDto);
    }

    @Transactional(readOnly = true)
    public Page<RoutePreviewDto> getPopularRoutes(Pageable pageable) {
        return publishedRouteRepository.findPopular(pageable)
                .map(mapperService::toRoutePreviewDto);
    }

    @Transactional(readOnly = true)
    public Page<RoutePreviewDto> getMostRatedRoutes(Pageable pageable) {
        return publishedRouteRepository.findMostRated(pageable)
                .map(mapperService::toRoutePreviewDto);
    }

    @Transactional(readOnly = true)
    public PublicRouteDetailDto getRouteDetails(Long routeId) {
        PublishedRoute publishedRoute = publishedRouteRepository.findByIdAndIsApprovedTrue(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found with id " + routeId));

        incrementViewCount(publishedRoute);
        
        return mapperService.toPublicRouteDetailDto(publishedRoute);
    }

    @Transactional
    protected void incrementViewCount(PublishedRoute publishedRoute) {
        publishedRoute.setViewCount(publishedRoute.getViewCount() + 1);
        publishedRouteRepository.save(publishedRoute);
    }

    @Transactional(readOnly = true)
    public List<RoutePreviewDto> getUserPublishedRoutes(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
                
        return publishedRouteRepository.findByUserIdAndIsApprovedTrue(userId).stream()
                .map(mapperService::toRoutePreviewDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PublicRouteDto publishRoute(Trip trip, Long userId) {
        if (publishedRouteRepository.existsByOriginalRouteId(trip.getId())) {
            throw new IllegalStateException("Route is already published");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        
        PublishedRoute publishedRoute = PublishedRoute.builder()
                .originalRouteId(trip.getId())
                .userId(userId)
                .title(trip.getTitle())
                .description(trip.getDescription())
                .country(trip.getCountry())
                .city(trip.getCity())
                .duration(trip.getDuration())
                .isApproved(false)
                .viewCount(0)
                .build();
        
        PublishedRoute saved = publishedRouteRepository.save(publishedRoute);
        return mapperService.toPublicRouteDto(saved);
    }

    @Transactional
    public PublicRouteDto approvePublishedRoute(Long routeId) {
        PublishedRoute publishedRoute = publishedRouteRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Published route not found with id " + routeId));
        
        publishedRoute.setIsApproved(true);
        PublishedRoute saved = publishedRouteRepository.save(publishedRoute);
        
        return mapperService.toPublicRouteDto(saved);
    }

    @Transactional
    public void deletePublishedRoute(Long routeId) {
        PublishedRoute publishedRoute = publishedRouteRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Published route not found with id " + routeId));
        
        publishedRouteRepository.delete(publishedRoute);
    }

    @Transactional(readOnly = true)
    public PublishedRoute getPublishedRouteById(Long routeId) {
        return publishedRouteRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Published route not found with id " + routeId));
    }
} 