package com.machinerift.machine_rift.exception;

import com.machinerift.machine_rift.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldUseChineseFallbackWhenValidationMessageIsNull() {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "request");
        FieldError fieldError = mock(FieldError.class);
        when(fieldError.getField()).thenReturn("score");
        when(fieldError.getDefaultMessage()).thenReturn(null);
        bindingResult.addError(fieldError);
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

        ResponseEntity<ApiResponse<Map<String, String>>> response =
                handler.handleValidationException(exception);
        ApiResponse<Map<String, String>> body = Objects.requireNonNull(response.getBody());

        assertEquals(400, response.getStatusCode().value());
        assertEquals("欄位格式不正確", body.getData().get("score"));
    }
}
