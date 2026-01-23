package com.ldz.park.model.vo;

import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分页信息 VO（简化版）
 * 只包含前端需要的核心分页字段，避免返回 PageInfo 的冗余字段（如 list）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageVO implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * 当前页码
   */
  private Integer pageNum;

  /**
   * 每页大小
   */
  private Integer pageSize;

  /**
   * 总记录数
   */
  private Long total;

  /**
   * 总页数
   */
  private Integer pages;

  /**
   * 是否有下一页
   */
  private Boolean hasNextPage;

  /**
   * 是否有上一页
   */
  private Boolean hasPreviousPage;

  /**
   * 从 PageInfo 转换为 PageVO
   */
  public static PageVO from(PageInfo<?> pageInfo) {
    if (pageInfo == null) {
      return null;
    }
    return PageVO.builder()
        .pageNum(pageInfo.getPageNum())
        .pageSize(pageInfo.getPageSize())
        .total(pageInfo.getTotal())
        .pages(pageInfo.getPages())
        .hasNextPage(pageInfo.isHasNextPage())
        .hasPreviousPage(pageInfo.isHasPreviousPage())
        .build();
  }
}
