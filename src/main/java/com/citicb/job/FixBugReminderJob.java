package com.citicb.job;

import com.citicb.common.ExcelReader;
import com.citicb.common.Property;
import com.citicb.utils.WebhookUtils;
import com.citicb.utils.WorkdayUtils;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * @title: FixBugReminderJob.java
 * @description:
 * @author qianzhengyuan
 * @date 2020-09-02 19:30:25
 * @version V1.0
**/
public class FixBugReminderJob implements Job {

    private final static Logger log = Logger.getLogger(FixBugReminderJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {

        String text ="请大家及时处理缺陷";
        WebhookUtils.sendTextAtAll(text);

    }

    public static void main(String[] args) {
        new FixBugReminderJob().execute(null);
    }
}
