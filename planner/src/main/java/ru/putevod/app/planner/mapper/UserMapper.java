package ru.putevod.app.planner.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.putevod.app.planner.dto.UserDto;
import ru.putevod.app.planner.model.User;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(source = "userId", target = "id")
    UserDto toDto(User user);
} 