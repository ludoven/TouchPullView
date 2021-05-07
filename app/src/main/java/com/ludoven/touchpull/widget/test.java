package com.ludoven.touchpull.widget;

import android.graphics.Point;

import java.nio.file.Path;

/**
 * author : ludoven
 * github : https://github.com/ludoven
 * date   : 2021/5/7  9:55
 */
public class test {
    private Float[] xPoints= new Float[]{0f,300f,200f,500f,700f};
    private Float[] yPoints= new Float[]{0f,300f,700f,500f,1200f};

    private final Point[][]mPoints=new Point[4][];

    private Path[] mPaths=new Path[4];
    private void initB(){
        calculateBezier(0.2f,xPoints);
    }

    private float calculateBezier(float t, Float[] values){
        //采用双层循环
        final int len = values.length;
        for (int i = len -1 ; i > 0; i--) {
            for (int j = 0; j < i; j++) {
                //计算
                values[j] = values[j] + (values[j+1] - values[j]) * t;
            }
        }



        //运算时结构保存在第一位
        //所以返回第一位
        return values[0];
    }
}
