package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.common.CustomException;
import com.reggie.entity.Category;
import com.reggie.entity.Dish;
import com.reggie.entity.Setmeal;
import com.reggie.mapper.CategoryMapper;
import com.reggie.service.CategoryService;
import com.reggie.service.DishService;
import com.reggie.service.SetMealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetMealService setMealService;

    /**
     * 根据id删除分类。删除之前需要判断
     * @param id
     */
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //根据菜品id查询，是否存在关联
        queryWrapper.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(queryWrapper);

        //存在关联才菜品
        if(count1 > 0){
            throw new CustomException("当前分类下关联菜品，不能删除");
        }

        LambdaQueryWrapper<Setmeal> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Setmeal::getCategoryId,id);
        int count2 = setMealService.count(queryWrapper1);
        //抛出业务异常
        if(count2 > 0){
            throw new CustomException("当前分分类下关联套餐，不能删除");
        }

        //没有关联，删除
        super.removeById(id);
    }
}
