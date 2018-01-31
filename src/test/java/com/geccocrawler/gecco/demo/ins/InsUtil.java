package com.geccocrawler.gecco.demo.ins;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.scheduler.SchedulerContext;
import io.webfolder.cdp.CdpPubUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nobody on 2018/1/2.
 */
public class InsUtil {
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
                    String jsContent = CdpPubUtil.getInstance().getHtml(jsUrl, 10);//HttpClientUtil.httpPure(jsUrl);//代理,否则访问不了
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
}
