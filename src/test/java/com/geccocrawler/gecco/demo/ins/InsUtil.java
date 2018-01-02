package com.geccocrawler.gecco.demo.ins;

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
            String queryId = str.substring("queryId".length()+2,str.length()-1);
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
//        String patternString = "p=\\\"(\\d{10,})\\\"";//p=17263623232  数字 至少10次
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
}
