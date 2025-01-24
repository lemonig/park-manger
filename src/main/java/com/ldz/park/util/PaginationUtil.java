package com.ldz.park.util;

import com.github.pagehelper.PageInfo;
import com.ldz.park.model.vo.PaginationVO;

public class PaginationUtil {

    public static PaginationVO getTotal(PageInfo pageInfo){
        PaginationVO paginationVO = new PaginationVO();
        paginationVO.setTotal(pageInfo != null ? pageInfo.getTotal() : 0);
        return paginationVO;
    }

}