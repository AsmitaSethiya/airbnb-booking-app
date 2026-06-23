package com.asmita.proejct.airBnBApp.service;


import com.asmita.proejct.airBnBApp.dto.HotelDto;
import com.asmita.proejct.airBnBApp.dto.HotelInfoDto;
import com.asmita.proejct.airBnBApp.dto.RoomDto;
import com.asmita.proejct.airBnBApp.entity.Hotel;
import com.asmita.proejct.airBnBApp.entity.Room;
import com.asmita.proejct.airBnBApp.entity.User;
import com.asmita.proejct.airBnBApp.exception.ResourceNotFoundException;
import com.asmita.proejct.airBnBApp.exception.UnAuthorizedException;
import com.asmita.proejct.airBnBApp.repository.HotelRepository;
import com.asmita.proejct.airBnBApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.lang.module.ResolutionException;
import java.util.List;
import java.util.stream.Collectors;

import static com.asmita.proejct.airBnBApp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService{

    //constructor injection
    private final HotelRepository hotelRepository;
    private  final InventoryService inventoryService;
    private  final RoomRepository roomRepository;
    private final ModelMapper modelMapper;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating a new hotel name : {}, ",hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto,Hotel.class); //converting DTO to Entity
        hotel.setActive(false);

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);

        hotel = hotelRepository.save(hotel);
        log.info("Created a new hotel with Id:{}",hotelDto.getId());
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Getting a  hotel with Id : {}, ",id);
        Hotel hotel =  hotelRepository.
                findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Hotel not found with id: " + id));


        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorizedException("This use does not own this hotel with id:"+id);
        }

        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating the hotel with Id: {}",id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id : " + id));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorizedException("This use does not own this hotel with id:"+id);
        }


        modelMapper.map(hotelDto,hotel);
        hotel.setId(id);
        hotel = hotelRepository.save(hotel);
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id) {
      //  boolean exists = hotelRepository.existsById(id);
        //if(!exists) throw new ResourceNotFoundException("hotel not found with Id: " + id);
        Hotel hotel = hotelRepository
                .findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id : " + id));


        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorizedException("This use does not own this hotel with id:"+id);
        }

        for(Room room: hotel.getRooms())
        {
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("activating the hotel with Id: {}",hotelId);
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id : " + hotelId));

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner()))
        {
            throw new UnAuthorizedException("This use does not own this hotel with id:"+hotel);
        }

        hotel.setActive(true);
        // assuming only do it once
        for(Room room: hotel.getRooms())
        {
            inventoryService.initializeForAYear(room);
        }
    }


    //public method
    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        Hotel hotel = hotelRepository
                .findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id : " + hotelId));

        List<RoomDto> rooms = hotel.getRooms()
                .stream()
                .map((element)-> modelMapper.map(element,RoomDto.class))
                .toList();

        return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class),rooms);
    }

    @Override
    public List<HotelDto> getAllHotel() {
        User user =getCurrentUser();
        log.info("Getting all hotels for the admin user with id : {}",user.getId());

        List<Hotel> hotels = hotelRepository.findByOwner(user);
        return hotels
                .stream()
                .map((element)->modelMapper.map(element,HotelDto.class))
                .collect(Collectors.toList());
    }


}
