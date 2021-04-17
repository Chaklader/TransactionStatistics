package com.n26.utls;

import com.n26.model.Statistics;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by Chaklader on Apr, 2021
 */
@Slf4j
public class StatisticsUtils {



    public static Statistics createEmptyStatisticsPojo() {

        final BigDecimal zero = (BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        final Statistics EMPTY_STATISTICS = Statistics.builder()
                                                .sum(String.valueOf(zero))
                                                .avg(String.valueOf(zero))
                                                .max(String.valueOf(zero))
                                                .min(String.valueOf(zero))
                                                .count(0L)
                                                .build();

        log.info("Create an empty statistics for the transactions ..");

        return EMPTY_STATISTICS;
    }
}
