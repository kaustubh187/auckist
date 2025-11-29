package com.pbop.services;

import com.pbop.config.GlobalExceptionHandler;
import com.pbop.dtos.user.LoginDto;
import com.pbop.dtos.user.RegisterUserDto;
import com.pbop.enums.UserRole;
import com.pbop.exceptions.InvalidRoleException;
import com.pbop.mappers.UserMapper;
import com.pbop.models.User;
import com.pbop.repositories.UserRepo;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepo repo;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserRepo repo, AuthenticationManager authenticationManager, UserMapper userMapper, JwtService jwtService) {
        this.repo = repo;
        this.authenticationManager = authenticationManager;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
    }

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(11);

    @Transactional
    public ResponseEntity<String> register(RegisterUserDto registerUserDto){

        User user = userMapper.toEntity(registerUserDto);
        user.setPassword(encoder.encode(user.getPassword()));
        try {
            log.debug("Attempting to set role: {}", registerUserDto.role());
            UserRole roleEnum = UserRole.valueOf(registerUserDto.role().toUpperCase());
            user.setRole(roleEnum);
        } catch (IllegalArgumentException e) {
            String validRoles = Arrays.stream(UserRole.values()).map(Enum::name).collect(Collectors.joining(", "));

            throw new InvalidRoleException("Invalid role provided. Role must be one of: " + validRoles);
        }
        log.debug("Registering user with email: {} and role: {}", user.getEmail(), user.getRole());
        repo.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    public String login(LoginDto user) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.email(),user.password()));

        if(authentication.isAuthenticated()){
            return jwtService.generateToken(user.email());
        }
        else{
            return "Login Failed :/";
        }
    }
    public List<User> getUsers(){
        log.debug("Fetching all users from the database");
        return repo.findAll();
    }
}
