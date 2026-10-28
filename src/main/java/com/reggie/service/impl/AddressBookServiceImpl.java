package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.entity.AddressBook;
import com.reggie.mapper.AddressBookMapper;
import com.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {

    @Override
    public List<AddressBook> listByUserId(Long userId) {
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, userId);
        queryWrapper.eq(AddressBook::getIsDeleted, 0);
        queryWrapper.orderByDesc(AddressBook::getIsDefault).orderByDesc(AddressBook::getUpdateTime);
        return this.list(queryWrapper);
    }

    @Override
    public AddressBook getDefault(Long userId) {
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, userId);
        queryWrapper.eq(AddressBook::getIsDefault, 1);
        queryWrapper.eq(AddressBook::getIsDeleted, 0);
        return this.getOne(queryWrapper);
    }

    @Override
    @Transactional
    public void setDefault(Long id) {
        // 获取当前地址
        AddressBook address = this.getById(id);
        if (address == null) {
            return;
        }

        Long userId = address.getUserId();

        // 先将该用户的所有地址设为非默认
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, userId);
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        AddressBook defaultAddress = this.getOne(queryWrapper);
        if (defaultAddress != null) {
            defaultAddress.setIsDefault(0);
            this.updateById(defaultAddress);
        }

        // 设置选中的地址为默认
        address.setIsDefault(1);
        address.setUpdateTime(LocalDateTime.now());
        this.updateById(address);
    }

    @Override
    public boolean save(AddressBook addressBook) {
        addressBook.setCreateTime(LocalDateTime.now());
        addressBook.setUpdateTime(LocalDateTime.now());
        addressBook.setIsDeleted(0);
        // 如果设置为默认地址，需要先取消其他默认地址
        if (addressBook.getIsDefault() != null && addressBook.getIsDefault() == 1) {
            setDefaultAddressNotDefault(addressBook.getUserId());
        }
        return super.save(addressBook);
    }

    @Override
    public boolean updateById(AddressBook addressBook) {
        addressBook.setUpdateTime(LocalDateTime.now());
        // 如果设置为默认地址，需要先取消其他默认地址
        if (addressBook.getIsDefault() != null && addressBook.getIsDefault() == 1) {
            setDefaultAddressNotDefault(addressBook.getUserId());
        }
        return super.updateById(addressBook);
    }

    private void setDefaultAddressNotDefault(Long userId) {
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, userId);
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        AddressBook defaultAddress = this.getOne(queryWrapper);
        if (defaultAddress != null) {
            defaultAddress.setIsDefault(0);
            defaultAddress.setUpdateTime(LocalDateTime.now());
            this.updateById(defaultAddress);
        }
    }
}