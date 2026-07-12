package com.ldz.park.web.wechat;

import com.ldz.park.model.DictItem;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "微信小程序 / 字典", description = "小程序端字典项查询")
@RestController
@RequestMapping("/api/wechat/dict")
@RequiredArgsConstructor
public class WechatDictController {

    private final DictService dictService;

    @Operation(summary = "按 typeCode 查询字典项")
    @GetMapping("/items")
    public ApiResponse<List<DictItem>> items(@RequestParam(required = false) String typeCode) {
        return ApiResponse.success(dictService.listItemsByTypeCode(typeCode));
    }
}
