package com.geccocrawler.gecco.demo.ins;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import me.postaddict.instagram.scraper.model.Account;
import org.bson.Document;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by nobody on 2017/12/21.
 * 一把锁可以生成多个条件
 */
public class Box {

    private Lock theLock = new ReentrantLock();
    // 消费者用判断条件
    private Condition full = theLock.newCondition();

    // 生产者用判断条件
    private Condition empty = theLock.newCondition();

    private int cacheSize = 1;//最开始是1


    private static List<Document> cache = new LinkedList<Document>();

    // 生产者线程任务
    public void put(Document doc) throws InterruptedException {
        // 获取线程锁
        theLock.lock();
        try {
            while (cache.size() >=cacheSize) {
                //缓存中的大小，超出之后，就先不往里放了
                System.out.println("超出缓存容量.暂停写入.");
                // 生产者线程阻塞 同时释放当前锁(唤醒其他线程.)
                full.await();
                System.out.println("生产者线程被唤醒");
            }
            System.out.println("写入数据");
            cache.add(doc);
            // 唤醒消费者
            empty.signal();
        } finally {
            // 锁使用完毕后不要忘记释放
            theLock.unlock();
        }
    }


    // 消费者线程任务
    public void get(MongoCollection<Document> coll, AtomicInteger ai) throws InterruptedException {
        try {
            while (!Thread.interrupted()) {
                // 获取锁
                theLock.lock();
                while (cache.size() == 0) {
                    System.out.println("缓存数据消费完毕.暂停读取");
                    // 消费者线程阻塞
                    empty.await();
                }
                System.out.println("消费数据,使用的线程为："+Thread.currentThread().getName());
                System.out.println("");
                Document doc = cache.get(0);
                work(doc,coll,ai);
                cache.remove(0);
                // 唤醒生产者线程
                full.signal();
            }

        } finally {
            // 锁使用完毕后不要忘记释放
            theLock.unlock();
        }
    }

    private void work(Document doc, MongoCollection<Document> coll, AtomicInteger ai){
        String username = (String)doc.get("username");
        System.out.println("工作在："+username);

        Object followedByObj = doc.get("followedBy");
        if(followedByObj!=null){
            System.out.println("followedByObj为空，不处理");
            return;
        }

        Account account = InsUtil.getInstagramAccountByName(username);
        if(account!=null){
            Integer followedBy = account.getFollowedBy();
            if(followedBy!=null && followedBy.intValue()>0){
                //更新文档   将文档中likes=100的文档修改为likes=200
                coll.updateMany(Filters.eq("username", username), new Document("$set",new Document("followedBy",followedBy)));
                Integer iNum = ai.getAndIncrement();
                System.out.println("更新"+username+" i="+iNum);
            }

        }

    }

}
