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
     * @param username
     */
    public void save2Coll(String username,String collName){
        MongoCollection<Document> mzddguanzhuCollection = mongoDatabase.getCollection(collName);
        Document document = new Document("username", username);
        List<Document> documents = new ArrayList<Document>();
        documents.add(document);
        mzddguanzhuCollection.insertMany(documents);//插入多个
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

    public static void main( String args[] ){
        printCollection("taiwan420");
    }
}
