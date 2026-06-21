package com.example.demo.service;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public User createUser(User newUser){
        return userRepository.save(newUser);
    }

    public String login(String username, String password){
        User user = userRepository.findByUsername(username)
                .orElseThrow(()-> new RuntimeException("User not found!"));
        if(!user.getPassword().equals(password)){
            throw new RuntimeException("invalid credentials");
        }
        return jwtUtil.generateToken(user.getUsername());
    }

}
