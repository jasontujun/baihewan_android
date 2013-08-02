package com.morln.app.lbstask.ui.controls;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by jasontujun.
 * Date: 12-2-21
 * Time: 下午9:44
 */
public class ChangeImageTouchListener implements View.OnTouchListener{

    /**
     * 触摸点在按钮的范围之内是有效点，如果触摸点出了按钮的范围，那么就应当视为无效点。此时就不应该再产生OnRelease的效果
     */
    private boolean goodTouch;

    /**
     * 触摸按钮的事件
     */
    private OnPressListener onPressListener;

    /**
     * 内部做一些预先准备和善后处理的事情
     */
    private InnerBeforeAfterListener innerBeforeAfterListener;

    /**
     * 外部做一些预先准备和善后处理的事情
     */
    private OuterBeforeAfterListener outerBeforeAfterListener;

    @Override
    public boolean onTouch(final View view, final MotionEvent e) {
        switch(e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                goodTouch = true;
                if (innerBeforeAfterListener != null) {
                    innerBeforeAfterListener.beforeFirstTouch(view);
                }
                if (outerBeforeAfterListener != null) {
                    outerBeforeAfterListener.beforeFirstTouch(view);
                }
                if(onPressListener != null) {
                    onPressListener.onFirstTouch(view, e);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(e.getX() < 0 ||
                        e.getY() < 0 ||
                        e.getX() > view.getWidth() ||
                        e.getY() > view.getHeight()) {
                    goodTouch = false;
                }else {
                    goodTouch = true;
                }

                if(onPressListener != null) {
                    onPressListener.onMoving(view, e);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (innerBeforeAfterListener != null) {
                    innerBeforeAfterListener.afterRelease(view);
                }
                if (outerBeforeAfterListener != null) {
                    outerBeforeAfterListener.afterRelease(view);
                }
                if (goodTouch) {
                    if (onPressListener != null) {
                        onPressListener.onRelease(view, e);
                    }
                }
                break;
        }
        return true;
    }

    /**
     * 设置OnPressListener
     */
    public void setOnPressListener(OnPressListener onPressListener) {
        this.onPressListener = onPressListener;
    }

    /**
     * 设置BeforeAfterListener
     */
    public void setInnerBeforeAfterListener(InnerBeforeAfterListener innerBeforeAfterListener) {
        this.innerBeforeAfterListener = innerBeforeAfterListener;
    }

    /**
     * 返回OnPressListener
     */
    public OnPressListener getOnPressListener() {
        return onPressListener;
    }

    public OuterBeforeAfterListener getOuterBeforeAfterListener() {
        return outerBeforeAfterListener;
    }

    public void setOuterBeforeAfterListener(OuterBeforeAfterListener outerBeforeAfterListener) {
        this.outerBeforeAfterListener = outerBeforeAfterListener;
    }

    /**
     * 监听手指点击按钮时候的各种事件
     */
    public static interface OnPressListener {
        /**
         * 刚刚碰到按钮的时候调用
         */
        void onFirstTouch(View view, MotionEvent e);
        /**
         * 手指在按钮上移动的时候调用
         */
        void onMoving(View view, MotionEvent e);
        /**
         * 释放按钮的时候调用（必须是有效的触摸事件）
         */
        void onRelease(View view, MotionEvent e);
    }

    /**
     * 在调用OnPressListener的之前或者之后调用，主要目的是组件内部为了播放声音准备图片这种事情
     */
    public static interface InnerBeforeAfterListener {
        /**
         * 在onFirstTouch之前调用
         */
        void beforeFirstTouch(View view);

        /**
         * 在onRelease之后调用
         * @param view
         */
        void afterRelease(View view);
    }

    /**
     * 在调用OnPressListener的之前或者之后调用，主要目的是外部监听能处理事情
     */
    public static interface OuterBeforeAfterListener {
        /**
         * 在onFirstTouch之前调用
         */
        void beforeFirstTouch(View view);

        /**
         * 在onRelease之后调用
         * @param view
         */
        void afterRelease(View view);
    }
}
