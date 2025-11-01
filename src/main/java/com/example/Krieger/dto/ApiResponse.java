package com.example.Krieger.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Format the response in format
@Getter
@Setter
@NoArgsConstructor
public class ApiResponse<T> {
    private String msg;
    private String status;
    private int code;
    private T data;

    public ApiResponse(String msg, String status, int code, T data) {
        this.msg = msg;
        this.status = status;
        this.code = code;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(String msg, int code, T data) {
        return new ApiResponse<>(msg, "SUCCESS", code, data);
    }

    public static <T> ApiResponse<T> error(String msg, int code) {
        return new ApiResponse<>(msg, "ERROR", code, null);
    }
}
