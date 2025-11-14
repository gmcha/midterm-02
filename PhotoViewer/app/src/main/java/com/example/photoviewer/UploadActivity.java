package com.example.photoviewer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UploadActivity extends AppCompatActivity {
    private EditText editTextTitle;
    private EditText editTextDescription;
    private Button btnSelectImage;
    private ImageView imageViewPreview;
    private Button btnSubmitUpload;
    private Button btnCancelUpload;

    private Uri selectedImageUri = null;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private final String site_url = "https://gmcha0323.pythonanywhere.com"; //http://10.0.2.2:8000
    private final String token = "39d178b77e6d46b3a32eb523dd920524103dbd86";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upload);

        editTextTitle = findViewById(R.id.edit_text_title);
        editTextDescription = findViewById(R.id.edit_text_description);
        btnSelectImage = findViewById(R.id.btn_select_image);
        imageViewPreview = findViewById(R.id.image_view_preview);
        btnSubmitUpload = findViewById(R.id.btn_submit_upload);
        btnCancelUpload = findViewById(R.id.btn_cancel_upload);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        imageViewPreview.setImageURI(selectedImageUri);
                    }
                });

        btnSelectImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

            pickImageLauncher.launch(intent);
        });

        btnSubmitUpload.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString();
            String text = editTextDescription.getText().toString();

            if (title.isEmpty() || text.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedImageUri == null) {
                Toast.makeText(this, "이미지를 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            new PutPost().execute(title, text, selectedImageUri.toString());
        });

        btnCancelUpload.setOnClickListener(v -> {
            finish();
        });
    }

    private String getMimeType(Uri uri) {
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType;
    }

    private class PutPost extends AsyncTask<String, Void, Boolean> {

        private final String lineEnd = "\r\n";
        private final String twoHyphens = "--";
        private final String boundary = "===" + System.currentTimeMillis() + "==="; // Multipart 경계

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(UploadActivity.this, "업로드를 시작합니다...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String title = params[0];
            String text = params[1];
            Uri imageUri = Uri.parse(params[2]);

            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            InputStream inputStream = null;

            try {
                URL url = new URL(site_url + "/api_root/Post/");
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");

                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                dos = new DataOutputStream(conn.getOutputStream());

                addFormField(dos, "author", "1");
                addFormField(dos, "title", title);
                addFormField(dos, "text", text);

                String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()).format(new Date());
                addFormField(dos, "created_date", timestamp);
                addFormField(dos, "published_date", timestamp);

                String mimeType = getMimeType(imageUri);
                String fileName = "upload.jpg";

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"" + fileName + "\"" + lineEnd);
                dos.writeBytes("Content-Type: " + (mimeType != null ? mimeType : "application/octet-stream") + lineEnd);
                dos.writeBytes("Content-Transfer-Encoding: binary" + lineEnd);
                dos.writeBytes(lineEnd);

                inputStream = getContentResolver().openInputStream(imageUri);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    dos.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                dos.writeBytes(lineEnd);

                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                dos.flush();
                dos.close();

                int responseCode = conn.getResponseCode();
                return (responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_OK);

            } catch (Exception e) {
                Log.e("PutPost", "Multipart Upload Error", e);
                return false;
            } finally {
                if (inputStream != null) try { inputStream.close(); } catch (Exception e) {}
                if (dos != null) try { dos.close(); } catch (Exception e) {}
                if (conn != null) conn.disconnect();
            }
        }

        private void addFormField(DataOutputStream dos, String name, String value) throws Exception {
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"" + lineEnd);
            dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
            dos.writeBytes(lineEnd);
            dos.write(value.getBytes("UTF-8"));
            dos.writeBytes(lineEnd);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (success) {
                Toast.makeText(UploadActivity.this, "업로드 성공!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(UploadActivity.this, "업로드 실패. 네트워크나 서버를 확인하세요.", Toast.LENGTH_LONG).show();
            }
        }
    }
}