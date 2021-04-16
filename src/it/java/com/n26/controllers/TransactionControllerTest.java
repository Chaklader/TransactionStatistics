package com.n26.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n26.dto.TransactionDto;
import com.n26.model.Transaction;
import com.n26.service.StatisticsService;
import com.n26.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Created by Chaklader on Apr, 2021
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(TransactionController.class)
@ActiveProfiles("test")
public class TransactionControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private StatisticsService statisticsService;

    @Test
    public void post_createsNewProperty_andReturnsObjWithIsCreated() throws Exception {

        TransactionDto transaction = createTransactionWithProvidedData2(100.50, 10);
        Transaction transaction32 = createTransactionWithProvidedData(100.50, 10);

        Mockito.when(transactionService.createTransaction(Mockito.any(TransactionDto.class))).thenReturn(transaction32);


        final byte[] bytes = this.objectMapper.writeValueAsBytes(transaction);
        final String s = Arrays.toString(bytes);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/transactions")
                                                    .contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON)
                                                    .characterEncoding("UTF-8").content(this.objectMapper.writeValueAsBytes(transaction));

        mockMvc.perform(builder).andExpect(status().isCreated())
            .andExpect(jsonPath("$.transactionAmount", is(100.5)))
            .andExpect(MockMvcResultMatchers.content().string(this.objectMapper.writeValueAsString(transaction32)));
    }


    final UUID fixed = UUID.randomUUID();

    private Transaction createTransactionWithProvidedData(double amount, long substructionSeconds){


        final LocalDateTime minus = LocalDateTime.now(ZoneOffset.UTC).minus(10, ChronoUnit.SECONDS);

        Transaction transaction = Transaction
                                      .builder()
                                      .transactionAmount(BigDecimal.valueOf(100.50).setScale(2, RoundingMode.HALF_UP))
                                      .localDateTime(minus)
                                      .uuid(fixed)
                                      .build();

        return transaction;
    }


    private TransactionDto createTransactionWithProvidedData2(double amount, long substructionSeconds){


        final LocalDateTime minus = LocalDateTime.now(ZoneOffset.UTC).minus(10, ChronoUnit.SECONDS);

        TransactionDto transaction = TransactionDto
                                         .builder()
                                         .amount(BigDecimal.valueOf(100.50).setScale(2, RoundingMode.HALF_UP))
                                         .timestamp(minus)
                                         .build();

        return transaction;
    }
}