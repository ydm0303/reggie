package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.dto.DishDto;
import com.reggie.entity.Category;
import com.reggie.entity.Dish;
import com.reggie.service.CategoryService;
import com.reggie.service.DishFlavorService;
import com.reggie.service.DishService;
import com.sun.org.apache.bcel.internal.generic.LineNumberGen;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    public R<Page> page(int page,int pageSize,String name){
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

        return R.success("新增菜品成功");
    }

    @GetMapping("/{id}")  ///{id},在请求参数中拼接id
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

        dishService.updateWithDishFlavor(dishDto);

        return R.success("新增菜品成功");
    }

}
