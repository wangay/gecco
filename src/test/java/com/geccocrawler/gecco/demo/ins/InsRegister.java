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
            String loginInSelector="p:contains('Have an account')";
            session
                    .navigate("https://www.instagram.com")
                    .waitDocumentReady()
                    .wait(3000)//必须加? 否则可能失败. 话说waitDocumentReady是只等待第一次请求的文档完毕? 后面的是js加载出来的
                    .installSizzle()
                    .enableNetworkLog()
                    .click(loginInSelector)
                    .click("input[name='username']")
                    .sendKeys("maozedongdong4069")
                    .click("input[name='password']")
                    .sendKeys("alexisgood")
                    .click("button:contains('Log')")
                    //.sendEnter()
                    .wait(10000);
//            String content = session.getContent();
//            String content = session.getText("body");
//            System.out.println(content);

            //处理业务逻辑 最后才会关闭session

            //
            session.navigate("https://www.instagram.com/maozedongdong4069/followers/")
                    .waitDocumentReady()
                    .wait(1000);
            String content = session.getContent();
            System.out.println(content);
        }
    }

    public static void main(String[] args) {
        InsRegister register = InsRegister.getInstance();
        register.go();
    }
}
