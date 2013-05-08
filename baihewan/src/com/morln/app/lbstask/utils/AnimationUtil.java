package com.morln.app.lbstask.utils;

import android.content.Context;
import android.view.View;
import android.view.animation.*;
import android.widget.ListView;
import android.widget.ViewFlipper;
import com.morln.app.lbstask.R;

/**
 * Created by jasontujun.
 * Date: 12-5-17
 * Time: 下午4:48
 */
public class AnimationUtil {

    private static Animation leftIn;
    private static Animation rightOut;
    private static Animation rightIn;
    private static Animation leftOut;

    public static void init(Context context) {
        leftIn = AnimationUtils.loadAnimation(context, R.anim.left_in);
        rightOut = AnimationUtils.loadAnimation(context, R.anim.right_out);
        rightIn = AnimationUtils.loadAnimation(context, R.anim.right_in);
        leftOut = AnimationUtils.loadAnimation(context, R.anim.left_out);
    }

    public static void startShakeAnimation(View view, Context context) {
        view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake));
    }

    public static void startVerticalSlideAnimation(Context context, ListView listView) {
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_top_to_bottom_slide);
        listView.setLayoutAnimation(controller);
    }

    /**
     * 设置listview的动画
     * @param listView
     * @param duration
     */
    public static void startListAnimation(ListView listView, int duration) {
        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(50);
        set.addAnimation(animation);

        animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(duration);
        set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
        listView.setLayoutAnimation(controller);
    }

    public static void startListAnimation(ListView listView) {
        startListAnimation(listView, 150);
    }


    /**
     * 设置ViewFlipper的滑动动画
     * @param flipper
     * @param isPrevious
     */
    public static void prepareFlipAnimation(ViewFlipper flipper, boolean isPrevious) {
        leftIn.reset();
        rightOut.reset();
        rightIn.reset();
        leftOut.reset();
        if(isPrevious) {
            flipper.setInAnimation(leftIn);
            flipper.setOutAnimation(rightOut);
        }else {
            flipper.setInAnimation(rightIn);
            flipper.setOutAnimation(leftOut);
        }
    }

}
