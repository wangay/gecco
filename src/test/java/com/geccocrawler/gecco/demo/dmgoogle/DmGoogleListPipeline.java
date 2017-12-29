package com.geccocrawler.gecco.demo.dmgoogle;

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
@PipelineName("dmGoogleListPipeline")
public class DmGoogleListPipeline implements Pipeline<DmGoogleListSpiderBean>{

    @Override
    public void process(DmGoogleListSpiderBean dmListSpiderBean) {

    }
}
