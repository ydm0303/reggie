package com.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.entity.AddressBook;

import java.util.List;

public interface AddressBookService extends IService<AddressBook> {
    /**
     * 查询当前用户的地址列表
     * @param userId 用户ID
     * @return 地址列表
     */
    List<AddressBook> listByUserId(Long userId);

    /**
     * 获取默认地址
     * @param userId 用户ID
     * @return 默认地址
     */
    AddressBook getDefault(Long userId);

    /**
     * 设置默认地址
     * @param id 地址ID
     */
    void setDefault(Long id);
}