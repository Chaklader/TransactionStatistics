package com.n26.utls;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.n26.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;



@Slf4j
public class ApiTestUtils {


    private final ObjectMapper objectMapper;


    public ApiTestUtils() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
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