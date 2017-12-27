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
@Gecco(matchUrl="https://www.reddit.com/r/feixingcn/?count={count}&{others}",pipelines={"dmListSpiderBean","dmListPipeline"}, downloader="chromeCdp4jDownloader")
public class DmListSpiderBean implements HtmlBean, Pipeline<DmListSpiderBean> {


    private static final long serialVersionUID = -7127412585200687232L;

    @Request
    private HttpRequest request;


    //请求request url的当前分页数
    @RequestParameter("count")
    private String count;

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

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    /***
     * 对于抓取到的数据的处理
     * @param sexweb1SpiderBean
     */
    @Override
    public void process(DmListSpiderBean sexweb1SpiderBean) {
        List<String> titleList = sexweb1SpiderBean.getTitleList();
        System.out.println("====下载完毕的列表页的当前页为:"+sexweb1SpiderBean.getCount()+" url:"+sexweb1SpiderBean.getRequest().getUrl()
                +" 本列表获取得到的内容如下:");
        List<String> urlList = sexweb1SpiderBean.getUrlList();
        for (int i = 0; i < titleList.size(); i++) {
            System.out.println(titleList.get(i)+" 连接:"+urlList.get(i));
            //记录在本地文件.
            try {
                //FileUtil.writeFileByFileWriterAdd("/Users/wangany/tem/spider/reddit-dm.html",titleList.get(i)+" 连接:"+urlList.get(i));
                FileUtil.writeFileByFileWriterAdd("/Users/wangany/tem/spider/reddit-dm.txt",titleList.get(i)+" 连接:"+urlList.get(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        GeccoEngine.create()
                //.pipelineFactory(springPipelineFactory)
                .classpath("com.geccocrawler.gecco.demo.dm")
                .start("https://www.reddit.com/r/feixingcn/?count=26&before=t3_5eikyr")
                .interval(3000)
                .start();


    }
}
