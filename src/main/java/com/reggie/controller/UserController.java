package com.reggie.controller;

import com.reggie.common.R;
import com.reggie.entity.User;
import com.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送手机验证码
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user) {
        String phone = user.getPhone();
        if (phone == null || phone.trim().isEmpty()) {
            return R.error("手机号不能为空");
        }
        userService.sendMsg(phone);
        return R.success("验证码发送成功");
    }

    /**
     * 验证码登录
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody User user, HttpServletRequest request) {
        String phone = user.getPhone();
        String code = user.getCode();

        if (phone == null || code == null) {
            return R.error("手机号或验证码不能为空");
        }

        User loginUser = userService.loginByCode(phone, code);

        // 登录成功，存入session
        HttpSession session = request.getSession();
        session.setAttribute("user", loginUser.getId());

        return R.success(loginUser);
    }

    /**
     * 退出登录
     */
    @PostMapping("/loginout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("user");
        return R.success("退出成功");
    }
}