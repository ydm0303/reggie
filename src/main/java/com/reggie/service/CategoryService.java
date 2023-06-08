package com.reggie.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.entity.Category;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface CategoryService extends IService<Category> {

    public void remove(Long id);
}
