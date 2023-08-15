package com.reggie.task;

import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.util.Date;

@Component
public class RiskAdminScheduler {

    private static final String SIXTY_MIN = "PT60M"; //如果任务执行时间超过了这个时间，ShedLock 将自动释放锁定
    private static final String THREE_MIN = "PT30S"; //设置为 PT3M 表示任务将至少锁定 3 分钟，即在这个时间范围内，其他节点不会执行相同的任务。

    @Scheduled(cron = "0 */1 * * * ? ")
    @SchedulerLock(name = "test",lockAtLeastForString = THREE_MIN,lockAtMostForString = SIXTY_MIN)
    public void test() {
        System.out.println(new Date());
        System.out.println(Thread.currentThread().getName()+"--executed......");
    }
}