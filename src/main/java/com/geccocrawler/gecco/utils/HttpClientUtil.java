package com.geccocrawler.gecco.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.geccocrawler.gecco.local.FileUtil;
import io.webfolder.cdp.CdpPubUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.List;


public class HttpClientUtil {


    private final static String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws Exception {

//            String s = httpPure("http://www.mangocity.com/searc");
//        String s = httpPure("httpsxxx");
//        for (int i = 0; i < 1; i++) {
//            //String s = httpPure("https://www.v2ex.com");
//            //System.out.println(s);
//        }
//        System.out.println(s);
        downImg("/Users/wangany/backups/spider/ins-a.txt","/tem/");

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

    /***
     * 下载指定网络图片到指定路径
     * download("http://blog.goyiyo.com/wp-content/uploads/2012/12/6E0E8516-E1DC-4D1D-8B38-56BDE1C6F944.jpg","/tem/aaa.jpg")
     * @param url
     * @param filePathName
     */
    public static void download(String url, String filePathName) {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        try {
            HttpGet httpget = new HttpGet(url);

//伪装成google的爬虫JAVA问题查询
            httpget.setHeader("User-Agent", "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
// Execute HTTP request
            System.out.println("executing request " + httpget.getURI());
            CloseableHttpResponse response = httpclient.execute(httpget);

            File storeFile = new File(filePathName);
            FileOutputStream output = new FileOutputStream(storeFile);

// 得到网络资源的字节数组,并写入文件
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                try {
                    byte b[] = new byte[1024];
                    int j = 0;
                    while ((j = instream.read(b)) != -1) {
                        output.write(b, 0, j);
                    }
                    output.flush();
                    output.close();
                } catch (IOException ex) {
// In case of an IOException the connection will be released
// back to the connection manager automatically
                    throw ex;
                } catch (RuntimeException ex) {
// In case of an unexpected exception you may want to abort
// the HTTP request in order to shut down the underlying
// connection immediately.
                    httpget.abort();
                    throw ex;
                } finally {
// Closing the input stream will trigger connection release
                    try {
                        instream.close();
                    } catch (Exception ignore) {
                    }
                }
            }

        } catch (Exception e) {
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void downImg(String localPath,String targetPath) throws Exception {

        List<String> urls = FileUtil.readFileByLines(localPath);
        for (int i = 0; i < urls.size(); i++) {
            if(i>=5){
                return;
            }
            String url = urls.get(i);
//            url="http://ws3.sinaimg.cn/mw600/d619dd01gy1fikmy06826j20k00zkq96.jpg";
            if(StringUtils.isEmpty(url)){
                continue;
            }
            int suffixIndex = StringUtils.lastIndexOf(url,'.');
            String suffix = url.substring(suffixIndex,url.length());
            download(url,targetPath+i+suffix);
//            CdpPubUtil.getInstance().getHtml(url,10);
        }

    }

}

