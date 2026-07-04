package com.example.robert.services;

import com.example.robert.DTO.UserRequestDTO;
import com.example.robert.DTO.UserResponseDTO;
import com.example.robert.exceptions.NotFoundException;
import com.example.robert.mappers.UserMapper;
import com.example.robert.models.User;
import com.example.robert.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserService implements UserServiceInterface {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toResponseDto);
    }

    @Override
    @Transactional
    public void saveUser(UserRequestDTO dto) {

        User user = userMapper.toEntity(dto);

        user.setPassword(
                passwordEncoder.encode(dto.password())
        );

        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getUser(Long id) {
        return userMapper.toResponseDto(
                userRepository.findById(id)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "User not found with id: " + id
                                )
                        )
        );
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateUser(Long id, UserRequestDTO dto) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException(
                                "User not found with id: " + id
                        )
                );

        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));

        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}