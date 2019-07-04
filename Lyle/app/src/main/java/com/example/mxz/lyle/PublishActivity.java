package com.example.mxz.lyle;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import me.iwf.photopicker.*;
import me.iwf.photopicker.adapter.PhotoGridAdapter;

public class PublishActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, LocationListener {

    private static final int LOCATION_CODE = 6666;
    private Intent intent;
    private GridView grid_view;
    private EditText edit_text;
    private TextView address_text;

    private ArrayList<String> photo_paths = new ArrayList<>();
    private GridAdapter grid_adapter;
    private LocationManager lm;
    private Location location;
    private MomentServer moment_server=new MomentServer();
    private int user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        grid_view = (GridView) findViewById(R.id.image_grid_view);
        edit_text = (EditText) findViewById(R.id.publish_text);
        address_text=(TextView)findViewById(R.id.address);
        user_id = getIntent().getIntExtra("id",-1);
        System.out.println("user id in publish "+user_id);

        photo_paths.add("add_photo");
        int cols = getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().densityDpi;
        cols = cols < 3 ? 3 : cols;
        grid_view.setNumColumns(cols);
        grid_adapter = new GridAdapter(photo_paths);
        grid_view.setAdapter(grid_adapter);
        grid_view.setOnItemClickListener(this);

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(PublishActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},200);
        }
        else {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            location=lm.getLastKnownLocation(lm.getProviders(true).get(0));
//            Toast.makeText(this,  "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude(),Toast.LENGTH_SHORT).show();

            locationDecode(location);

        }

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

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 200://刚才的识别码
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){//用户同意权限,执行我们的操作
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                }else{//用户拒绝之后,当然我们也可以弹出一个窗口,直接跳转到系统设置页面
                    Toast.makeText(PublishActivity.this,"未开启定位权限,请手动到设置去开启权限",Toast.LENGTH_LONG).show();
                }
                break;
            default:break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_publish, menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 给左上角图标的左边加上一个返回的图标 。
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
//                Toast.makeText(this, "您点击了返回按钮", Toast.LENGTH_LONG).show();
                intent=new Intent(this,MomentActivity.class);
                startActivity(intent);
                break;
            case R.id.publish_button:
//                check publish content function
                Bitmap bitmap;
                ArrayList<String> img_list=new ArrayList<>();
                ListAdapter adapter=grid_view.getAdapter();
                for (int i=0; i<photo_paths.size(); i++) {
                    if (photo_paths.get(i)=="add_photo")
                        continue;
                    FileInputStream fis = null;
                    try {
                        fis = new FileInputStream(photo_paths.get(i));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    bitmap  = BitmapFactory.decodeStream(fis);
                    img_list.add(moment_server.uploadPic(bitmap));

                }
                moment_server.addMoment(user_id,edit_text.getText().toString(),img_list,location.getLatitude(),location.getLongitude(),address_text.getText().toString());
//                System.out.println("publish");
                intent=new Intent(this,MomentActivity.class);
                intent.putExtra("id",user_id);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PhotoPicker.REQUEST_CODE) {
            if (data != null) {
                ArrayList<String> photos =
                        data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
//                System.out.println(photos);
                loadAdapter(photos);
            }
        }
    }


    private void loadAdapter(ArrayList<String> photos) {
//        if (photo_paths!=null&&photo_paths.size()>0){
//            photo_paths.clear();
//        }
        if (photo_paths.size()>0&&photo_paths.get(photo_paths.size()-1)=="add_photo"){
            photo_paths.remove(photo_paths.size()-1);
        }
        photo_paths.addAll(photos);
        if (photo_paths.size()<9){
            photo_paths.add("add_photo");
        }
        grid_adapter=new GridAdapter(photo_paths);
        grid_view.setAdapter(grid_adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (photo_paths.get(position)=="add_photo"){
//            Toast.makeText(this, photo_paths.get(position), Toast.LENGTH_SHORT).show();
            PhotoPicker.builder()
                    .setPhotoCount((photo_paths.size()>0&&photo_paths.get(photo_paths.size()-1)=="add_photo")?10-photo_paths.size():9-photo_paths.size())
                    .setShowCamera(true)
                    .setShowGif(true)
                    .setPreviewEnabled(false)
                    .start(PublishActivity.this, PhotoPicker.REQUEST_CODE);
        }
        else{
            photo_paths.remove(position);
            if (photo_paths.size()==8&&photo_paths.get(7)!="add_photo") {
                photo_paths.add("add_photo");
            }
            grid_adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
//        txtLat = (TextView) findViewById(R.id.textview1);
//        txtLat.setText("Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude());
        Toast.makeText(this,  "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude(),Toast.LENGTH_SHORT).show();


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        location=lm.getLastKnownLocation(lm.getProviders(true).get(0));
//            Toast.makeText(this,  "Latitude:" + location.getLatitude() + ", Longitude:" + location.getLongitude(),Toast.LENGTH_SHORT).show();

        locationDecode(location);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    private void locationDecode(Location location){
        Geocoder geocoder = new Geocoder(this);
        boolean flag = Geocoder.isPresent();
        if (flag&&location!=null) {
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    String sAddress;
                    if (!TextUtils.isEmpty(address.getLocality())) {
                        if (!TextUtils.isEmpty(address.getFeatureName())) {
                            //市和周边地址
                            sAddress = address.getLocality() + " " + address.getFeatureName();
                        } else {
                            sAddress = address.getLocality();
                        }
                    } else {
                        sAddress = "定位失败";
                    }

                    address_text.setText(sAddress);
                }
            } catch (IOException e) {
            }
        }
    }
    private class GridAdapter extends BaseAdapter{
        private ArrayList<String> list_urls;
        private LayoutInflater inflater;
        public GridAdapter(ArrayList<String> photo_paths){
            this.list_urls=photo_paths;
            inflater = LayoutInflater.from(PublishActivity.this);
        }
        @Override
        public int getCount() {
            return list_urls.size();
        }

        @Override
        public Object getItem(int position) {
            return list_urls.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder=null;
            if (convertView==null){
                holder=new ViewHolder();
                convertView=inflater.inflate(R.layout.photo_item,parent,false);
                holder.image=(ImageView)convertView.findViewById(R.id.image_view);
                convertView.setTag(holder);
            }
            else{
                holder=(ViewHolder)convertView.getTag();
            }
            final String path=list_urls.get(position);
            if (path.equals("add_photo")){
                holder.image.setImageResource(R.mipmap.find_add_img);
            }
            else{
                Glide.with(PublishActivity.this)
                        .load(path)
                        .placeholder(R.mipmap.default_error)
                        .error(R.mipmap.default_error)
                        .centerCrop()
                        .crossFade()
                        .into(holder.image);
            }
            return convertView;

        }
        class ViewHolder{
            ImageView image;
        }
    }
}
