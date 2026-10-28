package com.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.reggie.entity.User;

public interface UserService extends IService<User> {
    /**
     * 发送手机验证码
     * @param phone 手机号
     * @return
     */
    void sendMsg(String phone);

    /**
     * 验证码登录
     * @param phone 手机号
     * @param code 验证码
     * @return User 对象
     */
    User loginByCode(String phone, String code);
}