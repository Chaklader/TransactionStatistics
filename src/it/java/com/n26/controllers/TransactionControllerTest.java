package com.n26.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.n26.dto.TransactionDto;
import com.n26.model.Transaction;
import com.n26.service.StatisticsService;
import com.n26.service.TransactionService;
import com.n26.utls.ApiResponseMessage;
import com.n26.utls.MessageConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
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

@Slf4j
public class TransactionControllerTest {


    private final UUID RANDOM_TRANSACTION_UUID = UUID.randomUUID();


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private StatisticsService statisticsService;


    @Test
    public void post_createsNewTransactionWithCorrectTimestampAndAmount_andReturnsCreatedTransactionJSON() throws Exception {

        final LocalDateTime localDateTimeNowMinusSeconds = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(10);
        Transaction transaction = createTransactionWithProvidedData(100.50, localDateTimeNowMinusSeconds);

        // make transaction JSON string
        final String transactionJSON = this.objectMapper.writeValueAsString(transaction);
        JSONObject transactionJsonObject = new JSONObject(transactionJSON);

        transactionJsonObject.remove("uuid");

        final String transactionStr = transactionJsonObject.toString();


        Mockito.when(transactionService.createTransaction(Mockito.any(TransactionDto.class))).thenReturn(transaction);


        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/transactions")
                                                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                          .accept(MediaType.APPLICATION_JSON)
                                                          .characterEncoding("UTF-8")
                                                          .content(transactionStr);

        final ResultActions resultActions = mockMvc.perform(builder);

        resultActions.andExpect(status().isCreated())
            .andExpect(jsonPath("$.amount", is(100.5)))
            .andExpect(jsonPath("$.timestamp", is(localDateTimeNowMinusSeconds.toString())))
            .andExpect(MockMvcResultMatchers.content().string(this.objectMapper.writeValueAsString(transaction)));

    }


    @Test
    public void post_createsNewTransactionWithFutureTimestampAndCorrectAmount_andReturnsUnProcessableEntityJSON() throws Exception {

        final LocalDateTime localDateTimeNowPlusSeconds = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(30);
        Transaction transaction = createTransactionWithProvidedData(100.50, localDateTimeNowPlusSeconds);

        final String transactionJSON = this.objectMapper.writeValueAsString(transaction);
        JSONObject transactionJsonObject = new JSONObject(transactionJSON);

        transactionJsonObject.remove("uuid");

        final String transactionStr = transactionJsonObject.toString();


        Mockito.when(transactionService.createTransaction(Mockito.any(TransactionDto.class))).thenReturn(transaction);

        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/transactions")
                                                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                          .accept(MediaType.APPLICATION_JSON)
                                                          .characterEncoding("UTF-8")
                                                          .content(transactionStr);

        final ResultActions resultActions = mockMvc.perform(builder);

        final Map<String, Object> expectedResponseMap = ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.UNPROCESSABLE_ENTITY,
            MessageConstant.FUTURE_DATE_TRANSACTION);

        JSONObject expectedResponseMapJSON = new JSONObject(expectedResponseMap);

        final String expectedResponseString = expectedResponseMapJSON.toString();

        resultActions.andExpect(status().isUnprocessableEntity())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponseMapJSON.toString()));

    }



    @Test
    public void post_createsNewTransactionWithPastTimestampAndCorrectAmount_andReturnsNonContentJSON() throws Exception {

        final LocalDateTime localDateTimeNowMinusSeconds = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(65);
        Transaction transaction = createTransactionWithProvidedData(100.50, localDateTimeNowMinusSeconds);

        final String transactionJSON = this.objectMapper.writeValueAsString(transaction);
        JSONObject transactionJsonObject = new JSONObject(transactionJSON);

        transactionJsonObject.remove("uuid");

        final String transactionStr = transactionJsonObject.toString();


        Mockito.when(transactionService.createTransaction(Mockito.any(TransactionDto.class))).thenReturn(transaction);

        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/transactions")
                                                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                          .accept(MediaType.APPLICATION_JSON)
                                                          .characterEncoding("UTF-8")
                                                          .content(transactionStr);

        final ResultActions resultActions = mockMvc.perform(builder);

        final Map<String, Object> expectedResponseMap = ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.NO_CONTENT,
            MessageConstant.OLDER_TRANSACTION);

        JSONObject expectedResponseMapJSON = new JSONObject(expectedResponseMap);

        final String expectedResponseString = expectedResponseMapJSON.toString();

        resultActions.andExpect(status().isNoContent())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));

    }




    @Test
    public void post_createsNewTransactionWithPastTimestampAndCorrectAmount_andReturnsNonContentJSON2() throws Exception {


        String content = FileUtils.readFileToString(new File("src/it/resources/testcases/TransactionWithUnParsableData_1.sjon"), StandardCharsets.UTF_8);


        Mockito.when(transactionService.createTransaction(Mockito.any(TransactionDto.class))).thenReturn(null);

        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/transactions")
                                                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                          .accept(MediaType.APPLICATION_JSON)
                                                          .characterEncoding("UTF-8")
                                                          .content(content);

        final ResultActions resultActions = mockMvc.perform(builder);

        final Map<String, Object> expectedResponseMap = ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.UNPROCESSABLE_ENTITY,
            "JSON parse error,  Cannot deserialize value of type `java.math.BigDecimal` from String \"sdss\"");

        JSONObject expectedResponseMapJSON = new JSONObject(expectedResponseMap);

        final String expectedResponseString = expectedResponseMapJSON.toString();

        resultActions.andExpect(status().isUnprocessableEntity())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));

    }


    private Transaction createTransactionWithProvidedData(Double amount, LocalDateTime minus) {

        Transaction transaction = Transaction
                                      .builder()
                                      .amount(BigDecimal.valueOf(100.50).setScale(2, RoundingMode.HALF_UP))
                                      .timestamp(minus)
                                      .uuid(RANDOM_TRANSACTION_UUID)
                                      .build();

        log.info("created a new transaction using the provided data = " + transaction.toString());

        return transaction;
    }

}