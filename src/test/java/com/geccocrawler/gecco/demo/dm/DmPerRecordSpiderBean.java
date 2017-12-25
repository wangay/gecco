package com.geccocrawler.gecco.demo.dm;

import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.spider.HtmlBean;

import java.util.List;

/***
 * dm 单条记录
 *
 *
 */
@PipelineName("dmPerRecordSpiderBean")
@Gecco(matchUrl = "https://www.reddit.com/r/feixingcn/comments/{part1}/{part2}/", pipelines = "dmPerRecordSpiderBean",downloader="chromeCdp4jDownloader")
public class DmPerRecordSpiderBean implements HtmlBean, Pipeline<DmPerRecordSpiderBean> {


    private static final long serialVersionUID = -7127412585200687225L;

    @Request
    private HttpRequest request;

    @RequestParameter("part1")
    private String part1;

    @RequestParameter("part2")
    private String part2;


    //发帖标题
    @Text
    @HtmlField(cssPath = " div.entry.unvoted > div.top-matter > p.title > a")
    private String title;

    //发帖人
    @Text
    @HtmlField(cssPath = " div.entry.unvoted > div.top-matter > p.tagline > a")
    private String auth;


    //回复人 #thing_t1_d54riw7 > div.entry.unvoted > p > a.author.submitter.may-blank.id-t2_t3d3c
    //siteTable_t3_4rwun0
    //#thing_t1_d56hmk9 > div.entry.unvoted > p > a.author.may-blank.id-t2_ty1ha
    @Text
    @HtmlField(cssPath = "div.commentarea  div.entry.unvoted > p > a.author.may-blank")
    public List<String> replyerList;

    //回复的帖子 #form-t1_d54riw7s92 > div > div > p
    @Text
    @HtmlField(cssPath = "div.commentarea form > div > div > p")
    public List<String> replyList;

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public String getPart1() {
        return part1;
    }

    public void setPart1(String part1) {
        this.part1 = part1;
    }

    public String getPart2() {
        return part2;
    }

    public void setPart2(String part2) {
        this.part2 = part2;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getReplyerList() {
        return replyerList;
    }

    public void setReplyerList(List<String> replyerList) {
        this.replyerList = replyerList;
    }

    public List<String> getReplyList() {
        return replyList;
    }

    public void setReplyList(List<String> replyList) {
        this.replyList = replyList;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    @Override
    public void process(DmPerRecordSpiderBean dmSpiderBean) {
        String title = dmSpiderBean.getTitle();
        String auth = dmSpiderBean.getAuth();
        System.out.println("发帖人:"+auth+"  --title:"+title);


        List<String> replyerList = dmSpiderBean.getReplyerList();
        List<String> replyList = dmSpiderBean.getReplyList();
        for (int i = 0; i < replyerList.size(); i++) {
            System.out.println(replyerList.get(i));
            System.out.println(replyList.get(i));
            System.out.println("------");
        }

    }

    /***
     * 单个连接测试.
     * 实际是被探测到的url,放入队列被执行.
     * @param args
     */
    public static void main(String[] args) {
        GeccoEngine.create()
                .classpath("com.geccocrawler.gecco.demo.dm")
                .start("https://www.reddit.com/r/feixingcn/comments/4rwun0/%E6%96%B0%E8%AE%BA%E5%9D%9Bwwwweedgtcom/")
                .interval(3000)
                .start();
    }
}
