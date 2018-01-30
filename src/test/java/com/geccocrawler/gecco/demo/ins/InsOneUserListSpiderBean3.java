package com.geccocrawler.gecco.demo.ins;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.local.FileUtil;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpGetRequest;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.scheduler.SchedulerContext;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.utils.JavaScriptUtil;
import io.webfolder.cdp.CdpPubUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/***
 * dm 单个用户的所有记录
 *
 *
 */
@PipelineName("InsOneUserListSpiderBean3")
@Gecco(matchUrl = "https://www.instagram.com/{username}/", pipelines = "InsOneUserListSpiderBean3",downloader="chromeCdp4jDownloader")
public class InsOneUserListSpiderBean3 implements HtmlBean, Pipeline<InsOneUserListSpiderBean3> {


    private static final long serialVersionUID = -7127412585200687235L;

    private static int pageCount=0;
    @Request
    private HttpRequest request;


    @RequestParameter("username")
    private String username;




    /***
     * 之前去寻找包含所需数据的js,不好找,那就提取出所有script元素.
     */
    @HtmlField(cssPath = "script")
    public List<String> picScript;

//    @HtmlField(cssPath = "//script[@crossorigin=\"anonymous\"]/@src")
    @Attr(value="src")
    @HtmlField(cssPath = "script[crossorigin=anonymous]")
    public List<String> picMoreScript;

    public static int getPageCount() {
        return pageCount;
    }

    public static void setPageCount(int pageCount) {
        InsOneUserListSpiderBean3.pageCount = pageCount;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public void setRequest(HttpRequest request) {
        this.request = request;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getPicScript() {
        return picScript;
    }

    public void setPicScript(List<String> picScript) {
        this.picScript = picScript;
    }

    public List<String> getPicMoreScript() {
        return picMoreScript;
    }

    public void setPicMoreScript(List<String> picMoreScript) {
        this.picMoreScript = picMoreScript;
    }

    /***
     * ins-img-list.html看json结构
     * @param dmSpiderBean
     */
    @Override
    public void process(InsOneUserListSpiderBean3 dmSpiderBean) {
//        System.out.println(dmSpiderBean.getTitle());
        String insBase ="https://www.instagram.com";
        List<String> pic2List = dmSpiderBean.getPicScript();

        for (int i = 0; i < pic2List.size(); i++) {
            String script = pic2List.get(i);
            if(StringUtils.isNotEmpty(script)){
                if(script.contains("sharedData")){
                    //只循环到这一个
                    try {


                        String jsongString = JavaScriptUtil.getInstance().jsJsonObj2String(script,"window._sharedData");
                        Object root = JSON.parse(jsongString);
//                        String selector = "window._sharedData.entry_data.ProfilePage[0].user.media.nodes";
                        String selector = "$.entry_data.ProfilePage[0].user.media.nodes";
                        JSONArray nodeJson = (JSONArray)com.alibaba.fastjson.JSONPath.eval(root, selector);
                        String userName = (String)com.alibaba.fastjson.JSONPath.eval(root, "$.entry_data.ProfilePage[0].user.username");
                        Iterator<Object> iterator = nodeJson.iterator();
                        String userId = null;
                        while (iterator.hasNext()){
                            JSONObject jObject = (JSONObject)iterator.next();
                            String bigImgUrl = (String)jObject.get("thumbnail_src");

                            if(StringUtils.isEmpty(userId)){
                                String idSelector = "$.owner.id";
                                userId = (String)com.alibaba.fastjson.JSONPath.eval(jObject, idSelector);
                                System.out.println("userId:"+userId);
                                MongoUtil.getInstance().saveSubColls(userName,userId,InsConsts.col_w_qianzaidaip);
                            }

                        }

                        break;
                    } catch (Exception e) {
                        System.out.println("失败:");
                        e.printStackTrace();
                    }
                }
            }
        }



    }


    public static void main(String[] args) {
        List<HttpRequest> requestList = new ArrayList<HttpRequest>();
        Set<String> subCollNames = MongoUtil.getInstance().findSubCollNames();
        for (String subCollName : subCollNames) {
            String url = InsConsts.insBaseUrl+subCollName+"/";
            requestList.add(new HttpGetRequest(url));
        }
        GeccoEngine.create()
                .classpath("com.geccocrawler.gecco.demo.ins")
//                .start("https://www.instagram.com/weeddogghome/")
//                .start("https://www.instagram.com/hkweed420/")
                .start(requestList)
                .interval(2000)
                .start();
    }
}
