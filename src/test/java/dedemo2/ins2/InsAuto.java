package dedemo2.ins2;

import com.geccocrawler.gecco.demo.ins.InsConsts;
import com.geccocrawler.gecco.demo.ins.InsUtil;
import com.geccocrawler.gecco.demo.ins.MongoUtil;
import com.geccocrawler.gecco.local.MongoDBJDBC;
import com.geccocrawler.gecco.utils.ReplyPeople;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import io.webfolder.cdp.Launcher;
import io.webfolder.cdp.session.Session;
import io.webfolder.cdp.session.SessionFactory;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private InsConfig insConfig;


    private InsAuto() {
        insConfig=new InsConfig();
        insConfig.setNeedChangeUser(true);//如果为false,下面的user设置无效 .都设为true把，否则setColWMyYgz不对
//        insConfig.setOneUser("jiangchunyun88");//使用哪个用户
        insConfig.setOneUser(InsConsts.curUsername);//使用哪个用户
        insConfig.setColWMyYgz(InsConsts.col_my_w_ygz_prefix+insConfig.getCurUserName());
        insConfig.setColWMyYfs(InsConsts.col_my_w_yfs_prefix+insConfig.getCurUserName());
        init();
    }

    /***
     * 初始化session
     */
    private void init() {
        Launcher launcher = new Launcher();
        factory = launcher.launch();
        session = factory.create();

        if(insConfig.isNeedChangeUser()){
            //清空cache,
            session.clearCache();
            //清空cookie  .可以用来重新登陆
            session.clearCookies();
        }
        session.navigate(InsConsts.insBaseUrl2)
                .waitDocumentReady().wait(5000);

        String content = session.getContent();
        if(insConfig.isNeedChangeUser() ||
                content.contains("Have an account") || content.contains("有帐户了")
                || content.contains("登录") || content.contains("Sign in")
                || content.contains("Log in")){
            //进入登陆页面
            session.navigate(InsConsts.insBaseUrl+"accounts/login/")
                    .waitDocumentReady();

            //必须做这个判断，否则下面的input找不到
            boolean isEleShowed = session.waitUntil(s -> {
                return s.matches("input[name='username']");
            },  5000);
            if(isEleShowed){
                session.installSizzle()
//                    .enableNetworkLog()
                        .focus("input[name='username']")//鼠标焦点
                        .sendKeys("xx")//随便输入东西，对抗浏览器的自动处理。这样才能成功在下面清空
                        .selectInputText("input[name='username']")//全选输入框
                        .sendBackspace()//退格键,清空
                        .sendKeys(insConfig.getCurUserName())
                        .focus("input[name='password']")//鼠标焦点
                        .sendKeys("xx")//随便输入东西，对抗浏览器的自动处理。这样才能成功在下面清空
                        .selectInputText("input[name='password']")//全选输入框
                        .sendBackspace()//退格键,清空
                        .sendKeys(insConfig.getCurUserPassword())
                        .sendEnter()
                        .wait(5000);//（必须加这个等待，否则直接跳转其他页面的话，会导致登陆操作没做完）
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
            int zanCountInt = InsUtil.zanCount.get();
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
                .waitDocumentReady();
//                .wait(1000);


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
                    .sendKeys(ReplyPeople.getText())
                    .sendEnter();


            int countInt = InsUtil.pinglunCount.getAndIncrement();
            System.out.println("评论的第几个:"+countInt+"已经处理的评论页面:"+picUrl);
        }
    }

    /***
     * 在一个照片页面,对图片评论.
     *
     */
    public  void tagPinglunAndGetUsername(String picUrl) {
        //进入某人的一张照片页面.
        session.navigate(picUrl)
                .waitDocumentReady();
//                .wait(1000);


        String selector="form textarea";
        //等待元素出来
        boolean isEleShowed = session.waitUntil(s -> {
            return s.matches(selector);
        },  500);
        //保存用户名 可以从页面js中提取（_window_data），也可以从html中
        String username = session.getText("article > header>div:eq(1) a:eq(0)");
        MongoUtil.getMongoDBJDBC().save2Coll(username,InsConsts.col_w_qianzaidaip_cn);
        if(isEleShowed){
            session
                    .focus(selector)//鼠标焦点
                    .selectInputText(selector)//全选输入框
                    .sendBackspace()//退格键,清空
                    .sendKeys(ReplyPeople.getText())
                    .sendEnter();


            int countInt = InsUtil.pinglunCount.getAndIncrement();
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
            if(buttonContent!=null && (buttonContent.equals("关注") || buttonContent.equals("Follow"))){
                session.click(guanzhuSelector);

                int guanzhuCountInt = InsUtil.guanzhuCount.getAndIncrement();
                System.out.println("点了关注的第几个人:"+guanzhuCountInt+",url:"+userUrl);
                //保存到mongo
                MongoDBJDBC.getInstance().save2Coll(user,insConfig.getColWMyYgz());
                if(guanzhuCountInt>=InsConsts.maxGuanzhuNum){
                    //每天最多关注的人数
                    canGuanzhu=false;
                }
            }else if(buttonContent!=null && (buttonContent.equals("正在关注") ||buttonContent.equals("Following")  )){
                System.out.println("之前已经关注了"+userUrl);
                boolean exist = MongoDBJDBC.getInstance().exist("username",user,insConfig.getColWMyYgz());
                if(!exist){
                    MongoDBJDBC.getInstance().save2Coll(user,insConfig.getColWMyYgz());
                }

            }else if(buttonContent!=null && (buttonContent.contains("发送") || buttonContent.contains("Requested"))){
                System.out.println("之前已经点了,处于已发送状态");
                boolean exist = MongoDBJDBC.getInstance().exist("username",user,insConfig.getColWMyYfs());
                if(!exist){
                    MongoDBJDBC.getInstance().save2Coll(user,insConfig.getColWMyYfs());
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
           MongoCollection<Document> allColl = MongoUtil.getMongoDBJDBC().addColl("col_w_super_lemon_he","col_w_hongkong420","col_w_daily420.hk");
            MongoCollection<Document> weiguanzhuColl = MongoUtil.getInstance().notFollowingColl2(allColl,insConfig.getCurUserName());//未关注的集合
//            MongoCollection<Document> weiguanzhuColl = MongoDBJDBC.getInstance().getMongoDatabase().getCollection("col_w_hongkong420");
//            MongoCursor<Document> iterator = weiguanzhuColl.find().iterator();
            List<Document> list = new ArrayList<Document>();
            MongoCursor<Document> iterator = weiguanzhuColl.find().noCursorTimeout(true).iterator();
            while (iterator.hasNext()){
                Document doc = iterator.next();
                list.add(doc);
            }
            Collections.shuffle(list);//洗牌
            for (Document doc : list) {
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
                InsUtil.guanzhuCount=new AtomicInteger(0);
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

        insAuto.guanzhuAll();
//        insAuto.yifasongAll();
//        insAuto.pinglun("https://www.instagram.com/p/BbtYLixAhO1/?taken-by=yauhongv3v");

    }
}
