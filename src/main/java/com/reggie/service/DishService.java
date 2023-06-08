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

    //通过菜品id查询口味
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品
    public void updateWithDishFlavor(DishDto dishDto);
}
