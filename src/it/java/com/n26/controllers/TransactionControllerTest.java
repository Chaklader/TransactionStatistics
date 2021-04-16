package com.n26.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n26.dto.TransactionDto;
import com.n26.model.Transaction;
import com.n26.service.StatisticsService;
import com.n26.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Created by Chaklader on Apr, 2021
 */
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

        Transaction transaction = createTransactionWithProvidedData(100.50, 10);

        Mockito.when(transactionService.createTransaction(Mockito.any(TransactionDto.class))).thenReturn(transaction);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/transactions")
                                                    .contentType(MediaType.APPLICATION_JSON_VALUE).accept(MediaType.APPLICATION_JSON)
                                                    .characterEncoding("UTF-8").content(this.objectMapper.writeValueAsBytes(transaction));

        mockMvc.perform(builder).andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount", is("100.50")))
            .andExpect(MockMvcResultMatchers.content().string(this.objectMapper.writeValueAsString(transaction)));
    }


    private Transaction createTransactionWithProvidedData(double amount, long substructionSeconds){

        Transaction transaction = Transaction
                                      .builder()
                                      .transactionAmount(BigDecimal.valueOf(100.50))
                                      .localDateTime(LocalDateTime.now().minus(10, ChronoUnit.SECONDS))
                                      .uuid(UUID.randomUUID()).build();

        return transaction;
    }
}