package com.hvtc.bakery.service;

import com.hvtc.bakery.dto.RegisterDto;
import com.hvtc.bakery.entity.Role;
import com.hvtc.bakery.entity.User;
import com.hvtc.bakery.exception.BadRequestException;
import com.hvtc.bakery.repository.RoleRepository;
import com.hvtc.bakery.repository.UserRepository;
import com.hvtc.bakery.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder().id(1L).name("ROLE_USER").build();
    }

    @Test
    void testRegisterUserSuccess() {
        RegisterDto dto = RegisterDto.builder()
                .username("testuser")
                .fullName("Test User")
                .email("test@hvtc.vn")
                .password("Password123")
                .confirmPassword("Password123")
                .phone("0912345678")
                .build();

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@hvtc.vn")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = userService.registerUser(dto);

        assertNotNull(registered);
        assertEquals("testuser", registered.getUsername());
        assertEquals("encodedPassword", registered.getPassword());
        assertTrue(registered.isEnabled());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUserPasswordMismatchThrowsException() {
        RegisterDto dto = RegisterDto.builder()
                .username("testuser")
                .password("Password123")
                .confirmPassword("WrongPassword")
                .build();

        assertThrows(BadRequestException.class, () -> userService.registerUser(dto));
        verify(userRepository, never()).save(any(User.class));
    }
}
