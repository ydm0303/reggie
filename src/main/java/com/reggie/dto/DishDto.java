package com.reggie.dto;


import com.reggie.entity.Dish;
import com.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    /**
     * 个人理解，dto是接收前端传进来的数据，当实体类无法满足时
     * vo是返回给前端的数据
     */

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
