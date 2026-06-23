package com.asmita.proejct.airBnBApp.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class HotelContactInfo {
private String address;
private String phoneNumber;
private String email;
private String location;
}
