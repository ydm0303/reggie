package com.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.dto.SetmealDto;
import com.reggie.entity.Setmeal;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface SetMealService extends IService<Setmeal> {
    /**
     * 新增套餐，同时保留套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐。同时删除关联表和菜品数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);
}
