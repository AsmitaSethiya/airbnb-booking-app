package com.asmita.proejct.airBnBApp.service;

import com.asmita.proejct.airBnBApp.dto.ProfileUpdateRequestDto;
import com.asmita.proejct.airBnBApp.dto.UserDto;
import com.asmita.proejct.airBnBApp.entity.User;

public interface UserService {
    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
