package com.geccocrawler.gecco.demo.sexsite;

import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.scheduler.SchedulerContext;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by nobody on 2017/12/24.
 * 对bean的处理类
 */
@PipelineName("sexwebListPipeline")
public class SexwebListPipeline implements Pipeline<SexwebListSpiderBean>{

    @Override
    public void process(SexwebListSpiderBean sexList) {
        HttpRequest currRequest = sexList.getRequest();
        //下一页继续抓取
        String currPageText = sexList.getCurrPageText();
        if(currPageText==null){
            System.out.println("null........pageNo="+sexList.getPageNo());
            currPageText = sexList.getPageNo();
            //return;
        }
        currPageText = currPageText.replaceAll("\\[","").replaceAll("\\]","");
        int currPage = Integer.valueOf(currPageText);
        int nextPage = currPage + 1;
        int totalPage = 406;//没有总页数,这里固定到1000页 alexTODO 如何识别出没有了.直接人工看最大页数.
        if(nextPage < totalPage) {
            String nextUrl = "";
            String currUrl = currRequest.getUrl();
            if(currUrl.indexOf("page-") != -1) {
                nextUrl = StringUtils.replaceOnce(currUrl, "page-" + currPage, "page-" + nextPage);
            } else {
                nextUrl = currUrl + "&" + "page-" + nextPage;
            }
            //把新的待抓取url放入任务队列
            SchedulerContext.into(currRequest.subRequest(nextUrl));
        }else if(nextPage == totalPage){
            System.out.println("---------Ok,爬取完毕~----------");
        }
    }
}
