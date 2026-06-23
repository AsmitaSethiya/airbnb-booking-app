package com.asmita.proejct.airBnBApp.service;

import com.asmita.proejct.airBnBApp.entity.Booking;

public interface CheckoutService {

    String getCheckOutSession(Booking booking, String successUrl, String failureUrl);
}
