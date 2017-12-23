package com.geccocrawler.gecco.demo.sexsite;

import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.spider.HtmlBean;

import java.util.List;

/***
 * 处理
 * http://jandan.net/ooxx
 *
 * selector :#comment-3647449 > div > div > div.text > p > img
 * 下载器:htmlUnitDownloder 暂时没调通
 */
@PipelineName("sexwebListSpiderBean")
//@Gecco(matchUrl="http://jandan.net/ooxx/page-{pageNo}",pipelines={"sexwebListSpiderBean"})
@Gecco(matchUrl="http://jandan.net/ooxx/page-{pageNo}",pipelines={"sexwebListSpiderBean"}, downloader="chromeCdp4jDownloader")
public class SexwebListSpiderBean implements HtmlBean, Pipeline<SexwebListSpiderBean> {


    private static final long serialVersionUID = -7127412585200687232L;

    @Request
    private HttpRequest request;

    @RequestParameter("pageNo")
    private String pageNo;



    //抓取图片的src地址.
    // 图片的src的List.
    @Image("org_src")
//    @Image({"org_src", "src"})
    @HtmlField(cssPath="div > div.text > p > img")
    public List<String> pics;

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public String getPageNo() {
        return pageNo;
    }

    public void setPageNo(String pageNo) {
        this.pageNo = pageNo;
    }

    public List<String> getPics() {
        return pics;
    }

    public void setPics(List<String> pics) {
        this.pics = pics;
    }

    @Override
    public void process(SexwebListSpiderBean sexweb1SpiderBean) {
        List<String> pics = sexweb1SpiderBean.getPics();
        for (int i = 0; i < pics.size(); i++) {
            System.out.println(pics.get(i));
        }
    }

    public static void main(String[] args) {
        GeccoEngine.create()
                //.pipelineFactory(springPipelineFactory)
                .classpath("com.geccocrawler.gecco.demo.sexsite")
//                .start("http://jandan.net/ooxx/page-393#comments")
                .start("http://jandan.net/ooxx/page-393")
                .interval(3000)
                .start();


    }
}
