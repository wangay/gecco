package com.geccocrawler.gecco.local;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by nobody on 2017/12/26.
 *
 * gson
 *
 * 使用:Gson gson = GsonUtil.getInstance().getGson();
 *
 <dependency>
 <groupId>com.google.code.gson</groupId>
 <artifactId>gson</artifactId>
 <version>2.8.2</version>
 </dependency>


 */
public class GsonUtil {
    private static GsonUtil ourInstance = new GsonUtil();

    public static GsonUtil getInstance() {
        return ourInstance;
    }

    public Gson getGson() {
        return gson;
    }

    private Gson gson;

    private GsonUtil() {
        gson = new GsonBuilder()
                .disableHtmlEscaping()
                .create();
        //gson=new Gson();
    }



}
