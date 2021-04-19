package com.transaction.statistics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created by Chaklader on Apr, 2021
 */
@Slf4j

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID uuid;

    @NotNull
    @Column(name = "amount")
    @Min(0)
    private BigDecimal amount;

    @NotNull
    @Column(name = "timestamp")
    private LocalDateTime timestamp;


    public static Transaction createTransactionWithProvidedData(Double amount, LocalDateTime localDateTime) {

        Transaction transaction = Transaction
                                      .builder()
                                      .amount(BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP))
                                      .timestamp(localDateTime)
                                      .uuid(UUID.randomUUID())
                                      .build();

        log.info("created a new transaction using the provided data = " + transaction.toString());

        return transaction;
    }
}
