package com.ldz.park.web;

import com.ldz.park.model.DictItem;
import com.ldz.park.model.DictType;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "字典管理", description = "字典类型与字典项接口")
@RestController
@RequestMapping("/api/dict")
public class DictController {

    @Autowired
    private DictService dictService;

    @Operation(summary = "字典类型列表")
    @PostMapping("/type/list")
    public ApiResponse typeList() {
        return dictService.listTypes();
    }

    @Operation(summary = "新增字典类型")
    @PostMapping("/type/add")
    public ApiResponse typeAdd(@RequestBody DictType dictType) {
        dictService.addType(dictType);
        return new ApiResponse();
    }

    @Operation(summary = "更新字典类型")
    @PostMapping("/type/update")
    public ApiResponse typeUpdate(@RequestBody DictType dictType) {
        dictService.updateType(dictType);
        return new ApiResponse();
    }

    @Operation(summary = "删除字典类型")
    @PostMapping("/type/delete")
    public ApiResponse typeDelete(@RequestBody DictType dictType) {
        dictService.deleteType(dictType.getId());
        return new ApiResponse();
    }

    @Operation(summary = "字典类型详情")
    @PostMapping("/type/detail")
    public ApiResponse typeDetail(@RequestBody DictType dictType) {
        return dictService.typeDetail(dictType.getId());
    }

    @Operation(summary = "字典项列表")
    @PostMapping("/item/list")
    public ApiResponse itemList(@RequestBody(required = false) DictItem dictItem) {
        String typeCode = dictItem == null ? null : dictItem.getTypeCode();
        return dictService.listItems(typeCode);
    }

    @Operation(summary = "新增字典项")
    @PostMapping("/item/add")
    public ApiResponse itemAdd(@RequestBody DictItem dictItem) {
        dictService.addItem(dictItem);
        return new ApiResponse();
    }

    @Operation(summary = "更新字典项")
    @PostMapping("/item/update")
    public ApiResponse itemUpdate(@RequestBody DictItem dictItem) {
        dictService.updateItem(dictItem);
        return new ApiResponse();
    }

    @Operation(summary = "删除字典项")
    @PostMapping("/item/delete")
    public ApiResponse itemDelete(@RequestBody DictItem dictItem) {
        dictService.deleteItem(dictItem.getId());
        return new ApiResponse();
    }

    @Operation(summary = "字典项详情")
    @PostMapping("/item/detail")
    public ApiResponse itemDetail(@RequestBody DictItem dictItem) {
        return dictService.itemDetail(dictItem.getId());
    }
}

