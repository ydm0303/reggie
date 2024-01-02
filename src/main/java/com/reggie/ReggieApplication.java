package com.reggie;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@EnableScheduling
@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement  //开启事务注解
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")//定义了在执行定时任务时获取分布式锁的最长时间,
// 指定的时间内没有成功获取到锁，则认为获取锁失败，其他实例可能会尝试获取锁并执行任务。30s
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class, args);
        log.info("项目启动成功..!");
        log.info("路径：/backend/page/login/login.html");
    }
}
