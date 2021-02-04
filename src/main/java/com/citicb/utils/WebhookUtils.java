package com.citicb.utils;

import com.alibaba.fastjson.JSONObject;
import com.citicb.common.Property;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import com.citicb.utils.WorkdayUtils;
import java.io.IOException;
import java.util.*;

/**
 * @author qianzhengyuan
 * @version V1.0
 * @title: WebhookUtils.java
 * @description:
 * @date 2020-08-19 22:21:08
 **/
public class WebhookUtils {

    private final static Logger log = Logger.getLogger(WebhookUtils.class);

    private static char count = 0;     //企业微信机器人消息发送频率限制每个机器人发送的消息不能超过20条/分钟。

    public static JSONObject doGet(String url) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(url);
        JSONObject response = null;
        try {
            HttpResponse res = client.execute(get);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = res.getEntity();
                String result = EntityUtils.toString(entity);
                response = JSONObject.parseObject(result);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    public static JSONObject doPost(String url, JSONObject jsonObject) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);
        JSONObject response = null;

        try {
            StringEntity s = new StringEntity(jsonObject.toString(), "UTF-8");
            s.setContentEncoding("UTF-8");
            s.setContentType("application/json");
            post.setEntity(s);
            HttpResponse res = client.execute(post);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = res.getEntity();
                String result = EntityUtils.toString(entity);
                response = JSONObject.parseObject(result);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    @SuppressWarnings("unused")
    public static String sendText(String content) {
        return sendText(content, null, null);
    }

    @SuppressWarnings("unused")
    public static String sendText(String content, Set<String> mentioned_mobile_list) {
        if (mentioned_mobile_list == null || mentioned_mobile_list.size() == 0) return sendText(content, null, null);
        return sendText(content, null, mentioned_mobile_list);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static String sendText(String content, String[] names) {
        if (names == null || names.length == 0) return sendText(content, null, null);
        Set<String> mentionedMobileList = new LinkedHashSet<>();
        for (String name : names) {
            if (Property.CONTACT.getValue(name) != null)
                mentionedMobileList.add(Property.CONTACT.getValue(name));
        }
        return sendText(content, null, mentionedMobileList);
    }

    public static String sendText(String content, List<String> mentioned_list, Set<String> mentioned_mobile_list) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("msgtype", "text");
        Map<String, Object> text = new LinkedHashMap<>();
        text.put("content", content);
        if (mentioned_list != null) text.put("mentioned_list", mentioned_list);
        if (mentioned_mobile_list != null) text.put("mentioned_mobile_list", mentioned_mobile_list);
        msg.put("text", text);
        return sendToWeChat(new JSONObject(msg));
    }

    public static void sendTextAtAll(String content) {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("msgtype", "text");
        Map<String, Object> text = new LinkedHashMap<>(); 
        text.put("content", content);
        Set<String> mentioned_mobile_list = new LinkedHashSet<>();
        mentioned_mobile_list.add("@all");
        text.put("mentioned_mobile_list", mentioned_mobile_list);
        msg.put("text", text);
        sendToWeChat(new JSONObject(msg));
    }

    public static void sendMarkdown(String content) {
        log.debug(content);
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("msgtype", "markdown");
        Map<String, Object> markdown = new LinkedHashMap<>();
        markdown.put("content", content);
        msg.put("markdown", markdown);
        sendToWeChat(new JSONObject(msg));
    }

    synchronized public static String sendToWeChat(JSONObject jsonObject) {
        log.info("\nSend to WeChat count#" + (count++ % 20) + "\n" + jsonObject);
        if (!Boolean.parseBoolean(Property.SYS.getValue("pushEnable"))) {
            log.info("Push disabled...");
            return "";
        }
        //节假日非测试模式推送
        if (!"test".equals(Property.SYS.getValue("mode")) && !WorkdayUtils.todayIsWorkday()) {
            log.info("Not workday,Push disabled...");
            return "";
        }
        String res = WebhookUtils.doPost(Property.SYS.getValue("webhookURL"), jsonObject).toJSONString();
        if (count == 20) {
            log.info("Request frequency exceeds limit, wait for 1 minute.");
            for (int i = 60; i > 1; i--) {
                log.info(i + " seconds remaining...");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.info(1 + " second remaining...");
            count = 0;
        } else try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("Response message:" + res);
        return res;
    }
}


