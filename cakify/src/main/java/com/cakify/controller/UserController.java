package com.cakify.controller;

import com.cakify.dto.AuthRequestDto;
import com.cakify.dto.AuthResponseDto;
import com.cakify.dto.UserRequestDto;
import com.cakify.dto.UserResponseDto;
import com.cakify.exception.UserExceptions;
import com.cakify.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Create admin
    @PostMapping("/create")
    public UserResponseDto createUser(@RequestBody @Valid UserRequestDto dto) {
        try {
            return userService.createUser(dto);
        } catch (Exception e) {
            throw new UserExceptions("Failed to create user: " + e.getMessage(), "USER_CREATE_ERROR");
        }
    }

    // Get all users (for dashboard)
    @GetMapping("/all")
    public List<UserResponseDto> getAllUsers() {
        try {
            return userService.getAllUsers();
        } catch (Exception e) {
            throw new UserExceptions("Failed to fetch users: " + e.getMessage(), "USER_FETCH_ERROR");
        }
    }

    // Update admin
    @PutMapping("/{id}")
    public UserResponseDto updateUser(@PathVariable Long id, @RequestBody @Valid UserRequestDto dto) {
        try {
            return userService.updateUser(id, dto);
        } catch (Exception e) {
            throw new UserExceptions("Failed to update user: " + e.getMessage(), "USER_UPDATE_ERROR");
        }
    }

    // Delete admin
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return "User deleted successfully";
        } catch (Exception e) {
            throw new UserExceptions("Failed to delete user: " + e.getMessage(), "USER_DELETE_ERROR");
        }
    }

    // Login
    @PostMapping("/login")
    public AuthResponseDto login(@RequestBody AuthRequestDto request) {
        try {
            return userService.login(request.getUsername(), request.getPassword());
        } catch (Exception e) {
            throw new UserExceptions("Login failed: " + e.getMessage(), "USER_LOGIN_ERROR");
        }
    }

}
