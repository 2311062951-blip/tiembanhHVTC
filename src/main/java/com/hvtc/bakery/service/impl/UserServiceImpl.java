package com.hvtc.bakery.service.impl;

import com.hvtc.bakery.dto.RegisterDto;
import com.hvtc.bakery.dto.UserUpdateDto;
import com.hvtc.bakery.entity.Role;
import com.hvtc.bakery.entity.User;
import com.hvtc.bakery.exception.BadRequestException;
import com.hvtc.bakery.exception.ResourceNotFoundException;
import com.hvtc.bakery.repository.RoleRepository;
import com.hvtc.bakery.repository.UserRepository;
import com.hvtc.bakery.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User registerUser(RegisterDto dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BadRequestException("Mật khẩu và xác nhận mật khẩu không trùng khớp");
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại trong hệ thống");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email đã được đăng ký tài khoản khác");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .username(dto.getUsername().trim())
                .fullName(dto.getFullName().trim())
                .email(dto.getEmail().trim())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone() != null ? dto.getPhone().trim() : null)
                .address(dto.getAddress() != null ? dto.getAddress().trim() : null)
                .enabled(true)
                .roles(roles)
                .build();

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng: " + username));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy email người dùng: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy ID người dùng: " + id));
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, UserUpdateDto dto) {
        User user = findById(userId);

        if (!user.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new BadRequestException("Email đã thuộc về tài khoản khác");
        }

        user.setFullName(dto.getFullName().trim());
        user.setEmail(dto.getEmail().trim());
        user.setPhone(dto.getPhone() != null ? dto.getPhone().trim() : null);
        user.setAddress(dto.getAddress() != null ? dto.getAddress().trim() : null);

        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            if (dto.getOldPassword() == null || !passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
                throw new BadRequestException("Mật khẩu cũ không chính xác");
            }
            if (dto.getNewPassword().length() < 6) {
                throw new BadRequestException("Mật khẩu mới phải từ 6 ký tự trở lên");
            }
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public User toggleUserStatus(Long userId) {
        User user = findById(userId);
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (isAdmin && user.isEnabled()) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> u.isEnabled() && u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN")))
                    .count();
            if (adminCount <= 1) {
                throw new BadRequestException("Không thể khóa tài khoản Quản trị viên (Admin) duy nhất trong hệ thống!");
            }
        }

        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User changeUserRole(Long userId, String roleName) {
        User user = findById(userId);
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(Role.builder().name(roleName).build()));

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = findById(userId);
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (isAdmin) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN")))
                    .count();
            if (adminCount <= 1) {
                throw new BadRequestException("Không thể xóa tài khoản Quản trị viên (Admin) duy nhất trong hệ thống!");
            }
        }

        userRepository.delete(user);
    }
}
