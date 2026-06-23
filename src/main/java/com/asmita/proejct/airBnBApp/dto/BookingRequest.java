package com.asmita.proejct.airBnBApp.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequest {

    private Long hotelId;
    private Long roomId;
    private LocalDate checkInDate;
    private LocalDate checkoutDate;
    private Integer roomsCount;
}
