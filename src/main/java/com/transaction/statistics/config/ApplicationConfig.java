package com.transaction.statistics.config;

import com.google.common.cache.CacheBuilder;
import com.transaction.statistics.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.guava.GuavaCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.TimeUnit;

import static com.transaction.statistics.utls.MessageConstant.CACHE_ONE;


/**
 * Created by Chaklader on Apr, 2021
 */


@Slf4j

@EnableCaching
@Configuration
@EnableScheduling
@EnableAsync
public class ApplicationConfig {


    @Autowired
    private TransactionService transactionService;


    @Bean
    public Cache cacheOne() {

        Cache cache = new GuavaCache(CACHE_ONE, CacheBuilder.newBuilder()
                                                    .expireAfterWrite(60, TimeUnit.SECONDS)
                                                    .build());

        return cache;
    }


    // TODO: implement the TTL in cache config

    @Async
    @Scheduled(fixedDelay = 60 * 1000, initialDelay = 60 * 1000)
    public void scheduleTransactionClearance() {

        log.info("Performing the clearance the transactions in each 60 sec from the storage");
        transactionService.deleteAllTransactions();
    }

}
