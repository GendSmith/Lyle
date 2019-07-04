package com.example.mxz.lyle;

import android.content.Context;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by mxz on 2019/6/27.
 */

public class LikeView extends TextView {

    private Context mContext;
    private List<String> list;

    public LikeView(Context context) {
        super(context, null);
        mContext=context;
    }

    public LikeView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        mContext=context;
    }

    public LikeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    /**
     * 设置点赞数据
     *
     * @param list
     */
    public void setList(List<String> list) {
        this.list = list;
    }

    /**
     * 刷新点赞列表
     */

    public void notifyDataSetChanged() {
//        System.out.println(list.size());
        if (list == null || list.size() <= 0) {
            return;
        }
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(setImageSpan());
        for (int i = 0; i < list.size(); i++) {
            String item = list.get(i);
            builder.append(setClickableSpan(item));
            if (i != list.size() - 1) {
                builder.append(" , ");
            } else {
                builder.append(" ");
            }
        }

        setText(builder);
        setMovementMethod(new CircleMovementMethod(0xffcccccc, 0xffcccccc));
//        setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * 设置评论用户名字点击事件
     *
     * @param item
     * @param bean
     * @return
     */
    public SpannableString setClickableSpan(final String item) {
        final SpannableString string = new SpannableString(item);
        ClickableSpan span = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                // TODO: 2017/9/3 评论用户名字点击事件
                Toast.makeText(mContext, item, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                // 设置显示的文字颜色
                ds.setColor(0xff387dcc);
                ds.setUnderlineText(false);
            }
        };

        string.setSpan(span, 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return string;
    }

    /**
     * 设置点赞图标
     *
     * @return
     */
    private SpannableString setImageSpan() {
        String text = "  ";
        SpannableString imgSpanText = new SpannableString(text);
        imgSpanText.setSpan(new ImageSpan(getContext(), R.drawable.heart4, DynamicDrawableSpan.ALIGN_BASELINE),
                0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return imgSpanText;
    }




}
