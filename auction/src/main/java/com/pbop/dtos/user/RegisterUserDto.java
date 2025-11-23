package com.pbop.dtos.user;

public record RegisterUserDto(

        String username,
        String email,
        String password,
        String role,
        String location,
        String phone
) { }