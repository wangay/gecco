package com.geccocrawler.gecco.utils;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;

/**
 * Created by nobody on 2018/2/9.
 */
public class Cdp4jUtil {

    private static Cdp4jUtil ourInstance = new Cdp4jUtil();

    public static Cdp4jUtil getInstance() {
        return ourInstance;
    }

    private Cdp4jUtil() {
    }

    private String content;

    private  boolean go(String url,int waitTime) {
        boolean result = false;
        try {
            Launcher launcher = new Launcher();
            try (SessionFactory factory = launcher.launch();
                 Session session = factory.create()) {
                session.navigate(url);
                session.waitDocumentReady(waitTime);
                content = session.getContent();
                if(content!=null  && (content.indexOf("html")>-1|| content.indexOf("meta")>-1)){
                    return true;//说明返回数据了
                }
            }
        } catch (Exception e) {
            //出错了,就返回false
            result=false;
        }
        return result;
    }

    public  synchronized String getHtml(String url,int maxTryTimes,int waitTime) {
        int tryTimes =0;//尝试次数
        while (true){
            boolean result = go(url,waitTime);
            if(result || tryTimes >=maxTryTimes){
                //结果正确,或者超过了允许的尝试次数
                break;
            }else{
                tryTimes++;
            }
        }
        return this.content;
    }

    /***
     * 整个网页
     */
    public static String getAll(String url) {
        Launcher launcher = new Launcher();
        try (SessionFactory factory = launcher.launch();
             Session session = factory.create()) {
            session.navigate(url);
            session.waitDocumentReady();
            String content = session.getContent();
            return content;
        }
    }

    /***
     * 整个网页
     */
    public static String getAllJson(String url) {
        Launcher launcher = new Launcher();
        try (SessionFactory factory = launcher.launch();
             Session session = factory.create()) {
            session.navigate(url);
            session.waitDocumentReady();
//            String content = session.getContent();
            String content = session.getText("body>pre");
            return content;
        }
    }

    public static void main(String[] args) {
        getAllJson("");
    }
}
