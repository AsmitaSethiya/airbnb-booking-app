package com.asmita.proejct.airBnBApp.controller;

import com.asmita.proejct.airBnBApp.dto.BookingDto;
import com.asmita.proejct.airBnBApp.dto.HotelDto;
import com.asmita.proejct.airBnBApp.dto.HotelReportDto;
import com.asmita.proejct.airBnBApp.entity.Hotel;
import com.asmita.proejct.airBnBApp.service.BookingService;
import com.asmita.proejct.airBnBApp.service.HotelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelService hotelService;
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<HotelDto> creteNewHotel(@RequestBody HotelDto hotelDto)
    {
     log.info("Attempting to create a new hotel with name: " + hotelDto.getName());
     HotelDto hotelDto1 = hotelService.createNewHotel(hotelDto);
     return  new ResponseEntity<>(hotelDto1, HttpStatus.CREATED);
    }

    @GetMapping("/{hotelId}")
    public ResponseEntity<HotelDto> getHotelById(@PathVariable Long hotelId)
    {
        HotelDto hotelDto = hotelService.getHotelById(hotelId);
        return  ResponseEntity.ok(hotelDto);
    }

    @PutMapping("/{hotelId}")
    public ResponseEntity<HotelDto> updateHotelById(@PathVariable Long hotelId, @RequestBody HotelDto hotelDto)
    {
        HotelDto hotel = hotelService.updateHotelById(hotelId,hotelDto);
        return  ResponseEntity.ok(hotel);
    }

    @DeleteMapping("/{hotelId}")
    public ResponseEntity<Void> deleteHotelById(@PathVariable Long hotelId)
    {
        hotelService.deleteHotelById(hotelId);
        return  ResponseEntity.noContent().build();
    }

    @PatchMapping("/{hotelId}/activate")
    public  ResponseEntity<Void> activateHotel(@PathVariable Long hotelId)
    {
        hotelService.activateHotel(hotelId);
        return  ResponseEntity.noContent().build();
    }


    @GetMapping
    public  ResponseEntity<List<HotelDto>> getAllHotel()
    {
        return  ResponseEntity.ok(hotelService.getAllHotel());
    }

    @GetMapping("/{hotelId}/bookings")
    public ResponseEntity<List<BookingDto>> getAllBookingByHotelId(@PathVariable Long hotelId)
    {
        return ResponseEntity.ok(bookingService.getAllBookingByHotelId(hotelId));
    }

    @GetMapping("/{hotelId}/reports")
    public ResponseEntity<HotelReportDto> getHotelReport(@PathVariable Long hotelId,
                                                         @RequestParam(required = false) LocalDate startDate,
                                                         @RequestParam(required = false) LocalDate endDate)
    {
        if (startDate == null) startDate = LocalDate.now().minusMonths(1);
        if (endDate == null) endDate = LocalDate.now();

        return ResponseEntity.ok(bookingService.getHotelReport(hotelId,startDate,endDate));
    }











}
