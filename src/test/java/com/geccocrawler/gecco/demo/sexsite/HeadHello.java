package com.geccocrawler.gecco.demo.sexsite;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;

/**
 * Created by nobody on 2017/12/18.
 */
public class HeadHello {
    public static void main(String[] args) {
        Launcher launcher = new Launcher();

        try (SessionFactory factory = launcher.launch();
             Session session = factory.create()) {
//            session.navigate("https://webfolder.io?cdp4j");
//            session.navigate("https://baidu.com");
            session.navigate("http://jandan.net/ooxx/page-393");
//            session.navigate("http://jandan.net/ooxx/page-393#comments");
            session.waitDocumentReady();
            String content = session.getContent();
            System.out.println(content);
        }
    }

}
