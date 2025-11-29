package com.pbop.controllers;

import com.pbop.dtos.user.LoginDto;
import com.pbop.dtos.user.RegisterUserDto;
import com.pbop.models.User;
import com.pbop.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody RegisterUserDto user) {
        return userService.register(user);
    }

    @PostMapping("login")
    public String login(@RequestBody LoginDto loginDto) {
        return userService.login(loginDto);
    }

    @GetMapping("/ruser")
    public List<User> helloWorld() {

        return userService.getUsers();
    }
}
