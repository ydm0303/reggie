package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.common.BaseContext;
import com.reggie.common.CustomException;
import com.reggie.dto.OrdersDto;
import com.reggie.entity.AddressBook;
import com.reggie.entity.OrderDetail;
import com.reggie.entity.Orders;
import com.reggie.entity.ShoppingCart;
import com.reggie.mapper.OrderDetailMapper;
import com.reggie.mapper.OrderMapper;
import com.reggie.service.AddressBookService;
import com.reggie.service.OrderService;
import com.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Override
    @Transactional
    public Orders submit(OrdersDto ordersDto) {
        Long userId = BaseContext.getCurrentId();

        log.info("用户下单开始，userId: {}", userId);

        // 1. 获取当前用户购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);

        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            log.warn("下单失败，购物车为空，userId: {}", userId);
            throw new CustomException("购物车为空，不能下单");
        }

        // 2. 获取地址信息
        Long addressBookId = ordersDto.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null) {
            log.warn("下单失败，地址信息有误，addressBookId: {}", addressBookId);
            throw new CustomException("地址信息有误，不能下单");
        }

        // 3. 计算总金额
        BigDecimal amount = new BigDecimal(0);
        for (ShoppingCart cart : shoppingCartList) {
            amount = amount.add(cart.getAmount().multiply(new BigDecimal(cart.getNumber())));
        }

        log.info("订单金额计算完成，amount: {}, 商品数量: {}", amount, shoppingCartList.size());

        // 4. 构建订单对象
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersDto, orders);
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(2); // 待派送
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setAmount(amount);
        orders.setPhone(addressBook.getPhone());
        orders.setAddress(addressBook.getProvinceName() + addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());
        orders.setUserName(addressBook.getConsignee());
        orders.setConsignee(addressBook.getConsignee());

        this.save(orders);

        log.info("订单主表保存成功，orderId: {}, 订单号: {}", orders.getId(), orders.getNumber());

        // 5. 保存订单明细
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(cart -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orders.getId());
            orderDetail.setName(cart.getName());
            orderDetail.setImage(cart.getImage());
            orderDetail.setDishId(cart.getDishId());
            orderDetail.setSetmealId(cart.getSetmealId());
            orderDetail.setDishFlavor(cart.getDishFlavor());
            orderDetail.setNumber(cart.getNumber());
            orderDetail.setAmount(cart.getAmount());
            return orderDetail;
        }).collect(Collectors.toList());

        // 批量插入订单明细
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetailMapper.insert(orderDetail);
        }

        log.info("订单明细保存成功，orderId: {}, 明细条数: {}", orders.getId(), orderDetailList.size());

        // 6. 清空购物车
        shoppingCartService.clean(userId);

        log.info("购物车已清空，userId: {}", userId);

        return orders;
    }

    @Override
    public Page<OrdersDto> userPage(int page, int pageSize) {
        Long userId = BaseContext.getCurrentId();

        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, userId);
        queryWrapper.orderByDesc(Orders::getOrderTime);

        this.page(pageInfo, queryWrapper);

        // 转换为 OrdersDto
        Page<OrdersDto> dtoPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");

        List<OrdersDto> dtoList = pageInfo.getRecords().stream().map(orders -> {
            OrdersDto dto = new OrdersDto();
            BeanUtils.copyProperties(orders, dto);

            // 查询订单明细
            LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(OrderDetail::getOrderId, orders.getId());
            List<OrderDetail> details = orderDetailMapper.selectList(detailWrapper);
            dto.setOrderDetails(details);

            return dto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public Page<OrdersDto> page(int page, int pageSize, String number, String beginTime, String endTime) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(number)) {
            queryWrapper.eq(Orders::getNumber, number);
        }
        if (StringUtils.isNotBlank(beginTime)) {
            queryWrapper.ge(Orders::getOrderTime, beginTime);
        }
        if (StringUtils.isNotBlank(endTime)) {
            queryWrapper.le(Orders::getOrderTime, endTime);
        }
        queryWrapper.orderByDesc(Orders::getOrderTime);

        this.page(pageInfo, queryWrapper);

        Page<OrdersDto> dtoPage = new Page<>();
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");

        List<OrdersDto> dtoList = pageInfo.getRecords().stream().map(orders -> {
            OrdersDto dto = new OrdersDto();
            BeanUtils.copyProperties(orders, dto);

            LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
            detailWrapper.eq(OrderDetail::getOrderId, orders.getId());
            List<OrderDetail> details = orderDetailMapper.selectList(detailWrapper);
            dto.setOrderDetails(details);

            return dto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    @Transactional
    public void again(Long id) {
        Long userId = BaseContext.getCurrentId();

        log.info("再来一单，userId: {}, orderId: {}", userId, id);

        // 查询订单明细
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, id);
        List<OrderDetail> orderDetailList = orderDetailMapper.selectList(queryWrapper);

        if (orderDetailList == null || orderDetailList.isEmpty()) {
            log.warn("再来一单失败，订单明细为空，orderId: {}", id);
            return;
        }

        // 将订单明细转为购物车对象并批量保存
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(detail -> {
            ShoppingCart cart = new ShoppingCart();
            cart.setUserId(userId);
            cart.setName(detail.getName());
            cart.setImage(detail.getImage());
            cart.setDishId(detail.getDishId());
            cart.setSetmealId(detail.getSetmealId());
            cart.setDishFlavor(detail.getDishFlavor());
            cart.setNumber(detail.getNumber());
            cart.setAmount(detail.getAmount());
            cart.setCreateTime(LocalDateTime.now());
            return cart;
        }).collect(Collectors.toList());

        shoppingCartService.saveBatch(shoppingCartList);

        log.info("再来一单完成，userId: {}, 添加购物车条数: {}", userId, shoppingCartList.size());
    }

    @Override
    public OrdersDto getByIdWithDetail(Long id) {
        Orders orders = this.getById(id);
        if (orders == null) {
            return null;
        }

        OrdersDto dto = new OrdersDto();
        BeanUtils.copyProperties(orders, dto);

        LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(OrderDetail::getOrderId, id);
        List<OrderDetail> details = orderDetailMapper.selectList(detailWrapper);
        dto.setOrderDetails(details);

        return dto;
    }

    @Override
    public void updateStatus(Orders orders) {
        this.updateById(orders);
    }

    @Override
    public void cancel(Long id) {
        Orders orders = this.getById(id);
        if (orders != null && orders.getStatus() == 2) { // 只有待派送状态可以取消
            orders.setStatus(5); // 已取消
            this.updateById(orders);
        }
    }
}