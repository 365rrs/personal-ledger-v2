package com.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.dto.BillPaymentChannelDTO;
import com.ledger.entity.BillPaymentChannel;
import com.ledger.exception.BusinessException;
import com.ledger.mapper.BillPaymentChannelMapper;
import com.ledger.service.BillPaymentChannelService;
import com.ledger.vo.BillPaymentChannelVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付渠道服务实现类
 *
 * @author personal-ledger
 * @date 2025-01-13
 */
@Slf4j
@Service
public class BillPaymentChannelServiceImpl implements BillPaymentChannelService {
    
    @Resource
    private BillPaymentChannelMapper billPaymentChannelMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillPaymentChannelVO create(BillPaymentChannelDTO dto) {
        // 检查渠道名称是否重复
        checkDuplicateName(dto.getChannelName(), null);
        
        BillPaymentChannel channel = new BillPaymentChannel();
        BeanUtils.copyProperties(dto, channel);
        
        // 设置默认值
        if (channel.getEnabled() == null) {
            channel.setEnabled("1"); // 默认启用
        }
        if (channel.getSortOrder() == null) {
            channel.setSortOrder(0);
        }
        
        billPaymentChannelMapper.insert(channel);
        
        log.info("创建支付渠道成功 - id: {}, channelName: {}", channel.getId(), channel.getChannelName());
        
        return convertToVO(channel);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BillPaymentChannelVO update(BillPaymentChannelDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("渠道 ID 不能为空");
        }
        
        BillPaymentChannel exists = billPaymentChannelMapper.selectById(dto.getId());
        if (exists == null) {
            throw new BusinessException("渠道不存在");
        }
        
        // 如果修改了渠道名称，检查是否重复
        if (!exists.getChannelName().equals(dto.getChannelName())) {
            checkDuplicateName(dto.getChannelName(), dto.getId());
        }
        
        BeanUtils.copyProperties(dto, exists, "id", "createTime", "updateTime", "creatorCode", "updaterCode", "creatorName", "updaterName");
        
        billPaymentChannelMapper.updateById(exists);
        
        log.info("更新支付渠道成功 - id: {}, channelName: {}", exists.getId(), exists.getChannelName());
        
        return convertToVO(exists);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        BillPaymentChannel channel = billPaymentChannelMapper.selectById(id);
        if (channel == null) {
            throw new BusinessException("渠道不存在");
        }
        
        // TODO: 检查是否有账单使用该渠道
        
        // 逻辑删除
        billPaymentChannelMapper.deleteById(id);
        
        log.info("删除支付渠道成功 - id: {}", id);
    }
    
    @Override
    public BillPaymentChannelVO getById(Long id) {
        BillPaymentChannel channel = billPaymentChannelMapper.selectById(id);
        if (channel == null) {
            throw new BusinessException("渠道不存在");
        }
        return convertToVO(channel);
    }
    
    @Override
    public IPage<BillPaymentChannelVO> page(BillPaymentChannelDTO dto) {
        Page<BillPaymentChannel> page = new Page<>(dto.getCurrent() != null ? dto.getCurrent() : 1, 
                                                    dto.getSize() != null ? dto.getSize() : 20);
        
        LambdaQueryWrapper<BillPaymentChannel> queryWrapper = buildQueryWrapper(dto);
        queryWrapper.orderByAsc(BillPaymentChannel::getSortOrder, BillPaymentChannel::getId);
        
        IPage<BillPaymentChannel> channelPage = billPaymentChannelMapper.selectPage(page, queryWrapper);
        
        return channelPage.convert(this::convertToVO);
    }
    
    @Override
    public List<BillPaymentChannelVO> list(BillPaymentChannelDTO dto) {
        LambdaQueryWrapper<BillPaymentChannel> queryWrapper = buildQueryWrapper(dto);
        queryWrapper.orderByAsc(BillPaymentChannel::getSortOrder, BillPaymentChannel::getId);
        
        List<BillPaymentChannel> channels = billPaymentChannelMapper.selectList(queryWrapper);
        
        return channels.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long id, String enabled) {
        BillPaymentChannel channel = billPaymentChannelMapper.selectById(id);
        if (channel == null) {
            throw new BusinessException("渠道不存在");
        }
        
        channel.setEnabled(enabled);
        billPaymentChannelMapper.updateById(channel);
        
        log.info("切换支付渠道状态成功 - id: {}, enabled: {}", id, enabled);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSortOrder(Long id, Integer newSortOrder) {
        BillPaymentChannel channel = billPaymentChannelMapper.selectById(id);
        if (channel == null) {
            throw new BusinessException("渠道不存在");
        }
        
        channel.setSortOrder(newSortOrder);
        billPaymentChannelMapper.updateById(channel);
        
        log.info("更新支付渠道排序成功 - id: {}, sortOrder: {}", id, newSortOrder);
    }
    
    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<BillPaymentChannel> buildQueryWrapper(BillPaymentChannelDTO dto) {
        LambdaQueryWrapper<BillPaymentChannel> queryWrapper = new LambdaQueryWrapper<>();
        
        if (dto.getChannelName() != null && !dto.getChannelName().isEmpty()) {
            queryWrapper.like(BillPaymentChannel::getChannelName, dto.getChannelName());
        }
        
        if (dto.getChannelType() != null && !dto.getChannelType().isEmpty()) {
            queryWrapper.eq(BillPaymentChannel::getChannelType, dto.getChannelType());
        }
        
        if (dto.getEnabled() != null && !dto.getEnabled().isEmpty()) {
            queryWrapper.eq(BillPaymentChannel::getEnabled, dto.getEnabled());
        }
        
        return queryWrapper;
    }
    
    /**
     * 检查渠道名称是否重复
     */
    private void checkDuplicateName(String channelName, Long excludeId) {
        LambdaQueryWrapper<BillPaymentChannel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BillPaymentChannel::getChannelName, channelName);
        
        if (excludeId != null) {
            queryWrapper.ne(BillPaymentChannel::getId, excludeId);
        }
        
        Long count = billPaymentChannelMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException("该渠道名称已存在");
        }
    }
    
    /**
     * Entity 转 VO
     */
    private BillPaymentChannelVO convertToVO(BillPaymentChannel channel) {
        BillPaymentChannelVO vo = new BillPaymentChannelVO();
        BeanUtils.copyProperties(channel, vo);
        return vo;
    }
}
