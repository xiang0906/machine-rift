package com.machinerift.machine_rift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Unified API response wrapper for all controller endpoints.
 *
 * <p>This class keeps REST responses consistent across success and failure cases,
 * making frontend integration and error handling simpler.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    /**
     * Creates a successful response payload.
     *
     * @param message user-friendly success message
     * @param data payload to return
     * @param <T> response type
     * @return wrapped response object
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    

    /**
     * Creates a failure response payload.
     *
     * @param message error message
     * @param <T> response type
     * @return wrapped response object
     */
    public static <T> ApiResponse<T> failure(String message) {
        return failure(message, null);
    }

    /**
     * Creates a failure response payload with optional error details.
     *
     * @param message user-friendly error message
     * @param data error details safe to expose to API clients
     * @param <T> response detail type
     * @return wrapped response object
     */
    public static <T> ApiResponse<T> failure(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
