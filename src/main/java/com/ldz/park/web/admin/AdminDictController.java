package com.ldz.park.web.admin;

import com.ldz.park.model.DictItem;
import com.ldz.park.model.DictType;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "后台管理 / 字典", description = "后台管理端字典管理")
@RestController
@RequestMapping("/api/admin/dict")
@RequiredArgsConstructor
public class AdminDictController {

    private final DictService dictService;

    /* ===================== 字典类型 ===================== */

    @Operation(summary = "字典类型列表")
    @GetMapping("/types")
    public ApiResponse<List<DictType>> listTypes() {
        return ApiResponse.success(dictService.listTypes());
    }

    @Operation(summary = "字典类型详情")
    @GetMapping("/types/{id}")
    public ApiResponse<DictType> typeDetail(@PathVariable Long id) {
        return ApiResponse.success(dictService.typeDetail(id));
    }

    @Operation(summary = "新增字典类型")
    @PostMapping("/types")
    public ApiResponse<Void> addType(@RequestBody DictType dictType) {
        dictService.addType(dictType);
        return ApiResponse.ok();
    }

    @Operation(summary = "更新字典类型")
    @PutMapping("/types/{id}")
    public ApiResponse<Void> updateType(@PathVariable Long id, @RequestBody DictType dictType) {
        dictType.setId(id);
        dictService.updateType(dictType);
        return ApiResponse.ok();
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/types/{id}")
    public ApiResponse<Void> deleteType(@PathVariable Long id) {
        dictService.deleteType(id);
        return ApiResponse.ok();
    }

    /* ===================== 字典项 ===================== */

    @Operation(summary = "字典项列表")
    @GetMapping("/items")
    public ApiResponse<List<DictItem>> listItems(@RequestParam(required = false) String typeCode) {
        return ApiResponse.success(dictService.listItemsByTypeCode(typeCode));
    }

    @Operation(summary = "字典项详情")
    @GetMapping("/items/{id}")
    public ApiResponse<DictItem> itemDetail(@PathVariable Long id) {
        return ApiResponse.success(dictService.itemDetail(id));
    }

    @Operation(summary = "新增字典项")
    @PostMapping("/items")
    public ApiResponse<Void> addItem(@RequestBody DictItem dictItem) {
        dictService.addItem(dictItem);
        return ApiResponse.ok();
    }

    @Operation(summary = "更新字典项")
    @PutMapping("/items/{id}")
    public ApiResponse<Void> updateItem(@PathVariable Long id, @RequestBody DictItem dictItem) {
        dictItem.setId(id);
        dictService.updateItem(dictItem);
        return ApiResponse.ok();
    }

    @Operation(summary = "删除字典项")
    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> deleteItem(@PathVariable Long id) {
        dictService.deleteItem(id);
        return ApiResponse.ok();
    }
}
