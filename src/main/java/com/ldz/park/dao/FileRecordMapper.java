package com.ldz.park.dao;


import com.ldz.park.entity.FileRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileRecordMapper {
    void insert(FileRecord fileRecord);
}
