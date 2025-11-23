package com.pbop.mappers;

import com.pbop.dtos.user.RegisterUserDto;
import com.pbop.enums.UserRole;
import com.pbop.models.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-22T23:52:57+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.5 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(RegisterUserDto dto) {
        if ( dto == null ) {
            return null;
        }

        User user = new User();

        user.setUsername( dto.username() );
        user.setEmail( dto.email() );
        user.setPassword( dto.password() );
        user.setPhone( dto.phone() );
        if ( dto.role() != null ) {
            user.setRole( Enum.valueOf( UserRole.class, dto.role() ) );
        }
        user.setLocation( dto.location() );

        return user;
    }
}
