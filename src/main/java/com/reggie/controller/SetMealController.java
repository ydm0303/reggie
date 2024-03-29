package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.dto.SetmealDto;
import com.reggie.entity.Category;
import com.reggie.entity.Setmeal;
import com.reggie.service.CategoryService;
import com.reggie.service.SetMealDishService;
import com.reggie.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetMealController {

    @Autowired
    private SetMealService setMealService;

    @Autowired
    private SetMealDishService setMealDishService;
    
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);

        setMealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功。");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page<Setmeal> pageInfo =  new Page<>(page, pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null,Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        setMealService.page(pageInfo,queryWrapper);
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");
        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> collect = records.stream().map((item -> {
            SetmealDto setmealDto = new SetmealDto();

            BeanUtils.copyProperties(item, setmealDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);

            }
            return setmealDto;

        })).collect(Collectors.toList());


        setmealDtoPage.setRecords(collect);
        return R.success(setmealDtoPage);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        setMealService.removeWithDish(ids);
        return R.success("套餐数据删除成功");
    }

    /**
     * 启停套餐
     * @param ids
     * @return
     */
    @PostMapping("status/{status}")
    public R<String> update(@PathVariable Integer status ,@RequestParam List<Long> ids){
        log.info("status ===========>:{}",status);
        log.info("ids ===========>:{}",ids);

        if (ids.size() > 1) { //批量
            LambdaQueryWrapper<Setmeal> wrapper2 = new LambdaQueryWrapper<>();
            wrapper2.in(ids != null ,Setmeal::getId,ids);
            List<Setmeal> list = setMealService.list(wrapper2);
            for (Setmeal setmeal : list) {
                if (setmeal.getStatus() == 0){
                    return R.error("批量启停时，售卖状态只能为【停售/起售】的菜品,请重新操作！");
                }
                setmeal.setStatus(status);
            }
            setMealService.updateBatchById(list);
        }

        QueryWrapper<Setmeal> setmealQueryWrapper = new QueryWrapper<>();
        setmealQueryWrapper.lambda().in(Setmeal::getId,ids);
        List<Setmeal> list = setMealService.list(setmealQueryWrapper);
        for (Setmeal setmeal : list) {
            setmeal.setStatus(status);
            setMealService.updateById(setmeal);
        }
        return R.success("套餐数据更新成功！");
    }


}
