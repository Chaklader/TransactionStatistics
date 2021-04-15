package com.n26.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created by Chaklader on Apr, 2021
 */
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
    private BigDecimal transactionAmount;

    @NotNull
    @Column(name = "timestamp")
    private LocalDateTime localDateTime;
}
