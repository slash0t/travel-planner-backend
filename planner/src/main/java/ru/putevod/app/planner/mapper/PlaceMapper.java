package ru.putevod.app.planner.mapper;

import org.mapstruct.*;
import ru.putevod.app.planner.dto.PlaceDto;
import ru.putevod.app.planner.model.Place;

@Mapper(
        config = MapstructConfig.class,
        uses = {PhotoMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PlaceMapper {

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "placeId", target = "id")
    PlaceDto toDto(Place place);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "id", target = "placeId")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "placeType", target = "placeType")
    @Mapping(source = "externalId", target = "externalId")
    @Mapping(source = "previewUrl", target = "previewUrl")
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Place toEntity(PlaceDto placeDto);

    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(target = "placeId", ignore = true)
    @Mapping(source = "name", target = "name")
    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "placeType", target = "placeType")
    @Mapping(source = "externalId", target = "externalId")
    @Mapping(source = "previewUrl", target = "previewUrl")
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(PlaceDto placeDto, @MappingTarget Place place);
} 