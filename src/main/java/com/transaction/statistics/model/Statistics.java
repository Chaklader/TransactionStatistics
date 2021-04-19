package com.transaction.statistics.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Column;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * Created by Chaklader on Apr, 2021
 */
@Slf4j

@Data
@Builder

@NoArgsConstructor
@AllArgsConstructor
public class Statistics {

    @NotNull
    @Column(name = "sum")
    @Min(0)
    private String sum;

    @NotNull
    @Column(name = "avg")
    @Min(0)
    private String avg;

    @NotNull
    @Column(name = "max")
    @Min(0)
    private String max;

    @NotNull
    @Column(name = "min")
    @Min(0)
    private String min;

    @NotNull
    @Column(name = "count")
    @Min(0)
    private Long count;



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
