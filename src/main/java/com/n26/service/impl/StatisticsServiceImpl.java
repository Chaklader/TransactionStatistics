package com.n26.service.impl;

import com.n26.model.Statistics;
import com.n26.model.Transaction;
import com.n26.service.StatisticsService;
import com.n26.utls.TransactionCollectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created by Chaklader on Apr, 2021
 */
@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {


    @Override
    public synchronized Statistics getTransactionsStatistics() {

        try {

            final List<Transaction> transactions = TransactionCollectors.getTransactionList();

            if (transactions.isEmpty()) {

                return Statistics.builder().build();
            }

            final Stream<BigDecimal> transactionsAmounts = transactions.stream().map(Transaction::getTransactionAmount);

            final List<BigDecimal> transactionsAmountsList = transactionsAmounts.collect(Collectors.toList());


            final long totalNumOfTransactions = BigDecimal.valueOf(transactions.size()).longValue();

            final BigDecimal sumOfTransactionsAmount = transactionsAmountsList.stream().reduce(BigDecimal.ZERO, BigDecimal::add);

            final BigDecimal averageTransactionAmount = sumOfTransactionsAmount.divide(BigDecimal.valueOf(totalNumOfTransactions), 2, RoundingMode.HALF_UP);

            final BigDecimal maxTransactionAmount = transactionsAmountsList.stream().max((Comparator.naturalOrder())).orElseThrow(NoSuchElementException::new);
            final BigDecimal minTransactionAmount = transactionsAmountsList.stream().min((Comparator.naturalOrder())).orElseThrow(NoSuchElementException::new);

            final Statistics statistics = Statistics.builder()

                                              .sum(sumOfTransactionsAmount)
                                              .avg(averageTransactionAmount)
                                              .max(maxTransactionAmount)
                                              .min(minTransactionAmount)
                                              .count(totalNumOfTransactions)

                                              .build();

            return statistics;

        } catch (Exception ex) {

            log.error("Error occurred while getting the transaction statistics ::" + ex.getMessage());
        }

        return null;
    }
}
