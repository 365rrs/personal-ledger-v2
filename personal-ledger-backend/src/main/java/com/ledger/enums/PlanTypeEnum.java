package com.ledger.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 待支出计划类型枚举
 *
 * @author personal-ledger
 * @date 2026-03-15
 */
@Getter
@AllArgsConstructor
public enum PlanTypeEnum {

    /**
     * 刚性支出
     */
    RIGID("RIGID", "刚性支出"),

    /**
     * 意向计划支出
     */
    INTENDED("INTENDED", "意向计划支出");

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
    public static PlanTypeEnum getByCode(String code) {
        for (PlanTypeEnum item : values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 判断是否为刚性支出
     *
     * @param code 编码
     * @return true-刚性支出，false-意向计划支出
     */
    public static boolean isRigid(String code) {
        return RIGID.getCode().equals(code);
    }

    /**
     * 判断是否为意向计划支出
     *
     * @param code 编码
     * @return true-意向计划支出，false-刚性支出
     */
    public static boolean isIntended(String code) {
        return INTENDED.getCode().equals(code);
    }
}
