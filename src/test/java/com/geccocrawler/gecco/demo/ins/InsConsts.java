package com.geccocrawler.gecco.demo.ins;

/**
 * Created by nobody on 2018/1/2.
 */
public interface InsConsts {
    String insBaseUrl = "https://www.instagram.com/";
    String insBaseUrl2 = "https://www.instagram.com";
    String pageCount = "20";//每页几条数据


    boolean saveLinksLocal = false;//本地是否保存图片链接
    String pic_local_position = "/Users/wangany/backups/spider/ins-a.txt";//本地存放图片链接的位置
    boolean linkInThePic = true;//是否爬进这张照片
    boolean likeNeeded = true;//是否处理like
    boolean likingUserNameSaved = true;//是否把like的人username保存到本地

    //  follow 相关
    String page_follow_Count = "50";//每页几条数据 默认10
    String userId = "6854724440";//"5383311519";//"6854724440";//直接从chrome中看userId   6854724440:maozedongdong  5383311519:420taiwan
    String follow_file_save_path="/Users/wangany/tem/spider/ins-user-following";//保存在本地哪里
    String followed_file_save_path="/Users/wangany/tem/spider/ins-user-followed";//保存在本地哪里

    //  follow 相关  end

    //点赞相关
    int maxZanADay =330;//每小时最多点赞次数
    int maxRequestNum =620;//多少个用户去被点赞
    int picNum = 1;//单个人,给他点几个赞

    //关注
    int maxGuanzhuNum = 70;//每天最多关注多少人

}
