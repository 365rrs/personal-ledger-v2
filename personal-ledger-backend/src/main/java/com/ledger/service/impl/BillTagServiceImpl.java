package com.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ledger.converter.BillTagConverter;
import com.ledger.dto.BillTagDTO;
import com.ledger.dto.BillTagQueryDTO;
import com.ledger.entity.BillTag;
import com.ledger.enums.TagStatusEnum;
import com.ledger.exception.BusinessException;
import com.ledger.mapper.BillTagMapper;
import com.ledger.service.BillTagService;
import com.ledger.vo.BillTagVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 账单标签 Service 实现
 *
 * @author personal-ledger
 * @date 2026-03-07
 */
@Slf4j
@Service
public class BillTagServiceImpl implements BillTagService {

    @Resource
    private BillTagMapper billTagMapper;

    @Resource
    private BillTagConverter converter;

    @Override
    public IPage<BillTagVO> page(BillTagQueryDTO dto) {
        Page<BillTag> page = new Page<>(dto.getCurrent(), dto.getSize());
        
        LambdaQueryWrapper<BillTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotBlank(dto.getTagName()), BillTag::getTagName, dto.getTagName())
               .eq(StringUtils.isNotBlank(dto.getTagCategory()), BillTag::getTagCategory, dto.getTagCategory())
               .eq(StringUtils.isNotBlank(dto.getTagStatus()), BillTag::getTagStatus, dto.getTagStatus())
               .orderByAsc(BillTag::getSortOrder);
        
        IPage<BillTag> tagPage = billTagMapper.selectPage(page, wrapper);
        return tagPage.convert(converter::toVO);
    }

    @Override
    public BillTagVO getById(Long id) {
        BillTag tag = billTagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        return converter.toVO(tag);
    }

    @Override
    public Long create(BillTagDTO dto) {
        // 检查标签名称是否重复
        LambdaQueryWrapper<BillTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillTag::getTagName, dto.getTagName());
        Long count = billTagMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("标签名称已存在");
        }
        
        BillTag tag = converter.toEntity(dto);
        tag.setTagStatus(TagStatusEnum.ENABLE.getCode()); // 默认启用
        
        billTagMapper.insert(tag);
        return tag.getId();
    }

    @Override
    public void update(BillTagDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("标签 ID 不能为空");
        }
        
        BillTag existTag = billTagMapper.selectById(dto.getId());
        if (existTag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // 如果修改了标签名称，检查是否与其他标签重复
        if (!existTag.getTagName().equals(dto.getTagName())) {
            LambdaQueryWrapper<BillTag> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BillTag::getTagName, dto.getTagName())
                   .ne(BillTag::getId, dto.getId());
            Long count = billTagMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException("标签名称已存在");
            }
        }
        
        BillTag tag = converter.toEntity(dto);
        billTagMapper.updateById(tag);
    }

    @Override
    public void delete(Long id) {
        BillTag tag = billTagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // TODO: 检查是否有账单使用该标签
        
        billTagMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, String status) {
        BillTag tag = billTagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        // 验证状态值
        if (!TagStatusEnum.isEnable(status) && !TagStatusEnum.isDisable(status)) {
            throw new BusinessException("无效的状态值");
        }
        
        tag.setTagStatus(status);
        billTagMapper.updateById(tag);
    }

    @Override
    public void updateSortOrder(Long id, Integer newSortOrder) {
        BillTag tag = billTagMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        
        tag.setSortOrder(newSortOrder);
        billTagMapper.updateById(tag);
    }

    @Override
    public java.util.List<BillTagVO> list() {
        LambdaQueryWrapper<BillTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillTag::getTagStatus, TagStatusEnum.ENABLE.getCode())
               .orderByAsc(BillTag::getSortOrder);
        
        java.util.List<BillTag> tags = billTagMapper.selectList(wrapper);
        return tags.stream().map(converter::toVO).collect(java.util.stream.Collectors.toList());
    }
}
