package com.ldz.park.model.meta;

public class Pagination {

    private Integer start_page;
    private Integer limit;
    private Integer start;
    private Integer total;
    private  Integer total_page;

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getTotal_page() {
        return total_page;
    }

    public void setTotal_page(Integer total_page) {
        this.total_page = total_page;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getStart_page() {
        return start_page;
    }

    public void setStart_page(Integer start_page) {
        this.start_page = start_page;
    }





}
