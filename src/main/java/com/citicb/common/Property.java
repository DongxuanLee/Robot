package com.citicb.common;

import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author qianzhengyuan
 * @version V1.0
 * @title: Property.java
 * @description:
 * @date 2020-08-19 22:20:52
 **/
public enum Property {

    SYS("config.properties"), CONTACT("contact.properties");

    private final static Logger log = Logger.getLogger(Property.class);

    private final Properties prop = new Properties();

    Property(String name) {
        String path = Property.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (System.getProperty("os.name").contains("dows")) {
            path = path.substring(1);
        }
        if (path.contains(".jar")) {
            path = path.substring(0, path.lastIndexOf("/") + 1);
        } else {
            path = path.replace("target/classes/", "");
        }
        try {
            prop.load(new FileInputStream(path + name));
            Logger.getLogger(Property.class).info(" Property " + name + " load success! os.name=" + System.getProperty("os.name") + ",path=" + path + "\n" + prop.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        prop.setProperty("path", path);
    }

    public String getValue(String key) {
        if (key == null || key.trim().equals("")) return null;
        if (prop.getProperty(key) == null) {
            log.error("Can't get property value:" + this.name() + " " + key);
        }
        return prop.getProperty(key);
    }

    public void setValue(String key, String value) {
        prop.setProperty(key, value);
    }
}
