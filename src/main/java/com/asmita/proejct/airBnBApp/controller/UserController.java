package com.asmita.proejct.airBnBApp.controller;

import com.asmita.proejct.airBnBApp.dto.BookingDto;
import com.asmita.proejct.airBnBApp.dto.ProfileUpdateRequestDto;
import com.asmita.proejct.airBnBApp.dto.UserDto;
import com.asmita.proejct.airBnBApp.service.BookingService;
import com.asmita.proejct.airBnBApp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BookingService bookingService;

    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto)
    {
        userService.updateProfile(profileUpdateRequestDto);

        return  ResponseEntity.noContent().build();
    }

    @GetMapping("/myBookings")
    public ResponseEntity<List<BookingDto>> getMyBookings()
    {
        return  ResponseEntity.ok(bookingService.getMyBoookings());
    }


    @GetMapping("/profile")
    public ResponseEntity<UserDto> getMyProfile()
    {
        return  ResponseEntity.ok(userService.getMyProfile());
    }
}
