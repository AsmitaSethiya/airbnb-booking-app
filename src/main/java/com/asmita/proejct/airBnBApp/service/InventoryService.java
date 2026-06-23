package com.asmita.proejct.airBnBApp.service;

import com.asmita.proejct.airBnBApp.dto.*;
import com.asmita.proejct.airBnBApp.entity.Room;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {

    static void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto) {
    }

    void initializeForAYear(Room room);
        void deleteAllInventories(Room room);

        Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest);

   List<InventoryDto> getAllInventoryByRoom(Long roomId);
}
