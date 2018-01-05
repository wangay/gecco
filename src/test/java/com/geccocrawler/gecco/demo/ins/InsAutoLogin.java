package com.geccocrawler.gecco.demo.ins;

import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;

/**
 * 自动登录
 * Created by nobody on 2018/1/3.
 */
public class InsAutoLogin {
    private static InsAutoLogin ourInstance = new InsAutoLogin();

    public static InsAutoLogin getInstance() {
        return ourInstance;
    }

    private InsAutoLogin() {
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
                    .wait(5000)//必须加? 否
                    // 则可能失败. 话说waitDocumentReady是只等待第一次请求的文档完毕? 后面的是js加载出来的
                    // 考虑用waitUtil那个方法. alexTODO
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
//            session.navigate("https://www.instagram.com/maozedongdong4069/followers/")
//                    .waitDocumentReady()
//                    .wait(1000);
            String content = session.getContent();
            System.out.println(content);
        }
    }

    public static void main(String[] args) {
        InsAutoLogin register = InsAutoLogin.getInstance();
        register.go();
    }
}
