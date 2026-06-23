package com.asmita.proejct.airBnBApp.service;

import com.asmita.proejct.airBnBApp.dto.BookingDto;
import com.asmita.proejct.airBnBApp.dto.BookingRequest;
import com.asmita.proejct.airBnBApp.dto.GuestDto;
import com.asmita.proejct.airBnBApp.dto.HotelReportDto;
import com.asmita.proejct.airBnBApp.entity.*;
import com.asmita.proejct.airBnBApp.entity.enums.BookingStatus;
import com.asmita.proejct.airBnBApp.exception.ResourceNotFoundException;
import com.asmita.proejct.airBnBApp.exception.UnAuthorizedException;
import com.asmita.proejct.airBnBApp.repository.*;
import com.asmita.proejct.airBnBApp.strategy.PricingService;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.sun.source.tree.LambdaExpressionTree;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.asmita.proejct.airBnBApp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements  BookingService{

    private  final ModelMapper modelMapper;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final InventoryRepository inventoryRepository;
    private  final GuestRepository guestRepository;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public BookingDto initializeBooking(BookingRequest bookingRequest) {
log.info("Initializing booking for hotel  : {}, room : {}, date :  {} to date {}",
        bookingRequest.getHotelId(),bookingRequest.getRoomId(),bookingRequest.getCheckInDate(),bookingRequest.getCheckoutDate());

        Hotel hotel  = hotelRepository.findById(bookingRequest.getHotelId()).
                orElseThrow(()->new ResourceNotFoundException("Hotel not found with id: "+bookingRequest.getHotelId()));

        Room room = roomRepository.findById(bookingRequest.getRoomId())
                .orElseThrow(()->new ResourceNotFoundException("Room not found with id:"+bookingRequest.getRoomId()));

        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(room.getId(),
                bookingRequest.getCheckInDate(), bookingRequest.getCheckoutDate(),bookingRequest.getRoomsCount());

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getCheckInDate(), bookingRequest.getCheckoutDate()) + 1;

        if (inventoryList.size() != daysCount) {
            throw new IllegalStateException("Room not available anymore");
        }

        //Reserve the room / update the booked count of inventories
//        for(Inventory inventory:inventoryList)
//        {
//            inventory.setReserveCount(inventory.getReserveCount() + bookingRequest.getRoomsCount());
//        }
//
//        inventoryRepository.saveAll(inventoryList);

        inventoryRepository.initBooking(room.getId(),bookingRequest.getCheckInDate(),
                bookingRequest.getCheckoutDate(),bookingRequest.getRoomsCount());


        // calculate dynamic amount
        BigDecimal priceForOneRoom =pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequest.getRoomsCount()));



        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequest.getCheckInDate())
                .checkOutDate(bookingRequest.getCheckoutDate())
                .user(getCurrentUser())
                .roomCount(bookingRequest.getRoomsCount())
                .amount(totalPrice)
                .build();

        booking = bookingRepository.save(booking);

        return modelMapper.map(booking,BookingDto.class);
    }



    @Override
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding guests for booking with id  : {} ",bookingId);

        Booking booking  = bookingRepository.findById(bookingId).
                orElseThrow(()->new ResourceNotFoundException("Booking not found with id: "+ bookingId));

        User user = getCurrentUser();

        log.info("Current User Id: {}", user.getId());
        log.info("Booking User Id: {}", booking.getUser().getId());



        if(!user.equals(booking.getUser()))
        {
            throw new UnAuthorizedException("Boooking does not belong to this user with id:"+user.getId());
        }

        if(hasBookingExpired(booking))
        {
            throw new IllegalStateException("Booking has already expired");
        }

        if(booking.getBookingStatus() != BookingStatus.RESERVED)
        {
            throw new IllegalStateException("Booking is not under reserve state, can not add guests ");
        }

        for(GuestDto guestDto: guestDtoList)
        {
            log.info("Guest DTO Name: {}", guestDto.getName());
            Guest guest = modelMapper.map(guestDto,Guest.class);
            guest.setUser(user);
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }

        booking.setBookingStatus(BookingStatus.GUEST_ADDED);
        booking = bookingRepository.save(booking);
        return modelMapper.map(booking,BookingDto.class);
    }

    @Override
    @Transactional
    public String initiatePayments(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()-> new ResourceNotFoundException("Booking not found with id:"+bookingId)
        );

        User user = getCurrentUser();

        if(!user.equals(booking.getUser()))
        {
            throw new UnAuthorizedException("Booking does not belong to this user with id:"+user.getId());
        }

        if(hasBookingExpired(booking))
        {
            throw new IllegalStateException("Booking has already expired");
        }

       String sessionUrl =  checkoutService.getCheckOutSession(booking,
               frontendUrl+"/payments/success",frontendUrl+"/payments/failure");

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);
        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if("checkout.session.completed".equals(event.getType()))
        {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if(session == null) return;

            String sessionId = session.getId();
            Booking booking =
                    bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(()-> new ResourceNotFoundException("Booking not found for session Id:"+sessionId));

            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate()
            ,booking.getCheckOutDate(),booking.getRoomCount());

            inventoryRepository.confirmBooking(booking.getRoom().getId(),booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomCount());

            log.info("Successfully confirmed the booking for booking id: {}",booking.getId());

        }
        else
        {
            log.warn("Unhandled event type:{}", event.getType());
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()-> new ResourceNotFoundException("Booking not found with id:"+bookingId)
        );

        User user = getCurrentUser();

        if(!user.equals(booking.getUser()))
        {
            throw new UnAuthorizedException("Booking does not belong to this user with id:"+user.getId());
        }

        if(booking.getBookingStatus() != BookingStatus.CONFIRMED)
        {
            throw  new IllegalStateException("Only confirmed booking can be cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(), booking.getCheckInDate()
                ,booking.getCheckOutDate(),booking.getRoomCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId(),booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomCount());

        //handle the refund
        try
        {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundCreateParams = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())
                    .build();

            Refund.create(refundCreateParams);

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()-> new ResourceNotFoundException("Booking not found with id:"+bookingId)
        );

        User user = getCurrentUser();

        if(!user.equals(booking.getUser()))
        {
            throw new UnAuthorizedException("Booking does not belong to this user with id:"+user.getId());
        }
        return booking.getBookingStatus().name();
    }

    @Override
    public List<BookingDto> getAllBookingByHotelId(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id:{}"+hotelId));

        User user = getCurrentUser();

        log.info("Getting all booking for the hotel with id: " + hotelId);

        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of the hotel with id:{}"+hotelId);

        List<Booking> bookings = bookingRepository.findByHotel(hotel);

        return bookings
                .stream()
                .map((element) -> modelMapper.map(element,BookingDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {

        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id:{}"+hotelId));

        User user = getCurrentUser();

        log.info("Generating report for hotel with ID: " + hotelId);

        if(!user.equals(hotel.getOwner())) throw new AccessDeniedException("You are not the owner of the hotel with id:{}"+hotelId);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel
        ,startDateTime,endDateTime);

        Long totalConfirmedBookings = bookings
                .stream()
                .filter(booking ->booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();

        BigDecimal totalRevenueOfConfirmedBooking = bookings
                .stream()
                .filter(booking -> booking.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        BigDecimal avgRevenue = totalConfirmedBookings == 0 ? BigDecimal.ZERO
                :  totalRevenueOfConfirmedBooking
                .divide(BigDecimal.valueOf(totalConfirmedBookings), RoundingMode.HALF_UP);

        return new HotelReportDto(totalConfirmedBookings,totalRevenueOfConfirmedBooking,avgRevenue);
    }

    @Override
    public List<BookingDto> getMyBoookings() {
        User user = getCurrentUser();

        return  bookingRepository.findByUser(user).stream().map((element) -> modelMapper
                .map(element,BookingDto.class))
                .collect(Collectors.toList());


    }


    public  boolean hasBookingExpired(Booking booking)
    {
        return  booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }
//
//    public User getCurrentUser()
//    {
//        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    }
}
