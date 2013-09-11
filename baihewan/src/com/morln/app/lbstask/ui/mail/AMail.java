package com.morln.app.lbstask.ui.mail;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.utils.ExpressionMap;
import com.morln.app.lbstask.data.model.BbsUserBase;
import com.morln.app.lbstask.data.model.Mail;
import com.morln.app.lbstask.data.cache.ImageSource;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.engine.MyImageScrollRemoteLoader;
import com.morln.app.lbstask.logic.BbsMailMgr;
import com.morln.app.lbstask.logic.BbsPersonMgr;
import com.morln.app.lbstask.data.model.UserBase;
import com.morln.app.lbstask.res.BbsPic;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.ui.person.DUser;
import com.morln.app.lbstask.ui.person.TPersonInfo;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.media.image.loader.XImageLocalUrl;
import com.xengine.android.media.image.loader.XScrollRemoteLoader;
import com.xengine.android.media.image.processor.XImageProcessor;
import com.xengine.android.session.download.XSerialDownloadListener;
import com.xengine.android.system.ui.XUILayer;
import com.xengine.android.utils.XLog;

import java.util.List;

/**
 * 邮件详情的List适配器
 * (帖子详情 = 主贴)
 * Created by jasontujun.
 * Date: 12-10-25
 * Time: 下午9:45
 */
public class AMail extends BaseAdapter implements AbsListView.OnScrollListener  {
    private static final String TAG = "MAIL";
    private ImageSource imageSource;

    private XUILayer layer;
    private ListView host;

    private Mail mail;// 帖子楼层：主题帖+跟帖
    private int itemCount;// 列表项数量
    private int[] itemTypeArray;// 列表项的类型
    private int[] itemIndexArray;// 列表项的索引位置

    // 表情字符
    private ExpressionMap expressionMap;
    // 图片加载器
    private XScrollRemoteLoader mImageScrollLoader;

    // view类型
    private static final int HEAD_VIEW = 0;
    private static final int CONTENT_WORD_VIEW = 1;
    private static final int CONTENT_IMAGE_VIEW = 2;
    private static final int FOOT_VIEW = 3;


    public AMail(XUILayer layer, ListView host) {
        this.layer = layer;
        this.host = host;

        imageSource = (ImageSource) DefaultDataRepo
                .getInstance().getSource(SourceName.IMAGE);
        expressionMap = ExpressionMap.getInstance();
        mImageScrollLoader = MyImageScrollRemoteLoader.getInstance();
        mImageScrollLoader.setWorking();
    }


    /**
     * 启动线性下载的异步线程
     */
    public void autoDownloadImg() {
//        SystemSettingSource systemSettingSource = (SystemSettingSource) DataRepo.
//                getInstance().getSource(SourceName.SYSTEM_SETTING);
//        int isAutoDownLoad = systemSettingSource.getAutoDownloadImg();
//        if (isAutoDownLoad == SystemSettingSource.AUTO_DOWNLOAD_IMG_CLOSE) {
//            return;
//        }
//        if ((isAutoDownLoad == SystemSettingSource.AUTO_DOWNLOAD_IMG_WIFI &&
//                XNetworkUtil.isWifiConnected()) ||
//                isAutoDownLoad == SystemSettingSource.AUTO_DOWNLOAD_IMG_ALWAYS)
//            ImageUtil.serialDownloadImage(imgUrlList, listenerList);
        notifyDataSetChanged();
    }

    /**
     * 清空后台加载图片的线程，并还原帖子图片（有或无）
     */
    public void clearImgAsyncTasks() {
        // 清空后台线程
        mImageScrollLoader.stopAndClear();
        mImageScrollLoader.setWorking();
        // 还原帖子图片
        if (mail != null)
            mail.resetImg();
    }

    /**
     * 刷新楼层(当前的楼层位置不变)
     * @param mailId
     */
    public void refresh(String mailId) {
        // 清空之前的数据
        mail = null;
        itemCount = 0;
        itemTypeArray = null;
        itemIndexArray = null;

        // 初始化articleFloors
        mail= BbsMailMgr.getInstance().getMailDetailFromLocal(mailId);
        if (mail == null) {
            return;
        }

        // 计算itemCount
        itemCount = itemCount + mail.getWordBlockSize()
                + mail.getImgSize() + 2;// 加上头和尾
        XLog.d(TAG, "itemCount:" + itemCount);

        // 初始化itemHostArray和itemTypeArray
        itemTypeArray = new int[itemCount];
        itemIndexArray = new int[itemCount];
        int itemIndex = 0;
        itemTypeArray[itemIndex] = HEAD_VIEW;
        itemIndex++;
        for (int j = 0; j < mail.getWordBlockSize(); j++) {
            // 文字
            itemTypeArray[itemIndex] = CONTENT_WORD_VIEW;
            itemIndexArray[itemIndex] = j;
            itemIndex++;
            // 图片
            if (j < mail.getImgSize()) {
                itemTypeArray[itemIndex] = CONTENT_IMAGE_VIEW;
                itemIndexArray[itemIndex] = j;
                itemIndex++;
            }
        }
        itemTypeArray[itemIndex] = FOOT_VIEW;
        notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        return itemTypeArray[position];
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getCount() {
        return itemCount;
    }

    @Override
    public Object getItem(int i) {
        return mail;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }



    private class HeadViewHolder {
        public TextView author;
        public TextView rightBrace;
        public TextView floor;
        public TextView title;
    }

    private class ContentWordHolder {
        public TextView wordView;
    }

    private class ContentImageHolder {
        public ImageView imageView;
    }

    public class FootViewHolder {
        public TextView time;
        public ImageView replyBtn;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        if (getItem(position) == null) {
            return null;
        }

        int type = getItemViewType(position);
        XLog.d(TAG, "view type:" + type + ", position:" + position);
        final Mail mail = (Mail) getItem(position);
        switch (type) {
            // 头部（标题、作者、楼层等）
            case HEAD_VIEW: {
                HeadViewHolder holderHead = null;
                if (convertView == null) {
                    convertView = View.inflate(layer.getContext(), R.layout.bbs_article_floor_head, null);
                    holderHead = new HeadViewHolder();
                    holderHead.author = (TextView) convertView.findViewById(R.id.author);
                    holderHead.rightBrace = (TextView) convertView.findViewById(R.id.author_right_brace);
                    holderHead.floor = (TextView) convertView.findViewById(R.id.floor);
                    holderHead.title = (TextView) convertView.findViewById(R.id.title);
                    convertView.setTag(holderHead);
                }else {
                    holderHead = (HeadViewHolder) convertView.getTag();
                }

                // 楼层表示
                holderHead.floor.setVisibility(View.GONE);
                // 作者(作者id + 昵称)
                final String floorAuthor = mail.getSender();
                holderHead.author.setText(floorAuthor + " (" + mail.getSenderNickName());
                    holderHead.author.setTextColor(layer.getContext().getResources().getColor(R.color.light_red));
                    holderHead.rightBrace.setTextColor(layer.getContext().getResources().getColor(R.color.light_red));

                holderHead.author.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 弹出对话框
                        UserBase user = BbsPersonMgr.getInstance().getBbsUserInfoFromLocal(floorAuthor);
                        if (user != null) {
                            new DUser(layer, floorAuthor).show();
                        } else {
                            new TPersonInfo(layer.getUIFrame(), floorAuthor, true,
                                    new TPersonInfo.GetUserInfoListener() {
                                        @Override
                                        public void onGettingUserInfo(BbsUserBase userInfo) {
                                            if (userInfo != null) {
                                                new DUser(layer, floorAuthor).show();
                                            } else {
                                                Toast.makeText(layer.getContext(), "找不到这个用户……", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        @Override
                                        public void onCancelled() {
                                        }
                                    }).execute(null);
                        }
                    }
                });
                // 标题
                if (position == 0) {
                    holderHead.title.setText(mail.getTitle());
                    holderHead.title.setVisibility(View.VISIBLE);
                } else {
                    holderHead.title.setVisibility(View.GONE);
                }
                return convertView;
            }

            // 一块文字内容
            case CONTENT_WORD_VIEW: {
                ContentWordHolder holderWord = null;
                if (convertView == null) {
                    convertView = View.inflate(layer.getContext(), R.layout.bbs_article_floor_content_word, null);
                    holderWord = new ContentWordHolder();
                    holderWord.wordView = (TextView) convertView.findViewById(R.id.word);
                    convertView.setTag(holderWord);
                } else {
                    holderWord = (ContentWordHolder) convertView.getTag();
                }
                int wordBlockIndex = itemIndexArray[position];
                String word = mail.getWordBlocks().get(wordBlockIndex);
                if (!TextUtils.isEmpty(word)) {
                    // 把表情符替换为表情图片
                    CharSequence spannedWord = expressionMap.changeToSpanString(layer, word);
                    holderWord.wordView.setText(spannedWord);
                } else {
                    holderWord.wordView.setText("");
                }
                return convertView;
            }

            // 一块图片内容
            case CONTENT_IMAGE_VIEW: {
                ContentImageHolder holderImage = null;
                if (convertView == null) {
                    convertView = View.inflate(layer.getContext(), R.layout.bbs_article_floor_content_image, null);
                    holderImage = new ContentImageHolder();
                    holderImage.imageView = (ImageView) convertView.findViewById(R.id.image);
                    convertView.setTag(holderImage);
                } else {
                    holderImage = (ContentImageHolder) convertView.getTag();
                }
                final int imgIndex = itemIndexArray[position];
                final String imgUrl = mail.getImgUrls().get(imgIndex);
                final String localImg = imageSource.getLocalImage(imgUrl);
                holderImage.imageView.setTag(imgUrl);// TIP 关键点1，设置该imageView的tag
                // 设置图片监听
                holderImage.imageView.setOnClickListener(new ImageViewClickListener
                        (imgIndex, imgUrl, holderImage.imageView, mail.getImgUrls()));
                // 设置图片大小
                ViewGroup.LayoutParams params = holderImage.imageView.getLayoutParams();
                if (TextUtils.isEmpty(localImg) || localImg.equals(XImageLocalUrl.IMG_ERROR) ||
                        localImg.equals(XImageLocalUrl.IMG_LOADING)) {
                    params.width = layer.screen().dp2px(100);
                    params.height = layer.screen().dp2px(88);
                    holderImage.imageView.setLayoutParams(params);
                } else {
                    params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    holderImage.imageView.setLayoutParams(params);
                }
                mImageScrollLoader.asyncLoadBitmap(layer.getContext(), imgUrl,
                        holderImage.imageView, XImageProcessor.ImageSize.SCREEN,
                        new ListImageDownloadListener());

                return convertView;
            }

            // 尾部（日期、回复）
            case FOOT_VIEW: {
                FootViewHolder holderFoot = null;
                if (convertView == null) {
                    convertView = View.inflate(layer.getContext(), R.layout.bbs_article_floor_bottom, null);
                    holderFoot = new FootViewHolder();
                    holderFoot.time = (TextView) convertView.findViewById(R.id.time);
                    holderFoot.replyBtn = (ImageView) convertView.findViewById(R.id.reply_btn);
                    layer.setImageViewPic(holderFoot.replyBtn, BbsPic.REPLY_BTN);
                    convertView.setTag(holderFoot);
                }else {
                    holderFoot = (FootViewHolder) convertView.getTag();
                }

                // 日期
                holderFoot.time.setText(mail.getDate());
                // 回复按钮
                holderFoot.replyBtn.setVisibility(View.INVISIBLE);

                return convertView;
            }
        }
        return null;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                mImageScrollLoader.onScroll();
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                mImageScrollLoader.onIdle();
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                mImageScrollLoader.onScroll();
                break;

            default:
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
    }

    private class ListImageDownloadListener implements XSerialDownloadListener {
        @Override
        public void beforeDownload(String s) {
        }

        @Override
        public void onStart(String s) {
        }

        @Override
        public void doDownload(String s, long l) {
        }

        @Override
        public void onComplete(String s, String s2) {
        }

        @Override
        public void onError(String s, String s2) {
        }

        @Override
        public void afterDownload(final String imgUrl) {
            // TIP 关键点2，通过之前设置的tag获取imageView
            final ImageView imageViewByTag = (ImageView) host.findViewWithTag(imgUrl);
            if (imageViewByTag != null) {
                final String localImg = imageSource.getLocalImage(imgUrl);
                // 设置图片大小
                ViewGroup.LayoutParams params = imageViewByTag.getLayoutParams();
                if (TextUtils.isEmpty(localImg) ||
                        localImg.equals(XImageLocalUrl.IMG_ERROR) ||
                        localImg.equals(XImageLocalUrl.IMG_LOADING)) {
                    params.width = layer.screen().dp2px(100);
                    params.height = layer.screen().dp2px(88);
                    imageViewByTag.setLayoutParams(params);
                } else {
                    params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    imageViewByTag.setLayoutParams(params);
                }
            }
        }
    }


    private class ImageViewClickListener implements View.OnClickListener {
        private int imgIndex;
        private String imgUrl;
        private ImageView imageView;
        private List<String> imgUrlList;

        private ImageViewClickListener(int imgIndex, String imgUrl,
                                       ImageView imageView, List<String> imgUrlList) {
            this.imgIndex = imgIndex;
            this.imgUrl = imgUrl;
            this.imageView = imageView;
            this.imgUrlList = imgUrlList;
        }

        @Override
        public void onClick(View view) {
            final String localImg = imageSource.getLocalImage(imgUrl);
            if (TextUtils.isEmpty(localImg)) {
                // 下载图片
                mImageScrollLoader.asyncLoadBitmap(layer.getContext(), imgUrl,
                        imageView, XImageProcessor.ImageSize.SCREEN,
                        new ListImageDownloadListener());
                mImageScrollLoader.onIdle();
            } else if (localImg.equals(XImageLocalUrl.IMG_ERROR)) {
                // 下载图片
                mImageScrollLoader.asyncLoadBitmap(layer.getContext(), imgUrl,
                        imageView, XImageProcessor.ImageSize.SCREEN,
                        new ListImageDownloadListener());
                mImageScrollLoader.onIdle();
                Toast.makeText(layer.getContext(), "尝试重新加载图片！", Toast.LENGTH_SHORT).show();
            } else if (localImg.equals(XImageLocalUrl.IMG_LOADING)) {
                Toast.makeText(layer.getContext(), "努力加载图片中，请稍后……", Toast.LENGTH_SHORT).show();
            } else {
                // 查看图片详情
                Handler handler = layer.getFrameHandler();
                Message msg = handler.obtainMessage(MainMsg.SEE_IMAGE_DETAIL, imgUrlList);
                msg.arg1 = imgIndex;
                handler.sendMessage(msg);
            }
        }
    }

}
