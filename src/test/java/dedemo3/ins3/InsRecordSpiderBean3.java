package dedemo3.ins3;

import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.demo.ins.InsConsts;
import com.geccocrawler.gecco.demo.ins.InsUtil;
import com.geccocrawler.gecco.demo.ins.MongoUtil;
import com.geccocrawler.gecco.local.MongoDBJDBC;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpGetRequest;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.utils.CommonUtil;
import com.mongodb.client.MongoCollection;
import dedemo2.ins2.InsAuto;
import dedemo2.ins2.InsOneUserListSpiderBean;
import me.postaddict.instagram.scraper.model.Account;
import me.postaddict.instagram.scraper.model.Media;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/***
 * dm 单条记录(含一张照片,被喜欢情况,评论等). https://www.instagram.com/p/Bc3MVJdjTjd/?taken-by=neymarjr 这样的一个页面
 *,downloader="chromeCdp4jDownloader"
 *
 */
@PipelineName("InsRecordSpiderBean3")
@Gecco(matchUrl = "https://www.instagram.com/p/{shortcode}/?taken-by={username}", pipelines = "InsRecordSpiderBean3",downloader="chromeCdp4jDownloader")
public class InsRecordSpiderBean3 implements HtmlBean, Pipeline<InsRecordSpiderBean3> {


    private static final long serialVersionUID = -7127412585200687225L;

    @Request
    private HttpRequest request;

    @RequestParameter("shortcode")
    private String shortcode;

    @RequestParameter("username")
    private String username;

    @Attr(value="src")
    @HtmlField(cssPath = "script[crossorigin=anonymous]")
    public List<String> picMoreScript;


    @Href
    @HtmlField(cssPath = "a:eq(0)")
    private String title;


    /***
     * 之前去寻找包含所需数据的js,不好找,那就提取出所有script元素.
     */
    @HtmlField(cssPath = "script")
    public List<String> scriptList;

    public List<String> getScriptList() {
        return scriptList;
    }

    public void setScriptList(List<String> scriptList) {
        this.scriptList = scriptList;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public List<String> getPicMoreScript() {
        return picMoreScript;
    }

    public void setPicMoreScript(List<String> picMoreScript) {
        this.picMoreScript = picMoreScript;
    }

    public String getShortcode() {
        return shortcode;
    }

    public void setShortcode(String shortcode) {
        this.shortcode = shortcode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    /***
     *
     * @param dmSpiderBean
     */
    @Override
    public void process(InsRecordSpiderBean3 dmSpiderBean) {
        String shortCode = dmSpiderBean.getShortcode();
        String username = dmSpiderBean.getUsername();
        String url = InsConsts.insBaseUrl3+shortCode+"/?taken-by="+username;


        InsAuto insAuto = InsAuto.getInstance();
        if(InsConsts.do_this==InsConsts.do_dianzan){
            int zanCountInt = InsUtil.zanCount.get();
            if(zanCountInt>= InsConsts.maxZanADay){
                System.out.println("已经到达每天最大点赞数量");
                //SchedulerContext.empty();//剩下的任务都清空 alexTODO 不好用?
                //退出整个jvm
                System.exit(0);
                return;
            }
            insAuto.dianzan(url);
        }else if(InsConsts.do_this==InsConsts.do_pinglun){
            int countInt = InsUtil.pinglunCount.get();
            if(countInt>= InsConsts.maxPinglunADay){
                System.out.println("已经到达每天最大评论数量");
                //退出整个jvm
                System.exit(0);
                return;
            }
            insAuto.pinglun(url);
        }else if(InsConsts.do_this==InsConsts.do_dianzanjipinglun){
            insAuto.dianzan(url);
            insAuto.pinglun(url);
        }

    }


    /***
     * 自动点赞.
     *  一天循环8~9次结束(中间加个一个90分钟左右), 每次点赞300左右
     */
    private static void dianzan() {
        CountDownLatch cdlWhole = null;
        int times = 0;
        while (true) {
            cdlWhole = new CountDownLatch(1);
            InsUtil.zanCount = new AtomicInteger(0);
            System.out.println("开始点赞,第几次" + (times + 1));
            List<HttpRequest> picRequests = new ArrayList<HttpRequest>();

//            List<String> followers = FileUtil.readFileByLines(InsConsts.follow_file_save_path + "_maozedongdong_20180115.txt");
            MongoCollection<Document> mzddguanzhu = MongoDBJDBC.getInstance().getMongoDatabase().getCollection("taiwan420");
            List<String> followers = MongoUtil.coll2List(mzddguanzhu);
            Collections.shuffle(followers);//洗牌 .打乱list内容的顺序 //只用某随机算法选出399个用户
            int num=0;
            for (int i = 0; i < followers.size(); i++) {

                String people = followers.get(i);
                Account account = InsUtil.getInstagramAccountByName(people);
                if(account!=null ){
                    List<Media> nodes = account.getMedia().getNodes();
                    if(nodes.size()>0){
                        if(num++>=InsConsts.maxRequestNum){
                            break;
                        }
                        Media media = nodes.get(0);
                        String shortCode = media.getShortcode();
                        //https://www.instagram.com/p/BeukMrFlhq3/?taken-by=iioozzz
                        String picUrl = InsConsts.insBaseUrl3+shortCode+"/?taken-by="+people;
                        picRequests.add(new HttpGetRequest(picUrl));
                    }
                }
            }

            System.out.println("要处理的数量："+picRequests.size());
            GeccoEngine.create()
                    .classpath("dedemo3.ins3")
                    .start(picRequests)
                    //开启几个爬虫线程(来通过处理foRequests这些请求)
                    .thread(3)
                    //单个爬虫每次抓取完一个请求后的间隔时间
                    .interval(2000)
                    .countDownLatchWhole(cdlWhole)
                    .start();

            //睡觉80~100分钟之间的随机数
            try {
                cdlWhole.await();//上面GeccoEngine start的任务都完成之前,都卡在这
                System.out.println("第几次点赞结束,开始睡觉" + times);
                int randomInt = CommonUtil.getRandomInt(80, 100);//3;//CommonUtil.getRandomInt(80, 100);//
                Thread.sleep(1000 * 60 * randomInt);
            } catch (InterruptedException e) {
            }
            if (times++ >= InsConsts.maxZanTimesADay) {
                break;
            }
        }
        System.out.println("今天的点赞结束");

    }

    /***
     * 自动评论
     */
    private static void pinglun() {
        CountDownLatch cdlWhole = null;
        int times = 0;
        while (true) {
            cdlWhole = new CountDownLatch(1);
            InsUtil.zanCount = new AtomicInteger(0);
            System.out.println("开始评论,第几次" + (times + 1));
            List<HttpRequest> picRequests = new ArrayList<HttpRequest>();

            MongoCollection<Document> coll = MongoUtil.getColl("taiwan420");
//            MongoCollection<Document> coll = MongoUtil.getMongoDBJDBC().addColl("taiwan420","col_w_hongkong420","col_w_daily420.hk");
            System.out.println("新集合的数量："+coll.count());
            List<String> peoples = MongoUtil.coll2List(coll);
            Collections.shuffle(peoples);//洗牌 .打乱list内容的顺序 //只用某随机算法选出399个用户
            int num=0;
            for (int i = 0; i < peoples.size(); i++) {

                String people = peoples.get(i);
                Account account = InsUtil.getInstagramAccountByName(people);
                if(account!=null ){
                    List<Media> nodes = account.getMedia().getNodes();
                    if(nodes.size()>0){
                        if(num++>=InsConsts.maxRequestNum){
                            break;
                        }
                        Media media = nodes.get(0);
                        String shortCode = media.getShortcode();
                        //https://www.instagram.com/p/BeukMrFlhq3/?taken-by=iioozzz
                        String picUrl = InsConsts.insBaseUrl3+shortCode+"/?taken-by="+people;
                        picRequests.add(new HttpGetRequest(picUrl));
                    }
                }
            }
            System.out.println("要处理的数量："+picRequests.size());

            GeccoEngine.create()
//                    .classpath("com.geccocrawler.gecco.dedemo3.ins3")
                    .classpath("dedemo3.ins3")
                    .start(picRequests)
                    //开启几个爬虫线程(来通过处理foRequests这些请求)
                    .thread(1)
                    //单个爬虫每次抓取完一个请求后的间隔时间
                    .interval(6000)
                    .countDownLatchWhole(cdlWhole)
                    .start();

            //睡觉80~100分钟之间的随机数
            try {
                cdlWhole.await();//上面GeccoEngine start的任务都完成之前,都卡在这
                System.out.println("第几次评论结束,开始睡觉" + times);
                int randomInt = CommonUtil.getRandomInt(80, 100);
                Thread.sleep(1000 * 60 * randomInt);
            } catch (InterruptedException e) {
            }
            if (times++ >= InsConsts.maxZanTimesADay) {
                break;
            }
        }
        System.out.println("今天的评论结束");

    }

    /***
     * 自动评论.热门标签
     */
    private static void pinglunHotTag() {
        int times = 0;
        while (true) {
            InsUtil.zanCount = new AtomicInteger(0);
            System.out.println("开始评论,第几次" + (times + 1));
            List<HttpRequest> picRequests = new ArrayList<HttpRequest>();

            Set<String> picUrls=new HashSet<String>();
            for (String hotTag: InsConsts.hot_w_tags) {
                List<String> picUrls1 = InsUtil.getPicUrls(hotTag, InsConsts.tag_howManyPages);
                picUrls.addAll(picUrls1);
            }
            for (String picUrl : picUrls) {
                picRequests.add(new HttpGetRequest(picUrl));
            }
            System.out.println("要处理的数量："+picRequests.size());
            GeccoEngine.create()
//                    .classpath("com.geccocrawler.gecco.dedemo3.ins3")
                    .classpath("dedemo3.ins3")
                    .start(picRequests)
                    //开启几个爬虫线程(来通过处理foRequests这些请求)
                    .thread(1)
                    //单个爬虫每次抓取完一个请求后的间隔时间
                    .interval(6000)
                    .start();

            //睡觉80~100分钟之间的随机数
            try {
                System.out.println("第几次评论结束,开始睡觉" + times);
                int randomInt = CommonUtil.getRandomInt(80, 100);
                Thread.sleep(1000 * 60 * randomInt);
            } catch (InterruptedException e) {
            }
            if (times++ >= InsConsts.maxZanTimesADay) {
                break;
            }
        }
        System.out.println("今天的评论结束");

    }


    /***
     * 单个连接测试.
     * 实际是被探测到的url,放入队列被执行.
     * @param args
     */
    public static void main(String[] args) {
//        dianzan();
//        pinglun();
        pinglunHotTag();
    }
}
