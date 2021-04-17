package com.n26.controllers;

import com.n26.dto.TransactionDto;
import com.n26.model.Statistics;
import com.n26.model.Transaction;
import com.n26.service.StatisticsService;
import com.n26.service.TransactionService;
import com.n26.utls.ApiResponseMessage;
import com.n26.utls.MessageConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;


/**
 * Created by Chaklader on Apr, 2021
 */
@Slf4j
@RestController
@RequestMapping("/")
@Validated
public class TransactionController {


    @Autowired
    private TransactionService transactionService;

    @Autowired
    private StatisticsService statisticsService;




    @Operation(description = "create a transaction using the request JSON data")

    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Create transaction", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = Transaction.class))}),
        @ApiResponse(responseCode = "204", description = MessageConstant.OLDER_TRANSACTION, content = @Content),
        @ApiResponse(responseCode = "400", description = MessageConstant.INVALID_JSON_REQUEST, content = @Content),
        @ApiResponse(responseCode = "200", description = MessageConstant.FIELDS_ARE_NOT_PARSABLE_OR_FUTURE_TRANSACTION, content = @Content),
        @ApiResponse(responseCode = "500", description = MessageConstant.INTERNAL_SERVER_ERROR_MSG, content = @Content)})

    @PostMapping(value = "/transactions")
    public ResponseEntity<Object> createProperty(@RequestBody @Valid TransactionDto transactionDto) {

        try {
            final LocalDateTime transactionTimestamp = transactionDto.getTimestamp();

            final LocalDateTime localDateTimeNow = LocalDateTime.now(ZoneOffset.UTC);

            final boolean isFutureTransaction = transactionTimestamp.isAfter(localDateTimeNow);

            if (isFutureTransaction) {

                return new ResponseEntity<>(ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.UNPROCESSABLE_ENTITY,
                    MessageConstant.FUTURE_DATE_TRANSACTION), new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY);
            }

            final long durationBetweenTransactionTimeStampAndNow = Duration.between(transactionTimestamp, localDateTimeNow).toSeconds();

            if (durationBetweenTransactionTimeStampAndNow >= 60) {

                return new ResponseEntity<>(ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.NO_CONTENT,
                    MessageConstant.OLDER_TRANSACTION), new HttpHeaders(), HttpStatus.NO_CONTENT);
            }

            final Transaction transaction = transactionService.createTransaction(transactionDto);

            return new ResponseEntity<>(transaction, new HttpHeaders(), HttpStatus.CREATED);

        } catch (Exception ex) {

            log.error(MessageConstant.INTERNAL_SERVER_ERROR_MSG + ex.getMessage());
            return new ResponseEntity<>(ApiResponseMessage.getInternalServerError(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @Operation(description = "get the statistics of all transactions for the last 60 sec")

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "get the transaction statistics for last 60 sec", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = Statistics.class))}),
        @ApiResponse(responseCode = "404", description = MessageConstant.STATISTICS_RESOURCE_NOT_FOUND_MSG, content = @Content),
        @ApiResponse(responseCode = "500", description = MessageConstant.INTERNAL_SERVER_ERROR_MSG, content = @Content)})

    @GetMapping("/statistics")
    public ResponseEntity<Object> getTransactionStatistics() {

        try {
            final Statistics statistics = statisticsService.getTransactionsStatistics();

            if (statistics != null) {

                return new ResponseEntity<>(statistics, new HttpHeaders(), HttpStatus.OK);
            }

            return new ResponseEntity<>(ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.NOT_FOUND,
                MessageConstant.STATISTICS_RESOURCE_NOT_FOUND_MSG), new HttpHeaders(), HttpStatus.NOT_FOUND);

        } catch (Exception ex) {

            log.error(MessageConstant.INTERNAL_SERVER_ERROR_MSG + ex.getMessage());
            return new ResponseEntity<>(ApiResponseMessage.getInternalServerError(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @Operation(description = "delete all the transactions from the local storage")

    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = MessageConstant.ALL_TRANSACTIONS_DELETED, content = @Content),
        @ApiResponse(responseCode = "422", description = MessageConstant.TRANSACTIONS_DELETE_UN_SUCCESS_MSG, content = @Content),
        @ApiResponse(responseCode = "400", description = MessageConstant.INVALID_JSON_REQUEST, content = @Content),
        @ApiResponse(responseCode = "500", description = MessageConstant.INTERNAL_SERVER_ERROR_MSG, content = @Content)})

    @DeleteMapping("/transactions")
    public ResponseEntity<Object> deleteAllTransactions() {

        try {
            boolean isAllTransactionsDeleted = transactionService.deleteAllTransactions();

            if (isAllTransactionsDeleted) {

                return new ResponseEntity<>(ApiResponseMessage.getGenericApiResponse(Boolean.TRUE, HttpStatus.NO_CONTENT, MessageConstant.ALL_TRANSACTIONS_DELETED),
                    new HttpHeaders(), HttpStatus.NO_CONTENT);
            }

            return new ResponseEntity<>(ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.NOT_FOUND,
                MessageConstant.TRANSACTIONS_DELETE_UN_SUCCESS_MSG), new HttpHeaders(), HttpStatus.NOT_FOUND);

        } catch (Exception ex) {

            log.error(MessageConstant.INTERNAL_SERVER_ERROR_MSG + ex.getMessage());
            return new ResponseEntity<>(ApiResponseMessage.getInternalServerError(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
