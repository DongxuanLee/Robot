package com.citicb.common;

import java.io.IOException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author qianzhengyuan
 * @version V1.0
 * @title: ExcelReader.java
 * @description:
 * @date 2020-08-19 21:53:33
 **/
public class ExcelReader {

    public static ExcelReader excelReader;
    private Workbook workbook;
    private String lastFileSHA1;
    private final static Logger log = Logger.getLogger(ExcelReader.class);

    private ExcelReader() {

    }

    synchronized public static ExcelReader getInstance() {
        if (excelReader == null) excelReader = new ExcelReader();
        return excelReader;
    }

    public Workbook getWorkbook() {
        return workbook;
    }


    public LocalDate getLocalDate(int sheet, int row, int column) {
        Date date = workbook.getSheetAt(sheet).getRow(row).getCell(column).getDateCellValue();
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        return instant.atZone(zoneId).toLocalDate();
    }

    public String getString(int sheet, int row, int column) {
        if (workbook.getSheetAt(sheet).getRow(row).getCell(column) != null)
            return workbook.getSheetAt(sheet).getRow(row).getCell(column).getStringCellValue();
        return "";
    }

    public Integer getInt(int sheet, int row, int column) {
        if (workbook.getSheetAt(sheet).getRow(row).getCell(column) != null)
            try {
                return (int) workbook.getSheetAt(sheet).getRow(row).getCell(column).getNumericCellValue();
            } catch (Exception e) {
                return Integer.parseInt(getString(sheet, row, column));
            }
        return -1;
    }

    @SuppressWarnings("unused")
    public void loadFromLocal() throws IOException {
        File file = new File(Property.SYS.getValue("xlsxPath"));
        excelReader.workbook = null;
        try {
            excelReader.workbook = new XSSFWorkbook(new FileInputStream(file));
        } catch (IOException e) {
            excelReader.workbook = new HSSFWorkbook(new FileInputStream(file));
        }
    }

    public void loadOnline() {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet httpGet = new HttpGet(Property.SYS.getValue("KdocURL"));
            httpGet.addHeader("Cookie", Property.SYS.getValue("cookie"));
            httpGet.addHeader("User-Agent", Property.SYS.getValue("user-agent"));
            HttpResponse res = client.execute(httpGet);
            JSONObject response;
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = res.getEntity();
                String result = EntityUtils.toString(entity);
                System.out.println(result);
                response = JSONObject.parseObject(result);
            } else {
                log.error("fileinfo get failed.");
                return;
            }
            log.info("fileinfo get success " + response.toJSONString());
            String fileSHA1 = response.getJSONObject("fileinfo").getString("sha1");
            log.info("fileSHA1 " + fileSHA1);
            log.info("lastFileSHA1 " + lastFileSHA1);
            //if (fileSHA1.equals(lastFileSHA1)) return ;
            lastFileSHA1 = fileSHA1;
            httpGet = new HttpGet(response.getJSONObject("fileinfo").getString("url"));
            httpGet.addHeader("Cookie", Property.SYS.getValue("cookie"));
            httpGet.addHeader("User-Agent", Property.SYS.getValue("user-agent"));
            HttpResponse httpResponse = client.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            InputStream is = entity.getContent();
            workbook = WorkbookFactory.create(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeWorkbook() {
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}