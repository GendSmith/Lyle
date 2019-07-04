package com.example.mxz.lyle;

/**
 * Created by mxz on 2019/7/2.
 */



import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import com.bumptech.glide.Glide;

import org.json.JSONObject;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import me.iwf.photopicker.*;

public class ProfileActivity extends AppCompatActivity {

    private UserServer userServer = new UserServer();
    private int id;
    private Button uploadButton, editButton, saveButton;
    private EditText[] editText;
    private ToggleButton toggleButton;
    private ImageView imageView;
    private String iconUrl = "";

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            imageView.setImageBitmap((Bitmap)msg.obj);
        }
    };

    private void showProfilePic(final String profilePic) {
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(profilePic);
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    InputStream is = conn.getInputStream();
                    Bitmap bmp = BitmapFactory.decodeStream(is);
                    Message msg = new Message();
                    msg.obj = bmp;
                    handler.sendMessage(msg);
                    is.close();
                }catch (Exception e) { e.printStackTrace(); }
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        id = getIntent().getIntExtra("id", -1);
        JSONObject json = userServer.getUserById(id);

        uploadButton = (Button)findViewById(R.id.button3);
        editButton = (Button)findViewById(R.id.button4);
        saveButton = (Button)findViewById(R.id.button5);
        editText = new EditText[4];
        editText[0] = (EditText)findViewById(R.id.editText3);
        editText[1] = (EditText)findViewById(R.id.editText4);
        editText[2] = (EditText)findViewById(R.id.editText6);
        editText[3] = (EditText)findViewById(R.id.editText7);
        toggleButton = (ToggleButton)findViewById(R.id.toggleButton);
        imageView = (ImageView)findViewById(R.id.imageView);

        try {
            editText[0].setText(json.getString("username"));
            editText[1].setText(json.getString("password"));
            editText[2].setText(String.valueOf(json.getInt("age")));
            editText[3].setText(json.getString("job"));
            toggleButton.setChecked(json.getInt("gender") > 0);
            iconUrl = json.getString("profilePic");
            showProfilePic(iconUrl);
        }catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.support_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.moment:
                Intent intent = new Intent(this, MomentActivity.class);
                intent.putExtra("id", id);
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void upload(View v) {
        //TODO: 从手机中选择一张图片，在imageView里显示出来（上传到服务器的工作已由下面的语句完成）
        PhotoPicker.builder()
                .setPhotoCount(1)
                .setShowCamera(true)
                .setShowGif(true)
                .setPreviewEnabled(false)
                .start(ProfileActivity.this, PhotoPicker.REQUEST_CODE);


    }

    public void edit(View v) {
        uploadButton.setVisibility(View.VISIBLE);
        editButton.setEnabled(false);
        saveButton.setEnabled(true);
        for(int i = 0; i < 4; i++)
            editText[i].setEnabled(true);
        toggleButton.setEnabled(true);
    }

    public void save(View v) {
        if(editText[2].getText().toString().length() == 0 || editText[2].getText().toString().length() >= 4) {
            Toast.makeText(this, "年龄不合法！", Toast.LENGTH_SHORT).show();
            editText[2].requestFocus();
            return;
        }

        String username = editText[0].getText().toString();
        String password = editText[1].getText().toString();
        boolean gender = toggleButton.isChecked();
        int age = Integer.parseInt(editText[2].getText().toString());
        String job = editText[3].getText().toString();

        if(username.length() < 4 || username.length() > 32) {
            Toast.makeText(this, "用户名不合法！", Toast.LENGTH_SHORT).show();
            editText[0].requestFocus();
            return;
        }
        if(password.length() < 4 || password.length() > 32) {
            Toast.makeText(this, "密码不合法！", Toast.LENGTH_SHORT).show();
            editText[1].requestFocus();
            return;
        }

        int tempID = userServer.getIdByUsername(username);
        if(tempID != id && tempID != -1) {
            Toast.makeText(this, "用户名已存在，请换一个", Toast.LENGTH_SHORT).show();
            editText[0].requestFocus();
            return;
        }

        try {
            JSONObject json = new JSONObject();
            json.put("type", "UpdateUserInfo");
            json.put("username", username);
            json.put("password", password);
            json.put("gender", gender?1:0);
            json.put("age", age);
            json.put("job", job);
            json.put("profilePicUrl", iconUrl);
            json.put("user_id", id);
            userServer.updateUser(json);
        }catch (Exception e) { e.printStackTrace(); }

        uploadButton.setVisibility(View.INVISIBLE);
        editButton.setEnabled(true);
        saveButton.setEnabled(false);
        for(int i = 0; i < 4; i++)
            editText[i].setEnabled(false);
        toggleButton.setEnabled(false);
    }
    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PhotoPicker.REQUEST_CODE) {
            if (data != null) {
                ArrayList<String> photos =
                        data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                System.out.println(photos);
                Glide.with(ProfileActivity.this)
                        .load(photos.get(0))
                        .placeholder(R.mipmap.default_error)
                        .error(R.mipmap.default_error)
                        .centerCrop()
                        .crossFade()
                        .into(imageView);

                imageView.setDrawingCacheEnabled(true);
                iconUrl = userServer.uploadPic(imageView.getDrawingCache());
                imageView.setDrawingCacheEnabled(false);
            }
        }
    }
}

