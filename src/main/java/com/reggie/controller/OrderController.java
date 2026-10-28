package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.R;
import com.reggie.dto.OrdersDto;
import com.reggie.entity.OrderDetail;
import com.reggie.entity.Orders;
import com.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     */
    @PostMapping("/submit")
    public R<Orders> submit(@RequestBody OrdersDto ordersDto) {
        log.info("用户下单: {}", ordersDto);
        Orders orders = orderService.submit(ordersDto);
        return R.success(orders);
    }

    /**
     * 用户端分页查询订单
     */
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> userPage(int page, int pageSize) {
        Page<OrdersDto> pageInfo = orderService.userPage(page, pageSize);
        return R.success(pageInfo);
    }

    /**
     * 后台管理端分页查询订单
     */
    @GetMapping("/page")
    public R<Page<OrdersDto>> page(int page, int pageSize, String number, String beginTime, String endTime) {
        Page<OrdersDto> pageInfo = orderService.page(page, pageSize, number, beginTime, endTime);
        return R.success(pageInfo);
    }

    /**
     * 再来一单
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody OrdersDto ordersDto) {
        Long id = ordersDto.getId();
        orderService.again(id);
        return R.success("已加入购物车");
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/{id}")
    public R<OrdersDto> getById(@PathVariable Long id) {
        OrdersDto ordersDto = orderService.getByIdWithDetail(id);
        if (ordersDto != null) {
            return R.success(ordersDto);
        }
        return R.error("订单不存在");
    }

    /**
     * 修改订单状态（后台管理用）
     */
    @PutMapping
    public R<String> updateStatus(@RequestBody Orders orders) {
        orderService.updateStatus(orders);
        return R.success("修改订单状态成功");
    }

    /**
     * 用户取消订单
     */
    @PutMapping("/cancel")
    public R<String> cancel(Long id) {
        orderService.cancel(id);
        return R.success("取消订单成功");
    }
}