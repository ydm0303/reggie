package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.reggie.common.R;
import com.reggie.entity.Employee;
import com.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    /**
     * 员工登录
     * @RequestBody 页面输入employee信息，表示以JSON的数据格式传输
     *
     * @param request 登录成功，把employee的信息塞入session，返回request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        /**
         * 1.页面提交的密码，md5加密
         * 2.根据页面提交的username查询数据库
         * 3.如果没有查询到数据，返回失败
         * 4.密码对比，不一致返回失败
         * 5.查询员工状态，禁用，返回禁用
         * 6.登陆成功，将员工id存入session并返回登录结果
         */

        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        String username = employee.getUsername();
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        if(emp == null){
            return R.error("不存在用户！");
        }

        if(!emp.getPassword().equals(password)){
            return R.error("密码错误，登陆失败！");
        }

        if(emp.getStatus()==0){
            return R.error("账号已禁用！");
        }

        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出成功！");
    }

}
