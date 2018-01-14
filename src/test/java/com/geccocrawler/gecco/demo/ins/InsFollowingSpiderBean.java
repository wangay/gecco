package com.geccocrawler.gecco.demo.ins;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.local.FileUtil;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.scheduler.SchedulerContext;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.utils.JavaScriptUtil;
import io.webfolder.cdp.CdpPubUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;

/***
 * dm 单个用户的following
 *
 *
 */
@PipelineName("InsFollowingSpiderBean")
@Gecco(matchUrl = "https://www.instagram.com/{username}/following/", pipelines = "InsFollowingSpiderBean",downloader="chromeCdp4jDownloader")
public class InsFollowingSpiderBean implements HtmlBean, Pipeline<InsFollowingSpiderBean> {


    private static final long serialVersionUID = -7127412585200687236L;

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
        InsFollowingSpiderBean.pageCount = pageCount;
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
    public void process(InsFollowingSpiderBean dmSpiderBean) {
//        System.out.println(dmSpiderBean.getTitle());
        String insBase =InsConsts.insBaseUrl2;
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
                            }

                            String imgShortCode = (String)com.alibaba.fastjson.JSONPath.eval(jObject, "$.code");
                            System.out.println(bigImgUrl);
                            if(InsConsts.saveLinksLocal){
                                //本地保存照片
                                try {
                                    FileUtil.writeFileByFileWriterAdd(InsConsts.pic_local_position,bigImgUrl);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            if(InsConsts.linkInThePic){
                                //进入单张照片的页面url
                                String oneRecordUrl = InsConsts.insBaseUrl+"p/"+imgShortCode+"/?taken-by="+userName;
                                System.out.println("进入单张照片的页面url:"+oneRecordUrl);
                                SchedulerContext.into(dmSpiderBean.getRequest().subRequest(oneRecordUrl));
                            }

                        }

                        //更多
                        String hasNextSelector = "$.entry_data.ProfilePage[0].user.media.page_info.has_next_page";
                        Boolean hasNext = (Boolean)com.alibaba.fastjson.JSONPath.eval(root, hasNextSelector);
                        if(hasNext){
                            //有更多图片
                            String afterSelector = "$.entry_data.ProfilePage[0].user.media.page_info.end_cursor";
                            String after = (String)com.alibaba.fastjson.JSONPath.eval(root, afterSelector);
                            morePic(dmSpiderBean, insBase,after,userId);
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

    private void morePic(InsFollowingSpiderBean dmSpiderBean, String insBase, String after, String userId) {
        //更多
        List<String> picMoreScript = dmSpiderBean.getPicMoreScript();
        for (String picMore : picMoreScript) {
            String jsUrl = insBase+picMore;
            //queryId在这个js里面.
            //https://www.instagram.com/static/bundles/ConsumerCommons.js/xxx.js
            if(jsUrl.contains("ConsumerCommons")){
                try {
                    String jsContent = CdpPubUtil.getInstance().getHtml(jsUrl,10);//HttpClientUtil.httpPure(jsUrl);//代理,否则访问不了
                    String queryId = InsUtil.getUserPostQueryId(jsContent);
                    JSONObject varJson = new JSONObject();
                    varJson.putIfAbsent("id",userId);
                    varJson.putIfAbsent("first","12");
                    varJson.putIfAbsent("after",after);
                    String variables = varJson.toJSONString();
                    String encode = URLEncoder.encode(variables, "utf-8");
                    String moreUrl = InsConsts.insBaseUrl+"graphql/query/?"+"query_id="+queryId+"&variables="+encode;
                    System.out.println("更多图片:"+moreUrl);
                    SchedulerContext.into(dmSpiderBean.getRequest().subRequest(moreUrl));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }


    public static void main(String[] args) {
        GeccoEngine.create()
                .classpath("com.geccocrawler.gecco.demo.ins")
//                .start("https://www.instagram.com/weeddogghome/")
                .start("https://www.instagram.com/maozedongdong4069/following/")
                .interval(2000)
                .start();
    }
}
