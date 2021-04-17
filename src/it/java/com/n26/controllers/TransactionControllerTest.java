package com.n26.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.n26.dto.TransactionDto;
import com.n26.model.Statistics;
import com.n26.model.Transaction;
import com.n26.service.StatisticsService;
import com.n26.service.TransactionService;
import com.n26.utls.ApiResponseMessage;
import com.n26.utls.MessageConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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


    @Autowired
    private MockMvc mockMvc;

    private ApiTestUtils apiTestUtils;

//    @Autowired
//    public ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private StatisticsService statisticsService;


    @BeforeEach
    void init() {

        apiTestUtils = new ApiTestUtils();
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
            .andExpect(MockMvcResultMatchers.content().json(apiTestUtils.getCon(transaction)));

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


    public static class ApiTestUtils {


        private final ObjectMapper objectMapper;

        public ApiTestUtils() {

            objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }

        public String getCon(Transaction transaction) throws JsonProcessingException {


            return this.objectMapper.writeValueAsString(transaction);
        }

        public String convertTransactionToJSONString(Transaction transaction) throws JSONException, JsonProcessingException {

            final String transactionJSON = this.objectMapper.writeValueAsString(transaction);
            JSONObject transactionJsonObject = new JSONObject(transactionJSON);

            transactionJsonObject.remove("uuid");

            final String transactionStr = transactionJsonObject.toString();

            return transactionStr;
        }

        public String getCustomMessageForEntityFieldParsingException(String field, String fieldType) {

            String result;

            switch (fieldType) {

                case "LOCAL_DATE_TIME" -> {

                    result = "JSON parse error,  Cannot deserialize value of type `java.time.LocalDateTime` from String " + field;
                }

                case "AMOUNT" -> {

                    result = "JSON parse error,  Cannot deserialize value of type `java.math.BigDecimal` from String " + field;
                }

                default -> {
                    log.error("We dont have this field type in the transaction entity");
                    return null;
                }
            }

            return result;
        }


        public String getFileContentWithLocation(String location) throws IOException {

            String content = FileUtils.readFileToString(new File(location), StandardCharsets.UTF_8);

            return content;
        }


        public String convertExpectedResponseMapToString(Map<String, Object> expectedResponseMap) {

            JSONObject expectedResponseMapJSON = new JSONObject(expectedResponseMap);

            return expectedResponseMapJSON.toString();
        }

    }

}