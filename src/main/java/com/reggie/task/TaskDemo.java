//package com.reggie.task;
//
//import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.reggie.entity.Employee;
//import com.reggie.service.EmployeeService;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang.StringUtils;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//
//
///**
// * 以下是一个示例代码，演示如何创建一个新的无参数方法用于定时任务，并在该方法内部调用 page 方法执行分页查询逻辑：
// */
//@Slf4j
//@Component
//@SpringBootTest
//public class TaskDemo {
//    @Autowired
//    private EmployeeService employeeService;
//
////    @Scheduled(initialDelay = 1000 * 60, fixedRate = 60 * 1000 * 2)
//    public void executePaginationTask() {
//        log.info("员工分页查询定时任务开始执行");
//
//        // 调用page方法，传递所需参数
//        int page = 1;
//        int pageSize = 10;
//        String name = "admin";
//        page(page, pageSize, name);
//
//        log.info("定时任务执行完成");
//    }
//
//    // 新的无参数方法，用于定时任务
//    public void page() {
//        // 调用原来的page方法，传递参数
//        int page = 1;
//        int pageSize = 10;
//        String name = "admin";
//        page(page, pageSize, name);
//    }
//
//    // 原来的page方法，带有参数
//    public void page(int page, int pageSize, String name) {
//        // 构造分页构造器
//        Page<Employee> pageInfo = new Page<>(page, pageSize);
//
//        // 构造分页查询条件
//        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
//        queryWrapper.orderByDesc(Employee::getUpdateTime);
//
//        // 执行查询
//        employeeService.page(pageInfo, queryWrapper);
//
//        // 输出查询结果
//        log.info("查询结果：{}", pageInfo.getRecords());
//    }
//
//    @Test
//    public void test(){
//        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
//        Date date = new Date();
//        String format1 = format.format(date);
//        System.out.println(format1);
//    }
//}
