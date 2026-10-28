package com.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService extends IService<ShoppingCart> {
    /**
     * 查询当前用户购物车列表
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<ShoppingCart> listByUserId(Long userId);

    /**
     * 添加/更新购物车项
     * @param cart 购物车对象
     * @return 更新后的购物车对象
     */
    ShoppingCart add(ShoppingCart cart);

    /**
     * 减少购物车商品数量
     * @param cart 购物车对象
     * @return 更新后的购物车对象
     */
    ShoppingCart sub(ShoppingCart cart);

    /**
     * 清空购物车
     * @param userId 用户ID
     */
    void clean(Long userId);
}