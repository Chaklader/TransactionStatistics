package com.n26.service;

import com.n26.model.Statistics;
import com.n26.model.Transaction;

import java.util.List;

/**
 * Created by Chaklader on Apr, 2021
 */
public interface StatisticsService {

    Statistics getTransactionsStatistics();

    List<Transaction> getAllTransactions();
}
