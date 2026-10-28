package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.entity.ShoppingCart;
import com.reggie.mapper.ShoppingCartMapper;
import com.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Override
    public List<ShoppingCart> listByUserId(Long userId) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        return this.list(queryWrapper);
    }

    @Override
    public ShoppingCart add(ShoppingCart cart) {
        Long userId = cart.getUserId();
        Long dishId = cart.getDishId();
        Long setmealId = cart.getSetmealId();

        // 查询购物车中是否已存在该商品
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        if (dishId != null) {
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
            queryWrapper.eq(ShoppingCart::getDishFlavor, cart.getDishFlavor());
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, setmealId);
        }

        ShoppingCart existingCart = this.getOne(queryWrapper);

        if (existingCart != null) {
            // 已存在，数量+1
            existingCart.setNumber(existingCart.getNumber() + 1);
            this.updateById(existingCart);
            return existingCart;
        } else {
            // 不存在，新增
            cart.setNumber(1);
            cart.setCreateTime(LocalDateTime.now());
            this.save(cart);
            return cart;
        }
    }

    @Override
    public ShoppingCart sub(ShoppingCart cart) {
        Long userId = cart.getUserId();
        Long dishId = cart.getDishId();
        Long setmealId = cart.getSetmealId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        if (dishId != null) {
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
            queryWrapper.eq(ShoppingCart::getDishFlavor, cart.getDishFlavor());
        } else {
            queryWrapper.eq(ShoppingCart::getSetmealId, setmealId);
        }

        ShoppingCart existingCart = this.getOne(queryWrapper);

        if (existingCart != null) {
            if (existingCart.getNumber() > 1) {
                // 数量大于1，减1
                existingCart.setNumber(existingCart.getNumber() - 1);
                this.updateById(existingCart);
                return existingCart;
            } else {
                // 数量等于1，直接删除
                this.removeById(existingCart.getId());
                existingCart.setNumber(0);
                return existingCart;
            }
        }

        return null;
    }

    @Override
    public void clean(Long userId) {
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        this.remove(queryWrapper);
    }
}