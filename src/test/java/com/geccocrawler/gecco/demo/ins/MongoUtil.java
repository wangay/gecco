package com.geccocrawler.gecco.demo.ins;

import com.geccocrawler.gecco.local.MongoDBJDBC;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

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
     * 我还未关注的
     */
    public MongoCollection<Document> notFollowingColl(){
        MongoCollection<Document> taiwan420 = mongoDBJDBC.getMongoDatabase().getCollection(InsConsts.taiwan420);
        MongoCollection<Document> mzddguanzhu = mongoDBJDBC.getMongoDatabase().getCollection(InsConsts.mzddguanzhu);
        MongoCollection<Document> newCollection = mongoDBJDBC.jianfa(taiwan420, mzddguanzhu, InsConsts.mzddNotFollowingFromTW420, "username");
        MongoCollection<Document> yfsColl=mongoDBJDBC.getMongoDatabase().getCollection(InsConsts.mzddYFS2Tai420);//点了关注,但是处于 已发送的状态
        MongoCollection<Document> newCollection2 = mongoDBJDBC.jianfa(newCollection, yfsColl, InsConsts.mzddNotFollowingFromTW420_2, "username");
        return newCollection2;
    }

    public static void main(String[] args) {
        MongoCollection<Document> coll = MongoUtil.getInstance().notFollowingColl();
        MongoDBJDBC.printCollection(coll);
    }
}
