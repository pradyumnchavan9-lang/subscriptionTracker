package com.subtracker.SubTracker.user;

import com.subtracker.SubTracker.common.PageMapper;
import com.subtracker.SubTracker.common.PageResponseDto;
import com.subtracker.SubTracker.enums.PlanType;
import com.subtracker.SubTracker.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PageMapper pageMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    //Create User
    public UserResponseDto createUser(CreateUserDto createUserDto) {

        UserEntity userEntity = userMapper.dtoToEntity(createUserDto);
        //ONLY ADMIN's CAN CREATE THROUGH THIS ENDPOINT (WE'LL ADD LATER)
        userEntity.onCreate();
        userEntity.setPassword(passwordEncoder.encode(createUserDto.getPassword()));
        userEntity.setRole(Role.ADMIN);
        userRepository.save(userEntity);
        return userMapper.entityToResponse(userEntity);
    }


    public UserResponseDto getUserById(Long userId) {

            UserEntity userEntity = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("User with id " + userId + " not found."));

            return userMapper.entityToResponse(userEntity);
    }

    public PageResponseDto<UserResponseDto> getAll(Pageable pageable) {

        Page<UserEntity> userEntityPage = userRepository.findAll(pageable);

        //Map entity to dto
        Page<UserResponseDto> userResponseDtos = userEntityPage.map(user -> userMapper.entityToResponse(user));

        return pageMapper.pageToPageDto(userResponseDtos);
    }

    public boolean deleteUser(Long userId) {
        if(userRepository.existsById(userId)){
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }


    //Get Current User
    public UserResponseDto getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserEntity user = userRepository.findByEmail(email).orElseThrow(
                () -> new NoSuchElementException(
                        "User with email " + email + " not found."
                )
        );

        return userMapper.entityToResponse(user);
    }

    public UserResponseDto updateCurrentUser(CreateUserDto createUserDto) {

        String name = createUserDto.getName();
        String updateEmail = createUserDto.getEmail();
        String password = createUserDto.getPassword();
        PlanType planType = createUserDto.getPlanType();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currEmail = authentication.getName();
        UserEntity user = userRepository.findByEmail(currEmail).orElseThrow(
                () -> new NoSuchElementException(
                        "User with email " + currEmail + " not found."
                )
        );

        if(name != null && !name.isEmpty()){
            user.setName(name);
        }
        if(updateEmail != null && !updateEmail.isEmpty()){
            user.setEmail(updateEmail);
        }
        if(password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        if(planType != null){
            user.setPlanType(planType);
        }

        userRepository.save(user);
        return userMapper.entityToResponse(user);
    }
}
