package com.morln.app.lbstask.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import com.morln.app.lbstask.R;

import java.util.Date;


/**
 * 下拉刷新列表
 */
public class XListView extends ListView
        implements OnScrollListener, Runnable {

    private static final String TAG = "XListView";

    private final static int STATE_RELEASE_TO_REFRESH = 0;
    private final static int STATE_PULL_TO_REFRESH = 1;
    private final static int STATE_REFRESHING = 2;
    private final static int STATE_DONE = 3;
    private final static int DEFAULT_ANIM_DURATION = 400;

    // 自定义属性
    private  float mRatio = 1.4f;// attr 实际的padding的距离与界面上便宜距离的比例
    private boolean mRefreshable = false;// attr 标识是否可刷新

    // 相关参数
    private int mState;
    private int mHeadContentHeight;// 头部高度
    private int mStartY;
    private int mLastY;// 恢复动画时需要的参数
    private int mFirstItemIndex;
    private boolean isBack;// 用于记录箭头是否翻转
    private boolean isRecorded;// 保证startY的值在一个完整的touch事件中只被记录一次

    // 组件
    private LinearLayout mHeadFrame;
    private TextView mTipsTextView;
    private TextView mLastUpdatedTextView;
    private ImageView mArrowImageView;
    private ProgressBar mProgressBar;

    // 动画
    private Scroller mScroller;
    private RotateAnimation animation;
    private RotateAnimation reverseAnimation;

    // 监听
    private OnRefreshListener mRefreshListener;
    private OnScrollListener mScrollListener;// 外部设置滑动监听
    private AnimationListener mAnimationListener;

    public XListView(Context context) {
        super(context);
        init(context, null);
    }

    public XListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public XListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mScroller = new Scroller(context);
        mState = STATE_DONE;

        // 设置监听
        super.setOnScrollListener(this);

        // 设置属性
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.XListView);
            mRatio = a.getFloat(R.styleable.XListView_ratio, 1.4f);
            mRefreshable = a.getBoolean(R.styleable.XListView_refreshable, false);
            a.recycle();
        }
        setCacheColorHint(context.getResources().getColor(android.R.color.transparent));

        // 初始化和布局
        mHeadFrame = (LinearLayout) LayoutInflater.from(context).
                inflate(R.layout.controls_xlistview_head, null);
        mArrowImageView = (ImageView) mHeadFrame.findViewById(R.id.head_arrowImageView);
        mProgressBar = (ProgressBar) mHeadFrame.findViewById(R.id.head_progressBar);
        mTipsTextView = (TextView) mHeadFrame.findViewById(R.id.head_tipsTextView);
        mLastUpdatedTextView = (TextView) mHeadFrame.findViewById(R.id.head_lastUpdatedTextView);
        measureView(mHeadFrame);
        mHeadContentHeight = mHeadFrame.getMeasuredHeight();
        mHeadFrame.setPadding(0, -mHeadContentHeight, 0, 0);
        mHeadFrame.invalidate();
        addHeaderView(mHeadFrame, null, false);// 添加头部

        // 初始化动画
        animation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(250);
        animation.setFillAfter(true);

        reverseAnimation = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        reverseAnimation.setInterpolator(new LinearInterpolator());
        reverseAnimation.setDuration(200);
        reverseAnimation.setFillAfter(true);
    }

    @Override
    public void run() {
        if (!mScroller.isFinished()) {
            mScroller.computeScrollOffset();
            final int curY = mScroller.getCurrY();
            int deltaY = curY - mLastY;
            mLastY = curY;

            final int curPaddingTop = mHeadFrame.getPaddingTop();
            if (curPaddingTop > -mHeadContentHeight) {
                int newPaddingTop = curPaddingTop - deltaY;
                if (newPaddingTop < -mHeadContentHeight) {
                    newPaddingTop = -mHeadContentHeight;
                }
                if (deltaY < 1 && newPaddingTop <= -mHeadContentHeight + 1) {
                    // 如果deltaY<1，则直接让动画跳转到末尾
                    mScroller.abortAnimation();
                } else {
                    mHeadFrame.setPadding(0, newPaddingTop, 0, 0);
                }
            } else {
                mScroller.forceFinished(true);
            }
            post(this);
        } else {
            if (mAnimationListener != null) {
                mAnimationListener.onFinish();
                mAnimationListener = null;
            }
        }
    }

    @Override
    public void onScroll(AbsListView arg0, int firstVisiableItem,
                         int visibleItemCount, int totalItemCount) {
        mFirstItemIndex = firstVisiableItem;

        // 外部Scroll监听
        if (mScrollListener != null) {
            mScrollListener.onScroll(arg0, firstVisiableItem,
                    visibleItemCount, totalItemCount);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView arg0, int arg1) {
        if (mScrollListener != null) {
            mScrollListener.onScrollStateChanged(arg0, arg1);
        }
    }

    @Deprecated
    public void setOnScrollListener(OnScrollListener listener) {
        throw new UnsupportedOperationException
                ("setOnScrollListener() is not support in XListView." +
                        " Please use setOnXListScrollListener()");
    }

    /**
     * 设置对XListView的滑动监听（代替原有的setOnScrollListener）
     * @param listener
     * @see #setOnScrollListener
     */
    public void setOnXListScrollListener(OnScrollListener listener) {
        this.mScrollListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mRefreshable) {
            return super.onTouchEvent(event);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mFirstItemIndex == 0 && !isRecorded && mState != STATE_REFRESHING) {
                    isRecorded = true;
                    mStartY = (int) event.getY();
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mState != STATE_REFRESHING) {
                    if (mState == STATE_DONE) {
                        changeHeaderViewByState();
                    }

                    mAnimationListener = null;
                    if (mState == STATE_PULL_TO_REFRESH) {
                        mAnimationListener = new AnimationListener() {
                            @Override
                            public void onFinish() {
                                mState = STATE_DONE;
                                changeHeaderViewByState();
                            }
                        };
                    }
                    if (mState == STATE_RELEASE_TO_REFRESH) {
                        mAnimationListener = new AnimationListener() {
                            @Override
                            public void onFinish() {
                                if (mRefreshListener != null && mRefreshable) {
                                    mState = STATE_REFRESHING;
                                    changeHeaderViewByState();
                                    mRefreshListener.onRefresh();
                                } else {
                                    onRefreshComplete();
                                }
                            }
                        };
                    }
                    // 启动动画
                    if (mAnimationListener != null) {
                        int pullDistance = (int) (((int)event.getY() - mStartY) / mRatio);
                        if (mState == STATE_RELEASE_TO_REFRESH) {// 释放刷新状态下，只要回复到
                            pullDistance = pullDistance - mHeadContentHeight;
                        }
                        if (pullDistance > 0) {
                            mState = STATE_REFRESHING;// 启动动画前，设置标志位，从而禁止其他触摸逻辑
                            startHeadReverseAnimation(pullDistance);
                        } else {
                            mAnimationListener.onFinish();
                        }
                    }
                }
                isRecorded = false;
                isBack = false;
                break;

            case MotionEvent.ACTION_MOVE:
                final int curY = (int) event.getY();
                if (!isRecorded && mFirstItemIndex == 0 && mState != STATE_REFRESHING) {
                    isRecorded = true;
                    mStartY = curY;
                }

                if (mState != STATE_REFRESHING && isRecorded) {
                    final int deltaY = curY - mStartY;
                    final int pullDistance = (int) (deltaY / mRatio);

                    // 可以松手去刷新了
                    if (mState == STATE_RELEASE_TO_REFRESH) {
                        setSelection(0);

                        // 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
                        if ((pullDistance < mHeadContentHeight) && deltaY > 0) {
                            mState = STATE_PULL_TO_REFRESH;
                            changeHeaderViewByState();
                        }
                        // 一下子推到顶了
                        else if (deltaY <= 0) {
                            mState = STATE_DONE;
                            changeHeaderViewByState();
                        }
                        // 往下拉了，或者还没有上推到屏幕顶部掩盖head的地步
                        else {
                            // 不用进行特别的操作，只用更新paddingTop的值就行了
                        }
                    }
                    // 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
                    if (mState == STATE_PULL_TO_REFRESH) {
                        setSelection(0);

                        // 下拉到可以进入RELEASE_TO_REFRESH的状态
                        if (pullDistance >= mHeadContentHeight) {
                            mState = STATE_RELEASE_TO_REFRESH;
                            isBack = true;
                            changeHeaderViewByState();
                        }
                        // 上推到顶了
                        else if (deltaY <= 0) {
                            mState = STATE_DONE;
                            changeHeaderViewByState();
                        }
                    }
                    // done状态下
                    if (mState == STATE_DONE) {
                        if (deltaY > 0) {
                            mState = STATE_PULL_TO_REFRESH;
                            changeHeaderViewByState();
                        }
                    }

                    // 改变头部位置。更新headView的paddingTop
                    if (mState == STATE_PULL_TO_REFRESH || mState == STATE_RELEASE_TO_REFRESH) {
                        mHeadFrame.setPadding(0, pullDistance - mHeadContentHeight, 0, 0);
                    }

                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 当状态改变时候，调用该方法，以更新界面
     */
    private void changeHeaderViewByState() {
        switch (mState) {
            case STATE_RELEASE_TO_REFRESH:
                mArrowImageView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mTipsTextView.setVisibility(View.VISIBLE);
                mLastUpdatedTextView.setVisibility(View.VISIBLE);

                mArrowImageView.clearAnimation();
                mArrowImageView.startAnimation(animation);

                mTipsTextView.setText("松开刷新");
                break;
            case STATE_PULL_TO_REFRESH:
                mProgressBar.setVisibility(View.GONE);
                mTipsTextView.setVisibility(View.VISIBLE);
                mLastUpdatedTextView.setVisibility(View.VISIBLE);
                mArrowImageView.clearAnimation();
                mArrowImageView.setVisibility(View.VISIBLE);
                // 是由RELEASE_To_REFRESH状态转变来的
                if (isBack) {
                    isBack = false;
                    mArrowImageView.clearAnimation();
                    mArrowImageView.startAnimation(reverseAnimation);

                    mTipsTextView.setText("下拉刷新");
                } else {
                    mTipsTextView.setText("下拉刷新");
                }
                break;

            case STATE_REFRESHING:
                mHeadFrame.setPadding(0, 0, 0, 0);

                mProgressBar.setVisibility(View.VISIBLE);
                mArrowImageView.clearAnimation();
                mArrowImageView.setVisibility(View.GONE);
                mTipsTextView.setText("正在刷新");
                mLastUpdatedTextView.setVisibility(View.VISIBLE);
                break;
            case STATE_DONE:
                mHeadFrame.setPadding(0, -mHeadContentHeight, 0, 0);

                mProgressBar.setVisibility(View.GONE);
                mArrowImageView.clearAnimation();
                mTipsTextView.setText("下拉刷新");
                mLastUpdatedTextView.setVisibility(View.VISIBLE);
                break;
        }
    }


    /**
     * 此方法直接照搬自网络上的一个下拉刷新的demo，此处是“估计”headView的width以及height
     * @param child
     */
    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    public float getRatio() {
        return mRatio;
    }

    public void setRatio(float ratio) {
        this.mRatio = ratio;
    }

    public boolean isRefreshable() {
        return mRefreshable;
    }

    public void setRefreshable(boolean refreshable) {
        this.mRefreshable = refreshable;
    }

    /**
     * 设置题头的字体颜色
     * @param color
     */
    public void setHeadTextColor(int color) {
        this.mTipsTextView.setTextColor(color);
        this.mLastUpdatedTextView.setTextColor(color);
    }

    /**
     * 设置箭头
     * @param resourceId
     */
    public void setArrowImage(int resourceId) {
        this.mArrowImageView.setImageResource(resourceId);
    }

    /**
     * 添加监听
     * @param refreshListener
     */
    public void setOnRefreshListener(OnRefreshListener refreshListener) {
        this.mRefreshListener = refreshListener;
    }

    /**
     * TIP 外部调用者告诉ListView刷新完毕
     */
    public void onRefreshComplete() {
        if (!mRefreshable) {
            return;
        }

        if (mState != STATE_REFRESHING) {
            return;
        }

        // 刷新结束，启动恢复的动画
        mState = STATE_REFRESHING;// 启动动画前，设置标志位，从而禁止其他触摸逻辑
        mAnimationListener = new AnimationListener() {
            @Override
            public void onFinish() {
                mState = STATE_DONE;
                mLastUpdatedTextView.setText("最近更新:"
                        + new Date(System.currentTimeMillis()).toLocaleString());
                changeHeaderViewByState();
            }
        };
        startHeadReverseAnimation(mHeadContentHeight);
    }

    /**
     * 设置apdter
     * @param adapter
     */
    @Override
    public void setAdapter(ListAdapter adapter) {
        mLastUpdatedTextView.setText("最近更新:"
                + new Date(System.currentTimeMillis()).toLocaleString());
        super.setAdapter(adapter);
    }

    /**
     * 顶部恢复的动画
     * @param distanceY
     */
    private void startHeadReverseAnimation(int distanceY) {
        if(distanceY > 0) {
            mLastY = 0;
            mScroller.startScroll(0, 0, 0, distanceY, DEFAULT_ANIM_DURATION);
            post(this);
        }
    }

    /**
     * 列表下拉刷新监听
     */
    public interface OnRefreshListener {
        public void onRefresh();
    }

    interface AnimationListener {
        void onFinish();
    }
}
