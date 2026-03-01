package com.ledger.common;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 统一响应结果
 *
 * @author personal-ledger
 * @version 1.0
 * @date 2025-01-13
 */
@Data
public class Response<T> {

    private Integer code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> Response<T> success() {
        Response<T> result = new Response<>();
        result.setCode(200);
        result.setMessage("success");
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    public static <T> Response<T> success(T data) {
        Response<T> result = new Response<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    public static <T> Response<T> error(String message) {
        Response<T> result = new Response<>();
        result.setCode(500);
        result.setMessage(message);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }

    public static <T> Response<T> error(Integer code, String message) {
        Response<T> result = new Response<>();
        result.setCode(code);
        result.setMessage(message);
        result.setTimestamp(LocalDateTime.now());
        return result;
    }
}
