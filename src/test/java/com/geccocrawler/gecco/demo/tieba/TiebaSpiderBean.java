package com.geccocrawler.gecco.demo.tieba;

import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.demo.ins.MongoUtil;
import com.geccocrawler.gecco.local.FileUtil;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.scheduler.SchedulerContext;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.utils.CommonUtil;

import java.io.IOException;
import java.util.List;

/***
 * 处理
 * http://jandan.net/ooxx
 *
 * selector :#comment-3647449 > div > div > div.text > p > img
 * 下载器:htmlUnitDownloder 暂时没调通
 *
 *
 */
@PipelineName("TiebaSpiderBean")
//@Gecco(matchUrl="https://tieba.baidu.com/p/5494054484?pn={pageNo}",pipelines={"TiebaSpiderBean"}, downloader="chromeCdp4jDownloader")
@Gecco(matchUrl="https://tieba.baidu.com/p/{tieId}?pn={pageNo}",pipelines={"TiebaSpiderBean"})
public class TiebaSpiderBean implements HtmlBean, Pipeline<TiebaSpiderBean> {


    private static final long serialVersionUID = -7127412585200611132L;

    @Request
    private HttpRequest request;

    //帖子id
    @RequestParameter("tieId")
    private String tieId;

    @RequestParameter("pageNo")
    private String pageNo;


    //总页数  eq的索引从0开始
    @Text
    @HtmlField(cssPath="li.l_reply_num>span:eq(1)")
//    @HtmlField(cssPath="li.l_reply_num>span(2)")   直接用（2）不好使
    private String totalPageNum;


    //回复内容
    @Text
    @HtmlField(cssPath="div[id*=post_content]")
    public List<String> postContent;

    public String getTieId() {
        return tieId;
    }

    public void setTieId(String tieId) {
        this.tieId = tieId;
    }

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

    public String getTotalPageNum() {
        return totalPageNum;
    }

    public void setTotalPageNum(String totalPageNum) {
        this.totalPageNum = totalPageNum;
    }

    public List<String> getPostContent() {
        return postContent;
    }

    public void setPostContent(List<String> postContent) {
        this.postContent = postContent;
    }

    /***
     * 对于抓取到的数据的处理
     */
    @Override
    public void process(TiebaSpiderBean tiebaSpiderBean) {

        System.out.println("正在处理第几页："+tiebaSpiderBean.getPageNo());
        List<String> postContentLit = tiebaSpiderBean.getPostContent();
        for (String postContent : postContentLit) {
            System.out.println(postContent);
            List<String> usernames = CommonUtil.getUsername(postContent);
            if(usernames.size()>0){
                String username = usernames.get(0);
                System.out.println("提取到的用户名："+username);
                MongoUtil.getMongoDBJDBC().save2Coll("username",username,"tem");
            }
        }

        String pageNo = tiebaSpiderBean.getPageNo();
        Integer pageNoInt = Integer.valueOf(pageNo);
        totalPageNum = tiebaSpiderBean.getTotalPageNum();

        if(pageNo!=null && pageNoInt<Integer.valueOf(totalPageNum)){
            String newUrl = TiebaCons.baseUrl+"/p/"+tiebaSpiderBean.getTieId()+"?pn="+(pageNoInt+1);
            SchedulerContext.into(tiebaSpiderBean.getRequest().subRequest(newUrl));
        }else{
            System.out.println("已经处理完所有");
        }
    }

    public static void main(String[] args) {
        MongoUtil.getMongoDBJDBC().deleleColl("tem");
        GeccoEngine.create()
                //.pipelineFactory(springPipelineFactory)
                .classpath("com.geccocrawler.gecco.demo.tieba")
                .start("https://tieba.baidu.com/p/5494054484?pn=1")
                .interval(3000)
                .start();


    }
}
