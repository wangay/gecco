package com.geccocrawler.gecco.utils;

import com.geccocrawler.gecco.local.FileUtil;
import org.apache.commons.lang3.StringUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * Created by nobody on 2017/12/30.
 */
public class JavaScriptUtil {
    private static JavaScriptUtil ourInstance = new JavaScriptUtil();

    public static JavaScriptUtil getInstance() {
        return ourInstance;
    }

    private ScriptEngineManager factory;
    private ScriptEngine engine;
    private JavaScriptUtil() {
        factory = new ScriptEngineManager();
        engine = factory.getEngineByName("JavaScript");
    }

    public static void main(String[] args) throws Exception {
        JavaScriptUtil.getInstance().getVarValueBySelector("","");
    }

    /***
     * 给一个字符串,它是一串js代码.
     * 根据选择器,返回其中的变量的值.
     * @throws Exception
     */
    public  String getVarValueBySelector(String scriptContent,String selector) throws Exception {
        String script = scriptContent;
        script="var window = {};var document = {};"+script;
        engine.eval(script);
        String jsAccessJsonObj = "var myVar = "+selector;
        engine.eval(jsAccessJsonObj);
        String value = (String)engine.get("myVar");
        return value;
    }
}
