package com.geccocrawler.gecco.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nobody on 2018/2/1.
 * 回复别人,要用看起来像人的语言
 */
public class ReplyPeople {

    private  static List<String> list = new ArrayList<String>();
    static {
        list.add("不错");
        list.add("挺好");
        list.add("赞");
        list.add("呵呵");
        list.add("~~");
        list.add("!");
        list.add("~!~");
        list.add("咔咔");
        list.add("这可以可以");
        list.add("真行");
        list.add("..");
        list.add("...");
        list.add("...");
        list.add("....");
        list.add(".....");
        list.add("......");
        list.add("不错,棒棒的~");

    }

    /***
     * 随机选择一个回复
     * @return
     */
    public static String getText(){
        int size=list.size();
        int randomInt = NumberUtil.getRandomInt(0, size-1);
        return list.get(randomInt);
    }
}
