package com.ledger.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 是否计入收支统计枚举
 *
 * @author personal-ledger
 * @date 2026-03-07
 */
@Getter
@AllArgsConstructor
public enum IncludeInStatisticsEnum {

    /**
     * 不计入收支统计
     */
    NO("0", "不计入"),

    /**
     * 计入收支统计
     */
    YES("1", "计入");

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
    public static IncludeInStatisticsEnum getByCode(String code) {
        for (IncludeInStatisticsEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 判断是否为计入状态
     *
     * @param code 编码
     * @return true-计入，false-不计入
     */
    public static boolean isYes(String code) {
        return YES.getCode().equals(code);
    }

    /**
     * 判断是否为不计入状态
     *
     * @param code 编码
     * @return true-不计入，false-计入
     */
    public static boolean isNo(String code) {
        return NO.getCode().equals(code);
    }
}
