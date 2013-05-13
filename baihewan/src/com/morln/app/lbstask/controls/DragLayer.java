package com.morln.app.lbstask.controls;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.*;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.xengine.android.utils.XLog;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 三屏式容器
 * Created by jasontujun.
 * Date: 12-4-3
 * Time: 上午10:40
 */
public class DragLayer extends AdapterView<Adapter> {

    private static final int SNAP_VELOCITY = 1000;
    private static final int INVALID_SCREEN = -1;
    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_SCROLLING = 1;

    private LinkedList<View> mLoadedViews;// 已经加载的View
    private int mCurrentBufferIndex;
    private int mCurrentAdapterIndex;
    private int mSideBuffer;
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mTouchState = TOUCH_STATE_REST;
    private float mFirstMotionX;
    private float mLastMotionX, mLastMotionY;
    private int mTouchSlop;// 触摸的容忍距离。大于此距离认为用户是滑动手势
    private int mMaximumVelocity;
    private int mCurrentScreen;// 当前是哪一屏
    private int mNextScreen = INVALID_SCREEN;
    private boolean mFirstLayout = true;// 第一次显示的屏幕
    private int mLastScrollDirection;
    private int mLastOrientation = -1;

    private Adapter mAdapter;
    private AdapterDataSetObserver mDataSetObserver;

    private int marginSize;// 边距
    private int bufferDragging;// 拖动时的缓冲距离
    private static final float tan15 = 0.268f;// 用户判断用户水平手势。当起点终点的角度小于15度的时候,识别为水平移动手势

    private ViewSwitchListener mViewSwitchListener;// 屏幕切换监听
    private List<DragHorizontallyListener> dragListenerList;// 水平拖动的监听

    private ViewTreeObserver.OnGlobalLayoutListener orientationChangeListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            getViewTreeObserver().removeGlobalOnLayoutListener(orientationChangeListener);
            setSelection(mCurrentAdapterIndex);
        }
    };

    /**
     * 横向拖动
     */
    public static interface DragHorizontallyListener {
        void onDragHorizontally();
    }

    /**
     * Receives call backs when a new {@link android.view.View} has been scrolled to.
     */
    public static interface ViewSwitchListener {
        /**
         * This method is called when a new View has been scrolled to.
         *
         * @param view
         *            the {@link android.view.View} currently in focus.
         * @param position
         *            The position in the adapter of the {@link android.view.View} currently in focus.
         */
        void onSwitched(View view, int position);

    }

    public DragLayer(Context context) {
        super(context);
        mSideBuffer = 3;
        init();
    }

    public DragLayer(Context context, int sideBuffer) {
        super(context);
        mSideBuffer = sideBuffer;
        init();
    }

    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.ViewFlow);
        mSideBuffer = styledAttrs.getInt(R.styleable.ViewFlow_sidebuffer, 3);
        init();
    }

    private void init() {
        mLoadedViews = new LinkedList<View>();
        mScroller = new Scroller(getContext());
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();// 是一个距离，表示滑动的时候，手的移动要大于这个距离才开始移动控件。
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();

        dragListenerList = new ArrayList<DragHorizontallyListener>();// 初始化监听列表
    }

    public void setMarginSize(int px) {
        marginSize = px;
    }

    public void setBufferDragging(int px) {
        bufferDragging = px;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation != mLastOrientation) {
            mLastOrientation = newConfig.orientation;
            getViewTreeObserver().addOnGlobalLayoutListener(orientationChangeListener);
        }
    }

    public int getViewsCount() {
        return mAdapter.getCount();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY && !isInEditMode()) {
            throw new IllegalStateException(
                    "ViewFlow can only be used in EXACTLY mode.");
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY && !isInEditMode()) {
            throw new IllegalStateException(
                    "ViewFlow can only be used in EXACTLY mode.");
        }

        // The children are given the same width and height as the workspace
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        // TIP 初始化第一个界面：中间 !!!
        if (mFirstLayout) {
            mScroller.startScroll(0, 0, width, 0, 0);
            mFirstLayout = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth,
                        child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (getChildCount() == 0)
            return false;

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                /*
                 * If being flinged and user touches, stop the fling. isFinished
                 * will be false if being flinged.
                 */
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                // 记录起始的位置x
                mFirstMotionX = x;

                // Remember where the motion event started
                mLastMotionX = x;
                mLastMotionY = y;

                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
                        : TOUCH_STATE_SCROLLING;

                break;

            case MotionEvent.ACTION_MOVE:
                // 计算横向/纵向移动距离
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);

                // TIP 判断用户是否为横向移动！
                boolean horizontalMoved = false;
                if(xDiff > mTouchSlop) {
                    float angle = (float)yDiff/(float)xDiff;// 计算触摸角度
                    if(angle <= tan15){
                        XLog.d("SCROLL", "水平移动！");
                        for(DragHorizontallyListener listener : dragListenerList){
                            listener.onDragHorizontally();
                        }
                        horizontalMoved = true;
                    }
                }

                if (horizontalMoved) {
                    // Scroll if the user moved far enough along the X axis
                    mTouchState = TOUCH_STATE_SCROLLING;
                }

                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    // Scroll to follow the motion event
                    final int deltaX = (int) (mLastMotionX - x);// 水平移动的距离
                    mLastMotionX = x;

                    int scrollX = getScrollX();
                    if (deltaX < 0) {// 向左滑动
                        // 留边距
                        scrollX = scrollX - marginSize;
                        if(scrollX < 0) {
                            scrollX = 0;
                        }

                        if (scrollX > 0) {
                            scrollBy(Math.max(-scrollX, deltaX), 0);
                        }
                    } else if (deltaX > 0) {// 向右滑动
                        int availableToScroll = getChildAt(getChildCount() - 1).getRight()
                                - scrollX - getWidth();
                        // 留边距
                        availableToScroll = availableToScroll - marginSize;
                        if(availableToScroll < 0) {
                            availableToScroll = 0;
                        }

                        if (availableToScroll > 0) {
                            scrollBy(Math.min(availableToScroll, deltaX), 0);
                        }
                    }
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int velocityX = (int) velocityTracker.getXVelocity();

                    if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
                        // Fling hard enough to move left
                        snapToScreen(mCurrentScreen - 1);
                    } else if (velocityX < -SNAP_VELOCITY
                            && mCurrentScreen < getChildCount() - 1) {
                        // Fling hard enough to move right
                        snapToScreen(mCurrentScreen + 1);
                    } else {
                        snapToDestination((int) (x - mFirstMotionX));
                    }

                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                }

                mTouchState = TOUCH_STATE_REST;

                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (getChildCount() == 0)
            return false;

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();
        final float x = ev.getX();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                /*                             1
                 * If being flinged and user touches, stop the fling. isFinished
                 * will be false if being flinged.
                 */
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                // Remember where the motion event started
                mLastMotionX = x;

                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
                        : TOUCH_STATE_SCROLLING;

                break;

            case MotionEvent.ACTION_MOVE:
                final int xDiff = (int) Math.abs(x - mLastMotionX);

                boolean xMoved = xDiff > mTouchSlop;

                if (xMoved) {
                    // Scroll if the user moved far enough along the X axis
                    mTouchState = TOUCH_STATE_SCROLLING;
                }

                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    // Scroll to follow the motion event
                    final int deltaX = (int) (mLastMotionX - x);
                    mLastMotionX = x;

                    int scrollX = getScrollX();
                    if (deltaX < 0) {
                        // 留边距
                        scrollX = scrollX - marginSize;
                        if(scrollX < 0) {
                            scrollX = 0;
                        }
                        if (scrollX > 0) {
                            scrollBy(Math.max(-scrollX, deltaX), 0);
                        }
                    } else if (deltaX > 0) {
                        int availableToScroll = getChildAt(getChildCount() - 1).getRight()
                                - scrollX - getWidth();
                        // 留边距
                        availableToScroll = availableToScroll - marginSize;
                        if(availableToScroll < 0) {
                            availableToScroll = 0;
                        }

                        if (availableToScroll > 0) {
                            scrollBy(Math.min(availableToScroll, deltaX), 0);
                        }
                    }
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mTouchState == TOUCH_STATE_SCROLLING) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int velocityX = (int) velocityTracker.getXVelocity();
                    // 当横向滑动的速度很大时，滑到下一屏
                    if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
                        // Fling hard enough to move left
                        snapToScreen(mCurrentScreen - 1);
                    } else if (velocityX < -SNAP_VELOCITY
                            && mCurrentScreen < getChildCount() - 1) {
                        // Fling hard enough to move right
                        snapToScreen(mCurrentScreen + 1);
                    } else {
                        snapToDestination((int) (x - mFirstMotionX));
                    }

                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                }

                mTouchState = TOUCH_STATE_REST;

                break;
            case MotionEvent.ACTION_CANCEL:
                snapToDestination((int) (x - mFirstMotionX));
                mTouchState = TOUCH_STATE_REST;
        }
        return true;
    }

    @Override
    protected void onScrollChanged(int h, int v, int oldh, int oldv) {
        super.onScrollChanged(h, v, oldh, oldv);
    }

    /**
     * 根据当前的x坐标位置和移动方向，判断要自动滑动的是哪一屏，并启动动画
     * <p>
     *     水平方向的位置含义：(注：bd为bufferDragging宽度)
     *          l    l+bd    m-bd  m  m+bd    r-bd    r
     *          |     |////////|   |   |////////|     |
     *          |     |////////|   |   |////////|     |
     *          |     |////////|   |   |////////|     |
     *          |LEFT |////////| CENTER|////////|RIGHT|
     *          |     |////////|   |   |////////|     |
     *          |     |////////|   |   |////////|     |
     *          |     |////////|   |   |////////|     |
     *          <- bd->        <--2bd-->
     * </p>
     * @param direction 拖动方向（eg:向左拖，右边呈现）
     */
    private void snapToDestination(int direction) {
        final int screenWidth = getWidth();
        final int scrollX = getScrollX();

        // 判断滑动到哪个屏（缓冲区法）
        if(mCurrentScreen == 0) {// 最左边的屏幕(只能向左拖动)
            int l_add_bd = marginSize + bufferDragging;// 最左边含buffer的坐标
            if(scrollX < l_add_bd) {// 在buffer线左边(还原)
                snapToScreen(0);
            }else {
                snapToScreen(1);
            }
        }else if(mCurrentScreen == getChildCount() - 1) {// 最右边的屏幕(只能向右拖动)
            int r_minus_bd = screenWidth*(getChildCount()-1)-marginSize-bufferDragging;
            if(scrollX > r_minus_bd) {// 在buffer线右边(还原)
                snapToScreen(getChildCount() - 1);
            }else {
                snapToScreen(getChildCount() - 2);
            }
        }else {// 中间屏幕
            int m = mCurrentScreen * screenWidth;
            int m_minus_bd = m - bufferDragging;
            int m_add_bd = m + bufferDragging;
//            XLog.d("SCROLL", "m:"+m+",m_minus_bd:"+m_minus_bd+",m_add_bd:"+m_add_bd);
//            XLog.d("SCROLL", "scrollX:"+scrollX);
            if(direction < 0) {// 向左拖动，右边呈现
                if(scrollX>m_add_bd) {
                    snapToScreen(mCurrentScreen + 1);
                }else {
                    snapToScreen(mCurrentScreen);
                }
            }else {// 向右拖动，左边呈现
                if(scrollX < m_minus_bd) {
                    snapToScreen(mCurrentScreen - 1);
                }else {
                    snapToScreen(mCurrentScreen);
                }
            }
        }
    }

    /**
     * 滑动到指定屏
     */
    public void snapToScreen(int whichScreen) {
        XLog.d("SCROLL", "snapToScreen:" + whichScreen);
        mLastScrollDirection = whichScreen - mCurrentScreen;
        if (!mScroller.isFinished())
            return;

        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1)); // 防止非法值

        mNextScreen = whichScreen;

        int scrollX = getScrollX();
        int newX = whichScreen * getWidth();// 如果目的地是中间的view，计算新的x坐标
        if(whichScreen == 0) {// 如果目的地是最左边的view，新的坐标要加上边距
            newX = newX + marginSize;
        }else if(whichScreen == getChildCount() - 1){// 如果目的地是最右边的view，新的坐标要减去边距
            newX = newX - marginSize;
        }
        final int delta = newX - scrollX;// 移动距离
        int duration = Math.abs(delta);// 动画时间
        mScroller.startScroll(scrollX, 0, delta, 0, duration);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        } else if (mNextScreen != INVALID_SCREEN) {
            mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
            mNextScreen = INVALID_SCREEN;
            postViewSwitched(mLastScrollDirection);
        }
    }

    /**
     * Scroll to the {@link android.view.View} in the view buffer specified by the index.
     *
     * @param indexInBuffer Index of the view in the view buffer.
     */
    private void setVisibleView(int indexInBuffer, boolean uiThread) {
        mCurrentScreen = Math.max(0, Math.min(indexInBuffer, getChildCount() - 1));
        // 计算偏移量
        int newX = mCurrentScreen * getWidth();// 如果当前是中间的view，计算新的x坐标
        if(mCurrentScreen == 0) {// 如果当前是最左边的view，新的坐标要加上边距
            newX = newX + marginSize;
        }else if(mCurrentScreen == getChildCount() - 1) {// 如果当前是最右边的view，新的坐标要减去边距
            newX = newX - marginSize;
        }
        int dx = newX - mScroller.getCurrX();
        mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(), dx, 0, 0);
        if(dx == 0)
            onScrollChanged(mScroller.getCurrX() + dx, mScroller.getCurrY(), mScroller.getCurrX() + dx, mScroller.getCurrY());

        if (uiThread)
            invalidate();
        else
            postInvalidate();
    }

    /**
     * Set the listener that will receive notifications every time the {code
     * ViewFlow} scrolls.
     *
     * @param l the scroll listener
     */
    public void setOnViewSwitchListener(ViewSwitchListener l) {
        mViewSwitchListener = l;
    }

    @Override
    public Adapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }

        mAdapter = adapter;

        if (mAdapter != null) {
            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);

        }
        if (mAdapter == null || mAdapter.getCount() == 0)
            return;

        setSelection(1);// 选择中间显示
    }


    /**
     * TIP 设置该组件的三屏内容（推荐）
     * @param v1
     * @param v2
     * @param v3
     */
    public void setContent(View v1, View v2, View v3) {
        setAdapter(new DragAdapter(v1, v2, v3));
    }


    @Override
    public View getSelectedView() {
        return (mCurrentBufferIndex < mLoadedViews.size() ? mLoadedViews
                .get(mCurrentBufferIndex) : null);
    }

    @Override
    public int getSelectedItemPosition() {
        return mCurrentAdapterIndex;
    }

    @Override
    public void setSelection(int position) {
        mNextScreen = INVALID_SCREEN;
        mScroller.forceFinished(true);
        if (mAdapter == null)
            return;

        // 防止异常输入参数
        position = Math.max(position, 0);
        position =  Math.min(position, mAdapter.getCount()-1);

        ArrayList<View> recycleViews = new ArrayList<View>();// 可回收利用的View
        View recycleView;
        while (!mLoadedViews.isEmpty()) {
            recycleViews.add(recycleView = mLoadedViews.remove());
            detachViewFromParent(recycleView);
        }

        View currentView = makeAndAddView(position, true,
                (recycleViews.isEmpty() ? null : recycleViews.remove(0)));
        mLoadedViews.addLast(currentView);

        for(int offset = 1; mSideBuffer - offset >= 0; offset++) {
            int leftIndex = position - offset;
            int rightIndex = position + offset;
            if(leftIndex >= 0)
                mLoadedViews.addFirst(makeAndAddView(leftIndex, false,
                        (recycleViews.isEmpty() ? null : recycleViews.remove(0))));
            if(rightIndex < mAdapter.getCount())
                mLoadedViews.addLast(makeAndAddView(rightIndex, true,
                        (recycleViews.isEmpty() ? null : recycleViews.remove(0))));
        }

        mCurrentBufferIndex = mLoadedViews.indexOf(currentView);
        mCurrentAdapterIndex = position;

        for (View view : recycleViews) {
            removeDetachedView(view, false);
        }
        requestLayout();
        setVisibleView(mCurrentBufferIndex, false);
        // 切屏监听
        if (mViewSwitchListener != null) {
            mViewSwitchListener.onSwitched(mLoadedViews.get(mCurrentBufferIndex),
                    mCurrentAdapterIndex);
            XLog.d("SCROLL", "view switch！ setSelection:"+position);
        }
    }

    private void resetFocus() {
        mLoadedViews.clear();
        removeAllViewsInLayout();

        for (int i = Math.max(0, mCurrentAdapterIndex - mSideBuffer); i < Math
                .min(mAdapter.getCount(), mCurrentAdapterIndex + mSideBuffer + 1); i++) {
            mLoadedViews.addLast(makeAndAddView(i, true, null));
            if (i == mCurrentAdapterIndex)
                mCurrentBufferIndex = mLoadedViews.size() - 1;
        }
        requestLayout();
    }

    /**
     * 预加载views
     * @param direction 正负代表方向，值代表切换的数量
     */
    private void postViewSwitched(int direction) {
        if (direction == 0)
            return;

        mCurrentAdapterIndex = mCurrentAdapterIndex + direction;
        mCurrentBufferIndex = mCurrentBufferIndex + direction;

        if (direction > 0) { // to the right
            View recycleView = null;

            // Remove view outside buffer range
            if (mCurrentAdapterIndex > mSideBuffer) {
                recycleView = mLoadedViews.removeFirst();
                detachViewFromParent(recycleView);
                // removeView(recycleView);
                mCurrentBufferIndex--;
            }

            // Add new view to buffer
            int newBufferIndex = mCurrentAdapterIndex + mSideBuffer;
            if (newBufferIndex < mAdapter.getCount())
                mLoadedViews.addLast(makeAndAddView(newBufferIndex, true,
                        recycleView));

        } else { // to the left
            View recycleView = null;

            // Remove view outside buffer range
            if (mAdapter.getCount() - 1 - mCurrentAdapterIndex > mSideBuffer) {
                recycleView = mLoadedViews.removeLast();
                detachViewFromParent(recycleView);
            }

            // Add new view to buffer
            int newBufferIndex = mCurrentAdapterIndex - mSideBuffer;
            if (newBufferIndex > -1) {
                mLoadedViews.addFirst(makeAndAddView(newBufferIndex, false,
                        recycleView));
                mCurrentBufferIndex++;
            }

        }

        requestLayout();
        setVisibleView(mCurrentBufferIndex, true);
        // 添加监听
        if (mViewSwitchListener != null) {
            mViewSwitchListener.onSwitched(mLoadedViews.get(mCurrentBufferIndex),
                            mCurrentAdapterIndex);
            XLog.d("SCROLL", "view switch！ postViewSwitched:"+mCurrentAdapterIndex);
        }
    }

    private View setupChild(View child, boolean addToEnd, boolean recycle) {
        LayoutParams p = (LayoutParams) child
                .getLayoutParams();
        if (p == null) {
            p = new AbsListView.LayoutParams(
                    LayoutParams.FILL_PARENT,
                    LayoutParams.WRAP_CONTENT, 0);
        }
        if (recycle)
            attachViewToParent(child, (addToEnd ? -1 : 0), p);
        else
            addViewInLayout(child, (addToEnd ? -1 : 0), p, true);
        return child;
    }

    private View makeAndAddView(int position, boolean addToEnd, View convertView) {
        View view = mAdapter.getView(position, convertView, this);
        return setupChild(view, addToEnd, convertView != null);
    }

    class AdapterDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            View v = getChildAt(mCurrentBufferIndex);
            if (v != null) {
                for (int index = 0; index < mAdapter.getCount(); index++) {
                    if (v.equals(mAdapter.getItem(index))) {
                        mCurrentAdapterIndex = index;
                        break;
                    }
                }
            }
            resetFocus();
        }

        @Override
        public void onInvalidated() {
            // Not yet implemented!
        }

    }

    public void registerDragHorizontallyListener(DragHorizontallyListener listener) {
        dragListenerList.add(listener);
    }

    public void unregisterDragHorizontallyListener(DragHorizontallyListener listener) {
        dragListenerList.remove(listener);
    }

    public void clearDragHorizontallyListener(){
        dragListenerList.clear();
    }


    /**
     * 三屏式
     */
    private class DragAdapter extends BaseAdapter {
        private static final int VIEW1 = 0;
        private static final int VIEW2 = 1;
        private static final int VIEW3 = 2;
        private static final int VIEW_MAX_COUNT = 3;

        private View view1, view2, view3;

        private DragAdapter(View view1, View view2, View view3) {
            this.view1 = view1;
            this.view2 = view2;
            this.view3 = view3;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return VIEW_MAX_COUNT;
        }

        @Override
        public int getCount() {
            return VIEW_MAX_COUNT;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int viewType = getItemViewType(position);
            if (convertView == null) {
                switch (viewType) {
                    case VIEW1: {
                        convertView = view1;
                        break;
                    }case VIEW2: {
                        convertView = view2;
                        break;
                    }case VIEW3: {
                        convertView = view3;
                        break;
                    }
                }
            }
            return convertView;
        }
    }
}

