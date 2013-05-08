package com.morln.app.lbstask.controls.gif;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.morln.app.utils.XSafeAsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * 本类可以显示一个gif动画，其使用方法和android的其它view（如imageview)一样。<br>
 * TIP 如果要显示的gif太大，会出现OOM的问题。
 * Created by jasontujun.
 * Date: 12-11-29
 * Time: 下午1:46
 */
public abstract class XGifView extends ImageView
        implements Runnable, XGifDecoder.GifDecodeListener {

    private static final String TAG = "GIF";

    /**
     * gif解码器
     */
    private XGifDecoder mGifDecoder;

    /**
     * 当前要画的帧的图
     */
    private Bitmap mCurrentImage;

    /**
     * 动画播放器
     */
    private XGifAnimator mGifAnimator;

    /**
     * 当前播放的帧
     */
    private int mCurrFrame;

    /**
     *  解码过程中，Gif动画显示的方式
     */
    private XGifLoadingType mLoadingType;

    /**
     * 解码过程中，Gif动画显示的方式<br>
     * 如果图片较大，那么解码过程会比较长，这个解码过程中，gif如何显示
     */
    public enum XGifLoadingType {
        /**
         * 在解码过程中，不显示图片，直到解码全部成功后，再显示
         */
        WAIT_FINISH (0),

        /**
         * 和解码过程同步，解码进行到哪里，图片显示到哪里
         */
        SYNC_DECODER (1),

        /**
         * 在解码过程中，只显示第一帧图片
         */
        SHOW_COVER(2);

        XGifLoadingType(int i) {
            nativeInt = i;
        }
        final int nativeInt;
    }

    private int resId;
    private File imgFile;


    public XGifView(Context context) {
        super(context);
        init();
    }

    public XGifView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public XGifView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mCurrFrame = -1;
        mLoadingType = XGifLoadingType.SYNC_DECODER;
        mGifAnimator = new XGifAnimator();
        mGifDecoder = new XGifDecoder();
        mGifDecoder.setDecodeListener(this);
    }


    /**
     * 通过递归调用post实现动画
     */
    @Override
    public void run() {
        if(mGifAnimator.computeGifAnimation()) {
            int frameIndex = mGifAnimator.getCurrentFrame();
            if(frameIndex != mCurrFrame) {// 如果帧数没变化，则不需要重绘
                mCurrFrame = frameIndex;
                mCurrentImage = mGifDecoder.getFrameImage(mCurrFrame);
                setImageBitmap(mCurrentImage);
            }
            post(this);
        }
    }


    /**
     * 以资源形式设置gif图片。<br>
     * GifImageType.COVER方式加载。
     * @see XGifLoadingType
     * @param resId gif图片的资源ID
     */
    public void setGifImage(int resId) {
        setGifImage(resId, XGifLoadingType.SHOW_COVER);
    }

    /**
     * 以资源形式设置gif图片
     * @param resId gif图片的资源ID
     * @param type gif动画加载方式
     * @see XGifLoadingType
     */
    public void setGifImage(int resId, XGifLoadingType type) {
        this.resId = resId;
        Resources r = this.getResources();
        InputStream is = r.openRawResource(resId);
        setGifImage(is, type);
    }

    /**
     * 以文件形式设置gif图片
     * @param imageFile gif图片文件
     * @see XGifLoadingType
     */
    public void setGifImage(File imageFile) {
        setGifImage(imageFile, XGifLoadingType.SHOW_COVER);
    }

    /**
     * 以文件形式设置gif图片
     * @param imageFile gif图片文件
     * @param type gif动画加载方式
     * @see XGifLoadingType
     */
    public void setGifImage(File imageFile, XGifLoadingType type) {
        if(imageFile != null && imageFile.exists()) {
            this.imgFile = imageFile;
            try {
                InputStream is = new FileInputStream(imageFile);
                setGifImage(is, type);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置图片，开始解码
     * @param is 要设置的图片
     */
    private void setGifImage(final InputStream is, XGifLoadingType type) {
        // 还原初始状态
        mGifAnimator.forceFinished(true);
        mCurrFrame = -1;
        setImageBitmap(null);
        mGifDecoder.free();

        if(is == null) {
            return;
        }

        // 设置gif加载方式
        mLoadingType = type;

        // 异步解析并加载
        new XSafeAsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                mGifDecoder.startDecode(is);
                return null;
            }
        }.safeExecute(null);
    }


    /**
     * 停止动画。只显示第一帧图片<br>
     * 调用本方法后，gif不会显示动画，只会显示gif的第一帧图
     */
    public void showCover() {
        mGifAnimator.forceFinished(true);
        mCurrFrame = 0;
        mCurrentImage = mGifDecoder.getCoverImage();
        postDraw();
    }

    /**
     * 暂停动画
     */
    public void pauseAnimation() {
        if (mGifAnimator.isFinished()) {
            return;
        }

        mGifAnimator.forceFinished(true);
        mCurrFrame = mGifAnimator.getCurrentFrame();
    }

    /**
     * 恢复动画
     */
    public void resumeAnimation() {
        if (!mGifAnimator.isFinished()) {
            return;
        }

        int remainLoop = mGifAnimator.getRemainLoop();
        if(!mGifAnimator.isInfinite() && remainLoop == 0) {
            // 动画循环结束
            return;
        }

        mGifAnimator.startGifAnimation(mGifDecoder.getDelays(),
                mCurrFrame, remainLoop);
        post(this);
    }

    @Override
    public Bitmap onBitmapProcess(Bitmap src) {
        // 进行图片压缩
        return compressBitmap(src);
    }

    @Override
    public void onDecoding(int frameIndex) {
        switch (mLoadingType) {
            case SHOW_COVER:
                if (frameIndex == 0) {
                    mCurrentImage = mGifDecoder.getCoverImage();
                    postDraw();
                }
                break;
            case SYNC_DECODER:
                if (frameIndex == 0) {
                    // 先设置第一张图封面
                    mCurrentImage = mGifDecoder.getCoverImage();
                    postDraw();
                } else {
                    // 暂停上次并重新继续动画
                    mGifAnimator.forceFinished(true);
                    mCurrFrame = mGifAnimator.getCurrentFrame();
                    mGifDecoder.computeDelays();
                    mGifAnimator.startGifAnimation(mGifDecoder.getDelays(),
                            mCurrFrame, mGifDecoder.getLoopCount());
                    post(this);
                }
                break;
        }
        onLoadingGif(frameIndex);
    }

    @Override
    public void onDecodeFinished(boolean success) {
        //  not gif. decode to normal image;
        if (!success) {
            if (imgFile != null) {
                loadNormalBitmap(imgFile);
            } else {
                loadNormalBitmap(resId);
            }
            imgFile = null;
            resId = -1;
            return;
        }

        //当帧数大于1时，启动动画线程
        if (mGifDecoder.getFrameCount() > 1) {
            switch (mLoadingType) {
                case WAIT_FINISH:
                    mGifDecoder.computeDelays();
                    mGifAnimator.startGifAnimation(mGifDecoder.getDelays(),
                            0, mGifDecoder.getLoopCount());
                    post(this);
                    break;
                case SHOW_COVER:
                    mGifDecoder.computeDelays();
                    mGifAnimator.startGifAnimation(mGifDecoder.getDelays(),
                            0, mGifDecoder.getLoopCount());
                    post(this);
                    break;
                case SYNC_DECODER:
                    // 开始动画
                    mGifAnimator.forceFinished(true);
                    mCurrFrame = mGifAnimator.getCurrentFrame();
                    mGifDecoder.computeDelays();
                    mGifAnimator.startGifAnimation(mGifDecoder.getDelays(),
                            mCurrFrame, mGifDecoder.getLoopCount());
                    post(this);
                    break;
            }
        } else {
            postDraw();
        }
        imgFile = null;
        resId = -1;

        onLoadedGifSuccessful();
    }

    /**
     * 异步重绘
     */
    private void postDraw() {
        redrawHandler.sendMessage(redrawHandler.obtainMessage());
    }

    private Handler redrawHandler = new Handler() {
        public void handleMessage(Message msg) {
            setImageBitmap(mCurrentImage);
            invalidate();
        }
    };


    protected abstract Bitmap compressBitmap(Bitmap src);

    protected abstract void loadNormalBitmap(int resId);

    protected abstract void loadNormalBitmap(File imageFile);

    protected abstract void onLoadingGif(int frameIndex);

    protected abstract void onLoadedGifSuccessful();
}
