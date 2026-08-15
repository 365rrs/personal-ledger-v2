#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
账单Excel转钱迹CSV导入脚本
将Excel账单数据转换为钱迹记账软件的CSV导入格式
官方文档: https://docs.qianjiapp.com/other/import_templete.html
"""

import pandas as pd
import warnings
warnings.filterwarnings('ignore')


def convert_excel_to_qianji(excel_path, output_path):
    """
    将Excel账单转换为钱迹CSV格式
    
    钱迹CSV字段顺序:
    时间,分类,二级分类,类型,金额,账户1,账户2,备注,账单标记,手续费,优惠券,标签,账单图片
    """
    # 读取源Excel
    df = pd.read_excel(excel_path)
    print(f"读取源数据: {len(df)} 条记录")
    
    # 创建钱迹格式的DataFrame
    qianji_df = pd.DataFrame()
    
    # 1. 时间: 格式化为 YYYY/MM/DD HH:MM
    # 源格式: 交易日期(2026-08-14) + 交易时间(11:26:46)
    def format_time(row):
        date_str = str(row['交易日期']).split(' ')[0]  # 取日期部分
        time_str = str(row['交易时间'])
        # 处理时间，只保留时:分
        if ':' in time_str:
            parts = time_str.split(':')
            time_short = f"{parts[0]}:{parts[1]}"
        else:
            time_short = time_str
        # 日期格式转换 2026-08-14 -> 2026/08/14
        date_formatted = date_str.replace('-', '/')
        return f"{date_formatted} {time_short}"
    
    qianji_df['时间'] = df.apply(format_time, axis=1)
    
    # 2. 分类: 一级分类
    qianji_df['分类'] = df['分类'].fillna('')
    
    # 3. 二级分类
    qianji_df['二级分类'] = df['二级分类'].fillna('')
    
    # 4. 类型: 收支类型 (支出/收入)
    qianji_df['类型'] = df['收支类型']
    
    # 5. 金额: 收入金额或支出金额
    def get_amount(row):
        if row['收支类型'] == '收入':
            return row['收入金额'] if pd.notna(row['收入金额']) else 0
        else:
            return row['支出金额'] if pd.notna(row['支出金额']) else 0
    
    qianji_df['金额'] = df.apply(get_amount, axis=1)
    
    # 6. 账户1: 留空 (用户指定无需导入)
    qianji_df['账户1'] = ''
    
    # 7. 账户2: 留空 (用户指定无需导入)
    qianji_df['账户2'] = ''
    
    # 8. 备注: 优先用备注列，为空则用交易描述
    def get_remark(row):
        remark = row['备注']
        if pd.notna(remark) and str(remark).strip() != '':
            return str(remark).strip()
        # 备注为空时用交易描述补充
        desc = row['交易描述']
        if pd.notna(desc) and str(desc).strip() != '':
            return str(desc).strip()
        return ''
    
    qianji_df['备注'] = df.apply(get_remark, axis=1)
    
    # 9. 账单标记: 计入统计为"否"时填"不计收支"
    def get_bill_flag(row):
        if str(row['计入统计']).strip() == '否':
            return '不计收支'
        return ''
    
    qianji_df['账单标记'] = df.apply(get_bill_flag, axis=1)
    
    # 10. 手续费: 留空 (用户指定无需导入)
    qianji_df['手续费'] = ''
    
    # 11. 优惠券: 留空 (用户指定无需导入)
    qianji_df['优惠券'] = ''
    
    # 12. 标签: 留空 (用户指定无需导入)
    qianji_df['标签'] = ''
    
    # 13. 账单图片: 留空 (用户指定无需导入)
    qianji_df['账单图片'] = ''
    
    # 输出CSV (UTF-8 with BOM，确保Excel和钱迹都能正确识别中文)
    qianji_df.to_csv(output_path, index=False, encoding='utf-8-sig')
    print(f"已输出钱迹CSV文件: {output_path}")
    print(f"共 {len(qianji_df)} 条记录")
    
    # 打印预览
    print("\n=== 数据预览 (前5行) ===")
    print(qianji_df[['时间','分类','二级分类','类型','金额','备注']].head().to_string())
    
    # 统计信息
    print(f"\n=== 统计信息 ===")
    print(f"支出笔数: {len(qianji_df[qianji_df['类型']=='支出'])}")
    print(f"收入笔数: {len(qianji_df[qianji_df['类型']=='收入'])}")
    print(f"支出总额: {qianji_df[qianji_df['类型']=='支出']['金额'].sum():.2f} 元")
    print(f"收入总额: {qianji_df[qianji_df['类型']=='收入']['金额'].sum():.2f} 元")
    
    return qianji_df


if __name__ == '__main__':
    excel_path = '账单导出_20260814114013.xlsx'
    output_path = '钱迹导入数据_20260814_1939.csv'
    
    convert_excel_to_qianji(excel_path, output_path)
