package com.reggie.dto;


import com.reggie.entity.Dish;
import com.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    /**
     * 个人理解，dto是接收前端传进来的数据，当实体类无法满足时，
     * DTO可以用来封装一些复杂的数据结构
     * DTO主要用于应用程序内部的数据传输，而VO主要用于应用程序与外部(如前端）的数据交互，二者的功能及用法有所不同。
     * vo是返回给前端的数据
     */

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
