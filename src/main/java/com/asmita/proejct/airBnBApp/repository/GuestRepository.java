package com.asmita.proejct.airBnBApp.repository;

import com.asmita.proejct.airBnBApp.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest,Long> {
}
