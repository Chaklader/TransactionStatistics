package com.n26.config;

import com.n26.utls.TransactionCollectors;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


/**
 * Created by Chaklader on Apr, 2021
 */
@Configuration
//@EnableScheduling
//@EnableAsync

@Slf4j
public class ApplicationConfig {


//    @Bean
//    public ModelMapper cacheManager() {
//
//        return new ModelMapper();
//    }

    @Async
    @Scheduled(fixedDelay = 60 * 1000)
    public void scheduleTransactionClearance() {

        log.info("Performing the clearance the transactions in each 60 sec from the storage");
        TransactionCollectors.getTransactionList().clear();
    }

}
