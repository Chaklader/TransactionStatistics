package com.n26.service;

import com.n26.dto.TransactionDto;
import com.n26.model.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Chaklader on Apr, 2021
 */
public interface TransactionService {

    Transaction createTransaction(TransactionDto transactionDto);

    boolean deleteAllTransactions();

    List<Transaction> getAllTransactions();
}
