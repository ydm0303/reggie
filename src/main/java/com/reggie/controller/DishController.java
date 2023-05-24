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
    @Scheduled(initialDelay = 1000 * 60,fixedRate = 60 * 1000 * 2)
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

        dishService.updateWithDishFlavor(dishDto);

        return R.success("更新菜品成功");
    }

    /**
     * 修改启售-停售状态 - 修改，单个或者批量
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/{status}/{status}")
    public R<String> discontinueDish(@PathVariable Integer status,@RequestParam List<Long> ids){
        log.info("status:{}",status);
        log.info("ids:{}",ids);
        // update set status =? where ids = ?
//        Dish dish = dishService.getById(ids);
//        if(dish != null){
//            dish.setStatus(status);
//            dishService.updateById(dish);
//            return R.success("更新状态成功");
//        }
        if(ids.size() > 1){ //表示批量
            if(status == 1){//启售
                LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.in(ids != null,Dish::getId,ids);
                List<Dish> dishList = dishService.list(queryWrapper);
                for (Dish dish : dishList){
                    if(dish.getStatus() ==1){
                        return  R.error("批量选择中存在售卖状态为启售的菜品,请重新操作！");
                    }
                }

                List<Dish> collect = dishList.stream().peek((item -> {
                    item.setStatus(status);
                    dishService.updateById(item);
                })).collect(Collectors.toList());

        }
            if (status == 0){
                LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.in(ids != null,Dish::getId,ids);
                List<Dish> dishList = dishService.list(queryWrapper);
                for (Dish dish : dishList){
                    if(dish.getStatus() ==0){
                        return  R.error("批量选择中存在售卖状态为停售的菜品,请重新操作！");
                    }
                }

                List<Dish> collect = dishList.stream().peek((item -> {
                    item.setStatus(status);
                    dishService.updateById(item);
                })).collect(Collectors.toList());

            }

        }
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids != null,Dish::getId,ids);
        List<Dish> dishList = dishService.list(queryWrapper);

        List<Dish> collect = dishList.stream().peek((item -> {
            item.setStatus(status);
            dishService.updateById(item);
        })).collect(Collectors.toList());

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
        //物理删除
        if(ids != null){
            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(Dish::getId,ids);

            List<Dish> dishList = dishService.list(queryWrapper);
            for(Dish dish : dishList){
                if(dish.getStatus()==1){
                    return R.error("存在在售状态的菜品,删除失败！");
                }
                dishService.removeById(dish.getId());
            }
            return R.success("菜品删除成功");



//
//            Dish dish = dishService.getById(ids);  //启售状态不可删除！
//            if(dish.getStatus() == 0){
//                dishService.removeById(ids);
//                return R.success("删除菜品成功");
//            }else {
//                return R.error("启售状态不可删除,请更改后再操作");
//            }
        }
//        //逻辑删除
//        if(ids != null){
//            Dish dish = dishService.getById(ids);  //启售状态不可删除！
//            if(dish.getStatus() ==0){
////                dishService.removeById(ids);
//                dish.setIsDeleted(1);
//                dishService.updateById(dish);
//                return R.success("删除菜品成功");
//            }else {
//                return R.error("启售状态不可删除,请更改后再操作");
//            }
//        }

        return R.error("菜品ID为空，删除失败");
    }

}
