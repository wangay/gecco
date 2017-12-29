package com.geccocrawler.gecco.demo.ins;

import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.utils.JavaScriptUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/***
 * dm 单条记录
 *
 *
 */
@PipelineName("InsRecordSpiderBean")
@Gecco(matchUrl = "https://www.instagram.com/p/{something}/?taken-by={username}", pipelines = "InsRecordSpiderBean",downloader="chromeCdp4jDownloader")
public class InsRecordSpiderBean implements HtmlBean, Pipeline<InsRecordSpiderBean> {


    private static final long serialVersionUID = -7127412585200687225L;

    private static int pageCount=0;
    @Request
    private HttpRequest request;

    @RequestParameter("something")
    private String something;

    @RequestParameter("username")
    private String username;


    //发帖标题 #react-root > section > main > div > div > article > div._ebcx9 > div._4a48i._277v9 > ul > li:nth-child(1)
//    @Text
//    @Html
//    @HtmlField(cssPath = "#react-root > section >article  ul > li:nth-child(1)")
    //#react-root > section > main > div > div > article > header > div._j56ec > div:nth-child(1) > div > a
    @Href
    @HtmlField(cssPath = "a:eq(0)")
    private String title;

//    @Text
//    @JSVar(var="sharedData",jsonpath="_sharedData.entry_data.PostPage[0].graphql.shortcode_media.display_resources[0].src")
//    @HtmlField(cssPath = "script:contains(sharedData)")
//    @HtmlField(cssPath = "#react-root~script ")
//    @HtmlField(cssPath = "script:contains(sharedData)")
    @HtmlField(cssPath = "scriptxx")
    public String pic;

    @HtmlField(cssPath = "script")
    public List<String> pic2;

    public List<String> getPic2() {
        return pic2;
    }

    public void setPic2(List<String> pic2) {
        this.pic2 = pic2;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public String getSomething() {
        return something;
    }

    public void setSomething(String something) {
        this.something = something;
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

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    @Override
    public void process(InsRecordSpiderBean dmSpiderBean) {
//        System.out.println(dmSpiderBean.getTitle());
        String picString = dmSpiderBean.getPic();
        List<String> pic2List = dmSpiderBean.getPic2();
//        System.out.println(picString);
        String imageUrl=null;
        for (int i = 0; i < pic2List.size(); i++) {
            String script = pic2List.get(i);
            if(StringUtils.isNotEmpty(script)){
                if(script.contains("sharedData")){
                    try {
                        imageUrl = JavaScriptUtil.getInstance().getVarValueBySelector(script,"window._sharedData.entry_data.PostPage[0].graphql.shortcode_media.display_resources[0].src");
                        System.out.println(imageUrl);
                        break;
                    } catch (Exception e) {
                        System.out.println("获取js变量的值失败:");
                        e.printStackTrace();
                    }
                }
            }
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
//                .start("https://www.instagram.com/p/BbCNcElgpOq/?taken-by=weeddogghome")
                .start("https://www.instagram.com/p/BaAr_SIARG-/?taken-by=weeddogghome")
                .interval(3000)
                .start();
    }
}
