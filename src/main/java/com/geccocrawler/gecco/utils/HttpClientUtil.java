package com.geccocrawler.gecco.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class HttpClientUtil {


    private final static String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws Exception {

//            String s = httpPure("http://www.mangocity.com/searc");
        String s = httpPure("httpsxxx");
        for (int i = 0; i < 1; i++) {
            //String s = httpPure("https://www.v2ex.com");
            //System.out.println(s);
        }
        System.out.println(s);
//
    }


    /**
     * 执行pure的http 返回json数据
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static JSONObject http(String url) throws Exception {

        String result = httpPure(url);
        if (result == null) return null;

        //变成json格式，方便取值

        //System.out.println(result.toString());

        JSONObject jo = (JSONObject)JSONObject.parse(result);
        return jo;

    }

    public static String httpPure(String url) throws IOException {
        StringBuffer result = new StringBuffer();
        CloseableHttpClient client = HttpClients.createDefault();
        BufferedReader rd = null;
        HttpGet request = new HttpGet(url);
        //添加请求头
        request.addHeader("User-Agent", USER_AGENT);
//        request.addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:56.0) Gecko/20100101 Firefox/56.0");
//        request.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//        request.addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
//        request.addHeader("Accept-Encoding", "gzip, deflate");
//        request.addHeader("Connection", "keep-alive");
//        request.addHeader("Upgrade-Insecure-Requests", "1");
//        request.addHeader("Pragma", "no-cache");
//        request.addHeader("Cache-Control", "no-cache");
//        request.addHeader("Cookie", "JSESSIONID=1DC5225AB09DC08E0A39366DFD40384B; user=DEFAULT_zhangsan");
        CloseableHttpResponse response = client.execute(request);
        try {
            if (!"200".equals("" + response.getStatusLine().getStatusCode())) {
                return null;
            }
            HttpEntity entity = response.getEntity();
            rd = new BufferedReader(new InputStreamReader(entity.getContent()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            EntityUtils.consume(entity);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            e.printStackTrace();
        } finally {
            if (rd != null) {
                rd.close();
            }
            response.close();
        }
        return result.toString();
    }

    public static JSONArray http2(String url) throws Exception {

        String result = httpPure(url);
        JSONArray ja = (JSONArray)JSONObject.parse(result);
        return ja;

    }


}

