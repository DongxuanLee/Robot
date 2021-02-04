package com.citicb.common;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author qianzhengyuan
 * @version V1.0
 * @title: KdocDownloader.java
 * @description:
 * @date 2020-08-19 22:20:41
 **/
public class KdocDownloader {

    private final static Logger log = Logger.getLogger(KdocDownloader.class);


    private static KdocDownloader kdocDownloader;

    private String lastFileSHA1;

    synchronized public static KdocDownloader getInstance() {
        if (kdocDownloader == null) {
            kdocDownloader = new KdocDownloader();
        }
        return kdocDownloader;
    }

    public Workbook getWorkbook() throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(Property.SYS.getValue("KdocURL"));
        httpGet.addHeader("Cookie", Property.SYS.getValue("cookie"));
        httpGet.addHeader("User-Agent", Property.SYS.getValue("user-agent"));
        HttpResponse res = client.execute(httpGet);
        JSONObject response;
        if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            HttpEntity entity = res.getEntity();
            String result = EntityUtils.toString(entity);
            response = JSONObject.parseObject(result);
        } else {
            log.error("fileinfo get failed.");
            return null;
        }
        log.info("fileinfo get success " + response.toJSONString());
        String fileSHA1 = response.getJSONObject("fileinfo").getString("sha1");
        log.info("fileSHA1 " + fileSHA1);
        log.info("lastFileSHA1 " + lastFileSHA1);
        Workbook workbook = null;
        if (fileSHA1.equals(lastFileSHA1)) return workbook;
        lastFileSHA1 = fileSHA1;
        httpGet = new HttpGet(response.getJSONObject("fileinfo").getString("static_url"));
        httpGet.addHeader("Cookie", Property.SYS.getValue("cookie"));
        httpGet.addHeader("User-Agent", Property.SYS.getValue("user-agent"));
        HttpResponse httpResponse = client.execute(httpGet);
        HttpEntity entity = httpResponse.getEntity();
        InputStream is = entity.getContent();
        workbook = WorkbookFactory.create(is);
        is.close();
        return workbook;
    }

}
