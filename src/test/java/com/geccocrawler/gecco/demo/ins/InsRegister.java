package com.geccocrawler.gecco.demo.ins;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;

import static java.util.Locale.ENGLISH;

/**
 * 自动多个注册ins账号
 * Created by nobody on 2018/1/3.
 */
public class InsRegister {
    private static InsRegister ourInstance = new InsRegister();

    public static InsRegister getInstance() {
        return ourInstance;
    }

    private InsRegister() {
    }

    public  void go() {
        Launcher launcher = new Launcher();

        try (SessionFactory factory = launcher.launch();
             Session session = factory.create()) {
//            String userNameInput="#react-root  form> input";
//            String userNameInput="input[aria-label*=Email]";
            String userNameInput="[aria-label]";
            session
                    .navigate("https://www.instagram.com/")
                    .waitDocumentReady()
                    .installSizzle()
                    .enableNetworkLog()
                    //.click(userNameInput)
                    //.sendKeys("lucky")
                    //.sendEnter()
                    .wait(1000);
            String content = session.getContent();
//            String content = session.getText("body");
            System.out.println(content);

        }
    }

    public static void main(String[] args) {
        InsRegister register = InsRegister.getInstance();
        register.go();
    }
}
