package com.asmita.proejct.airBnBApp.service;

import com.asmita.proejct.airBnBApp.dto.*;
import com.asmita.proejct.airBnBApp.entity.Hotel;
import com.asmita.proejct.airBnBApp.entity.Inventory;
import com.asmita.proejct.airBnBApp.entity.Room;
import com.asmita.proejct.airBnBApp.entity.User;
import com.asmita.proejct.airBnBApp.exception.ResourceNotFoundException;
import com.asmita.proejct.airBnBApp.repository.HotelMinPriceRepository;
import com.asmita.proejct.airBnBApp.repository.InventoryRepository;
import com.asmita.proejct.airBnBApp.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.asmita.proejct.airBnBApp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService{

    private final InventoryRepository inventoryRepository;
    private final ModelMapper modelMapper;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private  final RoomRepository roomRepository;

    @Override
    public void initializeForAYear(Room room) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusYears(1);
        for(; !today.isAfter(endDate); today=today.plusDays(1))
        {
            Inventory inventory = Inventory.builder()
                    .hotel(room.getHotel())
                    .room(room)
                    .bookedCount(0)
                    .reserveCount(0)
                    .city(room.getHotel().getCity())
                    .date(today)
                    .price(room.getBasePrice())
                    .surgeFactor(BigDecimal.ONE)
                    .totalCount(room.getTotalCount())
                    .closed(false)
                    .build();

            inventoryRepository.save(inventory);
        }

    }

    @Override
    public void deleteAllInventories(Room room) {
     //  LocalDate today = LocalDate.now();
       log.info("Deleting the inventory of rooms with id{}",room.getId());
        inventoryRepository.deleteByRoom( room);
    }

    @Override
    public Page<HotelPriceDto> searchHotels(HotelSearchRequest hotelSearchRequest) {
        log.info("Searching hotels for {} city, from {} to {}",hotelSearchRequest.getCity(),hotelSearchRequest.getStartDate(),hotelSearchRequest.getStartDate());
        Pageable pageable = PageRequest.of(hotelSearchRequest.getPage(),hotelSearchRequest.getSize());
        Long dateCount = ChronoUnit.DAYS.between(hotelSearchRequest.getStartDate(), hotelSearchRequest.getEndDate()) + 1;


//
//        Page<Hotel> hotelPage = inventoryRepository.findHotelsWithAvailableInventory(hotelSearchRequest.getCity(),
//                  hotelSearchRequest.getStartDate(),hotelSearchRequest.getEndDate(),hotelSearchRequest.getRoomsCount(),
//                dateCount, pageable);

        Page<HotelPriceDto> hotelPage = hotelMinPriceRepository.findHotelsWithAvailableInventory(hotelSearchRequest.getCity(),
                hotelSearchRequest.getStartDate(),hotelSearchRequest.getEndDate(),hotelSearchRequest.getRoomsCount(),
                dateCount, pageable);

            return hotelPage;
    }

    @Override
    public List<InventoryDto> getAllInventoryByRoom(Long roomId) {
        log.info("Getting all inventory by room for room with id:{}"+roomId);
        Room room  = roomRepository.findById(roomId)
                .orElseThrow(()-> new ResourceNotFoundException("Room not found with room id: {}"+roomId));

        User user  = getCurrentUser();

        if(!user.equals(room.getHotel().getOwner())) throw new AccessDeniedException("You are not the owner of room  with id: {}"+roomId);


        return inventoryRepository.findByRoomOrderByDate(room).stream()
                .map((element) -> modelMapper.map(element,InventoryDto.class)).collect(Collectors.toList());

    }

    @Transactional
    public void updateInventory(Long roomId, UpdateInventoryRequestDto updateInventoryRequestDto)
    {
        log.info("Updating all inventory by room for room with id: {} between date range : ",roomId,updateInventoryRequestDto.getStartDate(),updateInventoryRequestDto.getEndDate());

        Room room  = roomRepository.findById(roomId)
                .orElseThrow(()-> new ResourceNotFoundException("Room not found with room id: {}"+roomId));

        User user  = getCurrentUser();

        if(!user.equals(room.getHotel().getOwner())) throw new AccessDeniedException("You are not the owner of room  with id: {}"+roomId);

        inventoryRepository.getInventoryAndLockedBeforeUpdate(roomId,updateInventoryRequestDto.getStartDate(),updateInventoryRequestDto.getEndDate());

        inventoryRepository.updateInventory(roomId,updateInventoryRequestDto.getStartDate(),updateInventoryRequestDto.getEndDate(),
                updateInventoryRequestDto.getClosed(),updateInventoryRequestDto.getSurgeFactor());


    }
}
