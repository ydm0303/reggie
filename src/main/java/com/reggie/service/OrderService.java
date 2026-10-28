package com.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.dto.OrdersDto;
import com.reggie.entity.Orders;

import java.util.List;

public interface OrderService extends IService<Orders> {
    /**
     * 用户下单
     * @param ordersDto 订单DTO
     * @return 订单对象
     */
    Orders submit(OrdersDto ordersDto);

    /**
     * 用户端分页查询订单
     * @param page 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    Page<OrdersDto> userPage(int page, int pageSize);

    /**
     * 后台管理端分页查询所有订单
     * @param page 页码
     * @param pageSize 页大小
     * @param number 订单号
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    Page<OrdersDto> page(int page, int pageSize, String number, String beginTime, String endTime);

    /**
     * 再来一单
     * @param id 订单ID
     */
    void again(Long id);

    /**
     * 查询订单详情
     * @param id 订单ID
     * @return 订单DTO
     */
    OrdersDto getByIdWithDetail(Long id);

    /**
     * 修改订单状态
     * @param orders 订单对象
     */
    void updateStatus(Orders orders);

    /**
     * 用户取消订单
     * @param id 订单ID
     */
    void cancel(Long id);
}