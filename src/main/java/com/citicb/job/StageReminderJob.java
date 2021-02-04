package com.citicb.job;

import com.citicb.common.ExcelReader;
import com.citicb.utils.WebhookUtils;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

/**
 * @author qianzhengyuan
 * @version V1.0
 * @title: StageReminderJob.java
 * @description:
 * @date 2020-08-19 22:20:28
 **/
public class StageReminderJob implements Job {

    private final static Logger log = Logger.getLogger(StageReminderJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        //读取excel
        ExcelReader excelReader = ExcelReader.getInstance();
        excelReader.loadOnline();
//        try {
//			excelReader.loadFromLocal();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
        if (excelReader.getWorkbook() == null) {
            log.error("Can't get workbook.");
            return;
        }

        //导入文本信息
        HashMap<String,HashMap<String,String >> contextMap = loadContextMap(excelReader);

        StringBuilder markDown = new StringBuilder();
        markDown.append("\uD83D\uDCA1\n## 阶段提醒小助手\n### 以下排期即将开始下一阶段\n");
        int count1 = 0;
        for (int i = 1; i < excelReader.getWorkbook().getSheetAt(0).getLastRowNum(); i++) { //i：行
            //暂停或者归档排期不做提醒
            String archive = excelReader.getString(0, i, 11);
            if (!"".equals(archive)) continue;

            String type = excelReader.getString(0, i, 16);
            for (int j = 9; j > 0; j--) {   //j：列
                LocalDate date;
                try {
                    date = excelReader.getLocalDate(0, i, j);
                } catch (Exception e) {
                    continue;
                }
                if(date !=null){
	                long dateBetween = ChronoUnit.DAYS.between(LocalDate.now(), date);
	                if (dateBetween < 0 && dateBetween >= -3) { //提前3天开始提醒即将开始下一阶段
	                    String[] names;
	                    if (j == 1) {   //即将进入启动状态：艾特需求分析师
	                        names = excelReader.getString(0, i, 13).trim().split("、");
	                    } else {        //其他状态：艾特牵头人
	                        names = excelReader.getString(0, i, 12).trim().split("、");
	                    }
	
	                    markDown.append(">").append(++count1).append(".")
	                            .append(excelReader.getString(0, i, 0))
	                            .append("\n")
	                            .append(excelReader.getLocalDate(0, i, j+1).format(DateTimeFormatter.ISO_LOCAL_DATE))
	                            .append("<font color=\"info\">")
	                                .append(contextMap.get(type).get(excelReader.getString(0, 0, j + 1)))
	                            .append("</font>");
//	                    if (!"".equals(names[0])){
//	                        for (String name : names)
//	                            markDown.append(" @").append(name);
//	                    }
	
	                    //排期/辅办系统负责人
	                    if (!"".equals(excelReader.getString(0, i, 15)))
	                        markDown.append("<font color=\"comment\"> @")
	                                .append(excelReader.getString(0, i, 15))
	                                .append("</font>");
	                    markDown.append("\n");
	                    break;
	                }
                }
             }
        }
        markDown.append("\n### 以下排期所处阶段即将到期\n");
        int count2 = 0;
        for (int i = 1; i < excelReader.getWorkbook().getSheetAt(0).getLastRowNum(); i++) {
            String type = excelReader.getString(0, i, 16);

            for (int j = 2; j < 10; j++) {
                LocalDate date;
                try {
                    date = excelReader.getLocalDate(0, i, j);
                } catch (Exception e) {
                    continue;
                }
                if(date!=null) {
	                long dateBetween = ChronoUnit.DAYS.between(LocalDate.now(), date);
	                if (dateBetween >= 0 && dateBetween <= 2) {
	                    String[] names;
	                    if (j == 2) {
	                        names = excelReader.getString(0, i, 13).split("、");
	                    } else {
	                        names = excelReader.getString(0, i, 12).split("、");
	                    }
	                    markDown.append(">").append(++count2).append(".")
	                            .append(excelReader.getString(0, i, 0))
	                            .append("\n")
	                            .append(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
	                            .append("<font color=\"info\">")
	                                .append(contextMap.get(type).get(excelReader.getString(0, 0, j)))
	                            .append("</font> ");
	                    if (!"".equals(names[0]))
	                        for (String name : names)
	                            markDown.append(" @").append(name);
	                    if (!"".equals(excelReader.getString(0, i, 15)))
	                        markDown.append("<font color=\"comment\"> @")
	                                .append(excelReader.getString(0, i, 15))
	                                .append("</font>");
	                    markDown.append("\n");
	                    break;
	                }
                }
            }
        }
        WebhookUtils.sendMarkdown(markDown.toString());
//        markDown = new StringBuilder();
//        markDown.append("### 以下项目阶段延后 请相关同事关注\n");
//        int count3 = 0;
//        for (int i = 3; i < excelReader.getWorkbook().getSheetAt(1).getRow(0).getLastCellNum(); i++) {
//            String str, str2;
//            try {
//                str = excelReader.getString(1, 2, i);
//                str2 = excelReader.getString(1, 4, i);
//            } catch (Exception e) {
//                continue;
//            }
//            if ("延后".equals(str) && !"暂停/归档".equals(str2)) {
//                String[] names;
//                Integer row = excelReader.getInt(1, 1, i);
//                if ("需求分析".equals(excelReader.getString(1, 3, i))) {
//                    names = excelReader.getString(0, row - 1, 13).split("、");
//                } else {
//                    names = excelReader.getString(0, row - 1, 12).split("、");
//                }
//                markDown.append(">").append(++count3).append(".").append(excelReader.getString(1, 0, i)).append("\n").append("<font color=\"warning\">").append(excelReader.getString(1, 4, i)).append("</font>  -> ").append("<font color=\"info\">").append(excelReader.getString(1, 3, i)).append("</font>");
//                if (!"".equals(names[0]))
//                    for (String name : names)
//                        markDown.append(" @").append(name);
//                if (!"".equals(excelReader.getString(0, i, 15)))
//                    markDown.append("<font color=\"comment\"> @").append(excelReader.getString(0, i, 15)).append("</font>");
//                markDown.append("\n");
//            }
//        }
//        if (count3 > 0)
//            WebhookUtils.sendMarkdown(markDown.toString());
        String text = "以上为 " + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) +
                " 项目提醒事项\n阶段即将开始 " +
                count1 +
                " 条\n阶段即将到期 " +
                count2 +
                " 条\n" +
//                "阶段延迟 " +
//                count3 +
//                " 条\n"+
                "请相关同事重点关注";

        WebhookUtils.sendTextAtAll(text);

        WebhookUtils.sendMarkdown(getExtraInfo(excelReader));

        excelReader.closeWorkbook();
    }

    private HashMap<String, HashMap<String,String >> loadContextMap(ExcelReader excelReader) {
        HashMap<String,HashMap<String,String > > contextMap = new HashMap<>();

        //新建产品
        HashMap<String,String> newProductMap = new HashMap<>();
        for (int i = 1; i <= 10; i++) {
            newProductMap.put(excelReader.getString(1, i, 0),excelReader.getString(1, i, 1));
        }
        contextMap.put(excelReader.getString(1, 0, 0),newProductMap);

        //重构/重要产品升级
        HashMap<String,String> importantProductMap = new HashMap<>();
        for (int i = 20; i <= 29; i++) {
            importantProductMap.put(excelReader.getString(1, i, 0),excelReader.getString(1, i, 1));
        }
        contextMap.put(excelReader.getString(1, 19, 0),importantProductMap);

        //常规优化
        HashMap<String,String> normalChangeMap = new HashMap<>();
        for (int i = 40; i <= 49; i++) {
            normalChangeMap.put(excelReader.getString(1, i, 0),excelReader.getString(1, i, 1));
        }
        contextMap.put(excelReader.getString(1, 39, 0),normalChangeMap);

        //紧急上线
        HashMap<String,String> urgentMap = new HashMap<>();
        for (int i = 60; i <= 69; i++) {
            urgentMap.put(excelReader.getString(1, i, 0),excelReader.getString(1, i, 1));
        }
        contextMap.put(excelReader.getString(1, 59, 0),urgentMap);

        return contextMap;
    }

    private String getExtraInfo(ExcelReader excelReader) {
        String extraInfoStr = "";

        for (int i = 1; i < excelReader.getWorkbook().getSheetAt(2).getLastRowNum(); i++) {
            LocalDate date;
            try {
                date = excelReader.getLocalDate(2, i, 0);
            } catch (Exception e) {
                continue;
            }

            if(ChronoUnit.DAYS.between(LocalDate.now(), date) == 0){
                extraInfoStr += excelReader.getString(2, i, 1) + "\n";
            }
        }

        if(extraInfoStr != ""){
            return "\n### 今日提醒\n" + extraInfoStr;
        }

        return extraInfoStr;
    }
    public static void main(String[] args) {
        new StageReminderJob().execute(null);
    }
}
