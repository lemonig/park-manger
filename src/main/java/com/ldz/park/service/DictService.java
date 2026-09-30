package com.ldz.park.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ldz.park.dao.DictItemMapper;
import com.ldz.park.dao.DictTypeMapper;
import com.ldz.park.model.DictItem;
import com.ldz.park.model.DictType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DictService {

    private final DictTypeMapper dictTypeMapper;
    private final DictItemMapper dictItemMapper;

    /* ===================== 字典类型 ===================== */

    public List<DictType> listTypes() {
        return dictTypeMapper.list();
    }

    public PageInfo<DictType> listTypes(int pageNum, int pageSize) {
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.max(pageSize, 1);
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(dictTypeMapper.list());
    }

    public DictType typeDetail(Long id) {
        return dictTypeMapper.detail(id);
    }

    public void addType(DictType dictType) {
        dictTypeMapper.add(dictType);
    }

    public void updateType(DictType dictType) {
        dictTypeMapper.update(dictType);
    }

    /**
     * 删除字典类型时，同时删除该类型下所有字典项
     */
    public void deleteType(Long id) {
        DictType type = dictTypeMapper.detail(id);
        if (type != null && type.getCode() != null) {
            dictItemMapper.deleteByTypeCode(type.getCode());
        }
        dictTypeMapper.delete(id);
    }

    /* ===================== 字典项 ===================== */

    public List<DictItem> listItemsByTypeCode(String typeCode) {
        return dictItemMapper.list(typeCode);
    }

    public PageInfo<DictItem> listItemsByTypeCode(String typeCode, int pageNum, int pageSize) {
        pageNum = Math.max(pageNum, 1);
        pageSize = Math.max(pageSize, 1);
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(dictItemMapper.list(typeCode));
    }

    public DictItem itemDetail(Long id) {
        return dictItemMapper.detail(id);
    }

    public void addItem(DictItem dictItem) {
        dictItemMapper.add(dictItem);
    }

    public void updateItem(DictItem dictItem) {
        dictItemMapper.update(dictItem);
    }

    public void deleteItem(Long id) {
        dictItemMapper.delete(id);
    }
}
