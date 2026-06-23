package com.asmita.proejct.airBnBApp.dto;

import com.asmita.proejct.airBnBApp.entity.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequestDto {

    private  String name;
    private LocalDate dateOfBirth;
    private Gender gender;
}
