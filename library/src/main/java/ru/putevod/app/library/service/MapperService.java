package ru.putevod.app.library.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.putevod.app.library.dto.*;
import ru.putevod.app.library.entity.PublishedRoute;
import ru.putevod.app.library.entity.RouteComment;
import ru.putevod.app.library.entity.RouteRating;
import ru.putevod.app.library.entity.User;
import ru.putevod.app.library.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MapperService {

    private final UserRepository userRepository;

    public RoutePreviewDto toRoutePreviewDto(PublishedRoute publishedRoute) {
        User author = userRepository.findById(publishedRoute.getUserId()).orElse(null);
        
        return RoutePreviewDto.builder()
                .id(convertToUuid(publishedRoute.getId()))
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
                .id(convertToUuid(publishedRoute.getId()))
                .originalRouteId(convertToUuid(publishedRoute.getOriginalRouteId()))
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
                .id(convertToUuid(publishedRoute.getId()))
                .originalRouteId(convertToUuid(publishedRoute.getOriginalRouteId()))
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
                .id(convertToUuid(rating.getId()))
                .routeId(convertToUuid(rating.getPublishedRoute().getId()))
                .author(toAuthorDto(author))
                .rating(rating.getRating())
                .createdAt(rating.getCreatedAt())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }

    public CommentDto toCommentDto(RouteComment comment) {
        User author = userRepository.findById(comment.getUserId()).orElse(null);
        
        return CommentDto.builder()
                .id(convertToUuid(comment.getId()))
                .routeId(convertToUuid(comment.getPublishedRoute().getId()))
                .author(toAuthorDto(author))
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }

    private RoutePreviewDto.AuthorDto toAuthorDto(User user) {
        if (user == null) {
            return RoutePreviewDto.AuthorDto.builder()
                    .id(UUID.randomUUID())
                    .username("Unknown")
                    .build();
        }
        
        return RoutePreviewDto.AuthorDto.builder()
                .id(convertToUuid(user.getId()))
                .username(user.getUsername())
                .avatarUrl(user.getProfilePictureUrl())
                .build();
    }

    private UUID convertToUuid(Long id) {
        if (id == null) return null;
        return UUID.nameUUIDFromBytes(id.toString().getBytes());
    }

    private String getPreviewImageUrl(PublishedRoute publishedRoute) {
        // TODO: здесь будет логика получения URL изображения
        return "https://example.com/images/" + publishedRoute.getId();
    }
} 