package com.asmita.proejct.airBnBApp.service;

import com.asmita.proejct.airBnBApp.entity.Hotel;
import com.asmita.proejct.airBnBApp.entity.HotelMinPrice;
import com.asmita.proejct.airBnBApp.entity.Inventory;
import com.asmita.proejct.airBnBApp.repository.HotelMinPriceRepository;
import com.asmita.proejct.airBnBApp.repository.HotelRepository;
import com.asmita.proejct.airBnBApp.repository.InventoryRepository;
import com.asmita.proejct.airBnBApp.strategy.PricingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PricingUpdateService {

    //scheduler to update the inventory and HotelMinPrice tables every hour
   private final PricingService pricingService;
   private final HotelRepository hotelRepository;
   private final InventoryRepository inventoryRepository;
   private final HotelMinPriceRepository hotelMinPriceRepository;

  // @Scheduled(cron = "*/5 * * * * *")
   @Scheduled(cron = "0 0 * * * *")
   public void updatePrice()
    {
        int page = 0;
        int batchSize = 100;

        while(true)
        {
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page,batchSize));
            if(hotelPage.isEmpty())
            {
                break;
            }
            hotelPage.getContent().forEach(this::updateHotelPrice);
            page++;
        }
    }


    private void updateHotelPrice(Hotel hotel)
    {
        log.info("updaeting hotel prices for hotel Id {}", hotel.getId());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusYears(1);

        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel,startDate,endDate);

        updateInventoryPrices(inventoryList);
        updateHotelMinPrice(hotel,inventoryList,startDate,endDate);
    }


    private void updateHotelMinPrice(Hotel hotel,List<Inventory> inventoryList,LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, BigDecimal> dailyMinPrices  = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice,Collectors.minBy(Comparator.naturalOrder()))
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,e-> e.getValue().orElse(BigDecimal.ZERO)));

        List<HotelMinPrice> hotelPrices = new ArrayList<>();
        dailyMinPrices.forEach((date, price) ->{
            HotelMinPrice hotelPrice = hotelMinPriceRepository.findByHotelAndDate(hotel,date)
                    .orElse(new HotelMinPrice(hotel,date));
            hotelPrice.setPrice(price);
            hotelPrices.add(hotelPrice);
        } );

        hotelMinPriceRepository.saveAll(hotelPrices);

   }

    private  void updateInventoryPrices(List<Inventory> inventoryList)
    {
        inventoryList.forEach(inventory ->
                {
                    BigDecimal dynamicPrice = pricingService.calculateDynamicPricing(inventory);
                    inventory.setPrice(dynamicPrice);
                });
        inventoryRepository.saveAll(inventoryList);

    }
}
