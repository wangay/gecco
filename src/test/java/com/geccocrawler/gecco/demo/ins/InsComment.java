package com.geccocrawler.gecco.demo.ins;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;

/**
 * 发布评论
 * Created by nobody on 2018/1/3.
 */
public class InsComment {
    private static InsComment ourInstance = new InsComment();

    public static InsComment getInstance() {
        return ourInstance;
    }

    private InsComment() {
    }

    public  void go() {
        Launcher launcher = new Launcher();

        try (SessionFactory factory = launcher.launch();
             Session session = factory.create()) {
            String userNameInput="div";
            session
                    .navigate("https://www.instagram.com/p/BcjRrJTAbls/?taken-by=weeddogghome")
                    .wait(1000)
                    .waitDocumentReady()
//                    .installSizzle()
//                    .enableNetworkLog()
                    .click(userNameInput)
//                    .sendKeys("蘑+菇-之*夏@"+"weeddogghome")
//                    .sendEnter()
//                    .wait(1000);
            ;
            String content = session.getContent();
//            String content = session.getText("body");
            System.out.println(content);

        }
    }

    public static void main(String[] args) {
        InsComment register = InsComment.getInstance();
        register.go();
    }
}
