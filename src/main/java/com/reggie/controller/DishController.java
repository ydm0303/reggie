package com.reggie.controller;

import com.reggie.service.DishFlavorService;
import com.reggie.service.DishService;
import com.sun.scenario.effect.impl.prism.PrCropPeer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

}
