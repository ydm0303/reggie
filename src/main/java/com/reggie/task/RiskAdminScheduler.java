package com.reggie.task;

import com.reggie.service.DishService;
import com.reggie.service.CategoryService;
import lombok.extern.apachecommons.CommonsLog;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 定时任务：数据库连接池保活
 * 注意：此任务无法防止 Render 休眠（Render 仅识别外部入站 HTTP 请求）
 * Render 保活请使用外部监控服务（如 GitHub Actions、UptimeRobot）定时调用 /common/health
 */
@Component
@CommonsLog
public class RiskAdminScheduler {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    // lockAtMostFor: 任务最大执行时间，超时自动释放锁
    private static final String LOCK_AT_MOST_FOR = "PT20S";
    // lockAtLeastFor: 任务最小持锁时间，防止并发执行
    private static final String LOCK_AT_LEAST_FOR = "PT10S";

    /**
     * 每 10 分钟执行一次：执行简单查询保持数据库连接池活跃
     * 防止长时间无访问导致数据库连接被 MySQL 服务端主动断开 (wait_timeout 默认 8 小时)
     */
    @Scheduled(cron = "0 0/10 * * * ?")
    @SchedulerLock(name = "db-keepalive", lockAtLeastForString = LOCK_AT_LEAST_FOR, lockAtMostForString = LOCK_AT_MOST_FOR)
    public void dbKeepAlive() {
        try {
            // 执行轻量级查询，不返回大量数据
            List<?> dishes = dishService.list();
            List<?> categories = categoryService.list();
            log.info("数据库连接池保活任务执行成功");
        } catch (Exception e) {
            log.error("数据库连接池保活任务执行失败", e);
        }
    }
}