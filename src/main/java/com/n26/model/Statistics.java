package com.n26.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;


/**
 * Created by Chaklader on Apr, 2021
 */
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

}
