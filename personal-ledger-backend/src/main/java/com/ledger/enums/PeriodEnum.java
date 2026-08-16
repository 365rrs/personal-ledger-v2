package com.ledger.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 待支出周期枚举
 *
 * @author personal-ledger
 * @date 2026-03-15
 */
@Getter
@AllArgsConstructor
public enum PeriodEnum {

    /**
     * 每月
     */
    MONTHLY("MONTHLY", "每月"),

    /**
     * 每年
     */
    YEARLY("YEARLY", "每年"),

    /**
     * 一次性
     */
    ONETIME("ONETIME", "一次性");

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
    public static PeriodEnum getByCode(String code) {
        for (PeriodEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 判断是否为每月周期
     *
     * @param code 编码
     * @return true-每月，false-其他
     */
    public static boolean isMonthly(String code) {
        return MONTHLY.getCode().equals(code);
    }

    /**
     * 判断是否为每年周期
     *
     * @param code 编码
     * @return true-每年，false-其他
     */
    public static boolean isYearly(String code) {
        return YEARLY.getCode().equals(code);
    }

    /**
     * 判断是否为一次性周期
     *
     * @param code 编码
     * @return true-一次性，false-其他
     */
    public static boolean isOnetime(String code) {
        return ONETIME.getCode().equals(code);
    }
}
