package com.asmita.proejct.airBnBApp.dto;

import com.asmita.proejct.airBnBApp.entity.Guest;
import com.asmita.proejct.airBnBApp.entity.Hotel;
import com.asmita.proejct.airBnBApp.entity.Room;
import com.asmita.proejct.airBnBApp.entity.User;
import com.asmita.proejct.airBnBApp.entity.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class BookingDto {
    private Long id;
    private Integer roomCount;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime createdAt;
    private LocalDateTime updateAt;
    private BookingStatus bookingStatus;
    private Set<GuestDto> guests;
    private BigDecimal amount;
}
