package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.dto.DishDto;
import com.reggie.entity.Dish;
import com.reggie.entity.DishFlavor;
import com.reggie.mapper.DishMapper;
import com.reggie.service.DishFlavorService;
import com.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
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
    @Transactional //事务控制，保证数据的一致性,执行失败时，事务回滚，执行成功，事务提交，保存到数据库
    @Override
    public void saveWithDishFlavor(DishDto dishDto) {

        //保存菜品基本信息
        this.save(dishDto);

        //分别获取菜品id和
        Long dishId = dishDto.getId();
        List<DishFlavor> dishDtoFlavors = dishDto.getFlavors();


        //菜品口味 -- 流- 遍历集合
        List<DishFlavor> dishFlavors = dishDtoFlavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味到菜品口味表，dishFlavor
//        dishFlavorService.saveBatch(dishDto.getFlavors());
        dishFlavorService.saveBatch(dishFlavors);

    }

    @Override
    public DishDto getByIdWithFlavor(Long id) {

        //1，查询菜品基本信息表dish
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);  //将菜品基本属性复制给dto

        //2，查询口味表dish_flavor
        //这行代码的作用是从数据库中查询符合条件的菜品口味列表 queryWrapper
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(dishFlavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithDishFlavor(DishDto dishDto) {

        //1.更新dish基本信息
        this.updateById(dishDto);
        //2.清理当前菜品对应的口味数据 -- dish_flavor -- 删除操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        dishFlavorService.remove(queryWrapper);
        //3.重新添加前端提交过来的口味数据-- dish_flavor-新增操作
        List<DishFlavor> flavors = dishDto.getFlavors();
        List<DishFlavor> collect = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(collect);
    }
}
