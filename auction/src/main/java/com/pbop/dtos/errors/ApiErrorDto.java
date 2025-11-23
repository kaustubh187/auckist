package com.pbop.dtos.errors;

import java.time.LocalDateTime;

public record ApiErrorDto(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
) {
}
