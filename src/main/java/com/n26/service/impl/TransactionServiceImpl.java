package com.n26.service.impl;

import com.n26.dto.TransactionDto;
import com.n26.model.Transaction;
import com.n26.service.TransactionService;
import com.n26.utls.TransactionCollectors;
import lombok.extern.slf4j.Slf4j;
//import org.modelmapper.ModelMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


/**
 * Created by Chaklader on Apr, 2021
 */
@Service
@Slf4j
public class TransactionServiceImpl implements TransactionService {


//    @Autowired
//    private ModelMapper modelMapper;


    @Override
    public synchronized Transaction createTransaction(TransactionDto transactionDto) {

        try {
//            Transaction newTransaction = modelMapper.map(transactionDto, Transaction.class);
//
//            if (newTransaction != null) {
//
////                newTransaction.setUuid(UUID.randomUUID());
//
//                newTransaction.setTransactionAmount(transactionDto.getAmount());
//                newTransaction.setLocalDateTime(transactionDto.getTimestamp());
//
//                TransactionCollectors.addTransaction(newTransaction);
//
//                return newTransaction;
//            }

            final Transaction transaction = Transaction.builder()
                                          .uuid(UUID.randomUUID())
                                          .transactionAmount(transactionDto.getAmount())
                                          .localDateTime(transactionDto.getTimestamp())
                                          .build();

            TransactionCollectors.addTransaction(transaction);

            return transaction;

        } catch (Exception ex) {
            log.error("Error occurred while create new transaction ::" + ex.getMessage());
        }

        return null;
    }

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

    @Override
    public List<Transaction> getAllTransactions() {

        final List<Transaction> transactions = TransactionCollectors.getTransactionList();

        if(transactions==null){

            log.info("we received null after calling the transaction list");
        }

        return transactions;
    }
}
