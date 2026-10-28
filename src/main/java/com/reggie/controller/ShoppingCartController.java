package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.reggie.common.BaseContext;
import com.reggie.common.R;
import com.reggie.entity.ShoppingCart;
import com.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 查看购物车列表
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> list = shoppingCartService.listByUserId(userId);
        return R.success(list);
    }

    /**
     * 添加购物车
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        ShoppingCart cart = shoppingCartService.add(shoppingCart);
        return R.success(cart);
    }

    /**
     * 减少购物车商品数量
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        ShoppingCart cart = shoppingCartService.sub(shoppingCart);
        return R.success(cart);
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartService.clean(userId);
        return R.success("清空购物车成功");
    }
}