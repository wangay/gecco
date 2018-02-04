package com.geccocrawler.gecco.demo.ins;

/**
 * Created by nobody on 2018/1/2.
 */
public interface InsConsts {
    String insBaseUrl = "https://www.instagram.com/";
    String insBaseUrl2 = "https://www.instagram.com";
    String pageCount = "20";//每页几条数据

    String query_id="query_hash";//这个会变

    boolean saveLinksLocal = false;//本地是否保存图片链接
    String pic_local_position = "/Users/wangany/backups/spider/ins-a.txt";//本地存放图片链接的位置
    boolean linkInThePic = true;//是否爬进这张照片
    boolean likeNeeded = false;//是否处理like
    boolean likingUserNameSaved = true;//是否把like的人username保存到本地

    //  follow 相关
    String page_follow_Count = "50";//每页几条数据 默认10
    String userId = "2303289858";//被查询的人的id "5383311519";//"6854724440";//直接从chrome中看userId   6854724440:maozedongdong 5620693450:hkweed420 5383311519:420taiwan  2303289858:maozenbei6368
    String follow_file_save_path="/Users/wangany/tem/spider/ins-user-following";//保存在本地哪里
    String followed_file_save_path="/Users/wangany/tem/spider/ins-user-followed";//保存在本地哪里

    //  follow 相关  end

    //点赞相关
    int maxZanADay =298;//330;//289;3;//每次最多点赞次数
    int maxPinglunADay =298;//330;//289;3;//每次平路次数
    int maxZanTimesADay =8;//一天循环处理点赞几次
    int maxRequestNum =1500;//500;//620;//多少个用户去被点赞
    int picNum = 1;//单个人,给他点几个赞

    //评论相关

    int do_dianzan=1;//仅点赞
    int do_pinglun=2;//仅评论
    int do_dianzanjipinglun=3;//点赞+评论
    int do_this=2;//默认点赞

    //关注
    int maxGuanzhuNum = 30;//5;//60;//每次最多关注多少人  限制每十分钟75个

    //mongon collection key

    String col_union_w = "col_union_w";//所有col_w_的集合

    String col_prefix = "col_w_";
    String col_w_my_mzdd ="col_w_my_mzdd";//我的某个账户关注的用户
    String col_w_my_maozebei6368 ="col_w_my_maozebei6368";//我的某个账户关注的用户

    String col_w_total="col_w_total";//最大的集合(所有的目标人群),由taiwan420,weedhk420的folloer等集合组成


    String col_w_qianzaidaip ="qianzaidaip";//潜在大ip,像taiwan420

    String col_w_taiwan420 ="taiwan420";//别人的账号的follower
    String col_w_weedhk420 ="col_w_weedhk420";//别人的账号的follower
    String col_w_hkweed420 ="col_w_hkweed420";//别人的账号的follower

    String mzddNotFollowingFromTW420="mzddNotFollowingFromTW420";
    String mzddNotFollowingFromTW420_2="mzddNotFollowingFromTW420_2";
    //已发送
    String mzddYFS2Tai420="mzddYFS2Tai420";

    //煎蛋
    String col_jiandan="col_jiandan";


}
