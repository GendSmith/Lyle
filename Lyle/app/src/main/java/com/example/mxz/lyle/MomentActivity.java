package com.example.mxz.lyle;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.jar.JarInputStream;

import me.iwf.photopicker.PhotoPicker;

public class MomentActivity extends AppCompatActivity implements LocationListener {
    private ImageView cover;
    private Intent intent;

    private List<FriendMoment> friend_list = new ArrayList<>();
    private FriendAdapter adapter;
    private LikeView like_view;
    private MomentServer moment_server=new MomentServer();
    private int user_id;

    private LocationManager lm;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moment);
        user_id = getIntent().getIntExtra("id", -1);

//        System.out.println("user id in moment "+user_id);
        if(!moment_server.isConnected()) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            AlertDialog alertDialog = dialogBuilder.setTitle("警告")
                    .setMessage("无法连接到服务器，请检查网络设置！")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create();
            alertDialog.setCancelable(false);
            alertDialog.show();
        }

        cover=(ImageView)findViewById(R.id.moment_cover);


        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
                int color = Color.argb(200,0,0,0);
                collapsingToolbar.setCollapsedTitleTextColor(color);
                ImageView imageView = (ImageView) findViewById(R.id.image1);
                if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) { // 折叠状态
                    collapsingToolbar.setTitle("朋友圈");
                    imageView.setVisibility(View.GONE);
                } else { // 非折叠状态
                    collapsingToolbar.setTitle("");
                    imageView.setVisibility(View.VISIBLE);
                }
            }
        });


        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MomentActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},200);
        }
        else {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            location=lm.getLastKnownLocation(lm.getProviders(true).get(0));
            Toast.makeText(this,  "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude(),Toast.LENGTH_SHORT).show();


        }

        while (location==null){

        }


//        try {
//            initFriends();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        initFriendsRand();


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new FriendAdapter(friend_list,user_id);
        recyclerView.setAdapter(adapter);



    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()){

            case android.R.id.home:
//                Toast.makeText(this, "您点击了返回按钮", Toast.LENGTH_LONG).show();
                intent=new Intent(this,ProfileActivity.class);
                intent.putExtra("id",user_id);
                startActivity(intent);
                break;
            case R.id.enter_publish:
                intent = new Intent(this,PublishActivity.class);
                intent.putExtra("id",user_id);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_moment, menu);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 给左上角图标的左边加上一个返回的图标 。
//        return true;
//    }



    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PhotoPicker.REQUEST_CODE) {
            if (data != null) {
                ArrayList<String> photos =
                        data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
//                System.out.println(photos);
                Glide.with(MomentActivity.this)
                        .load(photos.get(0))
                        .placeholder(R.mipmap.default_error)
                        .error(R.mipmap.default_error)
                        .centerCrop()
                        .crossFade()
                        .into(cover);
            }
        }
    }

    private void initFriendsRand() {
        friend_list.clear();
        Random random=new Random();

        for (int i=0; i<20; i++){
            friend_list.add(new FriendMoment("cxk_"+random.nextInt(),"鸡你太美","篮球场"));
        }
    }
    private void initFriends() throws JSONException {
        friend_list.clear();

        JSONObject moment_json=moment_server.getMoments(user_id,0,1000,location.getLatitude(),location.getLongitude());
        JSONArray moments=moment_json.getJSONArray("moments");

        for (int i=0; i<moments.length(); i++){
            friend_list.add(new FriendMoment(moments.getJSONObject(i)));
        }

    }

    public void changeCover(View view) {
        PhotoPicker.builder()
                .setPhotoCount(1)
                .setShowCamera(true)
                .setShowGif(true)
                .setPreviewEnabled(false)
                .start(MomentActivity.this, PhotoPicker.REQUEST_CODE);

    }

    public void enterPublish(View view) {

        intent=new Intent(this,PublishActivity.class);
        intent.putExtra("id",user_id);
        startActivity(intent);
    }


    @Override
    public void onLocationChanged(Location location) {
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        location=lm.getLastKnownLocation(lm.getProviders(true).get(0));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void backProfile(View view) {
        intent=new Intent(this,ProfileActivity.class);
        intent.putExtra("id",user_id);
        startActivity(intent);
    }
}
