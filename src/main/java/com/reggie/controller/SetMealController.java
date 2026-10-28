package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.AuditLogger;
import com.reggie.common.BaseContext;
import com.reggie.common.R;
import com.reggie.dto.SetmealDto;
import com.reggie.entity.Category;
import com.reggie.entity.Setmeal;
import com.reggie.entity.SetmealDish;
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

        AuditLogger.create("EMPLOYEE", BaseContext.getCurrentId(), "SETMEAL", setmealDto.getId(), setmealDto.getName());

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

        // 记录删除前的套餐名称用于审计
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        List<Setmeal> list = setMealService.list(queryWrapper);

        setMealService.removeWithDish(ids);

        // 审计日志：记录每个套餐的删除
        for (Setmeal setmeal : list) {
            AuditLogger.delete("EMPLOYEE", BaseContext.getCurrentId(), "SETMEAL", setmeal.getId(), setmeal.getName());
        }

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

        if (ids == null || ids.isEmpty()) {
            return R.error("套餐ID不能为空");
        }

        // 校验状态值
        if (status != 0 && status != 1) {
            return R.error("状态值无效，只能为0(停售)或1(起售)");
        }

        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Setmeal::getId, ids);
        List<Setmeal> list = setMealService.list(wrapper);

        if (list.isEmpty()) {
            return R.error("未找到对应套餐");
        }

        // 批量更新状态
        for (Setmeal setmeal : list) {
            if (setmeal.getStatus() != status) {
                int oldStatus = setmeal.getStatus();
                setmeal.setStatus(status);
                setMealService.updateById(setmeal);
                // 审计日志：记录状态变更
                AuditLogger.statusChange("EMPLOYEE", BaseContext.getCurrentId(), "SETMEAL", setmeal.getId(), oldStatus, status);
            }
        }

        return R.success("套餐数据更新成功！");
    }

    /**
     * 根据套餐id查询包含的菜品列表
     * @param id 套餐ID
     * @return 菜品列表
     */
    @GetMapping("/dish/{id}")
    public R<List<SetmealDish>> getDishBySetmealId(@PathVariable Long id) {
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        queryWrapper.orderByAsc(SetmealDish::getSort);
        List<SetmealDish> list = setMealDishService.list(queryWrapper);
        return R.success(list);
    }

}