package com.n26.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Created by Chaklader on Apr, 2021
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

//@Entity
public class Statistics {

//    @Id
//    @GeneratedValue(strategy = GenerationType.)
//    @Column(name = "id")
//    private UUID uuid;


    /*
    expected:<{"sum":["99388.99","avg":"49.69","max":"99.95","min":"0.02"],"count":2000}>

    but was:<{"sum":[99388.99,"avg":49.69,"max":99.95,"min":0.02],"count":2000}>
    * */



    /*
    {"sum":["99388.99","avg":"49.69","max":"99.95","min":"0.02"],"count":2000}
    {"sum":[99388.99,"avg":49.69,"max":99.95,"min":0.02],"count":2000}
    * */
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
