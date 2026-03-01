package com.ledger.exception;

import lombok.Getter;

/**
 * 业务异常类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Getter
public class BusinessException extends RuntimeException {

    private Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
