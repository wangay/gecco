package com.geccocrawler.gecco.demo.sexsite;

import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.local.FileUtil;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.spider.HtmlBean;

import java.io.IOException;
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
@Gecco(matchUrl="http://jandan.net/ooxx/page-{pageNo}",pipelines={"sexwebListSpiderBean","sexwebListPipeline"}, downloader="chromeCdp4jDownloader")
public class SexwebListSpiderBean implements HtmlBean, Pipeline<SexwebListSpiderBean> {


    private static final long serialVersionUID = -7127412585200687232L;

    @Request
    private HttpRequest request;

    @RequestParameter("pageNo")
    private String pageNo;

    /**
     * 获得当前页
     * #comments > div:nth-child(4) > div > span
     */
    @Text
//    @HtmlField(cssPath="div:#comments > div:nth-child(4) > div > span")
    @HtmlField(cssPath="div.comments > div.cp-pagenavi > span.current-comment-page")
    private String currPageText;


    /***
     * list的图片,好像还不能直接download
     * 原始大图
     */
    //抓取图片的src地址.
    // 图片的src的List.

    @Image(value="org_src")
//    @Image(value="org_src",download="~/tem/gecco/xxoo/img")
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

    public String getCurrPageText() {
        return currPageText;
    }

    public void setCurrPageText(String currPageText) {
        this.currPageText = currPageText;
    }


    /***
     * 对于抓取到的数据的处理
     * @param sexweb1SpiderBean
     */
    @Override
    public void process(SexwebListSpiderBean sexweb1SpiderBean) {
        List<String> pics = sexweb1SpiderBean.getPics();
        System.out.println("正在处理的page:"+sexweb1SpiderBean.getCurrPageText());
        StringBuilder stringBuilderWithLink = new StringBuilder();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < pics.size(); i++) {
            System.out.println(pics.get(i));
//            stringBuilderWithLink.append("<a href='"+pics.get(i)+"' target='_blank'><img src='"+littlePics.get(i)+"'></img></a>").append("<br>");
            stringBuilderWithLink.append("<a href='"+pics.get(i)+"' target='_blank'>"+pics.get(i)+"</a>").append("<br>");
            stringBuilder.append(pics.get(i)).append("\n");
        }
        try {
            FileUtil.writeFileByFileWriterAdd("/Users/wangany/tem/spider/jiandanxxooWithlink5.html",stringBuilderWithLink.toString());
            FileUtil.writeFileByFileWriterAdd("/Users/wangany/tem/spider/jiandanxxoo5.txt",stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GeccoEngine.create()
                //.pipelineFactory(springPipelineFactory)
                .classpath("com.geccocrawler.gecco.demo.sexsite")
//                .start("http://jandan.net/ooxx/page-393#comments")
//                .start("http://jandan.net/ooxx/page-393")
                .start("http://jandan.net/ooxx/page-1")
                .interval(3000)
                .start();


    }
}
