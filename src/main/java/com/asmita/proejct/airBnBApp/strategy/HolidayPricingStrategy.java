package com.asmita.proejct.airBnBApp.strategy;

import com.asmita.proejct.airBnBApp.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;


@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy{

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);
        boolean isHoliday = true; //can an api or check with local data
        if(isHoliday)
        {
            price = price.multiply(BigDecimal.valueOf(1.25));
        }
        return price;
    }
}
