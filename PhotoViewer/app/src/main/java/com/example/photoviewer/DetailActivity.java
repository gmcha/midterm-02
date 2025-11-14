package com.example.photoviewer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private ImageView detailImageView;
    private TextView detailTextTitle;
    private TextView detailTextDescription;
    private Button btnPrevious;
    private Button btnNext;
    private Button btnClose;

    private List<Post> postList;
    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // 뷰 연결
        detailImageView = findViewById(R.id.detail_image_view);
        detailTextTitle = findViewById(R.id.detail_text_title);
        detailTextDescription = findViewById(R.id.detail_text_description);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);
        btnClose = findViewById(R.id.btn_close);

        postList = DataHolder.getInstance().getPostList();
        currentPosition = getIntent().getIntExtra("currentPosition", 0);

        loadPost(currentPosition);

        btnPrevious.setOnClickListener(v -> {
            if (currentPosition > 0) {
                currentPosition--;
                loadPost(currentPosition);
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPosition < postList.size() - 1) {
                currentPosition++;
                loadPost(currentPosition);
            }
        });

        btnClose.setOnClickListener(v -> {
            finish();
        });
    }

    private void loadPost(int position) {
        currentPosition = position; // 현재 위치 저장

        Post post = postList.get(position);

        detailImageView.setImageBitmap(post.getImageBitmap());
        detailTextTitle.setText(post.getTitle());
        detailTextDescription.setText(post.getText());

        btnPrevious.setEnabled(position > 0);
        btnNext.setEnabled(position < postList.size() - 1);
    }
}