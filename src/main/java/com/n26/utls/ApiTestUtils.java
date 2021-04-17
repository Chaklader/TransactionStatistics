package com.n26.utls;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;



/*
 * this class provides the custom Utilities for the executing the transaction controller test
 * */
//@Slf4j
public class ApiTestUtils {


    public static String getCustomMessageForEntityFieldParsingException(String field, String fieldType) {

        String result;

        switch (fieldType) {

            case "LOCAL_DATE_TIME" -> {

                result = "JSON parse error,  Cannot deserialize value of type `java.time.LocalDateTime` from String " + field;
            }

            case "AMOUNT" -> {

                result = "JSON parse error,  Cannot deserialize value of type `java.math.BigDecimal` from String " + field;
            }

            default -> {
//                log.error("We dont have this field type in the transaction entity");
                return null;
            }
        }

        return result;
    }


    public static String getFileContentWithLocation(String location) throws IOException {

        String content = FileUtils.readFileToString(new File(location), StandardCharsets.UTF_8);

        return content;
    }
}