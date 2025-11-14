package com.example.photoviewer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    // 수정: Bitmap 리스트에서 Post 리스트로 변경
    private List<Post> postList;
    private Context context;

    // 수정: 생성자에서 Context와 List<Post>를 받음
    public ImageAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 이미지 항목을 나타낼 뷰 생성
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        // 해당 위치의 Post를 뷰에 설정
        Post post = postList.get(position);
        holder.imageView.setImageBitmap(post.getImageBitmap());

        // --- added ---
        if ("book!".equals(post.getTitle())) {
            holder.textViewTitleOverlay.setVisibility(View.VISIBLE);
        } else {
            holder.textViewTitleOverlay.setVisibility(View.GONE);
        }
        // ---

        // 추가: 이미지를 클릭할 때 동작하는 함수
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 클릭된 아이템의 position을 전달
                Intent intent = new Intent(context, DetailActivity.class);

                intent.putExtra("currentPosition", holder.getAdapterPosition());

                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        // 수정: postList의 크기를 반환
        return postList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewTitleOverlay; // added
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewItem); // item_image.xml에 있는 ImageView
            // added
            textViewTitleOverlay = itemView.findViewById(R.id.textViewTitleOverlay);
        }
    }
}