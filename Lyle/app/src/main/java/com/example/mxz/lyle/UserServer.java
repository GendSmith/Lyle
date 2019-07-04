package com.example.mxz.lyle;

/**
 * Created by mxz on 2019/7/2.
 */


        import android.graphics.Bitmap;
        import org.json.JSONObject;
        import java.io.*;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.util.UUID;

public class UserServer {

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
                    HttpURLConnection conn = (HttpURLConnection)new URL("http://193.112.12.199:7899/user").openConnection();
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
                    HttpURLConnection conn = (HttpURLConnection)new URL("http://193.112.12.199:7899/user").openConnection();
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

    public JSONObject getUserById(int id) {
        if(id == -1) { return null; }
        JSONObject json2 = new JSONObject();
        try {
            JSONObject json = new JSONObject();
            json.put("type", "GetOneUserInfo");
            json.put("id", id);
            post(String.valueOf(json));
            json2 = new JSONObject(res);
        }catch (Exception e) { e.printStackTrace(); }
        return json2;
    }

    public void addUser(String username, String password) {
        try {
            JSONObject json = new JSONObject();
            json.put("type", "AddOneUser");
            json.put("username", username);
            json.put("password", password);
            json.put("gender", 1);
            json.put("age", 0);
            json.put("job", "");
            json.put("profilePicUrl", "");
            post(String.valueOf(json));
        }catch (Exception e) { e.printStackTrace(); }
    };

    public void updateUser(JSONObject user) {
        try {
            post(String.valueOf(user));
        }catch (Exception e) { e.printStackTrace(); }
    }

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

