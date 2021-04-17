package com.n26.controllers;

import com.n26.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

/*
 * this class provides the custom Utilities for the executing the transaction controller test
 * */
@Slf4j
public class TransactionControllerTestUtils {


    private static final UUID RANDOM_TRANSACTION_UUID = UUID.randomUUID();

    public static String getCustomMessageForEntityFieldParsingException(String field, String fieldType) {

        String result = null;

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


    public static String getFileContentWithLocation(String location) throws IOException {

        String content = FileUtils.readFileToString(new File(location), StandardCharsets.UTF_8);

        return content;
    }


    public static Transaction createTransactionWithProvidedData(Double amount, LocalDateTime minus) {

        Transaction transaction = Transaction
                                      .builder()
                                      .amount(BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP))
                                      .timestamp(minus)
                                      .uuid(RANDOM_TRANSACTION_UUID)
                                      .build();

        log.info("created a new transaction using the provided data = " + transaction.toString());

        return transaction;
    }
}