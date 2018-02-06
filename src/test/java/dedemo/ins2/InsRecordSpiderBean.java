package dedemo.ins2;

import com.geccocrawler.gecco.GeccoEngine;
import com.geccocrawler.gecco.annotation.*;
import com.geccocrawler.gecco.demo.ins.InsConsts;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.spider.HtmlBean;

import java.util.List;

/***
 * dm 单条记录(含一张照片,被喜欢情况,评论等). https://www.instagram.com/p/Bc3MVJdjTjd/?taken-by=neymarjr 这样的一个页面
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
     *
     * @param dmSpiderBean
     */
    @Override
    public void process(InsRecordSpiderBean dmSpiderBean) {
        String shortCode = dmSpiderBean.getShortcode();
        String username = dmSpiderBean.getUsername();
        String url = "https://www.instagram.com/p/"+shortCode+"/?taken-by="+username;


        InsAuto register = InsAuto.getInstance();
        if(InsConsts.do_this==InsConsts.do_dianzan){
            int zanCountInt = InsOneUserListSpiderBean.zanCount.getAndIncrement();
            if(zanCountInt>= InsConsts.maxZanADay){
                System.out.println("已经到达每天最大点赞数量");
                //SchedulerContext.empty();//剩下的任务都清空 alexTODO 不好用?
                //退出整个jvm
                System.exit(0);
                return;
            }
            register.dianzan(url);
        }else if(InsConsts.do_this==InsConsts.do_pinglun){
            int countInt = InsOneUserListSpiderBean.pinglunCount.getAndIncrement();
            if(countInt>= InsConsts.maxPinglunADay){
                System.out.println("已经到达每天最大评论数量");
                //退出整个jvm
                System.exit(0);
                return;
            }
            register.pinglun(url);
        }else if(InsConsts.do_this==InsConsts.do_dianzanjipinglun){
            register.dianzan(url);
            register.pinglun(url);
        }

    }



    /***
     * 单个连接测试.
     * 实际是被探测到的url,放入队列被执行.
     * @param args
     */
    public static void main(String[] args) {
        GeccoEngine.create()
                .classpath("com.geccocrawler.gecco.dedemo.ins2")
//                .start("https://www.instagram.com/p/BcjRrJTAbls/?taken-by=weeddogghome")
//                .start("https://www.instagram.com/p/BbCNcElgpOq/?taken-by=weeddogghome")

                .start("https://www.instagram.com/p/Bc7r7wHDMoY/?taken-by=neymarjr")
                .interval(2000)
                .start();
    }
}
