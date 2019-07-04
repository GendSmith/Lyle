package com.example.mxz.lyle;

import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by mxz on 2019/6/26.
 */

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FriendMoment {
    private int moment_id;
    private String username,profile_pic,moment_text,address;
    private ArrayList<String> img_list=new ArrayList<>();
    private boolean is_like=false;
    private List<String> like_friend=new ArrayList<>();

    public FriendMoment(String username,String moment_text,String address){
        this.username=username;
        this.moment_text=moment_text;
        this.address=address;
        Random random=new Random();
        for(int i=0; i<5; i++){
            like_friend.add("cxk_"+random.nextInt(10));
        }
    }

    public FriendMoment(JSONObject json_object) throws JSONException {
        moment_id=json_object.getInt("id");
        username=json_object.getString("username");
        profile_pic=json_object.getString("profilePic");
        moment_text=json_object.getString("moment_text");
        address=json_object.getString("address");


        String img_list_str=json_object.getString("img_list");
        int left=-1;
        for (int i=0; i<img_list_str.length(); i++){
            if ("'".equals(img_list_str.charAt(i))){
                if (left==-1){
                    left=i;
                }
                else{
                    img_list.add(img_list_str.substring(left,i));
                    System.out.println(img_list.get(img_list.size()-1));
                    left=-1;
                }

            }
        }
        JSONArray like_friend_json=json_object.getJSONArray("likeUsernameList");
        for (int i=0; i<like_friend_json.length(); i++){
            like_friend.add(like_friend_json.getString(i));
        }

    }


    public int getMomentImageSize() {
        return img_list.size();
    }

    public String getMomentImageUrl(int i) {

        return img_list.get(i);
    }
    public List<String> getLikeFriend(){
        return like_friend;
    }

    public boolean isLike(String like_username) {
        for (int i=0; i<like_friend.size(); i++){
            if (like_friend.get(i).equals(like_username)){
                return true;
            }
        }
        return false;
    }

    public void setIsLike(boolean is_like, String like_username) {
        if (is_like){
            like_friend.add(like_username);
            return;
        }
        for (int i=0; i<like_friend.size(); i++){
            if (like_friend.get(i).equals(like_username)){
                like_friend.remove(i);
                return ;
            }
        }
        return;
    }

    public String getMomentText() {
        return moment_text;
    }

    public String getAddress() {
        return address;
    }
}
