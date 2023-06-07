package com.reggie.task;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class TaskConfig {

    @Bean
    public JobDetailFactoryBean jobDetail(){
        JobDetailFactoryBean detailFactoryBean = new JobDetailFactoryBean();
        detailFactoryBean.setJobClass(TestJob.class);
        detailFactoryBean.setName("这个一个任务名123");
        detailFactoryBean.setGroup("TestJob");
        detailFactoryBean.getJobDataMap().put("arms","这是一个参数123");
        return detailFactoryBean;
    }

    @Bean
    public CronTriggerFactoryBean trigger(JobDetail jobDetail){
        CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setCronExpression("0/10 * * * * ?");
        return factoryBean;
    }

    @Bean
    public SchedulerFactoryBean scheduler(Trigger trigger){
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setTriggers(trigger);
        return schedulerFactoryBean;
    }

}
