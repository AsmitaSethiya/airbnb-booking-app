package com.asmita.proejct.airBnBApp.service;

import com.asmita.proejct.airBnBApp.dto.RoomDto;
import com.asmita.proejct.airBnBApp.entity.Hotel;
import com.asmita.proejct.airBnBApp.entity.Room;
import com.asmita.proejct.airBnBApp.entity.User;
import com.asmita.proejct.airBnBApp.exception.ResourceNotFoundException;
import com.asmita.proejct.airBnBApp.exception.UnAuthorizedException;
import com.asmita.proejct.airBnBApp.repository.HotelRepository;
import com.asmita.proejct.airBnBApp.repository.RoomRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.asmita.proejct.airBnBApp.util.AppUtils.getCurrentUser;

@Service
@Data
@Slf4j
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryService inventoryService;
    private final ModelMapper modelMapper;


    @Override
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
        log.info("Creating a new room in hotel with ID : {}", hotelId);

        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : " + hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorizedException("This use does not own this hotel with id:"+hotelId);
        }
        Room room = modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room = roomRepository.save(room);

        //TODO: create inventory as soon as room is created and if hotel os active
        if(hotel.getActive())
        {
            inventoryService.initializeForAYear(room);
        }


        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomInHotel(Long hotelId) {
        log.info("Getting all room in hotel with ID : {}", hotelId);

        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : " + hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorizedException("This use does not own this hotel with id:"+hotelId);
        }

        return hotel.getRooms()
                .stream()
                .map((element) -> modelMapper.map(element, RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting the room in hotel by ID : {}", roomId);

        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id : " + roomId));
        return modelMapper.map(room, RoomDto.class);
    }

    @Override
    public void deleteRoomById(Long roomId) {
        log.info("Deleting the room in hotel by ID : {}", roomId);
//        boolean exists = roomRepository.existsById(roomId);
//        if (!exists) {
//            throw new ResourceNotFoundException("Room not found with Id: " + roomId);
//        }

        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id : " + roomId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(room.getHotel().getOwner()))
        {
            throw new UnAuthorizedException("This user does not own this hotel with id:"+roomId);
        }

        inventoryService.deleteAllInventories(room);
        roomRepository.deleteById(roomId);
        //TODO: DELETE ALL FUTURE INVENTORY FOR THIS ROOM

    }

    @Override
    @Transactional
    public RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto) {
       log.info("Updating the room with ID: {}",roomId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id : " + hotelId));

        User user = getCurrentUser();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorizedException("This use does not own this hotel with id:"+hotelId);
        }

        Room room = roomRepository
                .findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id : " + roomId));


        modelMapper.map(roomDto, room);
        room.setId(roomId);

        //TODO : if price or inventory is updated then update the inventory for this room
        room = roomRepository.save(room);
        return modelMapper.map(room, RoomDto.class);
    }
}