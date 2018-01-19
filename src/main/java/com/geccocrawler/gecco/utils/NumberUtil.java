package com.geccocrawler.gecco.utils;

import java.util.Random;

/**
 * Created by nobody on 2018/1/18.
 */
public class NumberUtil {
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
}
