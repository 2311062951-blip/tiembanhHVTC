package com.hvtc.bakery.service;

import com.hvtc.bakery.dto.RegisterDto;
import com.hvtc.bakery.dto.UserUpdateDto;
import com.hvtc.bakery.entity.User;
import java.util.List;

public interface UserService {
    User registerUser(RegisterDto dto);
    User findByUsername(String username);
    User findByEmail(String email);
    User findById(Long id);
    User updateProfile(Long userId, UserUpdateDto dto);
    List<User> findAllUsers();
    User toggleUserStatus(Long userId);
    User changeUserRole(Long userId, String roleName);
    void deleteUser(Long userId);
}
