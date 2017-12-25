package com.geccocrawler.gecco.demo.dm;

import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.local.FileUtil;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.spider.HtmlBean;

import java.io.IOException;
import java.util.List;

/***
 * 分页
 * dm相关信息的收集
 * https://www.reddit.com/r/feixingcn/
 */
@PipelineName("dmListSpiderBean")
@Gecco(matchUrl="https://www.reddit.com/r/feixingcn/?count={count}&after={after}",pipelines={"dmListSpiderBean","dmListPipeline"}, downloader="chromeCdp4jDownloader")
public class DmListSpiderBean implements HtmlBean, Pipeline<DmListSpiderBean> {


    private static final long serialVersionUID = -7127412585200687232L;

    @Request
    private HttpRequest request;


    //标题 所有的
    @Text
    @HtmlField(cssPath=" div.entry.unvoted > div.top-matter > p.title > a")
    private List<String> titleList;

    @Href
    @HtmlField(cssPath="#siteTable > div.nav-buttons > span > span.next-button > a")
    private String nextPageurl;

    //每条记录,它的进一步链接url list
    @Href
    @HtmlField(cssPath="div.content div.entry.unvoted > div.top-matter > p.title > a")
    private List<String> urlList;

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public List<String> getTitleList() {
        return titleList;
    }

    public void setTitleList(List<String> titleList) {
        this.titleList = titleList;
    }

    public String getNextPageurl() {
        return nextPageurl;
    }

    public void setNextPageurl(String nextPageurl) {
        this.nextPageurl = nextPageurl;
    }

    public List<String> getUrlList() {
        return urlList;
    }

    public void setUrlList(List<String> urlList) {
        this.urlList = urlList;
    }

    /***
     * 对于抓取到的数据的处理
     * @param sexweb1SpiderBean
     */
    @Override
    public void process(DmListSpiderBean sexweb1SpiderBean) {
        List<String> titleList = sexweb1SpiderBean.getTitleList();
        List<String> urlList = sexweb1SpiderBean.getUrlList();
//        StringBuilder stringBuilderWithLink = new StringBuilder();
//        StringBuilder stringBuilder = new StringBuilder();
        for (String url : urlList) {
            System.out.println("单条记录的进一步链接:"+url);
        }
        for (int i = 0; i < titleList.size(); i++) {
            System.out.println(titleList.get(i));
//            stringBuilderWithLink.append("<a href='"+pics.get(i)+"' target='_blank'><img src='"+littlePics.get(i)+"'></img></a>").append("<br>");
//            stringBuilderWithLink.append("<a href='"+titleList.get(i)+"' target='_blank'>"+titleList.get(i)+"</a>").append("<br>");
//            stringBuilder.append(titleList.get(i)).append("\n");
        }
//        try {
//            FileUtil.writeFileByFileWriterAdd("/Users/wangany/tem/spider/jiandanxxooWithlink5.html",stringBuilderWithLink.toString());
//            FileUtil.writeFileByFileWriterAdd("/Users/wangany/tem/spider/jiandanxxoo5.txt",stringBuilder.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public static void main(String[] args) {
        GeccoEngine.create()
                //.pipelineFactory(springPipelineFactory)
                .classpath("com.geccocrawler.gecco.demo.dm")
                .start("https://www.reddit.com/r/feixingcn/?count=25&after=t3_5gnvbw")
                .interval(3000)
                .start();


    }
}
