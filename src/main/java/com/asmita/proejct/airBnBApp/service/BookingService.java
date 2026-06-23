package com.asmita.proejct.airBnBApp.service;

import com.asmita.proejct.airBnBApp.dto.BookingDto;
import com.asmita.proejct.airBnBApp.dto.BookingRequest;
import com.asmita.proejct.airBnBApp.dto.GuestDto;
import com.asmita.proejct.airBnBApp.dto.HotelReportDto;
import com.stripe.model.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BookingService {

     BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);


    BookingDto initializeBooking(BookingRequest bookingRequest);

    String initiatePayments(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

     String getBookingStatus(Long bookingId);

    List<BookingDto> getAllBookingByHotelId(Long hotelId);

    HotelReportDto getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);

    List<BookingDto> getMyBoookings();
}
