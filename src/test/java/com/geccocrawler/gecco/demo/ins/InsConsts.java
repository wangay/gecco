package com.geccocrawler.gecco.demo.ins;

/**
 * Created by nobody on 2018/1/2.
 */
public interface InsConsts {
    String insBaseUrl ="https://www.instagram.com/";
    String insBaseUrl2 ="https://www.instagram.com";
    String pageCount="20";//每页几条数据

    boolean saveLinksLocal = false;//本地是否保存图片链接
    String pic_local_position="/Users/wangany/backups/spider/ins-a.txt";//本地存放图片链接的位置
    boolean linkInThePic=true;//是否爬进这张照片
    boolean likeNeeded = true;//是否处理like
    boolean likingUserNameSaved = true;//是否把like的人username保存到本地
}
