package com.asmita.proejct.airBnBApp.service;

import com.asmita.proejct.airBnBApp.dto.HotelDto;
import com.asmita.proejct.airBnBApp.dto.HotelInfoDto;
import com.asmita.proejct.airBnBApp.entity.Hotel;

import java.util.List;

public interface HotelService {

    HotelDto createNewHotel(HotelDto hotelDto);

    HotelDto getHotelById(Long id);

    HotelDto updateHotelById(Long id, HotelDto hotelDto);

    void deleteHotelById(Long id);

    void activateHotel(Long hotelId);

    HotelInfoDto getHotelInfoById(Long hotelId);

    List<HotelDto> getAllHotel();
}
