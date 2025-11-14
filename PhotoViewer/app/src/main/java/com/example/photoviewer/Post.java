package com.example.photoviewer;

import android.graphics.Bitmap;

// 하나의 게시물 데이터를 담는 클래스
public class Post {
    private String title;
    private String text;
    private String imageUrl;
    private Bitmap imageBitmap;

    // 생성자
    public Post(String title, String text, String imageUrl, Bitmap imageBitmap) {
        this.title = title;
        this.text = text;
        this.imageUrl = imageUrl;
        this.imageBitmap = imageBitmap;
    }

    // Getter
    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }
}