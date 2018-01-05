package com.geccocrawler.gecco.demo.ins;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.local.FileUtil;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.scheduler.SchedulerContext;
import com.geccocrawler.gecco.spider.HtmlBean;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/***
 * 通过queryId的形式请求的内容 包括:
 * 单个用户的所有记录,或者被喜欢的情况
 * 他们请求的js ajax连接,都是同一个模式,都在这个判断处理
 *
 */
@PipelineName("InsByQueryIdSpriderBean")
@Gecco(matchUrl = "https://www.instagram.com/graphql/query/?query_id={queryId}&variables={variables}", pipelines = "InsByQueryIdSpriderBean",downloader="chromeCdp4jDownloader")
public class InsByQueryIdSpriderBean implements HtmlBean, Pipeline<InsByQueryIdSpriderBean> {


    private static final long serialVersionUID = -712741258524433435L;

    @Request
    private HttpRequest request;

    @RequestParameter
    private String queryId;

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
    @HtmlField(cssPath="pre")
    private String all;

    public String getAll() {
        return all;
    }

    public void setAll(String all) {
        this.all = all;
    }


    @Override
    public void process(InsByQueryIdSpriderBean dmSpiderBean)  {
        String all = dmSpiderBean.getAll();
        if(all.contains("edge_liked_by")){
            //like的请求
            this.processLikes(dmSpiderBean);
        }else{
            this.processUserRecords(dmSpiderBean);
        }

    }

    /***
     * 处理用户图片记录
     * @param dmSpiderBean
     */
    private void processUserRecords(InsByQueryIdSpriderBean dmSpiderBean)  {
        Object allJsonObject = JSONObject.parse(dmSpiderBean.getAll());
        String selector = "$.data.user.edge_owner_to_timeline_media.edges";
        String selectorHasNextPage = "$.data.user.edge_owner_to_timeline_media.page_info.has_next_page";
        JSONArray imgArr = (JSONArray)com.alibaba.fastjson.JSONPath.eval(allJsonObject, selector);
        Boolean hasNextPage = (Boolean)com.alibaba.fastjson.JSONPath.eval(allJsonObject, selectorHasNextPage);
        String userId=null;
        if(imgArr==null){
            return;
        }
        for (Object o : imgArr) {
            JSONObject imgJson = (JSONObject)o;
            String url = (String)com.alibaba.fastjson.JSONPath.eval(imgJson,"$.node.display_url");
            userId = (String)com.alibaba.fastjson.JSONPath.eval(imgJson,"$.node.owner.id");
            System.out.println(url);
            if(InsConsts.saveLinksLocal){
                //本地保存照片
                try {
                    FileUtil.writeFileByFileWriterAdd(InsConsts.pic_local_position,url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(hasNextPage){
            String after = (String)com.alibaba.fastjson.JSONPath.eval(allJsonObject,"$.data.user.edge_owner_to_timeline_media.page_info.end_cursor");
            JSONObject varJson = new JSONObject();
            varJson.putIfAbsent("id",userId);
            varJson.putIfAbsent("first","50");
            varJson.putIfAbsent("after",after);
            String variables = varJson.toJSONString();
            String encode = null;
            try {
                encode = URLEncoder.encode(variables, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String moreUrl = "https://www.instagram.com/graphql/query/?"+"query_id="+dmSpiderBean.getQueryId()+"&variables="+encode;
            System.out.println("下一页:"+moreUrl);
            SchedulerContext.into(dmSpiderBean.getRequest().subRequest(moreUrl));
        }

    }

    /***
     * 处理用户被like的情况
     * 样例数据:ins-img-like.txt
     * @param dmSpiderBean
     */
    private void processLikes(InsByQueryIdSpriderBean dmSpiderBean)  {
        Object allJsonObject = JSONObject.parse(dmSpiderBean.getAll());
        String selector = "$.data.shortcode_media.edge_liked_by.edges";
        String selectorHasNextPage = "$.data.shortcode_media.edge_liked_by.page_info.has_next_page";
        String afterSelector = "$.data.shortcode_media.edge_liked_by.page_info.end_cursor";
        JSONArray likesArr = (JSONArray)com.alibaba.fastjson.JSONPath.eval(allJsonObject, selector);
        Boolean hasNextPage = (Boolean)com.alibaba.fastjson.JSONPath.eval(allJsonObject, selectorHasNextPage);
        if(likesArr==null){
            return;
        }
        System.out.println("被如下人like:");
        for (Object o : likesArr) {
            JSONObject likeJson = (JSONObject)o;
            String likingUserName = (String)com.alibaba.fastjson.JSONPath.eval(likeJson,"$.node.username");
            System.out.println(likingUserName);

            if(InsConsts.likingUserNameSaved){
                try {
                    FileUtil.writeFileByFileWriterAdd("/Users/wangany/tem/spider/ins-cl-like.txt",likingUserName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        if(hasNextPage){
            String after = (String)com.alibaba.fastjson.JSONPath.eval(allJsonObject,afterSelector);
            String shortcode = (String)com.alibaba.fastjson.JSONPath.eval(allJsonObject,"$.data.shortcode_media.shortcode");
            InsUtil.createLikeRecordScheduler(shortcode,after,dmSpiderBean.getQueryId(),dmSpiderBean.getRequest());
        }

    }

    public static void main(String[] args) {
        GeccoEngine.create()
                .classpath("com.geccocrawler.gecco.demo.ins")
//                .start("https://www.instagram.com/graphql/query/?query_id=17845312237175864&variables=%7B%22id%22%3A%223865704649%22%2C%22after%22%3A%22AQCXCMgt8EDny-RIks_aF8Pl3XqSQBCvkfYa2GR0-LeLFxUqoSQCEWUxwzc9y5YAqR_ihXLpK6WYeCqrYdyZxZXQPS67Nup8Ukmjak4pxxACTg%22%2C%22first%22%3A%2212%22%7D")
                .start("https://www.instagram.com/graphql/query/?query_id=17864450716183058&variables=%7B%22shortcode%22%3A%22BYawx7JA5JK%22%2C%22first%22%3A%2220%22%7D")
                .interval(3000)
                .start();
    }
}
