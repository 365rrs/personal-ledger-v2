package com.ledger.util;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 数据指纹工具类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
public class DataHashUtil {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 计算数据指纹
     * data_hash = MD5(type + amount + transaction_time + description)
     */
    public static String calculateHash(String type, BigDecimal amount, LocalDateTime transactionTime, String description) {
        StringBuilder sb = new StringBuilder();
        sb.append(type != null ? type : "");
        sb.append(amount != null ? amount.toString() : "");
        sb.append(transactionTime != null ? FORMATTER.format(transactionTime) : "");
        sb.append(description != null ? description : "");
        
        return md5(sb.toString());
    }
    
    /**
     * MD5加密
     */
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }
}
