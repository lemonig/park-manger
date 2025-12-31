package com.ldz.park.web;

import com.ldz.park.model.Carport;
import com.ldz.park.model.meta.ApiResponse;
import com.ldz.park.service.CarportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "车位管理", description = "车位相关接口")
@RestController
@RequestMapping("/api/carport")
public class CarportController {
    @Autowired
    private CarportService carportService;


}
