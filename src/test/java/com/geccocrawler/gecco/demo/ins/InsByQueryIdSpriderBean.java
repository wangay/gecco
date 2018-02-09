package com.geccocrawler.gecco.demo.ins;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.local.FileUtil;
import com.geccocrawler.gecco.local.MongoDBJDBC;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpGetRequest;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.scheduler.SchedulerContext;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.utils.DateUtil;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import dedemo2.ins2.InsAuto;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/***
 * 通过queryId的形式请求的内容 包括:
 * 单个用户的所有记录,或者被喜欢的情况
 * 他们请求的js ajax连接,都是同一个模式,都在这个判断处理
 *
 * 这个也可以作为入口:
 * following: 从chrome中看userId,以及第一个url
 *
 * followed: 参考following
 *
 * query_id:17851374694183129
 variables:{"id":"6854724440","first":10,"after":"AQBJo1EEtR9Ga_pTkhD03uGDMB5Yq_xAbmeHQZnvi9txUKsGVMP6feRfdjigfpmxtO0vSvqTqnGWruAxO7Otjo8k8Hb0Ge2b8ZZfPCv9jSCUvQ"}
 *
 * 从chrome中,找到?query_id....请求. 并且是不带after参数的那个连接.比如:https://www.instagram.com/graphql/query/?query_id=17851374694183129&variables=%7B%22id%22%3A%226854724440%22%2C%22first%22%3A20%7D
 *
 *
 * #############################################################################
 * 新：热门标签的查询也用这个
 * https://www.instagram.com/explore/tags/yeezy350/
 *
 * 1. Request URL:
 https://www.instagram.com/graphql/query/?query_hash=298b92c8d7cad703f7565aa892ede943&variables=%7B%22tag_name%22%3A%22yeezy350%22%2C%22first%22%3A4%2C%22after%22%3A%22J0HWnyu-wAAAF0HWnycuQAAAFnIA%22%7D

 1. query_hash:
 298b92c8d7cad703f7565aa892ede943
 2.
 3. variables:
 {"tag_name":"yeezy350","first":4,"after":"J0HWnys0wAAAF0HWnyaCwAAAFnIA"}

 */
@PipelineName("InsByQueryIdSpriderBean")
//@Gecco(matchUrl = "https://www.instagram.com/graphql/query/?query_id={queryId}&variables={variables}", pipelines = "InsByQueryIdSpriderBean",downloader="chromeCdp4jDownloader")
@Gecco(matchUrl = "https://www.instagram.com/graphql/query/?query_hash={queryId}&variables={variables}", pipelines = "InsByQueryIdSpriderBean", downloader = "chromeCdp4jDownloader")
public class InsByQueryIdSpriderBean implements HtmlBean, Pipeline<InsByQueryIdSpriderBean> {


    private static final long serialVersionUID = -712741258524433435L;

    @Request
    private HttpRequest request;

    @RequestParameter
    private String queryId;

    @RequestParameter
    private String variables;

    @RequestParameter
    private String picid;

    public String getPicid() {
        return picid;
    }

    public void setPicid(String picid) {
        this.picid = picid;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    /***
     * cdp4j,返回的数据,即使本来是纯的json,也会给加上一些外部的东西:
     * <html><head></head><body><pre style="word-wrap: break-word; white-space: pre-wrap;">
     *
     * 纯的json
     * </pre></body></html>
     */
    @Text
    @HtmlField(cssPath = "pre")
    private String all;

    public String getAll() {
        return all;
    }

    public void setAll(String all) {
        this.all = all;
    }


    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    @Override
    public void process(InsByQueryIdSpriderBean dmSpiderBean) {
        String all = dmSpiderBean.getAll();
        if(StringUtils.isEmpty(all)){
            return;
        }
        Object allJsonObject = JSONObject.parse(all);
        String selector = "$.data";
        JSONObject jsonObject = (JSONObject) com.alibaba.fastjson.JSONPath.eval(allJsonObject, selector);


        if (jsonObject.containsKey("hashtag")) {
            //tag
            this.processTag(dmSpiderBean);
        }else if (all.contains("edge_liked_by")) {
            //like的请求
            this.processLikes(dmSpiderBean);
        } else if (all.contains("edge_followed_by")) {
            //followed
            this.processFollowed(dmSpiderBean);
        } else if (all.contains("edge_follow")) {
            //following
            this.processFollowing(dmSpiderBean);
        } else if (all.contains("errors")) {
            System.out.println("报错了,先检查");
        } else {
            this.processUserRecords(dmSpiderBean);
        }

    }

    /***
     * 处理用户图片记录
     * @param dmSpiderBean
     */
    private void processUserRecords(InsByQueryIdSpriderBean dmSpiderBean) {
        Object allJsonObject = JSONObject.parse(dmSpiderBean.getAll());
        String selector = "$.data.user.edge_owner_to_timeline_media.edges";
        String selectorHasNextPage = "$.data.user.edge_owner_to_timeline_media.page_info.has_next_page";
        JSONArray imgArr = (JSONArray) com.alibaba.fastjson.JSONPath.eval(allJsonObject, selector);
        Boolean hasNextPage = (Boolean) com.alibaba.fastjson.JSONPath.eval(allJsonObject, selectorHasNextPage);
        String userId = null;
        if (imgArr == null) {
            return;
        }
        for (Object o : imgArr) {
            JSONObject imgJson = (JSONObject) o;
            String url = (String) com.alibaba.fastjson.JSONPath.eval(imgJson, "$.node.display_url");
            userId = (String) com.alibaba.fastjson.JSONPath.eval(imgJson, "$.node.owner.id");
            System.out.println(url);
            if (InsConsts.saveLinksLocal) {
                //本地保存照片
                try {
                    FileUtil.writeFileByFileWriterAdd(InsConsts.pic_local_position, url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (hasNextPage) {
            String after = (String) com.alibaba.fastjson.JSONPath.eval(allJsonObject, "$.data.user.edge_owner_to_timeline_media.page_info.end_cursor");
            JSONObject varJson = new JSONObject();
            varJson.putIfAbsent("id", userId);
            varJson.putIfAbsent("first", "50");
            varJson.putIfAbsent("after", after);
            String variables = varJson.toJSONString();
            String encode = null;
            try {
                encode = URLEncoder.encode(variables, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String moreUrl = "https://www.instagram.com/graphql/query/?" + "query_id=" + dmSpiderBean.getQueryId() + "&variables=" + encode;
            System.out.println("下一页:" + moreUrl);
            SchedulerContext.into(dmSpiderBean.getRequest().subRequest(moreUrl));
        }

    }

    /***
     * 处理用户被like的情况
     * 样例数据:ins-img-like.txt
     * @param dmSpiderBean
     */
    private void processLikes(InsByQueryIdSpriderBean dmSpiderBean) {
        Object allJsonObject = JSONObject.parse(dmSpiderBean.getAll());
        String selector = "$.data.shortcode_media.edge_liked_by.edges";
        String selectorHasNextPage = "$.data.shortcode_media.edge_liked_by.page_info.has_next_page";
        String afterSelector = "$.data.shortcode_media.edge_liked_by.page_info.end_cursor";
        JSONArray likesArr = (JSONArray) com.alibaba.fastjson.JSONPath.eval(allJsonObject, selector);
        Boolean hasNextPage = (Boolean) com.alibaba.fastjson.JSONPath.eval(allJsonObject, selectorHasNextPage);
        if (likesArr == null) {
            return;
        }
        System.out.println("被如下人like:");
        for (Object o : likesArr) {
            JSONObject likeJson = (JSONObject) o;
            String likingUserName = (String) com.alibaba.fastjson.JSONPath.eval(likeJson, "$.node.username");
            System.out.println(likingUserName);

            if (InsConsts.likingUserNameSaved) {
                try {
                    FileUtil.writeFileByFileWriterAdd("/Users/wangany/tem/spider/ins-cl-like.txt", likingUserName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        if (hasNextPage) {
            String after = (String) com.alibaba.fastjson.JSONPath.eval(allJsonObject, afterSelector);
            String shortcode = (String) com.alibaba.fastjson.JSONPath.eval(allJsonObject, "$.data.shortcode_media.shortcode");
            InsUtil.createLikeRecordScheduler(shortcode, after, dmSpiderBean.getQueryId(), dmSpiderBean.getRequest());
        }

    }

    /***
     * 处理用户following的情况
     * 样例数据:ins-img-like.txt
     * @param dmSpiderBean
     */
    private void processFollowing(InsByQueryIdSpriderBean dmSpiderBean) {
        Object allJsonObject = JSONObject.parse(dmSpiderBean.getAll());
        String selector = "$.data.user.edge_follow.edges";
        String selectorHasNextPage = "$.data.user.edge_follow.page_info.has_next_page";
        String afterSelector = "$.data.user.edge_follow.page_info.end_cursor";
        JSONArray likesArr = (JSONArray) com.alibaba.fastjson.JSONPath.eval(allJsonObject, selector);
        Boolean hasNextPage = (Boolean) com.alibaba.fastjson.JSONPath.eval(allJsonObject, selectorHasNextPage);
        if (likesArr == null) {
            return;
        }
        for (Object o : likesArr) {
            JSONObject likeJson = (JSONObject) o;
            String userName = (String) com.alibaba.fastjson.JSONPath.eval(likeJson, "$.node.username");
            System.out.println(userName);

            if (InsConsts.likingUserNameSaved) {
                try {
                    String date = DateUtil.parseDateToStr(new Date());
//                    FileUtil.writeFileByFileWriterAdd(InsConsts.follow_file_save_path+"_"+InsConsts.userId+"_"+date+".txt",userName);
                    //持久化到mongodb
                    MongoDBJDBC mongo = MongoDBJDBC.getInstance();
                    mongo.saveMzddguanzhu(userName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        if (hasNextPage) {
            String after = (String) com.alibaba.fastjson.JSONPath.eval(allJsonObject, afterSelector);
            InsUtil.createFollowingScheduler(after, dmSpiderBean.getQueryId(), dmSpiderBean.getRequest());
        }

    }

    /***
     * 处理用户followed的情况
     * 样例数据:ins-img-like.txt
     * @param dmSpiderBean
     */
    private void processFollowed(InsByQueryIdSpriderBean dmSpiderBean) {
        Object allJsonObject = JSONObject.parse(dmSpiderBean.getAll());
        String selector = "$.data.user.edge_followed_by.edges";
        String selectorHasNextPage = "$.data.user.edge_followed_by.page_info.has_next_page";
        String afterSelector = "$.data.user.edge_followed_by.page_info.end_cursor";
        JSONArray likesArr = (JSONArray) com.alibaba.fastjson.JSONPath.eval(allJsonObject, selector);
        Boolean hasNextPage = (Boolean) com.alibaba.fastjson.JSONPath.eval(allJsonObject, selectorHasNextPage);
        // 找到是哪个大ip
        String userId = null;
        userId = InsUtil.getFromEncode(dmSpiderBean.getRequest().getParameter("variables"));
        if (likesArr == null) {
            return;
        }
        String usernameIP = MongoUtil.getInstance().findByUserId(userId);
        for (Object o : likesArr) {
            JSONObject likeJson = (JSONObject) o;
            String userName = (String) com.alibaba.fastjson.JSONPath.eval(likeJson, "$.node.username");
            System.out.println(userName);

            if (InsConsts.likingUserNameSaved) {
                try {
                    if (StringUtils.isNotEmpty(usernameIP)) {
                        System.out.println("保存进本地库:" + InsConsts.col_prefix + usernameIP);
                        //持久化到mongodb
                        MongoUtil.getMongoDBJDBC().save2Coll(userName, InsConsts.col_prefix + usernameIP);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        if (hasNextPage) {
            String after = (String) com.alibaba.fastjson.JSONPath.eval(allJsonObject, afterSelector);
            InsUtil.createFollowedScheduler(userId,after, dmSpiderBean.getQueryId(), dmSpiderBean.getRequest());
        }

    }

    /***
     * 处理tag的情况
     * 样例数据:ins-img-like.txt
     * @param dmSpiderBean
     */
    private void processTag(InsByQueryIdSpriderBean dmSpiderBean) {
        Object allJsonObject = JSONObject.parse(dmSpiderBean.getAll());
        String selector = "$.data.hashtag.edge_hashtag_to_media.edges";
        String selectorHasNextPage = "$.data.hashtag.edge_hashtag_to_media.page_info.has_next_page";
        String afterSelector = "$.data.hashtag.edge_hashtag_to_media.page_info.end_cursor";
        JSONArray likesArr = (JSONArray) com.alibaba.fastjson.JSONPath.eval(allJsonObject, selector);
        Boolean hasNextPage = (Boolean) com.alibaba.fastjson.JSONPath.eval(allJsonObject, selectorHasNextPage);

        String tagName=(String)InsUtil.getJsonFromEncode(dmSpiderBean.getVariables()).get("tag_name");;


        //以上是对的
        String first = "4";//InsConsts.page_follow_Count;//TODO first
        if (likesArr == null) {
            return;
        }

        for (Object o : likesArr) {
            JSONObject likeJson = (JSONObject) o;
//            String userId = (String) com.alibaba.fastjson.JSONPath.eval(likeJson, "$.node.owner.id");
            String shortcode = (String) com.alibaba.fastjson.JSONPath.eval(likeJson, "$.node.shortcode");

            //放单页的请求到scheduler
            String picUrl = InsConsts.insBaseUrl3+shortcode;
//            SchedulerContext.into(request.subRequest(picUrl));
            InsAuto insAuto = InsAuto.getInstance();
            insAuto.tagPinglunAndGetUsername(picUrl);
        }
        if (hasNextPage) {
            String after = (String) com.alibaba.fastjson.JSONPath.eval(allJsonObject, afterSelector);
            InsUtil.createTagScheduler(tagName,dmSpiderBean.getQueryId(),first,after, dmSpiderBean.getRequest());
        }

    }


    /***
     * 某账户所关注的人
     */
    private static void following() {
        MongoDBJDBC mongo = MongoDBJDBC.getInstance();
        mongo.deleteMzddGuanzhu();

        String queryId = "17874545323001329";
        String url = InsUtil.createInitQueryEncodedUrl(InsConsts.userId, queryId, InsConsts.page_follow_Count);
        GeccoEngine.create()
                .classpath("com.geccocrawler.gecco.demo.ins")
                .start(url)
                .interval(3000)
                .start();
    }

    /***
     * 某账户被follow的人(粉丝)
     */
    private static void followed() {
        MongoDBJDBC mongo = MongoDBJDBC.getInstance();
//        mongo.getMongoDatabase().getCollection(InsConsts.col_w_taiwan420).drop();

        //query_hash:37479f2b8209594dde7facb0d904896a
        //variables:{"id":"5620693450","first":20}

        String queryId = "37479f2b8209594dde7facb0d904896a";//"17851374694183129";
        String username="super_lemon_he";

        long userId =InsUtil.getUserIdByUsername(username);
//        id=InsConsts.userId;
        String url = InsUtil.createInitQueryEncodedUrl(userId+"", queryId, InsConsts.page_follow_Count);
        GeccoEngine.create()
                .classpath("com.geccocrawler.gecco.demo.ins")
                .start(url)
                .interval(3000)
                .start();
    }

    /***
     * 一批账户被follow的人(粉丝)
     */
    private static void followedMany() {
        String queryId = "37479f2b8209594dde7facb0d904896a";//这个id是固定的?跟用户无关 "17851374694183129";
        List<HttpRequest> requestList = new ArrayList<HttpRequest>();
        MongoCollection<Document> coll = MongoUtil.getColl(InsConsts.col_w_qianzaidaip);
        FindIterable<Document> findIterable = coll.find().noCursorTimeout(true);
        MongoCursor<Document> mongoCursor = findIterable.iterator();

        String str="col_jiandan\n" +
                "col_w_420.vanesssssa\n" +
                "col_w_420852_\n" +
                "col_w_420_onfleek\n" +
                "col_w_420_rabbit\n" +
                "col_w_420_trips\n" +
                "col_w_420blazetheganja\n" +
                "col_w_420buddy\n" +
                "col_w_420colorado420\n" +
                "col_w_420litaf\n" +
                "col_w_420magazine\n" +
                "col_w_420newsworldhq\n" +
                "col_w_420science\n" +
                "col_w_420stoneman\n" +
                "col_w_57.420\n" +
                "col_w_852smoker420\n" +
                "col_w__keepweed\n" +
                "col_w__weedcn\n" +
                "col_w_addsomemoreweed\n" +
                "col_w_annylu420\n" +
                "col_w_autoweed\n" +
                "col_w_benkush4200\n" +
                "col_w_besttimeforweed\n" +
                "col_w_blaze.thegreen.weed\n" +
                "col_w_c.chill_420\n" +
                "col_w_cannabis420_daily\n" +
                "col_w_caogulaochou420\n" +
                "col_w_carg420\n" +
                "col_w_chrisyeh420bpm\n" +
                "col_w_cloud9weed\n" +
                "col_w_dab420420\n" +
                "col_w_daily420.hk\n" +
                "col_w_dope420online\n" +
                "col_w_eeeeee420\n" +
                "col_w_enjoy_weed\n" +
                "col_w_fb420\n" +
                "col_w_goldenleaf_420\n" +
                "col_w_goooofy420\n" +
                "col_w_happy.hour.420\n" +
                "col_w_hkweed420\n" +
                "col_w_hongkong420\n" +
                "col_w_hymanoki420\n" +
                "col_w_jan420hk\n" +
                "col_w_javier_og420\n" +
                "col_w_joint.420\n" +
                "col_w_kushpalace420_\n" +
                "col_w_lifeshit_420\n" +
                "col_w_lucysd_420\n" +
                "col_w_mocc420\n" +
                "col_w_og420_hkstoner\n" +
                "col_w_ohlala.420\n" +
                "col_w_oldgrouchybastard420\n" +
                "col_w_quincyweed\n" +
                "col_w_space_craft420\n" +
                "col_w_steven974208\n" +
                "col_w_weed.diary420\n" +
                "col_w_weed.frique\n" +
                "col_w_weed.museum\n" +
                "col_w_weed.temple\n" +
                "col_w_weed4200man\n" +
                "col_w_weed420ing\n" +
                "col_w_weed420smile\n" +
                "col_w_weeddogghome\n" +
                "col_w_weedfirmgame\n" +
                "col_w_weedgazing\n" +
                "col_w_weedhighaf\n" +
                "col_w_weediswhatilove\n" +
                "col_w_weedlandcol1\n" +
                "col_w_weedmaps\n" +
                "col_w_weedpark420\n" +
                "col_w_weedrl\n" +
                "col_w_x_weed_xx";

        while (mongoCursor.hasNext()) {
            Document doc = mongoCursor.next();
            String username = (String) doc.get("username");
            String userid = (String) doc.get("userId");
            String url = InsUtil.createInitQueryEncodedUrl(userid, queryId, InsConsts.page_follow_Count);
            // || username.equals("weeddogghome") || username.equals("hongkong420")
//            if(!str.contains(username) && username.equals("hongkong420"))
            if(!str.contains(username)){
                requestList.add(new HttpGetRequest(url));
            }
//            requestList.add(new HttpGetRequest(url));
        }
        GeccoEngine.create()
                .classpath("com.geccocrawler.gecco.demo.ins")
                .start(requestList)
                .interval(5000)
//                .thread(3)
                .start();
    }

    /***
     * tag 热门标签
     */
    private static void tag() {
        String queryId = InsConsts.query_hash_tag;
        String tagName="hk420";
//        String tagName="飞行中国";

        String url = InsUtil.createTagInitEncodedUrl(tagName+"", queryId, InsConsts.pageCount_tag);
        GeccoEngine.create()
                .classpath("com.geccocrawler.gecco.demo.ins")
                .start(url)
                .interval(3000)
                .start();
    }

    /***
     * tag 热门标签
     */
    private static void tagAll() {
        String queryId = InsConsts.query_hash_tag;
        List<HttpRequest> requestList = new ArrayList<HttpRequest>();
        for (String hot_w_tag : InsConsts.hot_w_tags) {
            String tagName=hot_w_tag;
            if(tagName.contains("飞行中国")){
                continue;
            }
            String url = InsUtil.createTagInitEncodedUrl(tagName+"", queryId, InsConsts.pageCount_tag);
            requestList.add(new HttpGetRequest(url));
        }
        GeccoEngine.create()
                .classpath("com.geccocrawler.gecco.demo.ins")
                .start(requestList)
                .interval(3000)
                .start();
    }


    public static void main(String[] args) {
//        following();
//        followed();
//        followedMany();
//        tag();
        tagAll();
    }

}
