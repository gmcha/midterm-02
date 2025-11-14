package com.example.photoviewer;

import java.util.List;

// 앱 전역에서 게시물 목록을 공유하기 위한 Singleton 클래스
public class DataHolder {
    private static DataHolder instance;
    private List<Post> postList;

    private DataHolder() {} // 외부에서 생성 방지

    public static synchronized DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    public void setPostList(List<Post> postList) {
        this.postList = postList;
    }

    public List<Post> getPostList() {
        return postList;
    }
}