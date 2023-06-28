package com.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.dto.SetmealDto;
import com.reggie.entity.Setmeal;
import org.springframework.stereotype.Service;

@Service
public interface SetMealService extends IService<Setmeal> {
    /**
     * 新增套餐，同时保留套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);
}
