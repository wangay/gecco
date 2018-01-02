package com.geccocrawler.gecco.demo.ins;

import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.scheduler.SchedulerContext;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.utils.JavaScriptUtil;
import io.webfolder.cdp.CdpPubUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/***
 * dm 单条照片记录. https://www.instagram.com/p/Bc3MVJdjTjd/?taken-by=neymarjr 这样的一个页面
 *,downloader="chromeCdp4jDownloader"
 *
 */
@PipelineName("InsRecordSpiderBean")
@Gecco(matchUrl = "https://www.instagram.com/p/{shortcode}/?taken-by={username}", pipelines = "InsRecordSpiderBean",downloader="chromeCdp4jDownloader")
public class InsRecordSpiderBean implements HtmlBean, Pipeline<InsRecordSpiderBean> {


    private static final long serialVersionUID = -7127412585200687225L;

    @Request
    private HttpRequest request;

    @RequestParameter("shortcode")
    private String shortcode;

    @RequestParameter("username")
    private String username;

    @Attr(value="src")
    @HtmlField(cssPath = "script[crossorigin=anonymous]")
    public List<String> picMoreScript;


    //发帖标题 #react-root > section > main > div > div > article > div._ebcx9 > div._4a48i._277v9 > ul > li:nth-child(1)
//    @Text
//    @Html
//    @HtmlField(cssPath = "#react-root > section >article  ul > li:nth-child(1)")
    //#react-root > section > main > div > div > article > header > div._j56ec > div:nth-child(1) > div > a
    @Href
    @HtmlField(cssPath = "a:eq(0)")
    private String title;


    /***
     * 之前去寻找包含所需数据的js,不好找,那就提取出所有script元素.
     */
    @HtmlField(cssPath = "script")
    public List<String> scriptList;

    public List<String> getScriptList() {
        return scriptList;
    }

    public void setScriptList(List<String> scriptList) {
        this.scriptList = scriptList;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public List<String> getPicMoreScript() {
        return picMoreScript;
    }

    public void setPicMoreScript(List<String> picMoreScript) {
        this.picMoreScript = picMoreScript;
    }

    public String getShortcode() {
        return shortcode;
    }

    public void setShortcode(String shortcode) {
        this.shortcode = shortcode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    /***
     * 样例数据ins-img-xxx.txt
     * @param dmSpiderBean
     */
    @Override
    public void process(InsRecordSpiderBean dmSpiderBean) {
       // System.out.println(dmSpiderBean.getTitle());
        List<String> scriptList = dmSpiderBean.getScriptList();
        String imageUrl=null;


        //当前图片被like的情况
        String jsContent = InsUtil.getCustomerDataScriptContent(dmSpiderBean.getPicMoreScript());
        if(StringUtils.isNotEmpty(jsContent)){
            String queryId = InsUtil.getLikeQueryId(jsContent);
            if (queryId==null){
                System.out.println("没获取到like 的queryId");
            }else{
                InsUtil.createLikeRecordScheduler(dmSpiderBean.getShortcode(),null,queryId,dmSpiderBean.getRequest());
            }
        }


        //处理当前页的那张图片
        String script = InsUtil.getDataScript(dmSpiderBean.getScriptList());
        if(StringUtils.isNotEmpty(script)){
            try {
                imageUrl = JavaScriptUtil.getInstance().getVarValueBySelector(script,
                        "window._sharedData.entry_data.PostPage[0].graphql.shortcode_media.display_resources[0].src");
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("当前页的唯一图片地址:"+imageUrl);
        }
    }



    /***
     * 单个连接测试.
     * 实际是被探测到的url,放入队列被执行.
     * @param args
     */
    public static void main(String[] args) {
        GeccoEngine.create()
                .classpath("com.geccocrawler.gecco.demo.ins")
//                .start("https://www.instagram.com/p/BcjRrJTAbls/?taken-by=weeddogghome")
//                .start("https://www.instagram.com/p/BbCNcElgpOq/?taken-by=weeddogghome")

                .start("https://www.instagram.com/p/Bc7r7wHDMoY/?taken-by=neymarjr")
                .interval(2000)
                .start();
    }
}
