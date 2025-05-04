package ru.putevod.app.planner.mapper;

import org.mapstruct.*;
import ru.putevod.app.planner.dto.FileDto;
import ru.putevod.app.planner.model.File;

@Mapper(
    config = MapstructConfig.class,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface FileMapper {
    
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "fileId", target = "id")
    @Mapping(source = "user.userId", target = "userId")
    FileDto toDto(File file);
    
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    @Mapping(source = "id", target = "fileId")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "tripFiles", ignore = true)
    @Mapping(target = "eventFiles", ignore = true)
    @Mapping(target = "photos", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    File toEntity(FileDto fileDto);
} 