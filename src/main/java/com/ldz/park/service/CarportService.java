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

    public ApiResponse list() {
        List<Carport> list = carportMapper.list();
        return new ApiResponse(list);
    }

    public void  add(Carport carport) {
        carportMapper.add(carport);
    }

    public void  update(Carport carport) {
        carportMapper.update(carport);
    }
    public void delete(Integer id) {
        carportMapper.delete(id);
    }
}
