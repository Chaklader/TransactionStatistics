package com.n26.service.impl;

import com.n26.dto.TransactionDto;
import com.n26.model.Transaction;
import com.n26.service.TransactionService;
import com.n26.utls.TransactionCollectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


/**
 * Created by Chaklader on Apr, 2021
 */
@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {


    @Override
    public synchronized Transaction createTransaction(TransactionDto transactionDto) {

        try {
            final Transaction transaction = Transaction.builder()
                                          .uuid(UUID.randomUUID())
                                          .amount(transactionDto.getAmount())
                                          .timestamp(transactionDto.getTimestamp())
                                          .build();

            TransactionCollectors.addTransaction(transaction);

            return transaction;

        } catch (Exception ex) {
            log.error("Error occurred while create new transaction ::" + ex.getMessage());
        }

        return null;
    }

    @CacheEvict(value = "transactions", allEntries = true)
    @Override
    public synchronized boolean deleteAllTransactions() {

        try {
            final List<Transaction> transactions = TransactionCollectors.getTransactionList();

            log.info("deleting all the transactions for the last 60 seconds");
            transactions.clear();

            return true;

        } catch (Exception ex) {

            log.error("Error occurred while deleting all the transactions ::" + ex.getMessage());
        }

        return false;
    }
}
