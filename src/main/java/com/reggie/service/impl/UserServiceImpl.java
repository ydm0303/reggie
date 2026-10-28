package com.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.reggie.common.CustomException;
import com.reggie.entity.User;
import com.reggie.mapper.UserMapper;
import com.reggie.service.UserService;
import com.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 简单的内存存储验证码（生产环境建议使用Redis）
    private static final Map<String, CodeEntry> codeCache = new ConcurrentHashMap<>();

    private static class CodeEntry {
        String code;
        long expireTime;

        CodeEntry(String code, long expireTime) {
            this.code = code;
            this.expireTime = expireTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    @Override
    public void sendMsg(String phone) {
        // 生成4位验证码
        String code = ValidateCodeUtils.generateValidateCode(4).toString();
        log.info("手机号: {}, 验证码: {}", phone, code);

        // 存入内存缓存，有效期5分钟
        codeCache.put(phone, new CodeEntry(code, System.currentTimeMillis() + 5 * 60 * 1000));

        // TODO: 调用阿里云短信服务发送验证码
        // SMSUtils.sendMessage(phone, code);
    }

    @Override
    public User loginByCode(String phone, String code) {
        // 从内存缓存获取验证码
        CodeEntry entry = codeCache.get(phone);

        if (entry == null || entry.isExpired() || !entry.code.equals(code)) {
            throw new CustomException("验证码错误或已过期");
        }

        // 查询用户是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        User user = this.getOne(queryWrapper);

        // 如果不存在，自动注册
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setStatus(1);
            this.save(user);
        }

        // 校验用户状态
        if (user.getStatus() == 0) {
            throw new CustomException("账号已禁用");
        }

        // 删除已使用的验证码
        codeCache.remove(phone);

        return user;
    }
}