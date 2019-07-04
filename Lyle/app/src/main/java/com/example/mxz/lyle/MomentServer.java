package com.example.mxz.lyle;

/**
 * Created by mxz on 2019/7/2.
 */

import android.graphics.Bitmap;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;




import android.graphics.Bitmap;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class MomentServer {

    private String res;
    private boolean connected = true;

    static String getInputStreamText(InputStream is) throws Exception {
        InputStreamReader isr = new InputStreamReader(is, "utf8");
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            sb.append(line);
        return sb.toString();
    }

    private void post(final String json) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection)new URL("http://193.112.12.199:7899/moment").openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Charset", "UTF-8");
                    conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();
                    os.write(json.getBytes());
                    os.close();
                    res = getInputStreamText(conn.getInputStream());
                }catch (Exception e) { e.printStackTrace(); }
            }
        };
        thread.start();
        try{
            thread.join();
        }catch (InterruptedException e) { e.printStackTrace(); }
    }

    public boolean isConnected() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    HttpURLConnection conn = (HttpURLConnection)new URL("http://193.112.12.199:7899/moment").openConnection();
                    conn.setConnectTimeout(3000);
                    conn.getResponseCode();
                }catch (Exception e) { connected = false; }
            }
        };
        thread.start();
        try{
            thread.join();
        }catch (InterruptedException e) { e.printStackTrace(); }
        return connected;
    }

    public int getIdByUsername(String username) {
        int ans = -1;
        try{
            JSONObject json = new JSONObject();
            json.put("type", "GetIdByUserName");
            json.put("username", username);
            post(String.valueOf(json));
            JSONObject json2 = new JSONObject(res);
            ans = json2.getInt("id");
        }catch (Exception e) { e.printStackTrace(); }
        return ans;
    }

    public void likeMoment(int user_id,int moment_id) {
        int ans = -1;
        try{
            JSONObject json = new JSONObject();
            json.put("type", "LikeMoment");
            json.put("user_id", user_id );
            json.put("moment_id",moment_id);
            post(String.valueOf(json));

        }catch (Exception e) { e.printStackTrace(); }

    }

    public JSONObject getMoments(int user_id,int begin,int distance,double location_x,double location_y) {
        if(user_id == -1) { return null; }
        JSONObject json2 = new JSONObject();
        try {
            JSONObject json = new JSONObject();
            json.put("type", "GetMoments");
            json.put("user_id", user_id);
            json.put("begin",begin);
            json.put("distance",distance);
            json.put("location_x",location_x);
            json.put("location_y",location_y);
            post(String.valueOf(json));
            json2 = new JSONObject(res);
        }catch (Exception e) { e.printStackTrace(); }
        return json2;
    }

    public void addMoment(int user_id, String moment_text, ArrayList<String> img_list,double location_x,double location_y,String address) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "PublishOneMoment");
            json.put("user_id", user_id);
            json.put("moment_text", moment_text);
            json.put("img_list", img_list.toString());
            json.put("location_x", location_x);
            json.put("location_y",location_y);
            json.put("address",address);
            post(String.valueOf(json));
        }catch (Exception e) { e.printStackTrace(); }
    };



    public String uploadPic(final Bitmap bmp) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    String boundary = UUID.randomUUID().toString();
                    HttpURLConnection conn = (HttpURLConnection)new URL("http://193.112.12.199:7899/upload/img").openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                    conn.setRequestProperty("Charset", "utf-8");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);
                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                    String s = "--" + boundary + "\r\nContent-Disposition: form-data; name=\"img\"; filename=\"icon.png\"\r\nContent-Type: application/octet-stream; charset=utf-8\r\n\r\n";
                    dos.write(s.getBytes());
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    dos.write(baos.toByteArray());
                    dos.write(("\r\n--" + boundary + "--\r\n").getBytes());
                    dos.flush();
                    JSONObject json = new JSONObject(getInputStreamText(conn.getInputStream()));
                    res = "http://" + json.getString("url");
                }catch (Exception e) { e.printStackTrace(); }
            }
        };
        thread.start();
        try {
            thread.join();
        }catch (InterruptedException e) { e.printStackTrace(); }
        return res;
    }
}

