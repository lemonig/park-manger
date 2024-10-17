package com.ldz.park.model;

import com.ldz.park.model.meta.Pagination;
import lombok.Data;

@Data
public class SearchForm extends Pagination {
private String keyword;
}
