package com.geccocrawler.gecco.demo.dm;

import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.local.FileUtil;
import com.geccocrawler.gecco.local.GsonUtil;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

/***
 * dm 单条记录
 *
 *
 */
@PipelineName("dmTopicSpiderBean")
@Gecco(matchUrl = "https://www.reddit.com/r/feixingcn/comments/{part1}/{part2}/", pipelines = "dmTopicSpiderBean",downloader="chromeCdp4jDownloader")
public class DmTopicSpiderBean implements HtmlBean, Pipeline<DmTopicSpiderBean> {


    private static final long serialVersionUID = -7127412585200687225L;

    private static int pageCount=0;
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

    //发帖内容. div里面会有多个p,每个的内容拼起来就是完整的
    //注意这个选择器有些复杂,中间的逗号分隔了两个选择器.
    @Text
    @HtmlField(cssPath = "div.content form:eq(0)> div > div > p,div.content form:eq(0)> div > div > p>a")
    public List<String> messagePText;


    //回复人 #thing_t1_d54riw7 > div.entry.unvoted > p > a.author.submitter.may-blank.id-t2_t3d3c
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

    public List<String> getMessagePText() {
        return messagePText;
    }

    public void setMessagePText(List<String> messagePText) {
        this.messagePText = messagePText;
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
    public void process(DmTopicSpiderBean dmSpiderBean) {
        String title = dmSpiderBean.getTitle();
        String auth = dmSpiderBean.getAuth();
        List<String> messagePText = dmSpiderBean.getMessagePText();
        StringBuffer sb = new StringBuffer();
        if(messagePText!=null){
            for (String message : messagePText) {
                sb.append(message).append("\n");
            }
        }

        System.out.println("发帖人:"+auth+"  --title:"+title+" --message:"+sb.toString());
        BbsThread bbsThread = new BbsThread(title,auth,sb.toString());//alexTODO messagePText 放上

        List<String> replyerList = dmSpiderBean.getReplyerList();
        List<String> replyList = dmSpiderBean.getReplyList();
        for (int i = 0; i < replyerList.size(); i++) {
            System.out.println(replyerList.get(i));
            System.out.println(replyList.get(i));
            System.out.println("------");
            BbsPost post=new BbsPost(replyerList.get(i),replyList.get(i));
            bbsThread.getPostList().add(post);
        }

        pageCount++;
        Gson gson = GsonUtil.getInstance().getGson();
        String jsonString = gson.toJson(bbsThread);
        try {
            System.out.println("正在处理的页数:"+pageCount);
            FileUtil.writeFileByFileWriterAdd("/Users/wangany/tem/spider/reddit-dm-json"+pageCount+".txt",jsonString);
        } catch (IOException e) {
            e.printStackTrace();
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
//                .start("https://www.reddit.com/r/feixingcn/comments/4rwun0/%E6%96%B0%E8%AE%BA%E5%9D%9Bwwwweedgtcom/")
                .start("https://www.reddit.com/r/feixingcn/comments/5ai3x7/%E8%AE%BA%E5%9D%9B%E5%8F%88%E8%A2%AB%E7%82%B9%E4%BA%86%E5%94%89/")
                .interval(3000)
                .start();
    }
}
