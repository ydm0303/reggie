package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.AuditLogger;
import com.reggie.common.BaseContext;
import com.reggie.common.R;
import com.reggie.dto.DishDto;
import com.reggie.entity.Category;
import com.reggie.entity.Dish;
import com.reggie.service.CategoryService;
import com.reggie.service.DishFlavorService;
import com.reggie.service.DishService;
import com.sun.org.apache.bcel.internal.generic.LineNumberGen;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 菜品分页，除了需要查询基本的菜品信息，还要需要展示图片，菜品分类
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info("page = {},pageSize = {},name = {}", page, pageSize, name);
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(name),Dish::getName,name);//name不为空时，才添加过滤条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        List<Dish> records = pageInfo.getRecords();

        //利用流遍历集合records
        List<DishDto> list = records.stream().map((item -> {
            //item就是每一个菜品对象

            DishDto dishDto = new DishDto();

            //对象拷贝
            BeanUtils.copyProperties(item, dishDto);

            Long categoryId = item.getCategoryId(); //获取分类id
            Category category = categoryService.getById(categoryId);  //根据id获取分类对象
            if(category != null){
                String categoryName = category.getName(); //根据对象获取属性-分类名称
                dishDto.setCategoryName(categoryName);
            }

            return dishDto; //返回dto

        })).collect(Collectors.toList());

        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /**
     * 个人理解：当从前端传入的数据对象是JSON时，需要加RequestBody注解
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithDishFlavor(dishDto);

        AuditLogger.create("EMPLOYEE", BaseContext.getCurrentId(), "DISH", dishDto.getId(), dishDto.getName());
        return R.success("新增菜品成功");
    }

    @GetMapping("/{id}")  ///{id},在请求参数中拼接id  例如：http://localhost:8080/dish/1413384757047271425
    public R<DishDto> getDish(@PathVariable Long id){  //@PathVariable 从请求路径中传入参数 id

        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }


    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        // 记录更新前信息用于审计
        DishDto oldDto = dishService.getByIdWithFlavor(dishDto.getId());
        String changes = "";
        if (oldDto != null) {
            StringBuilder sb = new StringBuilder();
            if (!oldDto.getName().equals(dishDto.getName())) {
                sb.append("name:").append(oldDto.getName()).append("->").append(dishDto.getName()).append("; ");
            }
            if (oldDto.getStatus() != dishDto.getStatus()) {
                sb.append("status:").append(oldDto.getStatus()).append("->").append(dishDto.getStatus()).append("; ");
            }
            if (oldDto.getCategoryId() != null && dishDto.getCategoryId() != null && !oldDto.getCategoryId().equals(dishDto.getCategoryId())) {
                sb.append("categoryId:").append(oldDto.getCategoryId()).append("->").append(dishDto.getCategoryId()).append("; ");
            }
            changes = sb.toString();
        }

        dishService.updateWithDishFlavor(dishDto);

        if (!changes.isEmpty()) {
            AuditLogger.update("EMPLOYEE", BaseContext.getCurrentId(), "DISH", dishDto.getId(), changes);
        }

        return R.success("更新菜品成功");
    }

    /**
     * 修改启售-停售状态 - 修改，单个或者批量
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> discontinueDish(@PathVariable Integer status,@RequestParam List<Long> ids){
        log.info("status:{}",status);
        log.info("ids:{}",ids);

        if (ids == null || ids.isEmpty()) {
            return R.error("菜品ID不能为空");
        }

        // 校验状态值
        if (status != 0 && status != 1) {
            return R.error("状态值无效，只能为0(停售)或1(起售)");
        }

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        List<Dish> dishList = dishService.list(queryWrapper);

        if (dishList.isEmpty()) {
            return R.error("未找到对应菜品");
        }

        // 批量更新状态
        for (Dish dish : dishList) {
            if (dish.getStatus() != status) {
                int oldStatus = dish.getStatus();
                dish.setStatus(status);
                dishService.updateById(dish);
                // 审计日志：记录每个菜品的状态变更
                AuditLogger.statusChange("EMPLOYEE", BaseContext.getCurrentId(), "DISH", dish.getId(), oldStatus, status);
            }
        }

        return R.success("更新状态成功");
    }

    /**
     * 删除菜品-采用逻辑删除,http://localhost:8080/dish?ids=1659577818215510017
     *
     * http://localhost:8080/dish?ids=1659577818215510017 和
     * http://localhost:8080/dish/ids=1659577818215510017区别是什么
     * ?ids=1659577818215510017,方法的请求参数，使用 key=value 的形式传递参数
     * /ids=1659577818215510017，这是将参数作为路径的一部分进行传递 ，需要注解@PathVariable
     * @return
     */
    @DeleteMapping()
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("id:{}",ids);
        //逻辑删除
        if(ids != null && !ids.isEmpty()){
            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Dish::getId,ids);

            List<Dish> dishList = dishService.list(queryWrapper);
            for(Dish dish : dishList){
                if(dish.getStatus()==1){
                    return R.error("存在在售状态的菜品,删除失败！");
                }
                // 逻辑删除：设置 is_deleted = 1
                dish.setIsDeleted(1);
                dishService.updateById(dish);
                // 审计日志：记录删除
                AuditLogger.delete("EMPLOYEE", BaseContext.getCurrentId(), "DISH", dish.getId(), dish.getName());
            }
            return R.success("菜品删除成功");
        }

        return R.error("菜品ID为空，删除失败");
    }

    /**
     *根据条件查询对应的菜品数据
     * @return
     */
    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish){
    LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.eq(dish.getCategoryId() != null,Dish::getCategoryId,dish.getCategoryId());
    //添加条件，查询状态为启售的
    lambdaQueryWrapper.eq(Dish::getStatus,1);
    // 排除已删除的
    lambdaQueryWrapper.eq(Dish::getIsDeleted, 0);
    lambdaQueryWrapper.orderByDesc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

    List<Dish> list = dishService.list(lambdaQueryWrapper);
    return R.success(list);
    }


}