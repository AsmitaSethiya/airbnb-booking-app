package com.asmita.proejct.airBnBApp.dto;

import com.asmita.proejct.airBnBApp.entity.HotelContactInfo;

import lombok.Data;

@Data
public class HotelDto {


    private Long id;
    private String name;
    private  String city;
    private String[] photos;
    private String[] amenities;
    private HotelContactInfo contactInfo;
    private Boolean active;



}
