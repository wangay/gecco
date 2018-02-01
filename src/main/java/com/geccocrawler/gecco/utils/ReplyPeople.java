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
        list.add("很棒");
        list.add("赞啊");
        list.add("厉害了");
        list.add("orzzz");
        list.add("orzzz~");
        list.add(">_<|||");
        list.add("⊙﹏⊙‖∣°");
        list.add("→_→ ");
        list.add("..@_@|||||.. ");
        list.add("…(⊙_⊙;)…");
        list.add("o_o ....");
        list.add("O__O");
        list.add("+_+");
        list.add("o_O???");
        list.add("（*@ο@*）");
        list.add("O_o ");
        list.add("°ο°）~ ");
        list.add("★~★");
        list.add("(～ o ～)~z");
        list.add("(*^‧^*)");
        list.add("（*∩_∩*");
        list.add("（＋﹏＋）");
        list.add("o(‧' '‧)o");
        list.add("*^﹏^*");

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
