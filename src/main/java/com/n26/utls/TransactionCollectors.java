package com.n26.utls;

import com.n26.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Chaklader on Apr, 2021
 */
@Slf4j
@Component
public class TransactionCollectors {



    private static final List<Transaction> transactionList;

    static {

        transactionList = new ArrayList<>() {

            @Override
            public boolean add(Transaction transaction) {

                super.add(transaction);

                transactionList.sort(Comparator.comparing(Transaction::getTimestamp));
                return true;
            }
        };
    }

    public synchronized void addTransaction(Transaction transaction) {

        try {
            final List<Transaction> transactionList = getTransactionList();
            final boolean isTransactionAdded = transactionList.add(transaction);

            log.info("add a new transaction to the transactions collector");

        } catch (Exception ex) {

            log.error("we cant add the transaction to the collector with reason " + ex.getMessage());
        }
    }

    public synchronized List<Transaction> getTransactionList() {

        return transactionList;
    }
}
