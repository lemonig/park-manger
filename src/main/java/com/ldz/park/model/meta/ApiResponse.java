package com.ldz.park.model.meta;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.pagehelper.PageInfo;
import com.ldz.park.model.vo.PageVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 统一API响应结构（行业标准版）
 * <p>
 * 参考标准：JSON API、RESTful API、GraphQL 等主流规范
 * <p>
 * 响应结构：
 * 
 * <pre>
 * {
 *   "code": 0,           // 响应码：0=成功，其他=业务错误
 *   "message": "",        // 响应消息
 *   "data": {...},        // 业务数据
 *   "page": {...},        // 分页信息（列表查询时）
 *   "meta": {...},        // 元数据（统计、汇总等额外信息）
 *   "timestamp": "..."    // 响应时间戳
 * }
 * </pre>
 * <p>
 * 设计原则：
 * 1. 使用泛型 <T> 支持任意类型数据返回，提升类型安全
 * 2. 标准 HTTP-like 状态码（0=成功，400+=客户端错误，500+=服务端错误）
 * 3. 使用 @JsonInclude(NON_NULL) 避免序列化 null 字段，减少响应体积
 * 4. 提供丰富的静态工厂方法，调用更便捷
 * 5. 支持 Builder 模式，构建更灵活
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     * 0 = 成功
     * 1-399 = 业务错误
     * 400-499 = 客户端错误
     * 500+ = 服务端错误
     */
    @Builder.Default
    private Integer code = 0;

    @Builder.Default
    private Boolean success = true;

    /**
     * 响应消息
     */
    @Builder.Default
    private String message = "";

    /**
     * 业务数据
     */
    private T data;

    /**
     * 分页信息（列表查询时使用）
     * 使用简化的 PageVO，只包含核心分页字段，避免冗余
     */
    private PageVO page;

    /**
     * 元数据（统计、汇总、额外信息等）
     * 符合 JSON API 规范，用于存放非业务数据的元信息
     */
    private Map<String, Object> meta;

    /**
     * 请求处理时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ====================== 成功响应方法 ======================

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .success(true)
                .build();
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .success(true)
                .build();
    }

    /**
     * 成功响应（带数据和消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .message(message)
                .data(data)
                .success(true)
                .build();
    }

    /**
     * 成功响应（带数据和分页信息）
     * 自动将 PageInfo 转换为简化的 PageVO
     */
    public static <T> ApiResponse<T> successWithPage(T data, PageInfo<?> pageInfo) {
        return ApiResponse.<T>builder()
                .data(data)
                .page(PageVO.from(pageInfo))
                .success(true)
                .build();
    }

    /**
     * 成功响应（带数据、分页信息和元数据）
     * 自动将 PageInfo 转换为简化的 PageVO
     */
    public static <T> ApiResponse<T> successWithPageAndMeta(T data, PageInfo<?> pageInfo, Map<String, Object> meta) {
        return ApiResponse.<T>builder()
                .data(data)
                .page(PageVO.from(pageInfo))
                .meta(meta)
                .success(true)
                .build();
    }

    /**
     * 成功响应（带数据和元数据）
     */
    public static <T> ApiResponse<T> successWithMeta(T data, Map<String, Object> meta) {
        return ApiResponse.<T>builder()
                .data(data)
                .meta(meta)
                .success(true)
                .build();
    }

    // ====================== 失败响应方法 ======================

    /**
     * 失败响应（自定义业务码和消息）
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .success(false)
                .build();
    }

    /**
     * 失败响应（默认业务错误码 1）
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(1, message);
    }

    /**
     * 客户端错误响应（400）
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return error(400, message);
    }

    /**
     * 未授权错误响应（401）
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return error(401, message);
    }

    /**
     * 禁止访问错误响应（403）
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return error(403, message);
    }

    /**
     * 资源不存在错误响应（404）
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return error(404, message);
    }

    /**
     * 系统异常响应（500）
     */
    public static <T> ApiResponse<T> systemError(String message) {
        return error(500, message);
    }

    // ====================== 便捷方法（别名） ======================

    /**
     * 成功响应（无数据）- ok() 别名
     */
    public static <T> ApiResponse<T> ok() {
        return success();
    }

    /**
     * 成功响应（带数据）- ok() 别名
     */
    public static <T> ApiResponse<T> ok(T data) {
        return success(data);
    }

    /**
     * 成功响应（带数据和消息）- ok() 别名
     */
    public static <T> ApiResponse<T> ok(String message, T data) {
        return success(message, data);
    }

    // ====================== 链式方法（用于添加元数据） ======================

    /**
     * 添加元数据（链式调用）
     */
    public ApiResponse<T> withMeta(String key, Object value) {
        if (this.meta == null) {
            this.meta = new java.util.HashMap<>();
        }
        this.meta.put(key, value);
        return this;
    }

    /**
     * 批量添加元数据（链式调用）
     */
    public ApiResponse<T> withMeta(Map<String, Object> meta) {
        if (this.meta == null) {
            this.meta = new java.util.HashMap<>();
        }
        if (meta != null) {
            this.meta.putAll(meta);
        }
        return this;
    }

}