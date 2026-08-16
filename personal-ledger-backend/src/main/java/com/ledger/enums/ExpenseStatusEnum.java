package com.ledger.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 待支出状态枚举
 *
 * @author personal-ledger
 * @date 2026-03-15
 */
@Getter
@AllArgsConstructor
public enum ExpenseStatusEnum {

    /**
     * 待支付
     */
    PENDING("PENDING", "待支付"),

    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "已完成"),

    /**
     * 已取消
     */
    CANCELLED("CANCELLED", "已取消");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;

    /**
     * 根据编码获取枚举
     *
     * @param code 编码
     * @return 枚举对象
     */
    public static ExpenseStatusEnum getByCode(String code) {
        for (ExpenseStatusEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 判断是否为待支付状态
     *
     * @param code 编码
     * @return true-待支付，false-其他
     */
    public static boolean isPending(String code) {
        return PENDING.getCode().equals(code);
    }

    /**
     * 判断是否为已完成状态
     *
     * @param code 编码
     * @return true-已完成，false-其他
     */
    public static boolean isCompleted(String code) {
        return COMPLETED.getCode().equals(code);
    }

    /**
     * 判断是否为已取消状态
     *
     * @param code 编码
     * @return true-已取消，false-其他
     */
    public static boolean isCancelled(String code) {
        return CANCELLED.getCode().equals(code);
    }
}
