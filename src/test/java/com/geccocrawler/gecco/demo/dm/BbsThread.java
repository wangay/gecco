package com.geccocrawler.gecco.demo.dm;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nobody on 2017/12/27.
 * 一个帖子
 */
public class BbsThread {

    private String title;//标题
    private String authorName;//发帖人
    private String message;//发帖内容
    private List<BbsPost> postList = new ArrayList<BbsPost>();//所有回帖


    public BbsThread() {
    }

    public BbsThread(String title, String authorName) {
        this.title = title;
        this.authorName = authorName;
    }

    public BbsThread(String title, String authorName, String message) {
        this.title = title;
        this.authorName = authorName;
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<BbsPost> getPostList() {
        return postList;
    }

    public void setPostList(List<BbsPost> postList) {
        this.postList = postList;
    }
}
