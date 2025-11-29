package com.pbop.services;

import com.pbop.dtos.user.LoginDto;
import com.pbop.dtos.user.RegisterUserDto;
import com.pbop.enums.UserRole;
import com.pbop.exceptions.InvalidRoleException;
import com.pbop.mappers.UserMapper;
import com.pbop.models.User;
import com.pbop.repositories.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    // ---------- register tests ----------

    @Test
    void register_success_withValidRole() {
        RegisterUserDto dto = new RegisterUserDto(
                "john",
                "john@example.com",
                "plainPass",
                "user", // lower-case, should map to USER
                "Somewhere",
                "1234567890"
        );

        User mappedUser = new User();
        mappedUser.setUsername("john");
        mappedUser.setEmail("john@example.com");
        mappedUser.setPassword("plainPass");

        when(userMapper.toEntity(dto)).thenReturn(mappedUser);
        when(userRepo.save(mappedUser)).thenReturn(mappedUser);

        ResponseEntity<String> response = userService.register(dto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User registered successfully", response.getBody());

        // password should be encoded (changed)
        assertNotEquals("plainPass", mappedUser.getPassword());
        assertEquals(UserRole.USER, mappedUser.getRole());

        verify(userMapper).toEntity(dto);
        verify(userRepo).save(mappedUser);
    }

    @Test
    void register_invalidRole_throwsInvalidRoleException() {
        RegisterUserDto dto = new RegisterUserDto(
                "john",
                "john@example.com",
                "plainPass",
                "invalidRole",
                "Somewhere",
                "1234567890"
        );

        User mappedUser = new User();
        mappedUser.setUsername("john");
        mappedUser.setEmail("john@example.com");
        mappedUser.setPassword("plainPass");

        when(userMapper.toEntity(dto)).thenReturn(mappedUser);

        InvalidRoleException ex = assertThrows(InvalidRoleException.class,
                () -> userService.register(dto));

        assertTrue(ex.getMessage().contains("Invalid role provided"));
        verify(userRepo, never()).save(any(User.class));
    }

    // ---------- login tests ----------

    @Test
    void login_success_authenticationTrue_returnsJwtToken() {
        LoginDto loginDto = new LoginDto("john@example.com", "password");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);

        when(jwtService.generateToken("john@example.com")).thenReturn("jwt-token");

        String result = userService.login(loginDto);

        assertEquals("jwt-token", result);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken("john@example.com");
    }

    @Test
    void login_authenticationFalse_returnsFailureMessage() {
        LoginDto loginDto = new LoginDto("john@example.com", "password");

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        String result = userService.login(loginDto);

        assertEquals("Login Failed :/", result);
        verify(jwtService, never()).generateToken(anyString());
    }

    // ---------- getUsers tests ----------

    @Test
    void getUsers_returnsAllUsersFromRepo() {
        User u1 = new User();
        User u2 = new User();
        List<User> users = List.of(u1, u2);

        when(userRepo.findAll()).thenReturn(users);

        List<User> result = userService.getUsers();

        assertSame(users, result);
        assertEquals(2, result.size());
        verify(userRepo).findAll();
    }
}
