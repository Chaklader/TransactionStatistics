package com.n26.service.impl;


import com.n26.model.Statistics;
import com.n26.model.Transaction;
import com.n26.service.StatisticsService;
import com.n26.utls.TransactionCollectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


/**
 * Created by Chaklader on Apr, 2021
 */
@Slf4j

@Service
public class StatisticsServiceImpl implements StatisticsService {


    @Override
    public synchronized Statistics getTransactionsStatistics() {

        try {

            final List<Transaction> transactions = getAllTransactions();

            if (transactions.isEmpty()) {

                return createEmptyStatisticsPojo();
            }


            deleteOlderTransaction(transactions);

            final List<BigDecimal> transactionsAmountsList = transactions.stream().map(Transaction::getTransactionAmount).collect(Collectors.toList());


            final long totalNumOfTransactions = BigDecimal.valueOf(transactions.size()).longValue();


            // make the calculations
            final BigDecimal sumOfTransactionsAmount = transactionsAmountsList.stream().reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

            final BigDecimal averageTransactionAmount = sumOfTransactionsAmount.divide(BigDecimal.valueOf(totalNumOfTransactions), 2, RoundingMode.HALF_UP);

            final BigDecimal maxTransactionAmount = transactionsAmountsList.stream().max((Comparator.naturalOrder())).orElseThrow(NoSuchElementException::new).setScale(2, RoundingMode.HALF_UP);
            final BigDecimal minTransactionAmount = transactionsAmountsList.stream().min((Comparator.naturalOrder())).orElseThrow(NoSuchElementException::new).setScale(2, RoundingMode.HALF_UP);


            // create the Statistics object
            final Statistics statistics = Statistics.builder()

                                              .sum(String.valueOf(sumOfTransactionsAmount))
                                              .avg(String.valueOf(averageTransactionAmount))
                                              .max(String.valueOf(maxTransactionAmount))
                                              .min(String.valueOf(minTransactionAmount))
                                              .count(totalNumOfTransactions)

                                              .build();

            log.info("create new statistics for the last 60 sec transactions with value = " + statistics.toString());

            return statistics;

        } catch (Exception ex) {

            log.error("Error occurred while getting the transaction statistics ::" + ex.getMessage());
        }

        return null;
    }


    @CacheEvict(value = "transactions", allEntries = true)
    public List<Transaction> getAllTransactions() {

        final List<Transaction> transactions = TransactionCollectors.getTransactionList();

        if (transactions == null) {

            log.info("we received null after calling the transaction list");
        }

        return transactions;
    }


    private static Statistics createEmptyStatisticsPojo() {

        final BigDecimal zero = (BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        final Statistics EMPTY_STATISTICS = Statistics.builder()
                                                .sum(String.valueOf(zero))
                                                .avg(String.valueOf(zero))
                                                .max(String.valueOf(zero))
                                                .min(String.valueOf(zero))
                                                .count(0L)
                                                .build();

        log.info("Create an empty statistics for the transactions ..");

        return EMPTY_STATISTICS;
    }

    private void deleteOlderTransaction(List<Transaction> transactions) {

        final int initialTransactionsSize = transactions.size();

        final boolean isOlderTransactionsDeleted = transactions.removeIf(transaction -> Duration.between(transaction.getLocalDateTime(), LocalDateTime.now(ZoneOffset.UTC)).toSeconds() >= 60);

        if (isOlderTransactionsDeleted) {

            log.info("we have deleted translations that are older than 60 seconds and the deleted transactions count =" + (initialTransactionsSize - transactions.size()));
            return;
        }

        log.info("No transactions are deleted...");
    }


}
