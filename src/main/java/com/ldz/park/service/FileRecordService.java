package com.ldz.park.service;

import com.ldz.park.dao.FileRecordMapper;
import com.ldz.park.entity.FileRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileRecordService {
    @Autowired
    FileRecordMapper fileRecordMapper;

    public void insert(FileRecord fileRecord){
        fileRecordMapper.insert(fileRecord);
    }


}
