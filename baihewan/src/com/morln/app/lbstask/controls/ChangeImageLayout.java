package com.morln.app.lbstask.controls;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import com.xengine.android.system.ui.XUIFrame;

/**
 * 可根据触摸变换背景的图层
 * TODO 图片加载没有使用XBaseLayer，所加载的图片没有经过它的管理！
 * Created by jasontujun.
 * Date: 12-3-10
 * Time: 下午2:38
 */
public class ChangeImageLayout extends RelativeLayout{
    /**
     * 正常状态下的图片
     */
    private Drawable normalImage;

    /**
     * 按下去的时候的图片
     */
    private Drawable pressedImage;

    /**
     * 按键触摸的listener
     */
    private ChangeImageTouchListener touchListener;

    public ChangeImageLayout(Context context) {
        super(context);
        init(context);
    }

    public ChangeImageLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChangeImageLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * 设置按钮的OnReleaseListener
     */
    public void setOnPressListener(ChangeImageTouchListener.OnPressListener listener) {
        touchListener.setOnPressListener(listener);
    }

    /**
     * 返回OnPressListener
     */
    public ChangeImageTouchListener.OnPressListener getOnPressListener() {
        return touchListener.getOnPressListener();
    }

    public void setOuterBeforeAfterListener(ChangeImageTouchListener.OuterBeforeAfterListener listener){
        touchListener.setOuterBeforeAfterListener(listener);
    }

    public ChangeImageTouchListener.OuterBeforeAfterListener getOuterBeforeAfterListener(){
        return touchListener.getOuterBeforeAfterListener();
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        // 取消按钮的OnClick动作
    }

    /**
     * 设置按钮的正常图片和按下图片。
     * 通过调用setNormalImageResource，setPressedImageResource
     * @param frame
     * @param normalPicName
     * @param pressedPicName
     */
    public void setLayoutImage(XUIFrame frame,
                            String normalPicName, String pressedPicName) {
        setNormalImage(frame, normalPicName);
        setPressedImage(frame, pressedPicName);
        frame.setViewBackground(this, normalPicName);
    }

    /**
     * 设置按钮的正常图片和按下图片。
     * 通过调用setNormalImageResource，setPressedImageResource
     * @param normalImageResource
     * @param pressedImageResource
     */
    public void setLayoutImage(Integer normalImageResource,
                            Integer pressedImageResource) {
        setNormalImage(normalImageResource);
        setPressedImage(pressedImageResource);
        this.setBackgroundResource(normalImageResource);
    }

    /**
     * 还原背景
     */
    public void resetBg(){
        setBackgroundDrawable(normalImage);
        invalidate();
    }

    /**
     * 设置按钮正常状态的图片（图片在res/drawable）
     */
    private void setNormalImage(Integer normalImageResource) {
        Resources res = getContext().getResources();
        this.normalImage = res.getDrawable(normalImageResource);
    }

    /**
     * 设置按钮正常状态的图片。（图片在assets）
     * @param frame
     * @param picName
     */
    private void setNormalImage(XUIFrame frame, String picName){
        Bitmap bitmap = frame.getBitmap(picName);
        this.normalImage = new BitmapDrawable(bitmap);
    }

    /**
     * 设置按钮按下状态下的图片（图片在res/drawable）
     */
    private void setPressedImage(Integer pressedImageResource) {
        Resources res = getContext().getResources();
        this.normalImage = res.getDrawable(pressedImageResource);
    }

    /**
     * 设置按钮按下状态下的图片（图片在assets）
     * @param frame
     * @param picName
     */
    private void setPressedImage(XUIFrame frame, String picName) {
        Bitmap bitmap = frame.getBitmap(picName);
        this.pressedImage = new BitmapDrawable(bitmap);
    }

    /**
     * 进行初始化
     */
    private void init(Context context) {
        touchListener = new ChangeImageTouchListener();
        touchListener.setInnerBeforeAfterListener(new ChangeImageTouchListener.InnerBeforeAfterListener() {
            @Override
            public void beforeFirstTouch(View view) {
                if (pressedImage != null) {
                    setBackgroundDrawable(pressedImage);
                    invalidate();
                }
            }

            @Override
            public void afterRelease(View view) {
                /*if(soundEffect != null) {
                    soundManager.playEffect(soundEffect, SoundManager.NO_LOOP);
                }else {
                    soundManager.playEffect(DEF_SOUND_EFFECT, SoundManager.NO_LOOP);
                }*/
                if (normalImage != null) {
                    setBackgroundDrawable(normalImage);
                    invalidate();
                }
            }
        });

        setOnTouchListener(touchListener);

        /*if(!soundManager.isInited()) {
            soundManager.init(context);
        }*/
    }

}
