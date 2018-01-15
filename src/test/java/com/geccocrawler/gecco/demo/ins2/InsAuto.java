package com.geccocrawler.gecco.demo.ins2;

import com.geccocrawler.gecco.demo.ins.InsConsts;
import com.geccocrawler.gecco.local.FileUtil;
import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;

import java.util.Collections;
import java.util.List;

/**
 * 自动登录
 * Created by nobody on 2018/1/3.
 */
public class InsAuto {
    private static InsAuto ourInstance = new InsAuto();

    public static InsAuto getInstance() {
        return ourInstance;
    }

    private SessionFactory factory;
    private Session session;


    private InsAuto() {
        init();
    }

    /***
     * 初始化session
     */
    private void init() {
        Launcher launcher = new Launcher();

        factory = launcher.launch();
        session = factory.create();
        String loginInSelector="p:contains('Have an account')";
        session.navigate(InsConsts.insBaseUrl2)
                .waitDocumentReady().wait(500);

        String content = session.getContent();
        if(content.contains("Have an account")){
            //没登录
            //等待元素出来
            boolean toLogShowed = session.waitUntil(s -> {
                return s.matches("input[name='username']");
            }, 20 * 1000);

            if(toLogShowed){
                //.wait(5000)//必须加? 否
                // 则可能失败. 话说waitDocumentReady是只等待第一次请求的文档完毕? 后面的是js加载出来的
                // 考虑用waitUtil那个方法. alexTODO
                session.installSizzle()
                        .enableNetworkLog()
                        .click(loginInSelector)
                        .wait(500)
                        .focus("input[name='username']")//鼠标焦点
                        .selectInputText("input[name='username']")//全选输入框
                        .sendBackspace()//退格键,清空
                        .sendKeys("maozedongdong4069")
                        .focus("input[name='password']")//鼠标焦点
                        .selectInputText("input[name='password']")//全选输入框
                        .sendBackspace()//退格键,清空
                        .sendKeys("alexisgood")
//                            .click("button:contains('Log')")
                        .sendEnter()
                        .wait(10000);
                session.navigate("https://www.instagram.com")
                        .waitDocumentReady()
                        .wait(1000);
            }
        }else{
            //已经登录
            System.out.println("已经登录");
        }

    }

    public void close(){
        session.close();
        factory.close();
    }

    /***
     * 在一个照片页面,对图片点赞.
     * //<span class="_8scx2 coreSpriteHeartOpen">赞</span>
     //<span class="_8scx2 coreSpriteHeartFull">取消赞</span>
     * 选择器:String zanSelector="span.coreSpriteHeartOpen";
     * 选中的仅是未赞过的. 避免了去掉了原来的赞.
     *
     * span:contains(赞)一直不行,
     * "https://www.instagram.com/p/BdwWQqEhb34/?taken-by=lysergicalpsilicybin"
     *
     */
    public  void dianzan(String picUrl) {
        //进入某人的一张照片页面.
        session.navigate(picUrl)
                .waitDocumentReady()
                .wait(1000);


        //span:contains('赞')
        String zanSelector="span.coreSpriteHeartOpen";
//            String zanSelector=":contains(赞)";
        //等待元素出来
        boolean isEleShowed = session.waitUntil(s -> {
            return s.matches(zanSelector);
        },  500);
        if(isEleShowed){
            session.click(zanSelector);
            int zanCountInt = InsOneUserListSpiderBean.zanCount.get();
            System.out.println("点的第几个赞:"+zanCountInt+"已经处理的点赞页面:"+picUrl);
        }
    }

    boolean canGuanzhu=true;//可以关注

    /***
     * 进入某人页面后,点关注
     */
    public  void guanzhu(String userUrl) {
        //进入某人的一张照片页面.
        session.navigate(userUrl)
                .waitDocumentReady()
                .wait(1000);


        String guanzhuSelector="h1+span>span>button";//关注按钮,在h1临近的第一个button.需要判断内容,否则取消掉了
//            String zanSelector=":contains(赞)";
        //等待元素出来
//        System.out.println(session.getContent());
        boolean isEleShowed = session.waitUntil(s -> {
            return s.matches(guanzhuSelector);
        },  500);
        if(isEleShowed){
            String buttonContent = session.getText(guanzhuSelector);
            if(buttonContent!=null && buttonContent.equals("关注")){
                session.click(guanzhuSelector);
                int guanzhuCountInt = InsOneUserListSpiderBean.guanzhuCount.getAndIncrement();
                System.out.println("关注的第几个人:"+guanzhuCountInt+",url:"+userUrl);
                if(guanzhuCountInt>=InsConsts.maxGuanzhuNum){
                    //每天最多关注的人数
                    canGuanzhu=false;
                }
            }else if(buttonContent!=null && buttonContent.equals("正在关注")){
                System.out.println("之前已经关注了"+userUrl);

            }
        }
    }

    /***
     * 关注一群人
     */
    public  void guanzhuAll(){
        List<String> followers = FileUtil.readFileByLines(InsConsts.followed_file_save_path+"_420taiwan-20180115.txt");
        List<String> myAlreadyGuanzhu =  FileUtil.readFileByLines(InsConsts.follow_file_save_path+"_maozedongdong_20180115.txt");;
        followers.removeAll(myAlreadyGuanzhu);//只剩下未关注的
//        Collections.shuffle(followers);//洗牌 .打乱list内容的顺序
        for (int i = 0; i < followers.size(); i++) {
            String follower = followers.get(i);
            String followUrl = InsConsts.insBaseUrl + follower + "/";
            if(canGuanzhu){
                guanzhu(followUrl);
            }
        }
        this.close();

    }



    public static void main(String[] args) {
        InsAuto insAuto= InsAuto.getInstance();
//        String url = "https://www.instagram.com/p/Bc7r7wHDMoY/?taken-by=neymarjr";
//        register.dianzan(url);
//        String url="https://www.instagram.com/hong2000_/";
//           register.guanzhu(url);
        insAuto.guanzhuAll();
    }
}
