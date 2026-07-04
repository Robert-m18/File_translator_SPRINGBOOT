package com.example.robert.services;
import com.example.robert.DTO.UserRequestDTO;
import com.example.robert.DTO.UserResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface UserServiceInterface {
    Page<UserResponseDTO> getAllUsers(Pageable pageable);
    void saveUser(UserRequestDTO dto);
    UserResponseDTO getUser(Long id);
    void deleteUser(Long id);
    void updateUser(Long id, UserRequestDTO dto);
    boolean userExistsByEmail(String email);

}
