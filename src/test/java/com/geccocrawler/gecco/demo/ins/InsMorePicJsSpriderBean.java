package com.geccocrawler.gecco.demo.ins;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.utils.JavaScriptUtil;
import org.apache.commons.lang3.StringUtils;

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
@Gecco(matchUrl = "https://www.instagram.com/static/bundles/ConsumerCommons.js/{theone}.js", pipelines = "InsMorePicJsSpriderBean",downloader="chromeCdp4jDownloader")
public class InsMorePicJsSpriderBean implements HtmlBean, Pipeline<InsMorePicJsSpriderBean> {


    private static final long serialVersionUID = -7127412585200687235L;

    @Request
    private HttpRequest request;


    @HtmlField(cssPath="*")
    private String all;

    public String getAll() {
        return all;
    }

    public void setAll(String all) {
        this.all = all;
    }

    @Override
    public void process(InsMorePicJsSpriderBean dmSpiderBean) {
        String all = dmSpiderBean.getAll();
        String patternString = "queryId:\\\"(\\d*)?\\\"";
        Matcher m = Pattern.compile(patternString).matcher(all);
        List<String> list = new ArrayList<String>();
        while (m.find()){
            //从头开始一直找,并打印找到的字符串
            list.add(m.group());
        }
       //queryId:"17895776530086866"
        String str = list.get(list.size()-1);
        String queryId = str.substring("queryId".length()+2,str.length()-1);
        System.out.println(queryId);

    }

    public static void main(String[] args) {
        GeccoEngine.create()
                .classpath("com.geccocrawler.gecco.demo.ins")
                .start("https://www.instagram.com/static/bundles/ConsumerCommons.js/0bc5c9fbbbcd.js")
                .interval(3000)
                .start();
    }
}
