package com.subtracker.SubTracker.user;


import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    //CreateUserDto to UserEntity
    public UserEntity dtoToEntity(CreateUserDto createUserDto) {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(createUserDto.getEmail());
        userEntity.setPassword(createUserDto.getPassword());
        userEntity.setName(createUserDto.getName());
        userEntity.setPlanType(createUserDto.getPlanType());

        return userEntity;
    }

    //UserEntity to UserResponse
    public UserResponseDto entityToResponse(UserEntity userEntity) {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setEmail(userEntity.getEmail());
        userResponseDto.setName(userEntity.getName());
        userResponseDto.setPlanType(userEntity.getPlanType());
        userResponseDto.setRole(userEntity.getRole());
        userResponseDto.setId(userEntity.getId());
        userResponseDto.setCreatedAt(userEntity.getCreatedAt());
        userResponseDto.setUpdatedAt(userEntity.getUpdatedAt());

        return userResponseDto;

    }
}
