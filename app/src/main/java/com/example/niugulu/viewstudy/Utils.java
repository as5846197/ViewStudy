package com.example.niugulu.viewstudy;

import android.content.Context;

/**
 * Created by zhangcaoyang on 16/8/10.
 */
public class Utils {
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
