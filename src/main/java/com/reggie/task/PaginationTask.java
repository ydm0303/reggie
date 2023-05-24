package com.reggie.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.reggie.entity.Employee;
import com.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.mybatis.logging.Logger;
import org.mybatis.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时任务
 *
 *
 * 错误信息中指出，@Scheduled 注解只能用于无参数的方法。然而，你的 page 方法带有参数，因此导致了异常。
 * 要解决这个问题，你可以考虑以下几种方式：
 * 1.移除 @Scheduled 注解：如果你的 page 方法不是一个定时任务方法，
 * 而只是一个普通的分页查询方法，那么你可以简单地移除 @Scheduled 注解即可。
 * 2.创建一个新的无参数方法用于定时任务：如果你需要将 page 方法作为定时任务执行，
 * 你可以创建一个新的无参数方法，并将 @Scheduled 注解添加到该方法上。在该方法内部，调用 page 方法执行分页查询逻辑。
 * 3.调整方法参数逻辑：如果你希望直接将 page 方法作为定时任务执行，并且需要传递参数，
 * 你可以考虑从其他地方获取参数值，而不是直接通过方法参数传递。例如，可以从配置文件中读取参数值，或者通过其他方式获取需要的参数值。
 */
@Component
@Slf4j
public class PaginationTask {

    @Autowired
    private EmployeeService employeeService;

    @Scheduled(initialDelay = 1000 * 60, fixedRate = 60 * 1000 * 2)
    public void executePaginationTask() {
        log.info("定时任务开始执行");

        // 构造分页构造器
        int page = 1;
        int pageSize = 10;
        String name = "example";
        Page<Employee> pageInfo = new Page<>(page, pageSize);

        // 构造分页查询条件
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        // 执行查询
        employeeService.page(pageInfo, queryWrapper);

        // 输出查询结果
        log.info("查询结果：{}", pageInfo.getRecords());

        log.info("定时任务执行完成");
    }
}
