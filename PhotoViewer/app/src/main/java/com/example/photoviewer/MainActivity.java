package com.example.photoviewer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.example.photoviewer.Post;
import com.example.photoviewer.DataHolder;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    ImageView imgView;
    TextView textView;
    String site_url = "https://gmcha0323.pythonanywhere.com"; //"http://10.0.2.2:8000"
    JSONObject post_json;
    String imageUrl = null;
    // Bitmap bmImg = null;

    CloadImage taskDownload;
    //PutPost taskUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //imgView = (ImageView) findViewById(R.id.imgView);
        textView = (TextView)findViewById(R.id.textView);
    }

    public void onClickDownload(View v) {
        if (taskDownload != null && taskDownload.getStatus() == AsyncTask.Status.RUNNING) {
            taskDownload.cancel(true);
        }
        taskDownload = new CloadImage();
        taskDownload.execute(site_url + "/api_root/Post/");
        Toast.makeText(getApplicationContext(), "이미지를 불러옵니다.", Toast.LENGTH_LONG).show();
    }

    public void onClickUpload(View v) {
        Intent intent = new Intent(MainActivity.this, UploadActivity.class);
        startActivity(intent);
//        Toast.makeText(getApplicationContext(), "Upload", Toast.LENGTH_LONG).show();
    }

    // 동기화 로딩 구현을 위해 추가:
    private static class ImageLoadResult {
        final List<Post> posts;
        final boolean success;
        final String errorMessage;

        // 성공 시 생성자
        ImageLoadResult(List<Post> posts) {
            this.posts = posts;
            this.success = true;
            this.errorMessage = null;
        }

        // 실패 시 생성자
        ImageLoadResult(String errorMessage) {
            this.posts = new ArrayList<>();
            this.success = false;
            this.errorMessage = errorMessage;
        }
    }

    // 수정: CloadImage가 List<Post>를 리턴하도록 변경
    private class CloadImage extends AsyncTask<String, Integer, ImageLoadResult>{
        @Override
        protected ImageLoadResult doInBackground(String... urls) {
            List<Post> postList = new ArrayList<>();

            try {
                String apiUrl = urls[0];
                String token = "39d178b77e6d46b3a32eb523dd920524103dbd86";
                URL urlAPI = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    is.close();

                    String strJson = result.toString();
                    JSONArray aryJson = new JSONArray(strJson);

                    // 배열 내 모든 이미지 다운로드
                    for (int i = 0; i < aryJson.length(); i++) {
                        post_json = (JSONObject) aryJson.get(i);

                        // 수정: JSON에서 title, text, imageUrl 추출
                        String title = post_json.getString("title");
                        String text = post_json.getString("text");
                        imageUrl = post_json.getString("image");

                        Bitmap imageBitmap = null;
                        if (!imageUrl.equals("")) {
                            URL myImageUrl = new URL(imageUrl);
                            conn = (HttpURLConnection) myImageUrl.openConnection();
                            InputStream imgStream = conn.getInputStream();
                            imageBitmap = BitmapFactory.decodeStream(imgStream);
                            imgStream.close();
                        }

                        // 수정: Post 객체를 생성 후 사용
                        postList.add(new Post(title, text, imageUrl, imageBitmap));
                    }
                    return new ImageLoadResult(postList);
                } else {
                    return new ImageLoadResult("서버 응답 오류: " + responseCode);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return new ImageLoadResult("데이터 처리 중 오류 발생: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(ImageLoadResult result) {
            if(result.success){
                if (result.posts.isEmpty()) { // .images에서 .posts로 변경
                    textView.setText("불러올 이미지가 없습니다.");
                } else {
                    textView.setText("이미지 갤러리\n (새로 게시한 이미지를 확인하기 위해 동기화 버튼을 또 눌러주세요.)");
                    Toast.makeText(MainActivity.this, "이미지 로드 완료!", Toast.LENGTH_LONG).show();

                    // 추가: DataHolder Singleton에 전체 Post 리스트 저장
                    DataHolder.getInstance().setPostList(result.posts);

                    RecyclerView recyclerView = findViewById(R.id.recyclerView);

                    // 수정: ImageAdapter 생성자에 Context와 Post 리스트 전달
                    ImageAdapter adapter = new ImageAdapter(MainActivity.this, result.posts);

                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    recyclerView.setAdapter(adapter);
                }
            } else {
                textView.setText("이미지를 불러오는데 실패했습니다.");
                // UploadActivity와 유사하게 Toast로 구체적인 오류 메시지 표시
                Toast.makeText(MainActivity.this,
                        "이미지 로드 실패. " + result.errorMessage,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}