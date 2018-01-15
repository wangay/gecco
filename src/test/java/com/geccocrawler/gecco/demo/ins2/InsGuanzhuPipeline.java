package com.geccocrawler.gecco.demo.ins2;

import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.pipeline.Pipeline;

/**
 * Created by nobody on 2018/1/15.
 * 处理关注 暂时无用
 */
@PipelineName("InsGuanzhuPipeline")
public class InsGuanzhuPipeline implements Pipeline<InsOneUserListSpiderBean> {

    @Override
    public void process(InsOneUserListSpiderBean dmSpiderBean) {

    }
}
