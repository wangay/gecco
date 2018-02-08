package com.geccocrawler.gecco.demo.ins;

import com.geccocrawler.gecco.utils.HttpClientUtil;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

/**
 * Created by nobody on 2018/1/29.
 * 知道了图片url,下载到本地
 */
public class Image2LocalByUrl {

    public static void download() {
        MongoCollection<Document> coll = MongoUtil.getColl(InsConsts.col_jiandan);
        //检索所有文档
        FindIterable<Document> findIterable = coll.find().noCursorTimeout(true);
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        int index = 0;
        int index2 = 0;
        String targetPath = "/Users/wangany/Downloads/liye/sexsite/";
        while (mongoCursor.hasNext()) {
            Document next = mongoCursor.next();
            String url = (String) next.get("url");

            try {
                if (index > 150 && index < 1000) {
                    //只下载一百张
                    if (StringUtils.isEmpty(url)) {
                        continue;
                    }
                    int suffixIndex = StringUtils.lastIndexOf(url, '.');
                    String suffix = url.substring(suffixIndex, url.length());
                    HttpClientUtil.download(url, targetPath + index + suffix);
                    index2++;
                }
//                else {
//                    break;
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            index++;

        }
    }

    public static void main(String[] args) {
        download();
    }
}
