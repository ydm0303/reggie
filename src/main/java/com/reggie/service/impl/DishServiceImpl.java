package com.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.dto.DishDto;
import com.reggie.entity.Dish;
import com.reggie.entity.DishFlavor;
import com.reggie.mapper.DishMapper;
import com.reggie.service.DishFlavorService;
import com.reggie.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper,Dish> implements DishService{

    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     *新增菜品，同时保存菜品口味
     * @param dishDto
     */
    @Transactional  //事务控制，保证数据的一致性
    public void saveWithDishFlavor(DishDto dishDto) {

        //保存菜品基本信息
        this.save(dishDto);

        //菜品id
        Long dishId = dishDto.getId();
        List<DishFlavor> dishDtoFlavors = dishDto.getFlavors();

        //菜品口味
        List<DishFlavor> dishFlavors = dishDtoFlavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味到菜品口味表，dishFlavor
//        dishFlavorService.saveBatch(dishDto.getFlavors());
        dishFlavorService.saveBatch(dishFlavors);

    }
}
