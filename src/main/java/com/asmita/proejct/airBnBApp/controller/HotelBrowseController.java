package com.asmita.proejct.airBnBApp.controller;

import com.asmita.proejct.airBnBApp.dto.HotelDto;
import com.asmita.proejct.airBnBApp.dto.HotelInfoDto;
import com.asmita.proejct.airBnBApp.dto.HotelPriceDto;
import com.asmita.proejct.airBnBApp.dto.HotelSearchRequest;
import com.asmita.proejct.airBnBApp.repository.HotelRepository;
import com.asmita.proejct.airBnBApp.service.HotelService;
import com.asmita.proejct.airBnBApp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelBrowseController {

    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest)
    {
      var page =  inventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId)
    {
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));

    }
}
