package com.ledger.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 钱迹格式导出VO
 * 钱迹CSV字段顺序: 时间,分类,二级分类,类型,金额,账户1,账户2,备注,账单标记,手续费,优惠券,标签,账单图片
 * 官方文档: https://docs.qianjiapp.com/other/import_templete.html
 *
 * @author personal-ledger
 * @date 2026-09-05
 */
@Data
public class BillQianjiExportVO {

    /**
     * 时间 (格式: YYYY/MM/DD HH:MM)
     */
    @ExcelProperty(value = "时间", index = 0)
    private String time;

    /**
     * 分类 (一级分类)
     */
    @ExcelProperty(value = "分类", index = 1)
    private String category;

    /**
     * 二级分类
     */
    @ExcelProperty(value = "二级分类", index = 2)
    private String subCategory;

    /**
     * 类型 (收入/支出/转账)
     */
    @ExcelProperty(value = "类型", index = 3)
    private String type;

    /**
     * 金额
     */
    @ExcelProperty(value = "金额", index = 4)
    private BigDecimal amount;

    /**
     * 账户1 (支付渠道)
     */
    @ExcelProperty(value = "账户1", index = 5)
    private String account1;

    /**
     * 账户2 (转账目标账户，一般为空)
     */
    @ExcelProperty(value = "账户2", index = 6)
    private String account2;

    /**
     * 备注
     */
    @ExcelProperty(value = "备注", index = 7)
    private String remark;

    /**
     * 账单标记 (不计收支/预算/报销等)
     */
    @ExcelProperty(value = "账单标记", index = 8)
    private String billFlag;

    /**
     * 手续费
     */
    @ExcelProperty(value = "手续费", index = 9)
    private String fee;

    /**
     * 优惠券
     */
    @ExcelProperty(value = "优惠券", index = 10)
    private String coupon;

    /**
     * 标签 (多个标签用#包裹，如 #标签1#标签2)
     */
    @ExcelProperty(value = "标签", index = 11)
    private String tags;

    /**
     * 账单图片
     */
    @ExcelProperty(value = "账单图片", index = 12)
    private String image;
}
