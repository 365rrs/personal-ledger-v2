package com.ledger.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 标签状态枚举
 *
 * @author personal-ledger
 * @date 2026-03-07
 */
@Getter
@AllArgsConstructor
public enum TagStatusEnum {

    /**
     * 启用
     */
    ENABLE("enable", "启用"),

    /**
     * 停用
     */
    DISABLE("disable", "停用");

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
    public static TagStatusEnum getByCode(String code) {
        for (TagStatusEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 判断是否为启用状态
     *
     * @param code 编码
     * @return true-启用，false-停用
     */
    public static boolean isEnable(String code) {
        return ENABLE.getCode().equals(code);
    }

    /**
     * 判断是否为停用状态
     *
     * @param code 编码
     * @return true-停用，false-启用
     */
    public static boolean isDisable(String code) {
        return DISABLE.getCode().equals(code);
    }
}
