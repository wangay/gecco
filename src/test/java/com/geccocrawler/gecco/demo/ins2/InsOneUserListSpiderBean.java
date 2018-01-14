package com.geccocrawler.gecco.demo.ins2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.demo.ins.InsConsts;
import com.geccocrawler.gecco.local.FileUtil;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpGetRequest;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.scheduler.SchedulerContext;
import com.geccocrawler.gecco.spider.HtmlBean;
import com.geccocrawler.gecco.utils.JavaScriptUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/***
 * dm 单个用户的所有记录
 *
 *
 */
@PipelineName("InsOneUserListSpiderBean")
@Gecco(matchUrl = "https://www.instagram.com/{username}/", pipelines = "InsOneUserListSpiderBean",downloader="chromeCdp4jDownloader")
public class InsOneUserListSpiderBean implements HtmlBean, Pipeline<InsOneUserListSpiderBean> {


    private static final long serialVersionUID = -7127412585200687235L;

    public static AtomicInteger zanCount=new AtomicInteger(0);//已经点赞的统计数量
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
        InsOneUserListSpiderBean.pageCount = pageCount;
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
    public void process(InsOneUserListSpiderBean dmSpiderBean) {
//        System.out.println(dmSpiderBean.getTitle());
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
                        int index=0;
                        while (iterator.hasNext()){
                            JSONObject jObject = (JSONObject)iterator.next();
                            if(StringUtils.isEmpty(userId)){
                                String idSelector = "$.owner.id";
                                userId = (String)com.alibaba.fastjson.JSONPath.eval(jObject, idSelector);
                            }

                            String imgShortCode = (String)com.alibaba.fastjson.JSONPath.eval(jObject, "$.code");
                            //进入单张照片的页面url
                            String oneRecordUrl = InsConsts.insBaseUrl+"p/"+imgShortCode+"/?taken-by="+userName;
                            //对一个人一次点赞1~3张图片,就近原则,非机器样子原则. 只取前4张,
                            if(++index<=InsConsts.picNum){
                                System.out.println("进入单张照片的页面url:"+oneRecordUrl);
                                SchedulerContext.into(dmSpiderBean.getRequest().subRequest(oneRecordUrl));
                            }else{
                                break;
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

        //把其他的用户连接,都放进任务队列

    }



    public static void main(String[] args) {
//        GeccoEngine.create()
//                .classpath("com.geccocrawler.gecco.demo.ins2")
//                .start("https://www.instagram.com/as59180/")
//                .interval(2000)
//                .start();

        //所有follower的url,每个是一个request
        List<HttpRequest> foRequests = new ArrayList<HttpRequest>();

        List<String> followers = FileUtil.readFileByLines(InsConsts.follow_file_save_path + "20180114.txt");
        Collections.shuffle(followers);//洗牌 .打乱list内容的顺序 //只用某随机算法选出399个用户
        for (int i = 0; i < followers.size(); i++) {
            String follower = followers.get(i);
            String followUrl = InsConsts.insBaseUrl+follower+"/";
            if(i>=InsConsts.maxRequestNum){
                break;
            }
            foRequests.add(new HttpGetRequest(followUrl));
        }


        GeccoEngine.create()
                .classpath("com.geccocrawler.gecco.demo.ins2")
                .start(foRequests)
                //开启几个爬虫线程
                .thread(3)
                //单个爬虫每次抓取完一个请求后的间隔时间
                .interval(1500)
                .start();
    }
}
