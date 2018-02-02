package com.geccocrawler.gecco.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nobody on 2018/1/18.
 */
public class CommonUtil {
    /***
     * 返回范围[min,max]的随机整数
     * @param min
     * @param max
     * @return
     */
    public static int getRandomInt(int min,int max) {
        Random random = new Random();

        int s = random.nextInt(max)%(max-min+1) + min;
        return s;
    }

    /***
     * 从一段字符串中，找到符合用户名的字符串
     * ^[A-Za-z0-9]{6,20}$
     ^符号表示以后面的内容开头  前面不能有东西
     中括号里面的意思是出现的字符必须是大写字母或小写字母或0-9数字中的任意一个
     大括号里面的意思是前面的东西  出现大于等于6次，小于等于20次
     $符号表示以前面的内容结尾  后面没东西了
     e.g.
     "ins:few__fe few__fe2 few__fe3"
     [A-Za-z0-9._-]{4,20}$ 只会找到few__fe3
     加问号之后会找到所有
     即：
     [A-Za-z0-9._-]{4,20}$?

     * @return
     */
    public static List<String> getUsername(String text){
        List<String> list = new ArrayList<String>();
        if(StringUtils.isEmpty(text)){
            return list;
        }
        String patternString = "[A-Za-z0-9._-]{4,20}$?";
//        String patternString = "p=\\\"(\\d{10,})\\\"";//p=17263623232  数字 至少10次
        Matcher m = Pattern.compile(patternString).matcher(text);
        while (m.find()){
            //从头开始一直找,并打印找到的字符串
            String str = m.group();
            list.add(str);
//            System.out.println(str);
        }
        return list;
    }
}
