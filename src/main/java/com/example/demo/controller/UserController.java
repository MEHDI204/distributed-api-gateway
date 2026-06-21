package com.example.demo.controller;

import com.example.demo.service.UserService;
import com.example.demo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User handleAddUser(@RequestBody User user, @RequestHeader("Authorization") String authHeader) {
        System.out.println("Received token: " + authHeader);
        if(!authHeader.equals("SecretToken123")){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Get out!");
        }
        return userService.createUser(user);
    }


    public User handlAddUser(User passedInUser){
        return userService.createUser(passedInUser);
    }

    @PostMapping("/login")
    public String handleLogin(@RequestBody User user){
        return userService.login(user.getUsername(), user.getPassword());
    }
}
