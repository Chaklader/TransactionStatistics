package com.transaction.statistics.service.impl;

import com.transaction.statistics.dto.TransactionDto;
import com.transaction.statistics.model.Transaction;
import com.transaction.statistics.service.TransactionService;
import com.transaction.statistics.utls.TransactionCollectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


/**
 * Created by Chaklader on Apr, 2021
 */
@Transactional
@Service

@Slf4j
public class TransactionServiceImpl implements TransactionService {



    @Autowired
    private TransactionCollectors transactionCollectors;


    @Override
    public Transaction createTransaction(TransactionDto transactionDto) {

        try {
            final Transaction transaction = Transaction.builder()
                                          .uuid(UUID.randomUUID())
                                          .amount(transactionDto.getAmount())
                                          .timestamp(transactionDto.getTimestamp())
                                          .build();

            transactionCollectors.addTransaction(transaction);

            return transaction;

        } catch (Exception ex) {

            log.error("Error occurred while create new transaction ::" + ex.getMessage());
        }

        return null;
    }

    @CacheEvict(value = "transactions", allEntries = true)
    @Override
    public boolean deleteAllTransactions() {

        try {
            final List<Transaction> transactions = transactionCollectors.getTransactionList();

            log.info("deleting all the transactions for the last 60 seconds");
            transactions.clear();

            return true;

        } catch (Exception ex) {

            log.error("Error occurred while deleting all the transactions ::" + ex.getMessage());
        }

        return false;
    }
}
