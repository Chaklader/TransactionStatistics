package com.n26.exceptionhandling;//package com.n26.exceptionhandling;


import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.n26.exceptionhandling.apierror.ApiErrorResponse;
import com.n26.utls.ApiResponseMessage;
import com.n26.utls.MessageConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.annotation.Nonnull;
import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;

import static org.springframework.http.HttpStatus.BAD_REQUEST;


/**
 * Created by Chaklader on Apr, 2021
 */
@Slf4j
@ControllerAdvice

public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {


    @Override
    @Nonnull
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
        MissingServletRequestParameterException ex,
        @Nonnull HttpHeaders headers,
        @Nonnull HttpStatus status,
        @Nonnull WebRequest request) {

        String error = ex.getParameterName() + " parameter is missing";

        return new ResponseEntity<>(ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.BAD_REQUEST,
            error), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @Override
    @Nonnull
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
        @Nonnull HttpMediaTypeNotSupportedException ex,
        @Nonnull HttpHeaders headers,
        @Nonnull HttpStatus status,
        @Nonnull WebRequest request
    ) {

        String message = prepareMessageFromException(ex, (ServletWebRequest) request);

        return new ResponseEntity<>(ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            " media type is not supported. Supported media types "), new HttpHeaders(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        @NonNull MethodArgumentNotValidException ex,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatus status,
        @NonNull WebRequest request

    ) {

        String message = prepareMessageFromException(ex, (ServletWebRequest) request);

        ApiErrorResponse apiError = new ApiErrorResponse(BAD_REQUEST);

        apiError.setMessage("Validation error");
        apiError.addValidationErrors(ex.getBindingResult().getFieldErrors());
        apiError.addValidationError(ex.getBindingResult().getGlobalErrors());

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {

        ApiErrorResponse apiError = new ApiErrorResponse(BAD_REQUEST);

        apiError.setMessage("Validation error");
        apiError.addValidationErrors(ex.getConstraintViolations());

        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {

        ServletWebRequest req = (ServletWebRequest) request;
        String message = prepareMessageFromException(ex, (ServletWebRequest) request);

        log.info("{} to {}", req.getHttpMethod(), req.getRequest().getServletPath());
        log.error(message, ex);

        return new ResponseEntity<>(ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.BAD_REQUEST,
            message), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @Override
    @Nonnull
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        @Nonnull HttpMessageNotReadableException ex,
        @Nonnull HttpHeaders headers,
        @Nonnull HttpStatus status,
        @Nonnull WebRequest request
    ) {

        String message = prepareMessageFromException(ex, (ServletWebRequest) request);
        final Throwable throwableCause = ex.getCause();

        if (throwableCause instanceof InvalidFormatException) {

            return new ResponseEntity<>(ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.UNPROCESSABLE_ENTITY,
                message), new HttpHeaders(), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return new ResponseEntity<>(ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.BAD_REQUEST,
            message), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    private String prepareMessageFromException(Exception ex, ServletWebRequest request) {

        String message = ex.getMessage();

        log.info("{} to {}", request.getHttpMethod(), request.getRequest().getServletPath());
        log.error(message);

        if (message != null && !message.isEmpty()) {

            final String[] split = Arrays.copyOfRange(message.split(":"), 0, 2);

            final String msg = Arrays.toString(split).replaceAll("[\\[\\](){}]", "");

            return msg;
        }

        return message;
    }

    @Override
    @Nonnull
    protected ResponseEntity<Object> handleHttpMessageNotWritable(
        @Nonnull HttpMessageNotWritableException ex,
        @Nonnull HttpHeaders headers,
        @Nonnull HttpStatus status,
        @Nonnull WebRequest request
    ) {

        String error = "Error writing JSON output";

        return new ResponseEntity<>(ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error. please contact support !!"), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    @Nonnull
    protected ResponseEntity<Object> handleNoHandlerFoundException(
        NoHandlerFoundException ex,
        @Nonnull HttpHeaders headers,
        @Nonnull HttpStatus status,
        @Nonnull WebRequest request
    ) {

        return new ResponseEntity<>(ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.BAD_REQUEST,
            String.format("Could not find the %s method for URL %s", ex.getHttpMethod(), ex.getRequestURL())), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiErrorResponse apiError) {

        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException ex) {

        return new ResponseEntity<>(ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.NOT_FOUND,
            "Resource not found: "), new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex, WebRequest request) {

        if (ex.getCause() instanceof ConstraintViolationException) {

            return new ResponseEntity<>(ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.CONFLICT,
                "Database error"), new HttpHeaders(), HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error. please contact support !!" + ex.getMessage()), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {

        return new ResponseEntity<>(ApiResponseMessage.getGenericApiResponse(Boolean.FALSE, HttpStatus.BAD_REQUEST,
            String.format("The parameter '%s' of value '%s' could not be converted to type '%s'", ex.getName(), ex.getValue(), ex.getRequiredType())), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

}
