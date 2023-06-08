//package com.reggie.task;
//
//import lombok.extern.slf4j.Slf4j;
//import org.quartz.*;
//import org.springframework.scheduling.quartz.QuartzJobBean;
//import org.springframework.stereotype.Component;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//@Slf4j
//@Component
//public class TestJob extends QuartzJobBean {
//
//
//    @Override
//    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
//
//        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
//        String arms = jobDataMap.getString("arms");
//
//        JobDetail jobDetail = jobExecutionContext.getJobDetail();
//        Class<? extends Job> jobClass = jobDetail.getJobClass();
//
//        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//        log.info("TestJob,定时时间：" + dateTime);
//        log.info("arms参数：" + arms );
//    }
//}
