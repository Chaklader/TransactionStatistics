package com.n26.controllers;


import com.n26.dto.TransactionDto;
import com.n26.model.Statistics;
import com.n26.model.Transaction;
import com.n26.service.StatisticsService;
import com.n26.service.TransactionService;
import com.n26.utls.ApiResponseMessage;
import com.n26.utls.ApiTestUtils;
import com.n26.utls.MessageConstant;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

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



    private static ApiTestUtils apiTestUtils;

    @Autowired
    private MockMvc mockMvc;



    @MockBean
    private TransactionService transactionService;

    @MockBean
    private StatisticsService statisticsService;



    @BeforeAll
    public static void setup() {

        log.info("creating new ApiTestUtils instance for the controller testing");
        apiTestUtils = new ApiTestUtils();
    }

    @AfterAll
    public static void tearDown() {

        apiTestUtils = null;
    }



    @Test
    public void post_createsNewTransactionWithCorrectTimestampAndAmount_andReturnsCreatedTransactionJSON() throws Exception {

        final LocalDateTime localDateTimeNowMinusSeconds = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(10);
        Transaction transaction = Transaction.createTransactionWithProvidedData(100.50, localDateTimeNowMinusSeconds);

        final String transactionStr = apiTestUtils.convertTransactionToJSONString(transaction);

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
            .andExpect(MockMvcResultMatchers.content().json(apiTestUtils.convertTransactionToJSONString(transaction)));

    }


    @Test
    public void post_createsNewTransactionWithFutureTimestampAndCorrectAmount_andReturnsUnProcessableEntityJSON() throws Exception {

        final LocalDateTime localDateTimeNowPlusSeconds = LocalDateTime.now(ZoneOffset.UTC).plusSeconds(30);
        Transaction transaction = Transaction.createTransactionWithProvidedData(100.50, localDateTimeNowPlusSeconds);

        final String transactionStr = apiTestUtils.convertTransactionToJSONString(transaction);


        Mockito.when(transactionService.createTransaction(Mockito.any(TransactionDto.class))).thenReturn(transaction);

        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/transactions")
                                                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                          .accept(MediaType.APPLICATION_JSON)
                                                          .characterEncoding("UTF-8")
                                                          .content(transactionStr);

        final ResultActions resultActions = mockMvc.perform(builder);

        final Map<String, Object> expectedResponseMap = ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.UNPROCESSABLE_ENTITY,
            MessageConstant.FUTURE_DATE_TRANSACTION);

        String expectedResponseString = apiTestUtils.convertExpectedResponseMapToString(expectedResponseMap);

        resultActions.andExpect(status().isUnprocessableEntity())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));

    }


    @Test
    public void post_createsNewTransactionWithPastTimestampAndCorrectAmount_andReturnsNonContentJSON() throws Exception {

        final LocalDateTime localDateTimeNowMinusSeconds = LocalDateTime.now(ZoneOffset.UTC).minusSeconds(65);
        Transaction transaction = Transaction.createTransactionWithProvidedData(100.50, localDateTimeNowMinusSeconds);

        final String transactionStr = apiTestUtils.convertTransactionToJSONString(transaction);


        Mockito.when(transactionService.createTransaction(Mockito.any(TransactionDto.class))).thenReturn(transaction);

        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/transactions")
                                                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                          .accept(MediaType.APPLICATION_JSON)
                                                          .characterEncoding("UTF-8")
                                                          .content(transactionStr);

        final ResultActions resultActions = mockMvc.perform(builder);

        final Map<String, Object> expectedResponseMap = ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.NO_CONTENT,
            MessageConstant.OLDER_TRANSACTION);

        final String expectedResponseString = apiTestUtils.convertExpectedResponseMapToString(expectedResponseMap);

        resultActions.andExpect(status().isNoContent())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));

    }


    @Test
    public void post_createsNewTransactionWithTimestampAndInvalidAmount_andReturnsUnprocessableEntityEntityJSON() throws Exception {

        final String FILE_LOC = "src/it/resources/testcases/TransactionWithUnParsableData_1.sjon";
        String content = apiTestUtils.getFileContentWithLocation(FILE_LOC);

        Mockito.when(transactionService.createTransaction(Mockito.any(TransactionDto.class))).thenReturn(Transaction.builder().build());

        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/transactions")
                                                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                          .accept(MediaType.APPLICATION_JSON)
                                                          .characterEncoding("UTF-8")
                                                          .content(content);

        final ResultActions resultActions = mockMvc.perform(builder);

        final Map<String, Object> expectedResponseMap = ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.UNPROCESSABLE_ENTITY,
            apiTestUtils.getCustomMessageForEntityFieldParsingException("\"sdss\"", "AMOUNT"));

        final String expectedResponseString = apiTestUtils.convertExpectedResponseMapToString(expectedResponseMap);

        resultActions.andExpect(status().isUnprocessableEntity())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));
    }


    @Test
    public void post_createsNewTransactionWithInvalidTimestampAndCorrectAmount_andReturnsUnprocessableEntityEntityJSON() throws Exception {

        final String FILE_NAME = "src/it/resources/testcases/TransactionWithUnParsableData_2.sjon";
        String content = apiTestUtils.getFileContentWithLocation(FILE_NAME);

        Mockito.when(transactionService.createTransaction(Mockito.any(TransactionDto.class))).thenReturn(Transaction.builder().build());

        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/transactions")
                                                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                          .accept(MediaType.APPLICATION_JSON)
                                                          .characterEncoding("UTF-8")
                                                          .content(content);

        final ResultActions resultActions = mockMvc.perform(builder);

        final Map<String, Object> expectedResponseMap = ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.UNPROCESSABLE_ENTITY,
            apiTestUtils.getCustomMessageForEntityFieldParsingException("\"sdsfsf\"", "LOCAL_DATE_TIME"));

        final String expectedResponseString = apiTestUtils.convertExpectedResponseMapToString(expectedResponseMap);

        resultActions.andExpect(status().isUnprocessableEntity())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));
    }


    @Test
    public void post_createsNewTransactionWithInvalidJson_andReturnsBadRequestResponse() throws Exception {

        String content = "some invalid JSON data";

        Mockito.when(transactionService.createTransaction(Mockito.any(TransactionDto.class))).thenReturn(Transaction.builder().build());

        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/transactions")
                                                          .contentType(MediaType.APPLICATION_JSON_VALUE)
                                                          .accept(MediaType.APPLICATION_JSON)
                                                          .characterEncoding("UTF-8")
                                                          .content(content);

        final ResultActions resultActions = mockMvc.perform(builder);

        final Map<String, Object> expectedResponseMap = ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.BAD_REQUEST,
            "JSON parse error,  Unrecognized token 'some'");

        final String expectedResponseString = apiTestUtils.convertExpectedResponseMapToString(expectedResponseMap);

        resultActions.andExpect(status().isBadRequest())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));

    }


    @Test
    public void get_transactionStatisticsUsingWithEmptyRequestBody_AndCorrectData_returnsOkWithCorrectStatistics() throws Exception {

        final Statistics statistics = Statistics.builder()
                                          .sum("100.00")
                                          .avg("12.5")
                                          .max("45.00")
                                          .min("31.50")
                                          .count(100L)
                                          .build();

        Mockito.when(statisticsService.getTransactionsStatistics()).thenReturn(statistics);


        mockMvc.perform(MockMvcRequestBuilders.get("/statistics").contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8"))

            .andExpect(status().isOk())

            .andExpect(jsonPath("$.sum", is("100.00")))
            .andExpect(jsonPath("$.avg", is("12.5")))
            .andExpect(jsonPath("$.max", is("45.00")))
            .andExpect(jsonPath("$.min", is("31.50")))
            .andExpect(jsonPath("$.count", is(100)));
    }


    @Test
    public void get_transactionStatisticsUsingWithEmptyRequestBody_AndEmptyData_returnsOkWithCorrectStatistics() throws Exception {


        final Statistics EMPTY_STATISTICS = Statistics.createEmptyStatisticsPojo();

        Mockito.when(statisticsService.getTransactionsStatistics()).thenReturn(EMPTY_STATISTICS);


        mockMvc.perform(MockMvcRequestBuilders.get("/statistics").contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8"))

            .andExpect(status().isOk())

            .andExpect(jsonPath("$.sum", is("0.00")))
            .andExpect(jsonPath("$.avg", is("0.00")))
            .andExpect(jsonPath("$.max", is("0.00")))
            .andExpect(jsonPath("$.min", is("0.00")))
            .andExpect(jsonPath("$.count", is(0)));

    }


    @Test
    public void get_transactionStatisticsUsingWithEmptyRequestBody_AndNullData_returnsNotFoundStatistics() throws Exception {

        Mockito.when(statisticsService.getTransactionsStatistics()).thenReturn(null);

        final Map<String, Object> expectedResponseMap = ApiResponseMessage.getGenericApiResponse(
            Boolean.FALSE,
            HttpStatus.NOT_FOUND,
            MessageConstant.STATISTICS_RESOURCE_NOT_FOUND_MSG
        );

        final String expectedResponseString = apiTestUtils.convertExpectedResponseMapToString(expectedResponseMap);

        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/statistics").contentType(MediaType.APPLICATION_JSON)
                                                          .accept(MediaType.APPLICATION_JSON)
                                                          .characterEncoding("UTF-8");

        final ResultActions resultActions = mockMvc.perform(builder);

        resultActions
            .andExpect(status().isNotFound())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));
    }


    @Test
    public void delete_AllTransactionsWithEmptyRequestBody_asSuccessfulRequest_returnsNoContentStatusResponse() throws Exception {


        Mockito.when(transactionService.deleteAllTransactions()).thenReturn(true);

        final Map<String, Object> expectedResponseMap = ApiResponseMessage.getGenericApiResponse(
            Boolean.TRUE,
            HttpStatus.NO_CONTENT,
            MessageConstant.ALL_TRANSACTIONS_DELETED
        );

        final String expectedResponseString = apiTestUtils.convertExpectedResponseMapToString(expectedResponseMap);


        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.delete("/transactions")
                                                          .contentType(MediaType.APPLICATION_JSON)
                                                          .accept(MediaType.APPLICATION_JSON)
                                                          .characterEncoding("UTF-8");

        final ResultActions resultActions = mockMvc.perform(builder);

        resultActions
            .andExpect(status().isNoContent())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));

    }


    @Test
    public void delete_AllTransactionsWithEmptyRequestBody_asFailedRequest_returnsNotFoundStatusResponse() throws Exception {

        Mockito.when(transactionService.deleteAllTransactions()).thenReturn(false);

        final Map<String, Object> expectedResponseMap = ApiResponseMessage.getGenericApiResponse(
            Boolean.FALSE,
            HttpStatus.NOT_FOUND,
            MessageConstant.TRANSACTIONS_DELETE_UN_SUCCESS_MSG
        );

        final String expectedResponseString = apiTestUtils.convertExpectedResponseMapToString(expectedResponseMap);


        final MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.delete("/transactions")
                                                          .contentType(MediaType.APPLICATION_JSON)
                                                          .accept(MediaType.APPLICATION_JSON)
                                                          .characterEncoding("UTF-8");

        final ResultActions resultActions = mockMvc.perform(builder);

        resultActions
            .andExpect(status().isNotFound())
            .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));

    }


}