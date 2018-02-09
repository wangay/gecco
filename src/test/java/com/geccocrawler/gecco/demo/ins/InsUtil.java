package com.geccocrawler.gecco.demo.ins;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.scheduler.SchedulerContext;
import io.webfolder.cdp.CdpPubUtil;
import me.postaddict.instagram.scraper.Instagram;
import me.postaddict.instagram.scraper.cookie.CookieHashSet;
import me.postaddict.instagram.scraper.cookie.DefaultCookieJar;
import me.postaddict.instagram.scraper.interceptor.ErrorInterceptor;
import me.postaddict.instagram.scraper.model.*;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nobody on 2018/1/2.
 */
public class InsUtil {

    //计数相关
    public  static AtomicInteger zanCount = new AtomicInteger(0);//已经点赞的统计数量
    public  static AtomicInteger pinglunCount = new AtomicInteger(0);//已经评论的统计数量
    public  static AtomicInteger guanzhuCount = new AtomicInteger(0);//已经点了关注的统计数量

    private static  Instagram instagram; //ig的接口。
    static{
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(loggingInterceptor)
                .addInterceptor(new ErrorInterceptor())
                .cookieJar(new DefaultCookieJar(new CookieHashSet()))
                .build();

        instagram = new Instagram(httpClient);
    }

    /**
     * queryId有很多种:比如查询用户照片,被喜欢的情况,评论的情况等7~8种
     * @param jsContent
     * @param keyword
     * @return
     */
    public static String getQueryIdByKeyword(String jsContent,String keyword){
        String patternString = "queryId:\\\"(\\d*)?\\\"";//p=
//        String patternString = "p=\\\"(\\d{10,})\\\"";//p=17263623232  数字 至少10次
        Matcher m = Pattern.compile(patternString).matcher(jsContent);
        while (m.find()){
            //从头开始一直找,并打印找到的字符串
            String str = m.group();
            String queryId = str.substring(InsConsts.query_id.length()+2,str.length()-1);
            Long queryID = Long.valueOf(queryId.replaceAll("\\\"",""));
            int start = m.start();
            String sibling = jsContent.substring(start-50,start);
            if(sibling.contains(keyword)){
                return queryID+"";
            }
        }
        return null;
    }
    /***
     *   p = Object(s.b)({
     pageSize: c,
     pagesToPreload: 0,
     getState: function(e, t) {
     return e.profilePosts.byUserId.get(t).pagination
     },
     queryId: "17888483320059182",

     特征:紧邻的getState函数中,包含profilePosts.byUserId字符串
     * @param jsContent
     * @return
     */
    public static String getUserPostQueryId(String jsContent){
        return getQueryIdByKeyword(jsContent,"profilePosts.byUserId");
    }

    /***
     * like情况的queryId
     *
     *
     * c = "17864450716183058"------>这是要找的
     u = 10,
     l = 1,
     p = Symbol(),
     d = Symbol(),
     f = function() {
     var e = c;
     return Object(s.b)({
     pageSize: u,
     pagesToPreload: l,
     getState: function(e, t) {
     return e.likedByLists.get(t, o.a).pagination
     },
     queryId: e,


     *
     * 评论:comments.byPostId
     * @param jsContent
     * @return
     */
    public static String getLikeQueryId(String jsContent){
        String patternString = "queryId:(\\w)?";//queryId: e,
        Matcher m = Pattern.compile(patternString).matcher(jsContent);
        while (m.find()){
            //从头开始一直找,并打印找到的字符串
            int start = m.start();
            String sibling = jsContent.substring(start-50,start);
            if(sibling.contains("like")){
                //再往上找
                String sibling2 = jsContent.substring(start-200,start);
                Matcher matcher = Pattern.compile("\\\"(\\d{10,})\\\"").matcher(sibling2);
                while (matcher.find()){
                    String queryId = matcher.group();
                    queryId = queryId.replaceAll("\\\"","");
                    return queryId;
                }
            }
        }
        return null;
    }

    public static String getFollowingQueryId(String jsContent){
        String patternString = "queryId:(\\w)?";//queryId: e,
        Matcher m = Pattern.compile(patternString).matcher(jsContent);
        while (m.find()){
            //从头开始一直找,并打印找到的字符串
            int start = m.start();
            String sibling = jsContent.substring(start-80,start);
            if(sibling.contains("following")){
                //再往上找
                String sibling2 = jsContent.substring(start-200,start);
                Matcher matcher = Pattern.compile("\\\"(\\d{10,})\\\"").matcher(sibling2);
                while (matcher.find()){
                    String queryId = matcher.group();
                    queryId = queryId.replaceAll("\\\"","");
                    return queryId;
                }
            }
        }
        return null;
    }

    /***
     * 页面中有很多script.找到它的内容包含sharedData的那一个.
     * @return
     */
    public static String getDataScript(List<String> scriptList){
        for (int i = 0; i < scriptList.size(); i++) {
            String script = scriptList.get(i);
            if(StringUtils.isNotEmpty(script)){
                if(script.contains("sharedData")){
                    return script;
                }
            }
        }
        return null;
    }

    /***
     * 有些数据是在js中的,而且这个js是当前页面用src的形式引入的.需要网络请求
     * 返回这个js的内容
     * @return
     */
    public static String getCustomerDataScriptContent(List<String> picMoreScript){
        for (String picMore : picMoreScript) {
            String jsUrl = InsConsts.insBaseUrl2+picMore;
            //queryId在这个js里面.
            //https://www.instagram.com/static/bundles/ConsumerCommons.js/xxx.js
            if(jsUrl.contains("ConsumerCommons")){
                try {
                    String jsContent = CdpPubUtil.getInstance().getHtml(jsUrl, 10,10*1000);//HttpClientUtil.httpPure(jsUrl);//代理,否则访问不了
                    return jsContent;
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
        return null;
    }

    /***
     * 把一个url放进请求队列:
     * 这个url,用于获得被喜欢的记录. 返回的数据是分页的一页.
     * @param shortcode
     * @param after
     * @param queryId
     * @param request
     * @return
     */
    public static void createLikeRecordScheduler(String shortcode, String after, String queryId, HttpRequest request) {
        JSONObject varJson = new JSONObject();

        varJson.putIfAbsent("shortcode",shortcode);
        varJson.putIfAbsent("first",InsConsts.pageCount);//每页几条
        if(StringUtils.isNotEmpty(after)){
            varJson.putIfAbsent("after",after);
        }

        String variables = varJson.toJSONString();
        String encode = null;
        try {
            encode = URLEncoder.encode(variables, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        String moreUrl = "https://www.instagram.com/graphql/query/?"+"query_id="+queryId+"&variables="+encode;
        String moreUrl = "https://www.instagram.com/graphql/query/?"+"query_hash="+queryId+"&variables="+encode;
        System.out.println("被like的下一页:"+moreUrl);
        SchedulerContext.into(request.subRequest(moreUrl));
    }

    /***
     * follow. 分页时的请求url,放进任务队列
     * @param after
     * @param queryId
     * @param request
     */
    public static void createFollowingScheduler(String after, String queryId, HttpRequest request) {
        JSONObject varJson = new JSONObject();

        varJson.putIfAbsent("id",InsConsts.userId);//alexTODO 用户id (username---userId)
        varJson.putIfAbsent("first",InsConsts.page_follow_Count);//每页几条
        if(StringUtils.isNotEmpty(after)){
            varJson.putIfAbsent("after",after);
        }

        String variables = varJson.toJSONString();
        String encode = null;
        try {
            encode = URLEncoder.encode(variables, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        String moreUrl = "https://www.instagram.com/graphql/query/?"+"query_id="+queryId+"&variables="+encode;
        String moreUrl = "https://www.instagram.com/graphql/query/?"+"query_hash="+queryId+"&variables="+encode;
        System.out.println("following的下一页:"+moreUrl);
        SchedulerContext.into(request.subRequest(moreUrl));
    }

    /***
     * followed. 分页时的请求url,放进任务队列
     * @param after
     * @param queryId
     * @param request
     */
    public static void createFollowedScheduler(String queryEdUserId,String after, String queryId, HttpRequest request) {
        JSONObject varJson = new JSONObject();

        varJson.putIfAbsent("id",queryEdUserId);
        varJson.putIfAbsent("first",InsConsts.page_follow_Count);
        if(StringUtils.isNotEmpty(after)){
            varJson.putIfAbsent("after",after);
        }

        String variables = varJson.toJSONString();
        String encode = null;
        try {
            encode = URLEncoder.encode(variables, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        String moreUrl = "https://www.instagram.com/graphql/query/?"+"query_id="+queryId+"&variables="+encode;
        String moreUrl = "https://www.instagram.com/graphql/query/?"+"query_hash="+queryId+"&variables="+encode;

        System.out.println("followed的下一页:"+moreUrl);
        SchedulerContext.into(request.subRequest(moreUrl));
    }




    /***
     * 拼接不带after参数的?query url
     * @param queryId
     */
    public static String createInitQueryEncodedUrl(String userId, String queryId,String first) {
        JSONObject varJson = new JSONObject();

        varJson.putIfAbsent("id",userId);
        varJson.putIfAbsent("first",first);
        String variables = varJson.toJSONString();
        String encode = null;
        try {
            encode = URLEncoder.encode(variables, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
//        String url = "https://www.instagram.com/graphql/query/?"+"query_id="+queryId+"&variables="+encode;
        String url = "https://www.instagram.com/graphql/query/?"+InsConsts.query_id+"="+queryId+"&variables="+encode;

        return url;
    }

    /***
     * 拼接tag热门的?query url
     * https://www.instagram.com/graphql/query/?query_hash=298b92c8d7cad703f7565aa892ede943&amp;variables=%7B%22tag_name%22%3A%22yeezy350%22%2C%22first%22%3A4%2C%22after%22%3A%22J0HWnyu-wAAAF0HWnycuQAAAFnIA%22%7D
     * @param queryId
     */
    public static String createTagEncodedUrl(String tagName, String queryId,String first,String after) {
        JSONObject varJson = new JSONObject();

        varJson.putIfAbsent("tag_name",tagName);
        varJson.putIfAbsent("first",first);
        if(StringUtils.isNotEmpty(after)){
            varJson.putIfAbsent("after",after);
        }
        String variables = varJson.toJSONString();
        String encode = null;
        try {
            encode = URLEncoder.encode(variables, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = "https://www.instagram.com/graphql/query/?"+InsConsts.query_id+"="+queryId+"&variables="+encode;

        return url;
    }

    /***
     * 拼接tag热门的?query url
     * https://www.instagram.com/graphql/query/?query_hash=298b92c8d7cad703f7565aa892ede943&amp;variables=%7B%22tag_name%22%3A%22yeezy350%22%2C%22first%22%3A4%2C%22after%22%3A%22J0HWnyu-wAAAF0HWnycuQAAAFnIA%22%7D
     * @param queryId
     */
    public static String createTagInitEncodedUrl(String tagName, String queryId,String first) {
        return createTagEncodedUrl(tagName,queryId,first,null);
    }

    public static void createTagScheduler(String tagName,String queryId,String first, String after, HttpRequest request) {
        String moreUrl = createTagEncodedUrl(tagName,queryId,first,after);

        System.out.println("followed的下一页:"+moreUrl);
        SchedulerContext.into(request.subRequest(moreUrl));
    }

    /***
     * 解析encode的值
     * String s = "%7B%22id%22%3A%222303289858%22%2C%22first%22%3A%2250%22%7D";
     * @param s
     * @return
     */
    public static String getFromEncode(String s) {
        try {
            String decode = URLDecoder.decode(s, "utf-8");
            Object json = JSON.parse(decode);
            JSONObject varJson = (JSONObject)json;
            return (String)varJson.get("id");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Instagram getInstagram() {
        return instagram;
    }

    /***
     * 根据用户名，得到账户对象（里面含有粉丝数量等信息）
     * Account account = getInstagramAccountByName();
     System.out.println(account.getFollows());//关注的数量

     System.out.println(account.getFollowedBy());//粉丝数量
     System.out.println(account.getMedia().getCount());//发帖数量
     */
    public  static Account getInstagramAccountByName(String username){
        Account account = null;
        try {
            account = instagram.getAccountByUsername(username);
        } catch (Exception e) {
            System.out.println("未找到用户："+username);
        }

        return account;
    }

    public  static Account getInstagramAccountById(long userId){
        Account account = null;
        try {
            account = instagram.getAccountById(userId);
        } catch (Exception e) {
            System.out.println("未找到用户："+userId);
        }

        return account;
    }

    public static long getUserIdByUsername(String username){
        long userId =0;
        try {
            userId = InsUtil.getInstagram().getAccountByUsername(username).getId();
        } catch (IOException e) {
            System.out.println("获取userId报错了");
        }
        return userId;
    }


    /***
     * 获得一个用户的照片列表 只会给出第一页
     *
     * @param account
     */
    public static List<Media> getInstagramMediaList(Account account){
        PageObject<Media> mediaPaged = account.getMedia();
        System.out.println(mediaPaged.getCount());//总共多少
        List<Media> nodes = mediaPaged.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            Media media = nodes.get(i);
            System.out.println(media.getDisplayUrl());
        }
        return nodes;
    }


    /***
     *
     * @throws IOException
     */
    public  static  Tag getTag(String name)  {

        Tag tag = null;
        try {
            tag = instagram.getTagByName(name);
        } catch (Exception e) {
            System.out.println("获取tag出错了:"+name);
        }
        return tag;

    }

    /***
     *
     * @throws IOException
     */
    public  static  Tag getTag(String name,int howManyPages)  {

        Tag tag = null;
        try {
            tag = instagram.getMediasByTag(name, howManyPages);
        } catch (Exception e) {
            System.out.println("获取tag出错了:"+name);
        }
        return tag;

    }
    /***
     * 某个tag的前100个左右的url链接
     * @param weedTag
     * @throws IOException
     */
    public  static  List<String> getPicUrls(Tag weedTag) throws IOException {

        if(weedTag==null){
            return new ArrayList<String>();
        }
        Integer count = weedTag.getCount();
        System.out.println(weedTag.getName()+"总数量："+count+"(不是本次都处理的)");

        MediaRating mediaRating = weedTag.getMediaRating();
        PageObject<Media> media = mediaRating.getMedia();

        List<Media> nodes = media.getNodes();
        List<String> list = new ArrayList<String>();
        for (Media node : nodes) {
            Account owner = node.getOwner();
//            Account owner2 = instagram.getAccountById(owner.getId());
            String username  =owner.getId()+"";//owner2.getUsername();
            //taken-by后面可以跟id也可以username
            String picUrl = "https://www.instagram.com/p/"+node.getShortcode()+"/?taken-by="+username;
            list.add(picUrl);
        }
        return list;
    }

    /***
     * 发了某hottag的用户，保存下来
     * @param weedTag
     * @return
     * @throws IOException
     */
    public  static  List<Account> getAllUserByHotTag(Tag weedTag) throws IOException {
        if(weedTag==null){
            return new ArrayList<Account>();
        }
        Integer count = weedTag.getCount();
        System.out.println(weedTag.getName()+"总数量："+count+"(不是本次都处理的)");

        MediaRating mediaRating = weedTag.getMediaRating();
        PageObject<Media> media = mediaRating.getMedia();

        List<Media> nodes = media.getNodes();
        List<Account> list = new ArrayList<Account>();
        for (Media node : nodes) {
            Account owner = node.getOwner();

            Account owner2 = getInstagramAccountById(owner.getId());
            if(owner2==null){
                System.out.println("未找到用户："+owner.getId());
            }else{
                list.add(owner2);
            }
        }
        return list;
    }


    /***
     *
     * @throws IOException
     */
    public  static  List<String> getPicUrls(String name,int howManyPages)  {

        try {
            return getPicUrls(getTag(name,howManyPages));
        } catch (Exception e) {
            System.out.println("获取tag的url出错");
        }

        return new ArrayList<String>();
    }


    public static void main(String[] args) throws IOException {
        Account account = getInstagramAccountByName("bigbong77777");
//        Account account = null;
//        try {
//            account = instagram.getAccountById(6738634545l);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println(account.getId());
//        System.out.println(account.getFollows());//关注的数量

//        account = instagram.getAccountById(6738634545l);
        account = instagram.getAccountById(6979000042l);
//        account = instagram.getAccountById(account.getId());
        System.out.println(account.getUsername());
//
//        System.out.println(account.getFollowedBy());//粉丝数量
//        System.out.println(account.getMedia().getCount());//发帖数量
//
//        System.out.println(account.getFollowedByViewer());//是否被我关注
//        System.out.println(account.getId());//id
//        System.out.println(account.getIsPrivate());//是否为私有
//        System.out.println(account.getLastUpdated());//最后更新日期
//        System.out.println(account.getMedia());//
//        getInstagramMediaList(account);

//        List<String> picUrls1 = InsUtil.getPicUrls("美女", InsConsts.tag_howManyPages);
//        System.out.println(picUrls1.size());
//        Tag tag = getTag("飞行中国", 1);
//        System.out.println(tag.getCount());
    }
}
