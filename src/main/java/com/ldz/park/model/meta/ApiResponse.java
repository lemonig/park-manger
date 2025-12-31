package com.ldz.park.model.meta;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 统一API响应结构（行业标准版）
 * <p>
 * 设计原则：
 * 1. 使用泛型 <T> 支持任意类型数据返回，提升类型安全和可读性
 * 2. 引入标准 HTTP-like 状态码（code），便于前端统一处理（0=成功，其他为业务错误，5xx/4xx可映射系统异常）
 * 3. timestamp 使用 java.time.LocalDateTime（Java8+ 推荐，取代过时的 Joda Time）
 * 4. message 默认空字符串，避免 null
 * 5. 使用 @JsonInclude(Include.NON_NULL) 避免序列化 null 字段，减少响应体积
 * 6. 提供丰富的静态工厂方法（success / error），调用更便捷
 * 7. 保留 Serializable，便于序列化/缓存
 * 8. 添加 @Builder、@AllArgsConstructor 等 Lombok 注解，提升构建灵活性
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
     * 响应码：0 表示成功，其他表示业务错误（可自定义枚举）
     * 行业惯例：0 / 200 = success，400+ = client error，500+ = server error
     */
    @Builder.Default
    private Integer code = 0;

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
     * 附加数据（如分页信息、统计等）
     */
    private Object additionalData;

    /**
     * 关联对象（如详情页相关的其他实体）
     */
    private Object relatedObjects;

    /**
     * 请求处理时间（响应生成时间）
     * 使用 @JsonFormat 统一格式化，避免前端时区问题
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // ====================== 静态工厂方法（推荐使用方式） ======================

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder().build();
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder().data(data).build();
    }

    /**
     * 成功响应（带数据和消息）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 失败响应（自定义业务码和消息）
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .build();
    }

    /**
     * 失败响应（默认业务错误码 1）
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(1, message);
    }

    /**
     * 系统异常响应（默认 500）
     */
    public static <T> ApiResponse<T> systemError(String message) {
        return error(500, message);
    }

    public static <T> ApiResponse<T> ok() {
        return ApiResponse.success();
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.success(data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.success(message, data);
    }

}