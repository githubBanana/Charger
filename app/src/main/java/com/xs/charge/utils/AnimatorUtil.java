package com.xs.charge.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

/**
 * @version V1.0 <动画>
 * @author: Xs
 * @date: 2016-06-08 10:14
 * @email Xs.lin@foxmail.com
 */
public class AnimatorUtil {

    public static void scaling(View view) {
        AnimatorSet animatorSe = new AnimatorSet();
        ObjectAnimator animatorX,animatorY;
        animatorX = ObjectAnimator.ofFloat(view,"scaleX", 1f,1.05f,1f);
        animatorY = ObjectAnimator.ofFloat(view,"scaleY", 1f,1.05f,1f);
        animatorSe.setDuration(1000);
        animatorSe.play(animatorX).with(animatorY);
        animatorSe.start();
    }

    public static void test(boolean action,View view) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator animatorX;
        ObjectAnimator animatorY;
        if (action) { //true:down
            animatorX = ObjectAnimator.ofFloat(view,"scaleX", 1f,1.05f,1f);
            animatorY = ObjectAnimator.ofFloat(view,"scaleY", 1f,1.05f,1f);
        } else {
            animatorX = ObjectAnimator.ofFloat(view,"scaleX", 1.05f,1f);
            animatorY = ObjectAnimator.ofFloat(view,"scaleY", 1.05f,1f);
        }
        animatorSet.setDuration(500);
        animatorSet.play(animatorX).with(animatorY);
        animatorSet.start();
    }
}
