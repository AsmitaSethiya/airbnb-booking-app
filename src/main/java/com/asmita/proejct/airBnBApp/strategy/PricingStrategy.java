package com.asmita.proejct.airBnBApp.strategy;

import com.asmita.proejct.airBnBApp.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculatePrice(Inventory inventory);

}
