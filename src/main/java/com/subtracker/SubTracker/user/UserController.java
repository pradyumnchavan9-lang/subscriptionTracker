package com.subtracker.SubTracker.user;

import com.subtracker.SubTracker.common.PageResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    //Create User
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody CreateUserDto createUserDto) {

        return new ResponseEntity<>(userService.createUser(createUserDto), HttpStatus.OK);
    }


    //Get user by user id
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable Long userId){
        return new ResponseEntity<>(userService.getUserById(userId),HttpStatus.OK);
    }


    //Get all Users
    @GetMapping
    public ResponseEntity<PageResponseDto<UserResponseDto>> getAll(@PageableDefault (page = 0,size = 10) Pageable pageable){

        return new ResponseEntity<>(userService.getAll(pageable),HttpStatus.OK);
    }


    //Delete User
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId){

        boolean deleted = userService.deleteUser(userId);
        if(deleted){
            return new ResponseEntity<>("User Deleted Successfully",HttpStatus.OK);
        }
        return new ResponseEntity<>("User Not Found",HttpStatus.NOT_FOUND);
    }
}
