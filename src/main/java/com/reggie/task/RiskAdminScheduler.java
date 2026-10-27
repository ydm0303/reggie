package com.reggie.task;

import com.reggie.controller.CategoryController;
import com.reggie.controller.DishController;
import lombok.extern.apachecommons.CommonsLog;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.util.Date;

@Component
@CommonsLog
public class RiskAdminScheduler {

    @Autowired
    private DishController dishController;
    @Autowired
    private CategoryController categoryController;

    private static final String SIXTY_MIN = "PT30S"; //如果任务执行时间超过了这个时间，ShedLock 将自动释放锁定
    private static final String THREE_MIN = "PT20S"; //设置为 PT3M 表示任务将至少锁定 3 分钟，即在这个时间范围内，其他节点不会执行相同的任务。\
    private static Integer count = 1;

    @Scheduled(cron = "0 0/30 * * * ?")
    @SchedulerLock(name = "test1",lockAtLeastForString = THREE_MIN,lockAtMostForString = SIXTY_MIN)
    public void test1() {
        dishController.page(1,10,"杨东铭");
        log.info(Thread.currentThread().getName() + "->>>应用数据库保活任务1：" + (count++) + "次");

    }

    @Scheduled(cron = "0/5 * * * * ? ")
    @SchedulerLock(name = "test2",lockAtLeastForString = THREE_MIN,lockAtMostForString = SIXTY_MIN)
    public void test2() {
        log.info(Thread.currentThread().getName() + "->>>应用数据库保活任务2：" + (count++) + "次");
        dishController.page(1,10,"杨东铭");
        categoryController.page(1,10);
    }
}