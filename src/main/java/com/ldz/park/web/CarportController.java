package com.ldz.park.web;

import com.ldz.park.model.Carport;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.CarportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/carport")
public class CarportController {
    @Autowired
    CarportService carportService;

    @PostMapping("/list")
    public ApiResponse carportList(HttpServletRequest request) {
        return carportService.list();

    }

    @PostMapping("/add")
    public ApiResponse carportAdd(HttpServletRequest request, @RequestBody Carport carport) {
         carportService.add(carport);
         return new ApiResponse();

    }

    @PostMapping("/update")
    public ApiResponse carportUpdate(HttpServletRequest request, @RequestBody Carport carport) {
        carportService.update(carport);
        return new ApiResponse();

    }


    @PostMapping(value = "/delete")
    public ApiResponse delete(@RequestBody Carport carport) {
        ApiResponse apiResponse = new ApiResponse();
        carportService.delete(carport.getId());
        return apiResponse;
    }
}
