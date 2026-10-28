package com.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.common.AuditLogger;
import com.reggie.common.R;
import com.reggie.entity.Employee;
import com.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@RestController
@Slf4j
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     *
     * @param request  登录成功，把employee的信息塞入session，返回request
     * @param employee
     * @return
     * @RequestBody 页面输入employee信息，表示以JSON的数据格式传输
     */
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
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
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);

        String clientIp = request.getRemoteAddr();

        if (emp == null) {
            AuditLogger.login("EMPLOYEE", null, username, clientIp, false, "用户不存在");
            return R.error("不存在用户！");
        }

        if (!emp.getPassword().equals(password)) {
            AuditLogger.login("EMPLOYEE", emp.getId(), username, clientIp, false, "密码错误");
            return R.error("密码错误，登陆失败！");
        }

        if (emp.getStatus() == 0) {
            AuditLogger.login("EMPLOYEE", emp.getId(), username, clientIp, false, "账号已禁用");
            return R.error("账号已禁用！");
        }

        request.getSession().setAttribute("employee", emp.getId());
        AuditLogger.login("EMPLOYEE", emp.getId(), username, clientIp, true, null);
        return R.success(emp);
    }

    /**
     * 员工退出
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        Long empId = (Long) request.getSession().getAttribute("employee");
        String username = null;
        if (empId != null) {
            Employee emp = employeeService.getById(empId);
            if (emp != null) {
                username = emp.getUsername();
            }
        }
        AuditLogger.logout("EMPLOYEE", empId, username);
        request.getSession().removeAttribute("employee");
        return R.success("退出成功！");
    }


    /**
     * 新增员工
     *
     * @param employee
     * @return
     */
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增用户，用户信息为:{}", employee.toString());

        // 校验用户名长度
        if (employee.getUsername() != null) {
            if (employee.getUsername().length() < 6) {
                return R.error("用户名长度不能小于6,请重新输入");
            }
        }

        //设置员工初始密码，并且md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //从session中获取当前用户id
        Long empId = (Long) request.getSession().getAttribute("employee");

        employee.setCreateUser(empId);
        employee.setUpdateUser(empId);

        employeeService.save(employee);

        AuditLogger.create("EMPLOYEE", empId, "EMPLOYEE", employee.getId(), employee.getName());
        return R.success("新增员工成功。");
    }

    /**
     * 用户分页查询
     *
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page = {},pageSize = {},name = {}", page, pageSize, name);
        //构造分页构造器
        Page pageInfo = new Page(page, pageSize);
        //构造分页构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        //排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);
    }

    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {
        log.info(employee.toString());

        long id = Thread.currentThread().getId();
        log.info("线程id为：{}",id);

        Long empId = (Long) request.getSession().getAttribute("employee");
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(empId);

        String name = employee.getName();

        // 记录更新前的状态用于审计
        Employee oldEmployee = employeeService.getById(employee.getId());
        String changes = "";
        if (oldEmployee != null) {
            StringBuilder sb = new StringBuilder();
            if (!oldEmployee.getName().equals(employee.getName())) {
                sb.append("name:").append(oldEmployee.getName()).append("->").append(employee.getName()).append("; ");
            }
            if (oldEmployee.getStatus() != employee.getStatus()) {
                sb.append("status:").append(oldEmployee.getStatus()).append("->").append(employee.getStatus()).append("; ");
            }
            changes = sb.toString();
        }

        employeeService.updateById(employee);

        if (!changes.isEmpty()) {
            AuditLogger.update("EMPLOYEE", empId, "EMPLOYEE", employee.getId(), changes);
        }

        if(name != null && name.equals("admin")){
            return R.error("管理员不能禁用！");
        }
        return R.success("修改用户信息成功！");
    }

    /**
     * 通过地址栏传id --> GetMapping("/{id}") , @PathVariable
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        log.info("根据id查询员工---->");
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("没有查询到对应员工信息。");

    }


}