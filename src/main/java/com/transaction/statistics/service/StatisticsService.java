package com.transaction.statistics.service;

import com.transaction.statistics.model.Statistics;
import com.transaction.statistics.model.Transaction;

import java.util.List;

/**
 * Created by Chaklader on Apr, 2021
 */
public interface StatisticsService {

    Statistics getTransactionsStatistics();

    List<Transaction> getAllTransactions();
}
