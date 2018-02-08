package com.geccocrawler.gecco.demo.ins;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import me.postaddict.instagram.scraper.model.Account;
import org.bson.Document;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nobody on 2017/12/21.
 * 用一个生产者,消费者例子,演示ReentrantLock+Condition的配合:
 */
public class BuildWork {

    public static void main(String[] args) {
        final Box box = new Box();

        MongoCollection<Document> coll = MongoUtil.getColl(InsConsts.col_union_w);
        FindIterable<Document> documents = coll.find().noCursorTimeout(true);
        MongoCursor<Document> iterator = documents.iterator();

        final AtomicInteger ai = new AtomicInteger(0);
        //生产者
       Thread produceThread =  new Thread(new Runnable() {
            public void run() {

                while (iterator.hasNext()){
                    System.out.println("生产正产生第几个："+ai.getAndIncrement());
                    Document doc = iterator.next();
                    try {
                        box.put(doc);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        produceThread.start();


        // 创建8个消费者线程
        custormer(box,coll,ai);


    }

    public static void custormer(final Box box,MongoCollection<Document> coll,AtomicInteger ai){
        ExecutorService threadPool = Executors.newCachedThreadPool();
        for (int i = 0; i < 8; i++) {
            threadPool.execute(new Thread(new Runnable() {
                public void run() {
                    try {
                        box.get(coll,ai);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }));
        }
    }

}
