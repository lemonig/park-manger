package com.ldz.park.web;

import com.ldz.park.model.DictItem;
import com.ldz.park.model.DictType;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Deprecated
@Tag(name = "字典管理（已废弃，请使用 /api/wechat/dict/** 或 /api/admin/dict/**）", description = "字典类型与字典项接口")
@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    /* ===================== 字典类型 ===================== */

    @Operation(summary = "字典类型列表")
    @PostMapping("/type/list")
    public ApiResponse<List<DictType>> listTypes() {
        return ApiResponse.success(dictService.listTypes());
    }
    @Operation(summary = "新增字典类型")
    @PostMapping("/type")
    public ApiResponse addType(@RequestBody DictType dictType) {
        dictService.addType(dictType);
        return ApiResponse.ok();
    }

    @Operation(summary = "更新字典类型")
    @PostMapping("/type/update")
    public ApiResponse updateType(@RequestBody DictType dictType) {
        dictService.updateType(dictType);
        return ApiResponse.ok();
    }

    @Operation(summary = "删除字典类型")
    @PostMapping("/type/delete")
    public ApiResponse deleteType(@RequestBody DictType dictType) {
        dictService.deleteType(dictType.getId());
        return ApiResponse.ok();
    }

    @Operation(summary = "字典类型详情")
    @PostMapping("/type/detail")
    public ApiResponse<DictType> typeDetail(@RequestBody DictType dictType) {
        DictType detail = dictService.typeDetail(dictType.getId());
        return ApiResponse.success(detail);
    }

    /* ===================== 字典项 ===================== */

    @Operation(summary = "字典项列表")
    @PostMapping("/item/list")
    public ApiResponse<List<DictItem>> listItems(@RequestBody(required = false) DictItem dictItem) {
        String typeCode = dictItem == null ? null : dictItem.getTypeCode();
        return ApiResponse.success(dictService.listItemsByTypeCode(typeCode));
    }
    @Operation(summary = "新增字典项")
    @PostMapping("/item")
    public ApiResponse addItem(@RequestBody DictItem dictItem) {
        dictService.addItem(dictItem);
        return ApiResponse.ok();
    }

    @Operation(summary = "更新字典项")
    @PostMapping("/item/update")
    public ApiResponse updateItem(@RequestBody DictItem dictItem) {
        dictService.updateItem(dictItem);
        return ApiResponse.ok();
    }

    @Operation(summary = "删除字典项")
    @PostMapping("/item/delete")
    public ApiResponse deleteItem(@RequestBody DictItem dictItem) {
        dictService.deleteItem(dictItem.getId());
        return ApiResponse.ok();
    }

    @Operation(summary = "字典项详情")
    @PostMapping("/item/detail")
    public ApiResponse<DictItem> itemDetail(@RequestBody DictItem dictItem) {
        DictItem detail = dictService.itemDetail(dictItem.getId());
        return ApiResponse.success(detail);
    }
}
