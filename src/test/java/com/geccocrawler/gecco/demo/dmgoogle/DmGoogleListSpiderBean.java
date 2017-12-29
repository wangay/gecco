package com.geccocrawler.gecco.demo.dmgoogle;

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
@PipelineName("dmGoogleListSpiderBean")
@Gecco(matchUrl="https://plus.google.com/communities/113052358002859876370",pipelines={"dmGoogleListSpiderBean","dmGoogleListPipeline"}, downloader="chromeCdp4jDownloader")
//@Gecco(matchUrl="https://plus.google.com/communities/113052358002859876370",pipelines={"dmGoogleListSpiderBean","dmGoogleListPipeline"})
public class DmGoogleListSpiderBean implements HtmlBean, Pipeline<DmGoogleListSpiderBean> {


    private static final long serialVersionUID = -7127412585200687288L;

    @Request
    private HttpRequest request;



    //标题 所有的 #body\3a i54 > div > div > div
    // #body\3a i25 > div > div > div

    @Text
//    @HtmlField(cssPath="div[data-cai] > div > div > div.wftCae")
    @HtmlField(cssPath="div[data-cai]  div.wftCae")
    private List<String> titleList;

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

    /***
     * 对于抓取到的数据的处理
     * @param sexweb1SpiderBean
     */
    @Override
    public void process(DmGoogleListSpiderBean sexweb1SpiderBean) {
        List<String> titleList = sexweb1SpiderBean.getTitleList();
        System.out.println(" 本列表获取得到的内容如下:");
        for (int i = 0; i < titleList.size(); i++) {
            System.out.println(titleList.get(i));
            System.out.println("=============");
            //记录在本地文件.
//            try {
//                FileUtil.writeFileByFileWriterAdd("/Users/wangany/tem/spider/google-dm.txt",titleList.get(i));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

    }

    public static void main(String[] args) {
        GeccoEngine.create()
                //.pipelineFactory(springPipelineFactory)
                .classpath("com.geccocrawler.gecco.demo.dmgoogle")
                .start("https://plus.google.com/communities/113052358002859876370")
                .interval(3000)
                .start();


    }
}
