package com.geccocrawler.gecco.demo.dm;

import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.pipeline.Pipeline;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.scheduler.SchedulerContext;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by nobody on 2017/12/24.
 * 对bean的处理类
 */
@PipelineName("dmListPipeline")
public class DmListPipeline implements Pipeline<DmListSpiderBean>{

    @Override
    public void process(DmListSpiderBean dmListSpiderBean) {
        String nextPageurl = dmListSpiderBean.getNextPageurl();
        List<String> urlList = dmListSpiderBean.getUrlList();
        HttpRequest currRequest = dmListSpiderBean.getRequest();
        if(StringUtils.isNotEmpty(nextPageurl)){
            System.out.println("加入请求任务队列的列表url:"+nextPageurl);
            SchedulerContext.into(currRequest.subRequest(nextPageurl));
        }else{
            System.out.println("ok,已经处理完所有列表.无下一页了.");
        }
        for (String url : urlList) {
            if(StringUtils.isNotEmpty(url)){
                System.out.println("加入请求任务队列的单个话题url:"+url);
                SchedulerContext.into(currRequest.subRequest(url));
            }
        }
    }
}
