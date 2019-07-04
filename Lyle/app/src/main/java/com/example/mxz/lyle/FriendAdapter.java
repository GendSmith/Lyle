package com.example.mxz.lyle;

/**
 * Created by mxz on 2019/6/26.
 */


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    private Context m_context;
    private List<FriendMoment> mfriend_list;
    private View view;
    private UserServer user_server=new UserServer();
    private int user_id;

//    private PopupWindow more_popupwindow;
    private int show_more_popupwindow_width,show_more_popupwindow_height;

    private AdapterView.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }


        class ViewHolder extends RecyclerView.ViewHolder {

            PopupWindow more_popupwindow;
            ImageView image_view;
            LikeView like_view;
            ImageView friend_icon=(ImageView) view.findViewById(R.id.friend_icon);
            TextView moment_text=(TextView)view.findViewById(R.id.friend_moment_text);
            TextView address=(TextView)view.findViewById(R.id.moment_address);

            Button more_button;

            ArrayList<ImageView> image_views=new ArrayList<>();

            int[]image_ids={R.id.friend_image_1,R.id.friend_image_2,R.id.friend_image_3,R.id.friend_image_4,R.id.friend_image_5,R.id.friend_image_6,R.id.friend_image_7,R.id.friend_image_8,R.id.friend_image_9};

            public ViewHolder(View view) {
                super(view);
                for (int i = 0; i<9; i++){
                    image_view=(ImageView)view.findViewById(image_ids[i]);
                    image_views.add(image_view);
                }
                more_button = (Button) view.findViewById(R.id.more_button);
                like_view=(LikeView)view.findViewById(R.id.like_view);
            }
        }

    public FriendAdapter(List<FriendMoment> friend_list,int user_id) {
        mfriend_list = friend_list;
        this.user_id=user_id;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (m_context == null) {
            m_context = parent.getContext();
        }
        view = LayoutInflater.from(m_context).inflate(R.layout.friend_moment, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        System.out.println("onBindViewHolder"+position);



        final FriendMoment m_friend_moment =mfriend_list.get(position);
        holder.friend_icon.setImageResource(R.drawable.cxk);
        holder.moment_text.setText(m_friend_moment.getMomentText());
        holder.address.setText(m_friend_moment.getAddress());
//        int size= m_friend_moment.getMomentImageSize();
        int size=new Random().nextInt(9);
        for(int i=0; i<size; i++){
            holder.image_views.get(i).setVisibility(View.VISIBLE);
//            Bitmap bmp=getBitmapFromUrl(m_friend_moment.getMomentImageUrl(i));
//            holder.image_views.get(i).setImageBitmap(bmp);

            holder.image_views.get(i).setImageResource(R.drawable.cxk);

        }
        for (int i=size; i<9; i++){
            holder.image_views.get(i).setVisibility(View.GONE);
        }


        holder.like_view.setList(m_friend_moment.getLikeFriend());
        holder.like_view.notifyDataSetChanged();

        holder.more_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMore(v,m_friend_moment,holder);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mfriend_list.size();
    }

    private void showMore(final View view, final FriendMoment friend_moment, final ViewHolder holder){
        if (holder.more_popupwindow == null) {
            LayoutInflater li = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View content = li.inflate(R.layout.like_comment_item, null, false);
            holder.more_popupwindow = new PopupWindow(content, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            holder.more_popupwindow.setBackgroundDrawable(new BitmapDrawable());
            holder.more_popupwindow.setOutsideTouchable(true);
            holder.more_popupwindow.setTouchable(true);
            content.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            show_more_popupwindow_width = content.getMeasuredWidth();
            show_more_popupwindow_height = content.getMeasuredHeight();
            final View parent = holder.more_popupwindow.getContentView();
            final TextView like = (TextView) parent.findViewById(R.id.like);
            final TextView comment = (TextView) parent.findViewById(R.id.comment);
            // 点赞的监听器
            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String user_name="我才是蔡徐坤";

//                    try {
//                        user_name=user_server.getUserById(user_id).getString("username");
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
                    if (friend_moment.isLike(user_name)){
                        parent.findViewById(R.id.like_image).setBackgroundResource(R.drawable.heart1);
                        friend_moment.setIsLike(false,user_name);
                        holder.like_view.setList(friend_moment.getLikeFriend());
                        System.out.println(friend_moment.getLikeFriend().size());
                        holder.like_view.notifyDataSetChanged();
                    }
                    else {
                        parent.findViewById(R.id.like_image).setBackgroundResource(R.drawable.heart2);
                        friend_moment.setIsLike(true,user_name);
                        holder.like_view.setList(friend_moment.getLikeFriend());
                        System.out.println(friend_moment.getLikeFriend().size());
                        holder.like_view.notifyDataSetChanged();
                    }

                }
            });

            // 评论的监听器
//            comment.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });
        }

        if (holder.more_popupwindow.isShowing()) {
            holder.more_popupwindow.dismiss();
        } else {
            int heightMoreBtnView = view.getHeight();

            holder.more_popupwindow.showAsDropDown(view, -show_more_popupwindow_width,
                    -(show_more_popupwindow_height + heightMoreBtnView) / 2);
        }
    }

    public Bitmap getBitmapFromUrl(String url_str){
        URL url = null;
        try {
            url = new URL(url_str);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        assert url != null;
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection)url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream is = null;
        try {
            is = conn.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bmp = BitmapFactory.decodeStream(is);
        return bmp;
    }

}
