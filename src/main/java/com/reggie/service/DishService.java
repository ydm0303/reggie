package com.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.dto.DishDto;
import com.reggie.entity.Dish;
import org.springframework.stereotype.Service;

@Service
public interface DishService extends IService<Dish> {

    /**
     * 新增菜品，同时新增菜品口味，需要同时操作两张表，dish,dishFlavor
     * @param dishDto
     */
    public void saveWithDishFlavor(DishDto dishDto);
}
