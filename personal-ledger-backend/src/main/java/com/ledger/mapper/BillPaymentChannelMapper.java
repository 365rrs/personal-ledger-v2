package com.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ledger.entity.BillPaymentChannel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付渠道 Mapper
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Mapper
public interface BillPaymentChannelMapper extends BaseMapper<BillPaymentChannel> {
}
