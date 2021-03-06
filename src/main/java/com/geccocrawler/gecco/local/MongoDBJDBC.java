package com.geccocrawler.gecco.local;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;

/**
 * java连接数据库
 * Created by nobody on 2018/1/5.
 */
public class MongoDBJDBC {

    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    private static MongoDBJDBC ourInstance = new MongoDBJDBC();

    public static MongoDBJDBC getInstance() {
        return ourInstance;
    }

    private MongoDBJDBC() {
        init();
    }


    private void init(){
        if(mongoClient==null){
            mongoClient = new MongoClient( "localhost" , 27017 );
        }
        if(mongoDatabase==null){
            mongoDatabase = mongoClient.getDatabase("insdb");
        }
    }

    public MongoClient getMongoClient() {
        return mongoClient;
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    public void setMongoDatabase(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

    public  MongoCollection<Document> getColl(String collName) {
        MongoCollection<Document> coll = getMongoDatabase().getCollection(collName);
        return coll;
    }

    /**
     * 给定几个名字，把对应的结合组合，并返回
     * @param callNames
     * @return
     */
    public  MongoCollection<Document> addColl(String... callNames) {
        MongoCollection<Document> temColl = getColl("temColl");
        //查询所有的聚集集合
        for (String name : callNames) {
            MongoCollection<Document> oneColl = getColl(name);
            FindIterable<Document> findIterable = oneColl.find();
            MongoCursor<Document> mongoCursor = findIterable.iterator();
            while (mongoCursor.hasNext()) {
                String username = (String)mongoCursor.next().get("username");
                if (exist("username", username, temColl)) {
                    continue;
                }else{
                    Document doc = new Document("username",username);
                    temColl.insertOne(doc);
                }

            }
        }
        return temColl;
    }

    /***
     * 保存进mzdguanzhu这个collection
     * @param username
     */
    public void saveMzddguanzhu(String username){
        MongoCollection<Document> mzddguanzhuCollection = getMzddguanzhuCollection();
        Document document = new Document("username", username);
//                append("by", "Fly");
        List<Document> documents = new ArrayList<Document>();
        documents.add(document);
        mzddguanzhuCollection.insertMany(documents);//插入多个
    }

    /***
     * 保存进mzdguanzhu这个collection
     */
    public boolean exist(String key,String value,String collName){
        MongoCollection<Document> mzddguanzhuCollection = mongoDatabase.getCollection(collName);

        boolean exist = mzddguanzhuCollection.find(eq(key,value)).iterator().hasNext();
        return exist;
    }

    /***
     * 保存进mzdguanzhu这个collection
     */
    public boolean exist(String key,String value,MongoCollection<Document> coll){
        boolean exist = coll.find(eq(key,value)).iterator().hasNext();
        return exist;
    }

    /***
     * 清空一个coll
     */
    public  void deleleColl(String collName){
        MongoCollection<Document> collection = mongoDatabase.getCollection(collName);
        collection.deleteMany(new Document());
    }

    /***
     * 保存进一个coll,指定key value
     */
    public void save2Coll(String key,String value,String collName){
        MongoCollection<Document> collection = mongoDatabase.getCollection(collName);
        if(exist(key,value,collection)){
            return;
        }
        Document document = new Document(key, value);
        List<Document> documents = new ArrayList<Document>();
        documents.add(document);
        collection.insertMany(documents);//插入多个
    }

    /***
     * 保存进mzdguanzhu这个collection
     * @param username
     */
    public void save2Coll(String username,String collName){
        MongoCollection<Document> mzddguanzhuCollection = mongoDatabase.getCollection(collName);
        if(exist("username",username,mzddguanzhuCollection)){
            return;
        }
        Document document = new Document("username", username);
        List<Document> documents = new ArrayList<Document>();
        documents.add(document);
        mzddguanzhuCollection.insertMany(documents);//插入多个
    }

    /***
     *
     */
    public void save2Coll(Document doc,String collName){
        MongoCollection<Document> coll = mongoDatabase.getCollection(collName);
        save2Coll(doc,coll);
    }

    public void save2Coll(Document doc,MongoCollection<Document> coll){
        String username = doc.getString("username");
        if(exist("username",username,coll)){
            return;
        }
        List<Document> documents = new ArrayList<Document>();
        documents.add(doc);
        coll.insertMany(documents);//插入多个
        System.out.println("保存成功"+username);
    }

    public void save(MongoCollection<Document> collection,String username){
        Document document = new Document("username", username);
//                append("by", "Fly");
        List<Document> documents = new ArrayList<Document>();
        documents.add(document);
        collection.insertMany(documents);//插入多个
    }

    private MongoCollection<Document> getMzddguanzhuCollection(){
        return mongoDatabase.getCollection("mzddguanzhu");
    }

    /***
     * 清空collection的内容.
     * drop是让这个collection不存在了
     */
    public  void deleteMzddGuanzhu(){
        MongoCollection<Document> mzddguanzhuCollection = getMzddguanzhuCollection();
        mzddguanzhuCollection.deleteMany(new Document());
    }

    public  void deleteAll(MongoCollection<Document> collection){
        collection.deleteMany(new Document());
    }

    /***
     * 得到c2-c1:即台湾420里面我还没关注的
     * @param c1 我关注
     * @param c2 台湾420的粉丝
     */
    public MongoCollection<Document> jianfa(MongoCollection<Document> c2,MongoCollection<Document> c1,String newCollName,String key){
        MongoCollection<Document> c3 = mongoDatabase.getCollection(newCollName);
        c3.deleteMany(new Document());//以前可能存在这个coll,清空
        List<Document> documents = new ArrayList<Document>();
        //检索所有文档
        FindIterable<Document> findIterable = c2.find();
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        while(mongoCursor.hasNext()){
            Document next = mongoCursor.next();
            String value = (String)next.get(key);

            FindIterable<Document> ite = c1.find(eq(key, value));
            if(ite.iterator().hasNext()){
            }else{
                //没有 insert到新collection

                //插入文档
                /**
                 * 1. 创建文档 org.bson.Document 参数为key-value的格式
                 * 2. 创建文档集合List<Document>
                 * 3. 将文档集合插入数据库集合中 mongoCollection.insertMany(List<Document>) 插入单个文档可以用 mongoCollection.insertOne(Document)
                 * */
                Document document = new Document(key, value);
                documents.add(document);
            }
        }
        c3.insertMany(documents);//插入多个
        return c3;
    }
//
//    ##############################下面是例子##############################################



    public static void printCollection(String name) {
        MongoDBJDBC mongo = MongoDBJDBC.getInstance();
        MongoCollection<Document> collection = mongo.getMongoDatabase().getCollection(name);
//        mongo.deleteAll(collection);
        printCollection(collection);
    }

    public static void printCollection(MongoCollection<Document> collection) {
        System.out.println("集合 test 选择成功");
        //检索所有文档
        FindIterable<Document> findIterable = collection.find();
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        while(mongoCursor.hasNext()){
            System.out.println(mongoCursor.next());
        }
        long count = collection.count();
        System.out.println("集合拥有的Document的数量:"+count);
    }

    public static long count(MongoCollection<Document> collection) {
        long count = collection.count();
        System.out.println("集合拥有的Document的数量:"+count);
        return count;
    }

    public static void main( String args[] ){
        printCollection("taiwan420");
    }
}
