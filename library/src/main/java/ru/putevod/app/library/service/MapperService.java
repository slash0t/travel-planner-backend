package ru.putevod.app.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.putevod.app.library.dto.PublicRouteDetailDto;
import ru.putevod.app.library.dto.PublicRouteDto;
import ru.putevod.app.library.dto.ReviewDto;
import ru.putevod.app.library.dto.RoutePreviewDto;
import ru.putevod.app.library.entity.PublishedRoute;
import ru.putevod.app.library.entity.RouteRating;
import ru.putevod.app.library.entity.User;
import ru.putevod.app.library.repository.UserRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MapperService {

    private final UserRepository userRepository;

    public RoutePreviewDto toRoutePreviewDto(PublishedRoute publishedRoute) {
        User author = userRepository.findById(publishedRoute.getUserId()).orElse(null);

        return RoutePreviewDto.builder()
                .id(publishedRoute.getId())
                .title(publishedRoute.getTitle())
                .description(publishedRoute.getDescription())
                .author(toAuthorDto(author))
                .countries(publishedRoute.getCountry() != null
                        ? Collections.singletonList(publishedRoute.getCountry())
                        : Collections.emptyList())
                .cities(publishedRoute.getCity() != null
                        ? Collections.singletonList(publishedRoute.getCity())
                        : Collections.emptyList())
                .duration(publishedRoute.getDuration())
                .rating(publishedRoute.getAverageRating())
                .reviewsCount(publishedRoute.getReviewsCount())
                .previewImageUrl(getPreviewImageUrl(publishedRoute))
                .tags(publishedRoute.getTags() != null
                        ? List.of(publishedRoute.getTags())
                        : Collections.emptyList())
                .createdAt(publishedRoute.getCreatedAt())
                .build();
    }

    public PublicRouteDto toPublicRouteDto(PublishedRoute publishedRoute) {
        User author = userRepository.findById(publishedRoute.getUserId()).orElse(null);

        return PublicRouteDto.builder()
                .id(publishedRoute.getId())
                .originalRouteId(publishedRoute.getOriginalRouteId())
                .title(publishedRoute.getTitle())
                .description(publishedRoute.getDescription())
                .author(toAuthorDto(author))
                .countries(publishedRoute.getCountry() != null
                        ? Collections.singletonList(publishedRoute.getCountry())
                        : Collections.emptyList())
                .cities(publishedRoute.getCity() != null
                        ? Collections.singletonList(publishedRoute.getCity())
                        : Collections.emptyList())
                .duration(publishedRoute.getDuration())
                .rating(publishedRoute.getAverageRating())
                .reviewsCount(publishedRoute.getReviewsCount())
                .previewImageUrl(getPreviewImageUrl(publishedRoute))
                .tags(publishedRoute.getTags() != null
                        ? List.of(publishedRoute.getTags())
                        : Collections.emptyList())
                .createdAt(publishedRoute.getCreatedAt())
                .updatedAt(publishedRoute.getUpdatedAt())
                .build();
    }

    public PublicRouteDetailDto toPublicRouteDetailDto(PublishedRoute publishedRoute) {
        User author = userRepository.findById(publishedRoute.getUserId()).orElse(null);

        PublicRouteDetailDto dto = PublicRouteDetailDto.builder()
                .id(publishedRoute.getId())
                .originalRouteId(publishedRoute.getOriginalRouteId())
                .title(publishedRoute.getTitle())
                .description(publishedRoute.getDescription())
                .author(toAuthorDto(author))
                .countries(publishedRoute.getCountry() != null
                        ? Collections.singletonList(publishedRoute.getCountry())
                        : Collections.emptyList())
                .cities(publishedRoute.getCity() != null
                        ? Collections.singletonList(publishedRoute.getCity())
                        : Collections.emptyList())
                .duration(publishedRoute.getDuration())
                .rating(publishedRoute.getAverageRating())
                .reviewsCount(publishedRoute.getReviewsCount())
                .previewImageUrl(getPreviewImageUrl(publishedRoute))
                .tags(publishedRoute.getTags() != null
                        ? List.of(publishedRoute.getTags())
                        : Collections.emptyList())
                .createdAt(publishedRoute.getCreatedAt())
                .updatedAt(publishedRoute.getUpdatedAt())
                .days(Collections.emptyList()) // TODO: здесь будет список дней маршрута
                .build();

        return dto;
    }

    public ReviewDto toReviewDto(RouteRating rating) {
        User author = userRepository.findById(rating.getUserId()).orElse(null);

        return ReviewDto.builder()
                .id(rating.getId())
                .routeId(rating.getPublishedRoute().getId())
                .author(toAuthorDto(author))
                .rating(rating.getRating())
                .comment(rating.getComment())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }

    private RoutePreviewDto.AuthorDto toAuthorDto(User user) {
        if (user == null) {
            return RoutePreviewDto.AuthorDto.builder()
                    .id(0L)
                    .username("Unknown")
                    .build();
        }

        return RoutePreviewDto.AuthorDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .avatarUrl(user.getProfilePictureUrl())
                .build();
    }

    private String getPreviewImageUrl(PublishedRoute publishedRoute) {
        // TODO: здесь будет логика получения URL изображения
        return "https://example.com/images/" + publishedRoute.getId();
    }
} 