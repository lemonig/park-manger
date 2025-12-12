package com.ldz.park.service;

import com.ldz.park.dao.DictItemMapper;
import com.ldz.park.dao.DictTypeMapper;
import com.ldz.park.model.DictItem;
import com.ldz.park.model.DictType;
import com.ldz.park.model.meta.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictService {

    @Autowired
    private DictTypeMapper dictTypeMapper;

    @Autowired
    private DictItemMapper dictItemMapper;

    public ApiResponse listTypes() {
        List<DictType> list = dictTypeMapper.list();
        return new ApiResponse(list);
    }

    public ApiResponse typeDetail(Long id) {
        DictType detail = dictTypeMapper.detail(id);
        return new ApiResponse(detail);
    }

    public void addType(DictType dictType) {
        dictTypeMapper.add(dictType);
    }

    public void updateType(DictType dictType) {
        dictTypeMapper.update(dictType);
    }

    public void deleteType(Long id) {
        DictType type = dictTypeMapper.detail(id);
        if (type != null && type.getCode() != null) {
            dictItemMapper.deleteByTypeCode(type.getCode());
        }
        dictTypeMapper.delete(id);
    }

    public ApiResponse listItems(String typeCode) {
        List<DictItem> list = dictItemMapper.list(typeCode);
        return new ApiResponse(list);
    }

    public ApiResponse itemDetail(Long id) {
        DictItem detail = dictItemMapper.detail(id);
        return new ApiResponse(detail);
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

