package com.transaction.statistics.config;

import com.transaction.statistics.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


/**
 * Created by Chaklader on Apr, 2021
 */
@Configuration
@EnableScheduling
@EnableAsync

@Slf4j
public class ApplicationConfig {


    @Autowired
    private TransactionService transactionService;

    // TODO: implement the TTL in cache config

    @Async
    @Scheduled(fixedDelay = 60 * 1000, initialDelay = 60 * 1000)
    public void scheduleTransactionClearance() {

        log.info("Performing the clearance the transactions in each 60 sec from the storage");
        transactionService.deleteAllTransactions();
    }

}
