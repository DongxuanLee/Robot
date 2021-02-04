package com.citicb;

import com.citicb.common.Property;
import com.citicb.job.FixBugReminderJob;
import com.citicb.job.StageReminderJob;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * @author qianzhengyuan
 * @version V1.0
 * @title: Main.java
 * @description:
 * @date 2020-08-19 22:21:19
 **/
public class Main {

    private final static Logger log = Logger.getLogger(Main.class);


    public static void main(String[] args) throws SchedulerException {

        log.info("DocPush run...");
        try {
            log.info("Init Property.SYS #" + Property.SYS.hashCode());
            //加载通讯录 企业微信按手机号@人员 暂时取消
            //log.info("Init Property.CONTACT #"+Property.CONTACT.hashCode());
        } catch (Exception e) {
            log.error(e);
            return;
        }
        // 测试模式 直接发送
        if ("test".equals(Property.SYS.getValue("mode"))) {
            new StageReminderJob().execute(null);
//            new StageReminderJob().execute(null);
            //new FixBugReminderJob().execute(null);
            return;
        }
        // 作业模式 定时发送
        if ("job".equals(Property.SYS.getValue("mode"))) {

            SchedulerFactory schedulerFactory = new StdSchedulerFactory();

            Scheduler scheduler = schedulerFactory.getScheduler();

            scheduler.start();

            scheduleJob(scheduler,"StageReminder",StageReminderJob.class);
//            scheduleJob(scheduler,"FixBugReminder",FixBugReminderJob.class);

        } else
            log.info("no match mode");

    }

    private static void scheduleJob(Scheduler scheduler, String jobNamePrefix, Class<? extends Job> jobClass) throws SchedulerException {
        JobDetail jobDetail = newJob(jobClass).withIdentity(jobNamePrefix + "Job").build();
        Trigger trigger = newTrigger().withIdentity(jobNamePrefix + "Trigger").withSchedule(CronScheduleBuilder.cronSchedule(Property.SYS.getValue(jobNamePrefix + "CronSchedule"))).forJob(jobDetail).build();
        scheduler.scheduleJob(jobDetail, trigger);
    }
}
