package com.example.Krieger.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

}
