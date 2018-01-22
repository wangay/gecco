package com.geccocrawler.gecco.demo.ins;

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

    private MongoDBJDBC mongoDBJDBC;

    private MongoUtil() {
        mongoDBJDBC=MongoDBJDBC.getInstance();
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

    public static void main(String[] args) {
        MongoCollection<Document> coll = MongoUtil.getInstance().notFollowingColl();
        MongoDBJDBC.printCollection(coll);
    }
}
