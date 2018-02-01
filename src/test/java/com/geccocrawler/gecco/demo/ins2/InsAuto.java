package com.geccocrawler.gecco.demo.ins2;

import com.geccocrawler.gecco.demo.ins.InsConsts;
import com.geccocrawler.gecco.local.MongoDBJDBC;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;
import org.bson.Document;

import java.util.concurrent.atomic.AtomicInteger;

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
        String loginInSelector="p:contains('Have an account'),p:contains('有帐户了')";
        session.navigate(InsConsts.insBaseUrl2)
                .waitDocumentReady().wait(500);

        String content = session.getContent();
        if(content.contains("Have an account") || content.contains("有帐户了")){
            //没登录
            //等待元素出来
            boolean toLogShowed = session.waitUntil(s -> {
                return s.matches("input[name='username']");
            }, 20 * 1000);

            if(toLogShowed){
                //.wait(5000)//必须加? 否
                // 则可能失败. 话说waitDocumentReady是只等待第一次请求的文档完毕? 后面的是js加载出来的
                // 考虑用waitUtil那个方法.
                session.installSizzle()
                        .enableNetworkLog()
                        .click(loginInSelector)
                        .wait(500)
                        .focus("input[name='username']")//鼠标焦点
                        .selectInputText("input[name='username']")//全选输入框
                        .sendBackspace()//退格键,清空
                        .sendKeys("maozebei6368")
                        .focus("input[name='password']")//鼠标焦点
                        .selectInputText("input[name='password']")//全选输入框
                        .sendBackspace()//退格键,清空
                        .sendKeys("maozebei63681")
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

    /***
     * 在一个照片页面,对图片评论.
     *
     */
    public  void pinglun(String picUrl) {
        //进入某人的一张照片页面.
        session.navigate(picUrl)
                .waitDocumentReady()
                .wait(1000);


        String selector="form textarea";
        //等待元素出来
        boolean isEleShowed = session.waitUntil(s -> {
            return s.matches(selector);
        },  500);
        if(isEleShowed){
            session
                    .focus(selector)//鼠标焦点
                    .selectInputText(selector)//全选输入框
                    .sendBackspace()//退格键,清空
                    .sendKeys("真不错,棒棒的~")//alexTODO 人性化语句收集
                    .sendEnter();


            int countInt = InsOneUserListSpiderBean.pinglunCount.get();
            System.out.println("评论的第几个:"+countInt+"已经处理的评论页面:"+picUrl);
        }
    }

    boolean canGuanzhu=true;//可以关注

    /***
     * 进入某人页面后,点关注
     */
    public  void guanzhu(String userUrl,String user) {
        //进入某人的一张照片页面.
        session.navigate(userUrl)
                .waitDocumentReady()
                .wait(1000);


//        String guanzhuSelector="h1+span>span>button";//关注按钮,在h1临近的第一个button.需要判断内容,否则取消掉了
        String guanzhuSelector="h1+span button,h1+span>span>button";//关注按钮,在h1临近的第一个button.需要判断内容,否则取消掉了
//            String zanSelector=":contains(赞)";
        //等待元素出来
//        System.out.println(session.getContent());
        boolean isEleShowed = session.waitUntil(s -> {
            return s.matches(guanzhuSelector);
        },  500);
        if(isEleShowed && session.matches(guanzhuSelector)){

            String buttonContent = null;
            try {
                //会有session问题,导致找不到,报错.
                buttonContent = session.getText(guanzhuSelector);
            } catch (Exception e) {
                System.out.println("找不到button,url:"+userUrl);
            }
            if(buttonContent!=null && buttonContent.equals("关注")){
                session.click(guanzhuSelector);

                int guanzhuCountInt = InsOneUserListSpiderBean.guanzhuCount.getAndIncrement();
                System.out.println("点了关注的第几个人:"+guanzhuCountInt+",url:"+userUrl);
                //保存到mongo
                MongoDBJDBC.getInstance().save2Coll(user,InsConsts.col_w_my_mzdd);
                if(guanzhuCountInt>=InsConsts.maxGuanzhuNum){
                    //每天最多关注的人数
                    canGuanzhu=false;
                }
            }else if(buttonContent!=null && buttonContent.equals("正在关注")){
                System.out.println("之前已经关注了"+userUrl);

            }else if(buttonContent!=null && buttonContent.contains("发送")){
                System.out.println("之前已经点了,处于已发送状态");
                boolean exist = MongoDBJDBC.getInstance().exist("username",user,InsConsts.mzddYFS2Tai420);
                if(!exist){
                    MongoDBJDBC.getInstance().save2Coll(user,InsConsts.mzddYFS2Tai420);
                }
            }
        }
    }

    /***
     * 关注一群人
     */
    public  void guanzhuAll(){
        int times=0;
        while(true){
//            MongoCollection<Document> weiguanzhuColl = MongoUtil.getInstance().notFollowingColl();//未关注的集合

            MongoCollection<Document> weiguanzhuColl = MongoDBJDBC.getInstance().getMongoDatabase().getCollection(InsConsts.col_w_taiwan420);
            MongoCursor<Document> iterator = weiguanzhuColl.find().iterator();
            while (iterator.hasNext()){
                Document doc = iterator.next();
                String follower = (String)doc.get("username");
                String followUrl = InsConsts.insBaseUrl + follower + "/";
                if(canGuanzhu){
                    guanzhu(followUrl,follower);
                }

            }

            //终止循环 为了下面的close()被执行到.
            if(times++>7){
                break;
            }
            //停35分钟
            try {
                System.out.println("处理完了第几次:"+times+"开始睡觉");

                Thread.sleep(1000*60*35);
                System.out.println("睡觉结束,开始下一次");
                //重置标志位
                InsOneUserListSpiderBean.guanzhuCount=new AtomicInteger(0);
                this.canGuanzhu=true;

            } catch (InterruptedException e) {

            }

        }

        this.close();

    }

    //---保存已发送信息
    public  void saveYiFaSong(String userUrl,String user) {
        //进入某人的一张照片页面.
        session.navigate(userUrl)
                .waitDocumentReady()
                .wait(1000);


        String guanzhuSelector="h1+span button,h1+span>span>button";//关注按钮,在h1临近的第一个button.需要判断内容,否则取消掉了
        boolean isEleShowed = session.waitUntil(s -> {
            return s.matches(guanzhuSelector);
        },  500);
        if(isEleShowed && session.matches(guanzhuSelector)){
            String buttonContent = null;
            try {
                //会有session问题,导致找不到,报错.
                buttonContent = session.getText(guanzhuSelector);
            } catch (Exception e) {
                System.out.println("找不到button,url:"+userUrl);
            }
            if(buttonContent!=null && buttonContent.contains("发送")){
                System.out.println("之前已经点了,处于已发送状态:"+userUrl);
                boolean exist = MongoDBJDBC.getInstance().exist("username",user,InsConsts.mzddYFS2Tai420);
                if(!exist){
                    MongoDBJDBC.getInstance().save2Coll(user,InsConsts.mzddYFS2Tai420);
                }
            }
        }
    }

    /***
     * 关注一群人
     */
    public  void yifasongAll(){
        while(true){
            MongoDBJDBC mongoDBJDBC = MongoDBJDBC.getInstance();
            MongoCollection<Document> taiwan420 = mongoDBJDBC.getMongoDatabase().getCollection(InsConsts.col_w_taiwan420);
            MongoCollection<Document> yfsColl=mongoDBJDBC.getMongoDatabase().getCollection(InsConsts.mzddYFS2Tai420);
            yfsColl.deleteMany(new Document());

            MongoCursor<Document> iterator = taiwan420.find().iterator();
            while (iterator.hasNext()){
                Document next = iterator.next();
                String username = (String)next.get("username");
                String followUrl = InsConsts.insBaseUrl + username + "/";
                saveYiFaSong(followUrl,username);
            }
        }
    }





    public static void main(String[] args) {
        InsAuto insAuto= InsAuto.getInstance();
//        String url = "https://www.instagram.com/p/Bc7r7wHDMoY/?taken-by=neymarjr";
//        register.dianzan(url);
//        String url="https://www.instagram.com/jiyouchen7655/";
//        String url="https://www.instagram.com/lucky42222/";
//        String user = "lucky42222";
//           insAuto.guanzhu(url,user);

//        insAuto.guanzhuAll();
//        insAuto.yifasongAll();
        insAuto.pinglun("https://www.instagram.com/p/Bei8YYmnR-V/?taken-by=hkweed420");
    }
}
