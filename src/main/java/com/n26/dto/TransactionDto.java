package com.n26.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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
public class TransactionDto {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID uuid;

    @NotNull
    @Min(0)
    private BigDecimal amount;

    @NotNull
    private LocalDateTime timestamp;
}
