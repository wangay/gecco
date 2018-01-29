package com.geccocrawler.gecco.demo.ins;

import com.geccocrawler.gecco.local.FileUtil;
import com.geccocrawler.gecco.local.MongoDBJDBC;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nobody on 2018/1/22.
 */
public class MongoUtil {
    private static MongoUtil ourInstance = new MongoUtil();

    public static MongoUtil getInstance() {
        return ourInstance;
    }

    private  static MongoDBJDBC mongoDBJDBC;

    private MongoUtil() {
        mongoDBJDBC=MongoDBJDBC.getInstance();
    }


    public static MongoCollection<Document> getColl(String collName){
        MongoCollection<Document> coll = mongoDBJDBC.getMongoDatabase().getCollection(collName);
        return coll;
    }
    /***
     * 我还未关注的 maozedongdong--taiwan420
     */
    public MongoCollection<Document> notFollowingColl(){
        MongoCollection<Document> taiwan420 = mongoDBJDBC.getMongoDatabase().getCollection(InsConsts.taiwan420);
        MongoCollection<Document> mzddguanzhu = mongoDBJDBC.getMongoDatabase().getCollection(InsConsts.mzddguanzhu);
        MongoCollection<Document> yfsColl=mongoDBJDBC.getMongoDatabase().getCollection(InsConsts.mzddYFS2Tai420);//点了关注,但是处于 已发送的状态
        MongoCollection<Document> newCollection = mongoDBJDBC.jianfa(taiwan420, mzddguanzhu, InsConsts.mzddNotFollowingFromTW420, "username");
        MongoCollection<Document> newCollection2 = mongoDBJDBC.jianfa(newCollection, yfsColl, InsConsts.mzddNotFollowingFromTW420_2, "username");
        return newCollection2;
    }

    public static List<String> coll2List(MongoCollection<Document> coll){
        MongoCursor<Document> iterator = coll.find().iterator();
        List<String> list = new ArrayList<String>();
        while (iterator.hasNext()){
            Document next = iterator.next();
            String username = (String)next.get("username");
            list.add(username);
        }
        return list;
    }

    /***
     * 把煎蛋的所有照片链接保存
     */
    private  void saveJiandan(){
        MongoCollection<Document> jiandan = mongoDBJDBC.getMongoDatabase().getCollection(InsConsts.col_jiandan);
        List<String> urlList = FileUtil.readFileByLines("/Users/wangany/backups/spider/jiandanxxoo5.txt");

        List<Document> documents = new ArrayList<Document>();


        for (int i = 0; i < urlList.size(); i++) {
            String url = urlList.get(i);
            Document document = new Document("url", url).
                append("status", "normal");
            documents.add(document);
        }
        jiandan.insertMany(documents);//插入多个
    }

    /***
     * 打印数量
     * @param name
     */
    public void printCount(String name){
        MongoCollection<Document> coll = mongoDBJDBC.getMongoDatabase().getCollection(name);
        long count = coll.count();
        System.out.println("集合拥有的Document的数量:"+count);
    }




    public static void main(String[] args) {

//        MongoCollection<Document> coll = MongoUtil.getInstance().notFollowingColl();
//        MongoDBJDBC.printCollection(coll);
//        MongoUtil.getInstance().saveJiandan();
//        MongoUtil.getInstance().printCount(InsConsts.col_jiandan);


    }
}
