package com.pbop.mappers;

import com.pbop.dtos.user.RegisterUserDto;
import com.pbop.models.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {


    @Mapping(target = "userId", ignore = true)
    User toEntity(RegisterUserDto dto);
}
