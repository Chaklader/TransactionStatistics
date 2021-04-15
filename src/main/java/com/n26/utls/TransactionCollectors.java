package com.n26.utls;

import com.n26.model.Transaction;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chaklader on Apr, 2021
 */
@Slf4j
public class TransactionCollectors {

    private static final List<Transaction> transactionList;

    static {

        transactionList = new ArrayList<>();
    }

    public synchronized static void addTransaction(Transaction transaction) {

        try {

            final boolean isTransactionAdded = getTransactionList().add(transaction);

            log.info("add a new transaction to the transactions collector");

        } catch (Exception ex) {

            log.error("we cant add the transaction to the collector with reason " + ex.getMessage());
        }
    }

    public synchronized static List<Transaction> getTransactionList() {

        return transactionList;
    }
}
