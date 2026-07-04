package com.example.robert.mappers;


import com.example.robert.DTO.UserRequestDTO;
import com.example.robert.DTO.UserResponseDTO;
import com.example.robert.models.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserRequestDTO dto);
    UserResponseDTO toResponseDto(User user);
}
