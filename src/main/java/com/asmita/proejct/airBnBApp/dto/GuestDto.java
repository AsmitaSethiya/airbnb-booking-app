package com.asmita.proejct.airBnBApp.dto;

import com.asmita.proejct.airBnBApp.entity.User;
import com.asmita.proejct.airBnBApp.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class GuestDto {
    private Long id;
    private User user;
    private  String name;
    private Gender gender;
    private Integer age;

}
