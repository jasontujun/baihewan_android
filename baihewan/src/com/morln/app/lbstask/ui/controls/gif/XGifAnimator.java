package com.morln.app.lbstask.ui.controls.gif;

import android.view.animation.AnimationUtils;

/**
 * 封装Gif动画的相关计算
 * Created by jasontujun.
 * Date: 12-11-29
 * Time: 下午1:46
 */
public class XGifAnimator {

    private static final int INFINITE_LOOP = 0;
    private int mLoopCount;// 循环次数。小于等于0：无限循环。
    private int mRemainLoop;// 剩余次数。无限循环，则此值为0。

    private int mStartFrame;
    private int mCurrFrame;

    private long mStartTime;
    private boolean mFinished;

    private int[] mStageDelays;// 各阶段的累计间隔
    private int mTotalDelay;


    /**
     * 是否无限循环
     * @return
     */
    public boolean isInfinite() {
        return mLoopCount <= INFINITE_LOOP;
    }

    public final boolean isFinished() {
        return mFinished;
    }


    public final void forceFinished(boolean finished) {
        mFinished = finished;
    }


    public int getCurrentFrame() {
        return mCurrFrame;
    }

    public int getRemainLoop() {
        return mRemainLoop;
    }

    /**
     * Call this when you want to know the new frame index.  If it returns true,
     * the animation is not yet finished.  loc will be altered to provide the
     * new gif frame index.
     * @see #getCurrentFrame()
     */
    public boolean computeGifAnimation() {
        if (mFinished) {
            return false;
        }

        final int size = mStageDelays.length;
        final int timePassed = timePassed();
        final int currentLoop = timePassed / mTotalDelay;
        mRemainLoop = mLoopCount - 1 - currentLoop;
        if(isInfinite() || mRemainLoop > 0) {
            if (isInfinite())
                mRemainLoop = 0;

            int remain = timePassed % mTotalDelay;
            for (int i = 0; i < size; i++) {
                if (remain < mStageDelays[i]) {
                    mCurrFrame = (i + mStartFrame) % size;
                    break;
                }
            }
        } else {
            mRemainLoop = 0;
            mCurrFrame = (mStartFrame + size - 1) % size;
            mFinished = true;
        }
        return true;
    }


    public void startGifAnimation(int[] delays) {
        startGifAnimation(delays, 0, INFINITE_LOOP);
    }

    public void startGifAnimation(int[] delays, int startFrame) {
        startGifAnimation(delays, startFrame, INFINITE_LOOP);
    }

    /**
     * 开始Gif动画
     * @param delays 每一帧的间隔
     * @param startFrame
     * @param loopCount
     */
    public void startGifAnimation(int[] delays, int startFrame, int loopCount) {
        if (delays == null || delays.length <= 1)
            return;

        // 记录循环次数和剩余循环次数
        mLoopCount = loopCount;
        mRemainLoop= mLoopCount;

        // 记录开始帧和开始时间
        final int size = delays.length;
        startFrame = Math.max(0, Math.min(startFrame, size - 1));
        mStartTime = AnimationUtils.currentAnimationTimeMillis();
        mStartFrame = startFrame;

        // 计算累计时间间隔
        mTotalDelay = 0;
        mStageDelays = new int[size];
        int index = startFrame;
        for (int i = 0; i < size; i++) {
            int currIndex = index % size;
            mTotalDelay += delays[currIndex];
            mStageDelays[i] = mTotalDelay;
            index++;
        }

        mFinished = false;
    }


    public int timePassed() {
        return (int) (AnimationUtils.currentAnimationTimeMillis() - mStartTime);
    }
}
