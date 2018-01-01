package com.geccocrawler.gecco.demo.ins;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.scheduler.SchedulerContext;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.utils.JavaScriptUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/***
 * dm 单个用户的所有记录
 *
 *
 */
@PipelineName("InsMorePicJsSpriderBean")
@Gecco(matchUrl = "https://www.instagram.com/graphql/query/?query_id={queryId}&variables={variables}", pipelines = "InsMorePicJsSpriderBean",downloader="chromeCdp4jDownloader")
public class InsMorePicJsSpriderBean implements HtmlBean, Pipeline<InsMorePicJsSpriderBean> {


    private static final long serialVersionUID = -712741258524433435L;

    @Request
    private HttpRequest request;

    @RequestParameter
    private String queryId;

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
    public void process(InsMorePicJsSpriderBean dmSpiderBean)  {
        String all = dmSpiderBean.getAll();
        Object allJsonObject = JSONObject.parse(all);
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
        }
        if(hasNextPage){
            String after = (String)com.alibaba.fastjson.JSONPath.eval(allJsonObject,"$.data.user.edge_owner_to_timeline_media.page_info.end_cursor");
            JSONObject varJson = new JSONObject();
            varJson.putIfAbsent("id",userId);
            varJson.putIfAbsent("first","12");
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

    public static void main(String[] args) {
        GeccoEngine.create()
                .classpath("com.geccocrawler.gecco.demo.ins")
                .start("https://www.instagram.com/graphql/query/?query_id=17845312237175864&variables=%7B%22id%22%3A%223865704649%22%2C%22after%22%3A%22AQCXCMgt8EDny-RIks_aF8Pl3XqSQBCvkfYa2GR0-LeLFxUqoSQCEWUxwzc9y5YAqR_ihXLpK6WYeCqrYdyZxZXQPS67Nup8Ukmjak4pxxACTg%22%2C%22first%22%3A%2212%22%7D")
                .interval(3000)
                .start();
    }
}