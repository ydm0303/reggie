package com.reggie.task;

import lombok.extern.apachecommons.CommonsLog;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.util.Date;

@Component
@CommonsLog
public class RiskAdminScheduler {

    private static final String SIXTY_MIN = "PT30S"; //如果任务执行时间超过了这个时间，ShedLock 将自动释放锁定
    private static final String THREE_MIN = "PT20S"; //设置为 PT3M 表示任务将至少锁定 3 分钟，即在这个时间范围内，其他节点不会执行相同的任务。\
    private static Integer count = 1;

    @Scheduled(cron = "0/5 * * * * ? ")
    @SchedulerLock(name = "test1",lockAtLeastForString = THREE_MIN,lockAtMostForString = SIXTY_MIN)
    public void test1() {
        log.info(Thread.currentThread().getName() + "->>>任务1执行第：" + (count++) + "次");
    }

    @Scheduled(cron = "0/5 * * * * ? ")
    @SchedulerLock(name = "test2",lockAtLeastForString = THREE_MIN,lockAtMostForString = SIXTY_MIN)
    public void test2() {
        log.info(Thread.currentThread().getName() + "->>>任务2执行第：" + (count++) + "次");
    }
}