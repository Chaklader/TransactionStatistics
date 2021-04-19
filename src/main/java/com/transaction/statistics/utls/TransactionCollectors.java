package com.transaction.statistics.utls;


import com.transaction.statistics.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * Created by Chaklader on Apr, 2021
 */

@Transactional
@Slf4j

@Component
public class TransactionCollectors {


    private static final List<Transaction> transactionList;

    static {

        transactionList = new ArrayList<>();
    }


    public void addTransaction(Transaction transaction) {

        try {
            final List<Transaction> transactionList = getTransactionList();
            final boolean isTransactionAdded = transactionList.add(transaction);

            transactionList.sort(Comparator.comparing(Transaction::getTimestamp));

            log.info("add a new transaction to the transactions collector");

        } catch (Exception ex) {

            log.error("we cant add the transaction to the collector with reason " + ex.getMessage());
        }
    }

    public List<Transaction> getTransactionList() {

        return transactionList;
    }
}
