package com.geccocrawler.gecco.demo.dm;

/**
 * Created by nobody on 2017/12/27.
 * 回帖
 */
public class BbsPost {
    private String authorName;//回帖人
    private String message;//回帖内容
//    private Date postTime;//回帖时间


    public BbsPost() {
    }

    public BbsPost(String authorName, String message) {
        this.authorName = authorName;
        this.message = message;
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

}
