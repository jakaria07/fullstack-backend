package com.jakaria.fullstack_backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import com.jakaria.fullstack_backend.exception.UserNotFoundException;
import com.jakaria.fullstack_backend.model.User;
import com.jakaria.fullstack_backend.repository.UserRepository;

@RestController
@CrossOrigin("http://localhost:3000")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // Add WebSocket messaging template

    @PostMapping("/user")
    User newUser(@RequestBody User newUser) {
        User savedUser = userRepository.save(newUser);
        messagingTemplate.convertAndSend("/topic/notifications", "User created: " + savedUser.getName());
        return savedUser;
    }

    @GetMapping("/users")
    List<User> getAllUsers(){
        return userRepository.findAll();
    }

    @GetMapping("/user/{id}")
    User getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
            .orElseThrow(()->new UserNotFoundException(id));
    }

    @PutMapping("/user/{id}")
    User updateUser(@RequestBody User newUser,@PathVariable Long id) {
        User updatedUser = userRepository.findById(id)
                .map(user -> {
                    user.setUsername(newUser.getUsername());
                    user.setName(newUser.getName());
                    user.setEmail(newUser.getEmail());
                    return userRepository.save(user);
                }).orElseThrow(() -> new UserNotFoundException(id));
        messagingTemplate.convertAndSend("/topic/notifications", "User updated: " + updatedUser.getName());
        return updatedUser;
    }

    @DeleteMapping("/user/{id}")
    String deleteUser(@PathVariable Long id) {
        if(!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
        messagingTemplate.convertAndSend("/topic/notifications", "User with ID " + id + " deleted");
        return "Success! User with ID "+id+" has been deleted.";
    }
}
