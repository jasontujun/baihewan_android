package com.morln.app.lbstask.controls;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Scroller;
import com.xengine.android.utils.XLog;

import java.util.NoSuchElementException;

/**
 * 一个扩充功能的容器，对放入其中的子组件添加缩放功能。
 * Created by jasontujun.
 * Date: 12-11-15
 * Time: 下午7:38
 */
public class XZoomHolder extends ViewGroup
        implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        ScaleGestureDetector.OnScaleGestureListener, Runnable {

    public static final String TAG = "ZOOM";

    public static final float   MIN_SCALE         = 1.0f;
    public static final float   MAX_SCALE         = 5.0f; // 最大放大到5倍。
    private static final int    MOVING_DIAGONALLY = 0;
    private static final int    MOVING_LEFT       = 1;
    private static final int    MOVING_RIGHT      = 2;
    private static final int    MOVING_UP         = 3;
    private static final int    MOVING_DOWN       = 4;
    private static final int    FLING_MARGIN      = 100;

    private static final int    DEFAULT_DURATION  = 400;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    private boolean mScaling;// Whether the user is currently pinch(收缩) zooming
    private float mScale = 1.0f;
    private int mScaleFocusX, mScaleFocusY;// 缩放中心（相对于ZoomHolder）
    private Scaler mScaler;// 封装缩放动画

    private boolean mUserInteracting;// Whether the user is interacting
    private boolean mScrollDisabled;
    private int mXScroll;
    private int mYScroll;
    private Scroller mScroller;// 封装滚动动画
    private int mScrollerLastX;
    private int mScrollerLastY;

    private boolean mIntercept;// 是否拦截触摸事件
    private boolean mOverScroll;// 是否有滑动回弹效果

    private ZoomViewListener mListener;// 监听者

    public XZoomHolder(Context context) {
        super(context);
        init(context);
    }

    public XZoomHolder(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public XZoomHolder(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }


    private void init(Context context) {
        mGestureDetector = new GestureDetector(this);
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
        mScroller = new Scroller(context);
        mScaler = new Scaler();
        mIntercept = false;
        mOverScroll = false;
    }


    public void setInterceptTouchEvent(boolean isIntercept) {
        mIntercept = isIntercept;
    }

    public void setOverScroll(boolean overScroll) {
        this.mOverScroll = overScroll;
    }

    public void setListener(ZoomViewListener mListener) {
        this.mListener = mListener;
    }


    /**
     * 以XZoomHolder的中心缩放
     * @param scale
     * @param hasAnimation
     */
    public void setScale(float scale, boolean hasAnimation) {
        setScale(0, 0, scale, hasAnimation);
    }

    /**
     * 以左上角(x,y)为中心缩放
     * @param focusX
     * @param focusY
     * @param scale
     * @param hasAnimation
     */
    public void setScale(final int focusX, final int focusY,
                         float scale, boolean hasAnimation) {
        // 如果正在手动缩放或正在缩放
        if(mScaling || !mScaler.isFinished()) {
            return;
        }

        // 如果缩放一样
        scale = Math.min(Math.max(MIN_SCALE, scale), MAX_SCALE);
        if(mScale == scale) {
            return;
        }

        mScaleFocusX = mScaleFocusY = 0;
        if(hasAnimation) {
            scaleViewWithAnimation(scale, focusX, focusY);
        } else {
            float factor = scale/mScale;
            mScale = scale;
            calculateScaleScroll(factor, focusX, focusY);
            requestLayout();
        }
    }

    public float getScale() {
        return mScale;
    }


    /**
     * TIP 通过post()的循环调用实现动画
     */
    @Override
    public void run() {
        // 缩放动画
        if(!mScaler.isFinished()) {
            float previousScale = mScale;
            mScaler.computeScaleOffset();
            mScale = mScaler.getCurrS();
            // 此次较上次共缩放了几倍
            float factor = mScale/previousScale;
            calculateScaleScroll(factor, mScaleFocusX, mScaleFocusY);
            requestLayout();
            post(this);
        }
        // 滚动动画
        else if (!mScroller.isFinished()) {
            mScroller.computeScrollOffset();
            final int x = mScroller.getCurrX();
            final int y = mScroller.getCurrY();
            mXScroll += x - mScrollerLastX;
            mYScroll += y - mScrollerLastY;
            mScrollerLastX = x;
            mScrollerLastY = y;
            requestLayout();
            post(this);
        }
        // 动画结束
        if (!mUserInteracting
                && mScroller.isFinished()
                && mScaler.isFinished()) {
            // End of an inertial(惯性的) scroll and the user is not
            // interacting（相互作用）.The layout is stable
            final int count = getChildCount();
            for(int i = 0; i<count; i++) {
                postSettle(getChildAt(i));
            }
        }
    }


    @Override
    public boolean onDown(MotionEvent arg0) {
        XLog.d(TAG, "onDown");
        mScroller.forceFinished(true);
        mScaler.forceFinished(true);
        return true;
    }

    /**
     * 接收[快速滑动] , 左右快速切页
     * 换页函数的实现
     *
     * @param e1
     * @param e2
     * @param velocityX
     * @param velocityY
     * @return
     */
    @Override
    public boolean onFling(MotionEvent e1,MotionEvent e2, float velocityX, float velocityY) {
        XLog.d(TAG, "onFling");

        if (mScrollDisabled) {
            return true;
        }


        final int count = getChildCount();
        for(int i = 0; i<count; i++) {
            View v = getChildAt(i);

            Rect bounds = getScrollBounds(v);

            switch(directionOfTravel(velocityX, velocityY)) {
                case MOVING_LEFT:
                    if (bounds.left >= 0) {
                        postFling(v, MOVING_LEFT);
                    }
                    break;
                case MOVING_RIGHT:
                    if (bounds.right <= 0) {
                        // Fling off to the right bring previous view onto screen
                        postFling(v, MOVING_RIGHT);
                    }
                    break;
                case MOVING_UP:
                    if (bounds.top >= 0) {
                        postFling(v, MOVING_UP);
                    }
                    break;
                case MOVING_DOWN:
                    if (bounds.right <= 0) {
                        // Fling off to the right bring previous view onto screen
                        postFling(v, MOVING_DOWN);
                    }
                    break;

            }
            mScrollerLastX = mScrollerLastY = 0;
            // If the page has been dragged out of bounds then we want to spring back nicely.
            // fling jumps back into bounds instantly, so we don't want to use fling in that case.
            // On the other hand, we don't want to forgo a fling
            // just because of a slightly off-angle drag taking us out of bounds other
            // than in the direction of the drag, so we test for out of bounds only
            // in the direction of travel.
            // Also don't fling if out of bounds in any direction by more than fling margin
            Rect expandedBounds = new Rect(bounds);
            expandedBounds.inset(-FLING_MARGIN, -FLING_MARGIN);

            if(withinBoundsInDirectionOfTravel(bounds, velocityX, velocityY) && expandedBounds.contains(0, 0)) {
                mScroller.fling(
                        0,
                        0,
                        (int)velocityX,
                        (int)velocityY,
                        bounds.left,
                        bounds.right,
                        bounds.top,
                        bounds.bottom
                );
                post(this);
            }
        }

        return true;
    }

    /**
     * Touch了不移动,一直Touch down时触发
     * @param e
     */
    @Override
    public void onLongPress(MotionEvent e) {
        final int count = getChildCount();
        for(int i = 0; i<count; i++) {
            postLongPress(getChildAt(i));
        }
    }


    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        XLog.d(TAG , "on scroll");

        if (!mScrollDisabled) {
            mXScroll -= distanceX;
            mYScroll -= distanceY;
            requestLayout();
        }
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        XLog.d(TAG , "onSingleTapConfirmed");
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        XLog.d(TAG , "onDoubleTap");
        if(!mScaling) {
            final int x = (int) event.getX();
            final int y = (int) event.getY();
            final int count = getChildCount();
            for(int i = 0; i<count; i++) {
                postDoubleClick(getChildAt(i), x, y);
            }
        }
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        XLog.d(TAG , "onDoubleTapEvent");
        return false;
    }


    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        XLog.d(TAG, "onScaleBegin~~~~");

        mScaling = true;
        // Ignore any scroll amounts yet to be accounted for:
        // the screen is not showing the effect of them,so they can only confuse the user
        mXScroll = mYScroll = 0;
        // Avoid jump at end of scaling by disabling scrolling until the next start of gesture
        mScrollDisabled = true;
        return true;
    }


    /**
     * 放大缩小的是view
     * 而如果是setImageBitmap,应该会相应的将Bitmap放大缩小吧。
     * @param detector
     * @return
     */
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        XLog.d(TAG, "onScale~~~~");

        float previousScale = mScale;
        mScale = Math.min(Math.max(mScale * detector.getScaleFactor(), MIN_SCALE), MAX_SCALE);
        // 此次较上次共缩放了几倍
        final float factor = mScale/previousScale;

        // 计算scrollXY, 保证focus点不变
        calculateScaleScroll(factor, (int)detector.getFocusX(), (int)detector.getFocusY());

        if (getChildCount() > 0)
            requestLayout();

        return true;
    }

    /**
     * 保证缩放之后viewFocus(由detectorFocus换算得)
     * 与“一开始”的viewFocus点（固定在View上）保持重合
     * @param factor 此次较上次的缩放倍数
     * @param focusX
     * @param focusY
     */
    private void calculateScaleScroll(final float factor, final int focusX, final int focusY) {
        if (getChildCount() > 0) {
            View view = getChildAt(0);
            // Work out the focus point relative to the view top left
            // 算出detectorFocus点相对于view的坐标
            final int viewFocusX = focusX - (view.getLeft() + mXScroll);
            final int viewFocusY = focusY - (view.getTop() + mYScroll);
            // Scroll to maintain the focus point
            // 对ScrollXY加上缩放的因素（-Δ = viewFocus - viewFocus*factor）
            mXScroll += viewFocusX - viewFocusX * factor;
            mYScroll += viewFocusY - viewFocusY * factor;
        }
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        XLog.d(TAG, "onScaleEnd~~~~");
        mScaling = false;
    }

    /**
     * TIP 不要在onInterceptTouchEvent()中传event给GestureDetector
     * (原因：你在OnTouchEvent()会再次传给GestureDetector，
     *  容易导致一些手势识别的错误，比如对于onDoubleTap())
     *
     * TIP 不要在onInterceptTouchEvent()调用requestLayout()或invalidate()
     * (原因：在onInterceptTouchEvent()调用requestLayout()等刷新方法后
     *  会马上调动OnTouchEvent()，可能会导致手势操作的错误)
     *
     * @param event
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        XLog.d(TAG, "onInterceptTouchEvent******");
        // 手指一按下，就表示【用户正在与界面交互】
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            XLog.d(TAG, "onInterceptTouchEvent down");
            mUserInteracting = true;
        }

        // 手指一离开，就表示【用户与界面脱离交互】
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            XLog.d(TAG, "onInterceptTouchEvent up");
            mScrollDisabled = false;
            mUserInteracting = false;

            if (mScroller.isFinished() && mScaler.isFinished()) {
                final int count = getChildCount();
                for(int i = 0; i<count; i++) {
                    postSettle(getChildAt(i));
                }
            }
        }

        return mIntercept;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        XLog.d(TAG, "onTouchEvent******");
        mScaleGestureDetector.onTouchEvent(event);

        if (!mScaling)
            mGestureDetector.onTouchEvent(event);

        // 手指一按下，就表示【用户正在与界面交互】
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            XLog.d(TAG, "onTouchEvent down");
            mUserInteracting = true;
        }

        // 手指一离开，就表示【用户与界面脱离交互】
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            XLog.d(TAG, "onTouchEvent up");
            mScrollDisabled = false;
            mUserInteracting = false;

            if (mScroller.isFinished() && mScaler.isFinished()) {
                final int count = getChildCount();
                View view;
                for(int i = 0; i<count; i++) {
                    view = getChildAt(i);
                    if(i == 0) {
                        slideViewWithAnimation(view);// 回滚
                    }

                    postSettle(view);
                }
            }
        }

        return true;
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int count = getChildCount();
        for(int i = 0; i<count; i++) {
            measureView(getChildAt(i));// KEY!(计算缩放)
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        final int count = getChildCount();
        if(count == 0) {
            return;
        }

        final int parentWidth = getWidth();
        final int parentHeight = getHeight();
        for(int i = 0; i<count; i++) {
            View v = getChildAt(i);
            final int viewWidth = v.getMeasuredWidth();
            final int viewHeight = v.getMeasuredHeight();
            int cvLeft, cvRight, cvTop, cvBottom;
            cvLeft = v.getLeft() + mXScroll;
            cvTop  = v.getTop()  + mYScroll;
            cvRight  = cvLeft + viewWidth;
            cvBottom = cvTop + viewHeight;

            if (!mUserInteracting && mScroller.isFinished() && mScaler.isFinished()) {
                Point corr = getCorrection(getScrollBounds(cvLeft, cvTop, cvRight, cvBottom));
                cvRight  += corr.x;
                cvLeft   += corr.x;
                cvTop    += corr.y;
                cvBottom += corr.y;
            }
            else if (viewHeight <= parentHeight) {
                // When the current view is as small as the screen in height, clamp
                // it vertically
                // 回滚到屏幕垂直方向中心
                Point corr = getCorrection(getScrollBounds(cvLeft, cvTop, cvRight, cvBottom));
                cvTop    += corr.y;
                cvBottom += corr.y;
            }

            // 调整边界，不要OverScroll效果
            if(!mOverScroll) {
                int horizontalOffset = viewWidth >= parentWidth ? 0 : (parentWidth - viewWidth) / 2;
                if(cvLeft > horizontalOffset) {
                    int delta = cvLeft - horizontalOffset;
                    cvLeft -= delta;
                    cvRight -= delta;
                }else if(cvRight < parentWidth - horizontalOffset) {
                    int delta = parentWidth - horizontalOffset - cvRight;
                    cvLeft += delta;
                    cvRight += delta;
                }

                int verticalOffset = viewHeight >= parentHeight ? 0 : (parentHeight - viewHeight) / 2;
                if(cvTop > 0) {
                    int delta = cvTop - verticalOffset;
                    cvTop -= delta;
                    cvBottom -= delta;
                } else if(cvBottom < parentHeight - verticalOffset) {
                    int delta = parentHeight - verticalOffset - cvBottom;
                    cvTop += delta;
                    cvBottom += delta;
                }
            }

            v.layout(cvLeft, cvTop, cvRight, cvBottom);
        }

        // Scroll values have been accounted for
        mXScroll = mYScroll = 0;
    }


    private void measureView(View v) {
        // See what size the view wants to be
        v.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        // Work out a scale that will fit it to this view
        float scale = Math.min((float)getWidth()/(float)v.getMeasuredWidth(),
                (float)getHeight()/(float)v.getMeasuredHeight());
        // Use the fitting values scaled by our current scale factor
        v.measure(MeasureSpec.EXACTLY | (int)(v.getMeasuredWidth()*scale*mScale),
                MeasureSpec.EXACTLY | (int)(v.getMeasuredHeight()*scale*mScale));
    }

    private void addAndMeasureChild(View v) {
        LayoutParams params = v.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
        }
        //Adds a view during layout.
        // This is useful if in your onLayout() method, you need to addEmpty more views
        // (as does the list view for example).
        // If index is negative, it means put it at the end of the list.
        //addViewInLayout(v, 0, params, true); //0:the index at which the child must be added
        addViewInLayout(v, 0, params, true);
        measureView(v);
    }


    private Rect getScrollBounds(int left, int top, int right, int bottom) {
        int xmin = getWidth() - right;
        int xmax = -left;
        int ymin = getHeight() - bottom;
        int ymax = -top;

        // In either dimension, if view smaller than screen then
        // constrain it to be central
        if (xmin > xmax) xmin = xmax = (xmin + xmax)/2;
        if (ymin > ymax) ymin = ymax = (ymin + ymax)/2;
        return new Rect(xmin, ymin, xmax, ymax);
    }

    private Rect getScrollBounds(View v) {
        // There can be scroll amounts not yet accounted for in onLayout,
        // so addEmpty mXScroll and mYScroll to the current positions when calculating the bounds.
        return getScrollBounds(v.getLeft() + mXScroll,
                v.getTop() + mYScroll,
                v.getLeft() + v.getMeasuredWidth() + mXScroll,
                v.getTop() + v.getMeasuredHeight() + mYScroll);
    }

    private Point getCorrection(Rect bounds) {
        return new Point(Math.min(Math.max(0, bounds.left), bounds.right),
                Math.min(Math.max(0, bounds.top), bounds.bottom));
    }

    /**
     * 利用滑动效果滑动当前页
     * @param v
     */
    private void slideViewWithAnimation(View v) {
        Point corr = getCorrection(getScrollBounds(v));
        slideViewWithAnimation(corr.x, corr.y);
    }

    /**
     * 利用滑动效果滑动当前页
     * @param dx
     * @param dy
     */
    private void slideViewWithAnimation(int dx, int dy) {
        if (dx != 0 || dy != 0) {
            mScrollerLastX = mScrollerLastY = 0;
            mScroller.startScroll(0, 0, dx, dy, DEFAULT_DURATION);
            post(this);
        }
    }

    /**
     * 利用缩放效果缩放当前页
     * @param scale
     */
    private void scaleViewWithAnimation(float scale, int focusX, int focusY) {
        if(scale != mScale) {
            mScaleFocusX = focusX;
            mScaleFocusY = focusY;
            float delta = scale - mScale;
            mScaler.startScale(mScale, delta, DEFAULT_DURATION);
            post(this);
        }
    }

    /**
     * 判断Fling方向
     * @param vx
     * @param vy
     * @return
     */
    private static int directionOfTravel(float vx, float vy) {
        if (Math.abs(vx) > 2 * Math.abs(vy)) {
            return (vx > 0) ? MOVING_RIGHT : MOVING_LEFT;
        }
        else if (Math.abs(vy) > 2 * Math.abs(vx)) {
            return (vy > 0) ? MOVING_DOWN : MOVING_UP;
        }
        else{
            return MOVING_DIAGONALLY;
        }
    }

    private static boolean withinBoundsInDirectionOfTravel(Rect bounds, float vx, float vy) {
        switch (directionOfTravel(vx, vy)) {
            case MOVING_DIAGONALLY: return bounds.contains(0, 0);
            case MOVING_LEFT:       return bounds.left <= 0;
            case MOVING_RIGHT:      return bounds.right >= 0;
            case MOVING_UP:         return bounds.top <= 0;
            case MOVING_DOWN:       return bounds.bottom >= 0;
            default: throw new NoSuchElementException();
        }
    }


    private void postSettle(final View v) {
        // onSettle and onUnsettle are posted so that the calls won't be executed
        // until after the system has performedLayout.
        post (new Runnable() {
            public void run () {
                if(mListener != null)
                    mListener.onSettle(v);
            }
        });
    }

    private void postFling(final View v, final int direction) {
        post (new Runnable() {
            public void run () {
                if(mListener != null)
                    switch (direction) {
                        case MOVING_LEFT:
                            mListener.onFlingLeft(v);
                            break;
                        case MOVING_RIGHT:
                            mListener.onFlingRight(v);
                            break;
                        case MOVING_UP:
                            mListener.onFlingUp(v);
                            break;
                        case MOVING_DOWN:
                            mListener.onFlingDown(v);
                            break;
                    }
            }
        });
    }

    private void postLongPress(final View v) {
        post (new Runnable() {
            public void run () {
                if(mListener != null)
                    mListener.onLongPress(v);
            }
        });
    }

    private void postDoubleClick(final View v, final int x, final int y) {
        post (new Runnable() {
            public void run () {
                if(mListener != null)
                    mListener.onDoubleClick(v, x, y);
            }
        });
    }

    public interface ZoomViewListener {
        void onSettle(View view);

        void onFlingLeft(View view);

        void onFlingRight(View view);

        void onFlingUp(View view);

        void onFlingDown(View view);

        void onLongPress(View view);

        void onDoubleClick(View view, int x, int y);
    }

    /**
     * 封装了缩放的动画相关参数
     * Created by jasontujun.
     * Date: 12-11-19
     * Time: 下午2:33
     */
    private class Scaler {

        private float mStartS;
        private float mFinalS;
        private float mCurrS;
        private long mStartTime;
        private int mDuration;
        private float mDurationReciprocal;
        private float mDeltaS;
        private boolean mFinished;
        private Interpolator mInterpolator;

        private static final int DEFAULT_DURATION = 250;

        private float sViscousFluidScale;
        private float sViscousFluidNormalize;

        public Scaler() {
            this(null);
            init();
        }

        public Scaler(Interpolator interpolator) {
            mFinished = true;
            mInterpolator = interpolator;
            init();
        }

        private void init() {
            // This controls the viscous fluid effect (how much of it)
            sViscousFluidScale = 8.0f;
            // must be set to 1.0 (used in viscousFluid())
            sViscousFluidNormalize = 1.0f;
            sViscousFluidNormalize = 1.0f / viscousFluid(1.0f);
        }


        public final boolean isFinished() {
            return mFinished;
        }


        public final void forceFinished(boolean finished) {
            mFinished = finished;
        }


        public final int getDuration() {
            return mDuration;
        }


        public final float getCurrS() {
            return mCurrS;
        }

        public final float getStartS() {
            return mStartS;
        }


        public final float getFinalS() {
            return mFinalS;
        }

        /**
         * Call this when you want to know the new scale.  If it returns true,
         * the animation is not yet finished.  loc will be altered to provide the
         * new scale.
         */
        public boolean computeScaleOffset() {
            if (mFinished) {
                return false;
            }

            int timePassed = (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);

            if (timePassed < mDuration) {
                float x = (float)timePassed * mDurationReciprocal;

                if (mInterpolator == null)
                    x = viscousFluid(x);
                else
                    x = mInterpolator.getInterpolation(x);

                mCurrS = mStartS + x * mDeltaS;
            }
            else {
                mCurrS = mFinalS;
                mFinished = true;
            }
            return true;
        }


        public void startScale(float startS, float ds) {
            startScale(startS, ds, DEFAULT_DURATION);
        }


        public void startScale(float startS, float ds, int duration) {
            mFinished = false;
            mDuration = duration;
            mStartTime = AnimationUtils.currentAnimationTimeMillis();
            mStartS = startS;
            mFinalS = startS + ds;
            mDeltaS = ds;
            mDurationReciprocal = 1.0f / (float) mDuration;
        }

        private float viscousFluid(float x) {
            x *= sViscousFluidScale;
            if (x < 1.0f) {
                x -= (1.0f - (float)Math.exp(-x));
            } else {
                float start = 0.36787944117f;   // 1/e == exp(-1)
                x = 1.0f - (float)Math.exp(1.0f - x);
                x = start + x * (1.0f - start);
            }
            x *= sViscousFluidNormalize;
            return x;
        }

        /**
         * 暂停动画。
         * 不同于ForceFinished是结束动画（直接到达DesScale）。
         * @see #forceFinished(boolean)
         */
        public void abortAnimation() {
            mCurrS = mFinalS;
            mFinished = true;
        }


        public void extendDuration(int extend) {
            int passed = timePassed();
            mDuration = passed + extend;
            mDurationReciprocal = 1.0f / (float)mDuration;
            mFinished = false;
        }

        public int timePassed() {
            return (int)(AnimationUtils.currentAnimationTimeMillis() - mStartTime);
        }

        public void setFinalS(float newS) {
            mFinalS = newS;
            mDeltaS = mFinalS - mStartS;
            mFinished = false;
        }
    }
}
