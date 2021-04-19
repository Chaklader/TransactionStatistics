package com.transaction.statistics.service;

import com.transaction.statistics.dto.TransactionDto;
import com.transaction.statistics.model.Transaction;


/**
 * Created by Chaklader on Apr, 2021
 */
public interface TransactionService {

    Transaction createTransaction(TransactionDto transactionDto);

    boolean deleteAllTransactions();
}
