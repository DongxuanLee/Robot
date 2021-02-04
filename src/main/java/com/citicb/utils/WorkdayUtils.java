package com.citicb.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author qianzhengyuan
 * @version V1.0
 * @title: WorkdayUtils.java
 * @description:
 * @date 2020-08-19 22:21:13
 **/
public class WorkdayUtils {
    private final static Logger log = Logger.getLogger(WorkdayUtils.class);

    public static boolean todayIsWorkday() {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        JSONObject resp = WebhookUtils.doGet("http://timor.tech/api/holiday/info/$" + today);
        return resp.getJSONObject("type").getInteger("type") == 0;
    }

    public static void main(String[] args) {
        log.info(WorkdayUtils.todayIsWorkday());
    }
}
