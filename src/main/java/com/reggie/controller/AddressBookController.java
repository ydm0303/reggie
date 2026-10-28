package com.reggie.controller;

import com.reggie.common.BaseContext;
import com.reggie.common.R;
import com.reggie.entity.AddressBook;
import com.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 查询当前用户的所有地址
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list() {
        Long userId = BaseContext.getCurrentId();
        List<AddressBook> list = addressBookService.listByUserId(userId);
        return R.success(list);
    }

    /**
     * 新增地址
     */
    @PostMapping
    public R<String> save(@RequestBody AddressBook addressBook) {
        Long userId = BaseContext.getCurrentId();
        addressBook.setUserId(userId);
        addressBookService.save(addressBook);
        return R.success("新增地址成功");
    }

    /**
     * 修改地址
     */
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook) {
        addressBookService.updateById(addressBook);
        return R.success("修改地址成功");
    }

    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")
    public R<AddressBook> getById(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return R.success(addressBook);
        }
        return R.error("没有查询到对应地址信息");
    }

    /**
     * 删除地址
     */
    @DeleteMapping
    public R<String> delete(Long id) {
        addressBookService.removeById(id);
        return R.success("删除地址成功");
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/default")
    public R<String> setDefault(@RequestBody AddressBook addressBook) {
        addressBookService.setDefault(addressBook.getId());
        return R.success("设置默认地址成功");
    }

    /**
     * 获取默认地址
     */
    @GetMapping("/default")
    public R<AddressBook> getDefault() {
        Long userId = BaseContext.getCurrentId();
        AddressBook addressBook = addressBookService.getDefault(userId);
        if (addressBook != null) {
            return R.success(addressBook);
        }
        return R.error("没有默认地址");
    }
}