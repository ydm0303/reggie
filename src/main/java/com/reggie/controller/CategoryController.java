package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.AuditLogger;
import com.reggie.common.BaseContext;
import com.reggie.common.R;
import com.reggie.entity.Category;
import com.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.swing.*;
import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("category{}",category);
        categoryService.save(category);
        AuditLogger.create("EMPLOYEE", BaseContext.getCurrentId(), "CATEGORY", category.getId(), category.getName());
        return R.success("新增分类成功");

    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        //分页构造器
        Page pageInfo = new Page(page, pageSize);
        //构造查询条件
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //排序
        queryWrapper.orderByAsc(Category::getSort);
        //分页查询
        categoryService.page(pageInfo,queryWrapper);

        return R.success(pageInfo);
    }

    @DeleteMapping
    public R<String> delete(Long ids){

        log.info("删除分类，id为{}",ids);

        Category category = categoryService.getById(ids);
        String categoryName = category != null ? category.getName() : "未知";

        categoryService.remove(ids);

        AuditLogger.delete("EMPLOYEE", BaseContext.getCurrentId(), "CATEGORY", ids, categoryName);

        return R.success("分类信息删除成功");

    }

    /**
     * 根据id修改分类信息
     * @param category
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息{}",category);

        Category oldCategory = categoryService.getById(category.getId());
        String changes = "";
        if (oldCategory != null) {
            StringBuilder sb = new StringBuilder();
            if (!oldCategory.getName().equals(category.getName())) {
                sb.append("name:").append(oldCategory.getName()).append("->").append(category.getName()).append("; ");
            }
            if (oldCategory.getSort() != category.getSort()) {
                sb.append("sort:").append(oldCategory.getSort()).append("->").append(category.getSort()).append("; ");
            }
            if (oldCategory.getType() != category.getType()) {
                sb.append("type:").append(oldCategory.getType()).append("->").append(category.getType()).append("; ");
            }
            changes = sb.toString();
        }

        categoryService.updateById(category);

        if (!changes.isEmpty()) {
            AuditLogger.update("EMPLOYEE", BaseContext.getCurrentId(), "CATEGORY", category.getId(), changes);
        }

        return R.success("更新分类成功");
    }

    /**
     * 获取分类-根据类型来获取
     * @param category
     * @return
     */
    @GetMapping("/list")
    public R<List<Category>> listR(Category category){
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        queryWrapper.eq(category.getType() != null,Category::getType,category.getType());
        //排序字段，有限使用Sort字段排序。如果相同，则使用Updatetime字段降序排序
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        //个人理解:LambdaQueryWrapper,是用来构建sql查询的条件，也就是写sql；执行sql查询还是通过调用service来实现

        List<Category> list = categoryService.list(queryWrapper);
        return R.success(list);
    }

}
