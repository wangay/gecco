package com.geccocrawler.gecco.demo.ins;

import com.geccocrawler.gecco.local.FileUtil;
import com.geccocrawler.gecco.local.MongoDBJDBC;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.eq;
import static com.sun.corba.se.spi.activation.IIOP_CLEAR_TEXT.value;

/**
 * Created by nobody on 2018/1/22.
 * MongoUtil是具体应用
 * MongoDBJDBC 是相对底层的基础接口
 */
public class MongoUtil {
    private static MongoUtil ourInstance = new MongoUtil();

    public static MongoUtil getInstance() {
        return ourInstance;
    }

    private static MongoDBJDBC mongoDBJDBC;

    private MongoUtil() {
        mongoDBJDBC = MongoDBJDBC.getInstance();
    }


    public static MongoCollection<Document> getColl(String collName) {
        MongoCollection<Document> coll = mongoDBJDBC.getMongoDatabase().getCollection(collName);
        return coll;
    }

    public static MongoDBJDBC getMongoDBJDBC() {
        return mongoDBJDBC;
    }

    /***
     * 把一些集合，变为一个大集合
     * List<MongoCollection<Document>> collList
     * @return
     */
    public MongoCollection<Document> unionColl( ){
        MongoCollection<Document> unionColl = getColl(InsConsts.col_union_w);
        MongoDatabase db = mongoDBJDBC.getMongoDatabase();
        //查询所有的聚集集合
        for (String name : db.listCollectionNames()) {
            if(name.contains(InsConsts.col_prefix)||name.equals("taiwan420")){
                MongoCollection<Document> oneColl = db.getCollection(name);
                FindIterable<Document> findIterable = oneColl.find();
                MongoCursor<Document> mongoCursor = findIterable.iterator();
                while (mongoCursor.hasNext()) {
                    String username = (String)mongoCursor.next().get("username");
                    if (mongoDBJDBC.exist("username", username, unionColl)) {
                        continue;
                    }else{
                        Document doc = new Document("username",username);
                        unionColl.insertOne(doc);
                    }

                }
            }
        }
        System.out.println("最大集合的数量："+unionColl.count());
        return unionColl;
    }

    /***
     * 在col_w_qianzaidaip中找到userid对应的username
     */
    public String findByUserId(String userid) {

        MongoCollection<Document> c3 = getColl(InsConsts.col_w_qianzaidaip);
        Document doc = new Document();
        doc.put("userId", userid);
//            FindIterable<Document> ite = c3.find(eq(key, value));
        FindIterable<Document> ite = c3.find(doc);
        MongoCursor<Document> iterator = ite.iterator();
        if (iterator.hasNext()) {
            return (String) iterator.next().get("username");
        }
        return null;
    }

    /***
     * 潜在大ip的用户保存,含userId信息
     * @param username
     * @param userId
     * @param collName
     */
    public void saveSubColls(String username, String userId, String collName) {
        if (mongoDBJDBC.exist("username", username, collName)) {
            return;
        }
        MongoCollection<Document> coll = getColl(collName);
        Document document = new Document("username", username);
        document.put("userId", userId);
        List<Document> documents = new ArrayList<Document>();
        documents.add(document);
        coll.insertMany(documents);//插入多个
    }

    /***
     * 所有潜在的subColl (即潜在的大的类似taiwan420的)
     * @return
     */
    public Set<String> findSubCollNames() {
        Set<String> set = new HashSet<String>();

        List<String> list = new ArrayList<String>();
        list.add(InsConsts.col_w_taiwan420);
        list.add("mzddguanzhu");

        for (int i = 0; i < list.size(); i++) {
            String name = list.get(i);
            MongoCollection<Document> coll = getColl(name);
            Document doc = new Document("username", Pattern.compile("weed|420|feixing"));
            FindIterable<Document> findIterable = coll.find(doc);
            MongoCursor<Document> mongoCursor = findIterable.iterator();
            while (mongoCursor.hasNext()) {
                String username = (String) mongoCursor.next().get("username");
                set.add(username);
            }
        }

        return set;
    }

    public void buildColWTotal() {
        MongoCollection<Document> collWTotal = getColl(InsConsts.col_w_total);

        String[] subCollNames = {
                InsConsts.col_w_taiwan420,
                InsConsts.col_w_weedhk420

        };
        List<MongoCollection<Document>> subCollList = new ArrayList<MongoCollection<Document>>();
        for (String subCollName : subCollNames) {
            MongoCollection<Document> collSub = getColl(subCollName);
            subCollList.add(collSub);
        }


        for (MongoCollection<Document> coll : subCollList) {
            //检索所有文档
            FindIterable<Document> findIterable = coll.find();
            MongoCursor<Document> mongoCursor = findIterable.iterator();
            List<Document> documents = new ArrayList<Document>();
            while (mongoCursor.hasNext()) {
                Document next = mongoCursor.next();
                String value = (String) next.get("username");

                boolean isExist = mongoDBJDBC.exist("username", value, collWTotal);
                if (isExist) {
                } else {
                    Document document = new Document("username", value);
                    documents.add(document);
                }
            }
            collWTotal.insertMany(documents);//插入多个
        }

    }

    /***
     * 我还未关注的 maozedongdong--col_w_taiwan420
     */
    public MongoCollection<Document> notFollowingColl() {
        MongoCollection<Document> taiwan420 = mongoDBJDBC.getMongoDatabase().getCollection(InsConsts.col_w_taiwan420);
        MongoCollection<Document> mzddguanzhu = mongoDBJDBC.getMongoDatabase().getCollection(InsConsts.col_w_my_mzdd);
        MongoCollection<Document> yfsColl = mongoDBJDBC.getMongoDatabase().getCollection(InsConsts.mzddYFS2Tai420);//点了关注,但是处于 已发送的状态
        MongoCollection<Document> newCollection = mongoDBJDBC.jianfa(taiwan420, mzddguanzhu, InsConsts.mzddNotFollowingFromTW420, "username");
        MongoCollection<Document> newCollection2 = mongoDBJDBC.jianfa(newCollection, yfsColl, InsConsts.mzddNotFollowingFromTW420_2, "username");
        return newCollection2;
    }

    /***
     * 未关注的
     *
     * @param allColl
     * @param curMyUser 当前登陆用户
     * @return
     */
    public MongoCollection<Document> notFollowingColl2(MongoCollection<Document> allColl,String curMyUser) {
        mongoDBJDBC.deleleColl("col_tem_1");
        mongoDBJDBC.deleleColl("col_tem_2");
        MongoCollection<Document> ygzColl = mongoDBJDBC.getMongoDatabase().getCollection(InsConsts.col_my_w_ygz_prefix+curMyUser);
        MongoCollection<Document> yfsColl = mongoDBJDBC.getMongoDatabase().getCollection(InsConsts.col_my_w_yfs_prefix+curMyUser);//点了关注,但是处于 已发送的状态
        MongoCollection<Document> newCollection = mongoDBJDBC.jianfa(allColl, ygzColl, "col_tem_1", "username");
        MongoCollection<Document> newCollection2 = mongoDBJDBC.jianfa(newCollection, yfsColl, "col_tem_2", "username");
        return newCollection2;
    }

    public static List<String> coll2List(MongoCollection<Document> coll) {
        MongoCursor<Document> iterator = coll.find().iterator();
        List<String> list = new ArrayList<String>();
        while (iterator.hasNext()) {
            Document next = iterator.next();
            String username = (String) next.get("username");
            list.add(username);
        }
        return list;
    }

    /***
     * 把煎蛋的所有照片链接保存
     */
    private void saveJiandan() {
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
    public void printCount(String name) {
        MongoCollection<Document> coll = mongoDBJDBC.getMongoDatabase().getCollection(name);
        long count = coll.count();
        System.out.println("集合拥有的Document的数量:" + count);
    }


    public static void main(String[] args) {

//        MongoCollection<Document> coll = MongoUtil.getInstance().notFollowingColl();
//        MongoDBJDBC.printCollection(coll);
//        MongoUtil.getInstance().saveJiandan();
//        MongoUtil.getInstance().printCount(InsConsts.col_jiandan);
//        Set<String> subCollNames = MongoUtil.getInstance().findSubCollNames();
//        int i = 0;
//        for (String subCollName : subCollNames) {
//            System.out.println(subCollName);
//            i++;
//        }
//        System.out.println(i);

        MongoUtil.getInstance().unionColl();
    }
}
