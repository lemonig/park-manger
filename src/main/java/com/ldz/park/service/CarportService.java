package com.ldz.park.service;

import com.ldz.park.dao.CarportMapper;
import com.ldz.park.model.Carport;
import com.ldz.park.model.meta.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarportService {
    @Autowired
    private CarportMapper carportMapper;

}
