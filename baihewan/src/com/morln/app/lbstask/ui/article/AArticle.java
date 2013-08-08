package com.morln.app.lbstask.ui.article;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.bbs.ExpressionMap;
import com.morln.app.lbstask.bbs.model.ArticleDetail;
import com.morln.app.lbstask.bbs.model.BbsUserBase;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.GlobalStateSource;
import com.morln.app.lbstask.cache.ImageSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.engine.ImgMgrHolder;
import com.morln.app.lbstask.engine.MyImageScrollRemoteLoader;
import com.morln.app.lbstask.logic.BbsArticleMgr;
import com.morln.app.lbstask.logic.BbsPersonMgr;
import com.morln.app.lbstask.model.UserBase;
import com.morln.app.lbstask.res.BbsPic;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.ui.login.DLogin;
import com.morln.app.lbstask.ui.person.DUser;
import com.morln.app.lbstask.ui.person.TPersonInfo;
import com.xengine.android.media.image.loader.XImageLocalUrl;
import com.xengine.android.media.image.loader.XScrollRemoteLoader;
import com.xengine.android.media.image.processor.XImageProcessor;
import com.xengine.android.session.series.XSerialDownloadListener;
import com.xengine.android.system.ui.XUILayer;
import com.xengine.android.utils.XLog;
import com.xengine.android.utils.XStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 帖子详情的List适配器
 * (帖子详情 = 主贴 + 回帖(多个))
 * (主贴/回帖 = 头 + 内容(多段) + 脚)
 * Created by jasontujun.
 * Date: 12-10-3
 * Time: 下午10:32
 */
public class AArticle extends BaseAdapter implements AbsListView.OnScrollListener {
    private static final String TAG = "ARTICLE";
    private ImageSource imageSource;

    private XUILayer layer;
    private ListView host;

    private List<ArticleDetail> articleFloors = new ArrayList<ArticleDetail>();// 帖子楼层：主题帖+跟帖
    private int itemCount;// 列表项数量
    private int[] itemHostArray;// 列表项所属的帖子
    private int[] itemTypeArray;// 列表项的类型
    private int[] itemIndexArray;// 列表项的索引位置
    private List<String> imgUrlList = new ArrayList<String>();
    private List<XSerialDownloadListener> listenerList = new ArrayList<XSerialDownloadListener>();

    // 表情字符
    private ExpressionMap expressionMap;

    // 监听帖子的刷新
    private RefreshListener refreshListener;
    // 图片加载器
    private XScrollRemoteLoader mImageScrollLoader;

    // view类型
    private static final int HEAD_VIEW = 0;
    private static final int CONTENT_WORD_VIEW = 1;
    private static final int CONTENT_IMAGE_VIEW = 2;
    private static final int FOOT_VIEW = 3;


    public AArticle(XUILayer layer, ListView host) {
        this.layer = layer;
        this.host = host;

        imageSource = (ImageSource) DataRepo.getInstance().getSource(SourceName.IMAGE);
        expressionMap = ExpressionMap.getInstance();
        mImageScrollLoader = MyImageScrollRemoteLoader.getInstance();
    }

    public RefreshListener getRefreshListener() {
        return refreshListener;
    }

    public void setRefreshListener(RefreshListener listener) {
        this.refreshListener = listener;
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
        ImgMgrHolder.getImageSerialDownloadMgr().stopAndReset();// 清空后台线程
        mImageScrollLoader.stopAndClear();

        // 还原帖子图片
        for (int i = 0; i < articleFloors.size(); i++) {
            ArticleDetail article = articleFloors.get(i);
            if (article != null) {
                article.resetImg();
            }
        }
    }


    /**
     * 刷新楼层(当前的楼层位置不变)
     * @param articleId
     * @param articleBoard
     */
    public void refresh(String articleId, String articleBoard) {
        // 清空之前的数据
        articleFloors.clear();
        itemCount = 0;
        itemHostArray = null;
        itemTypeArray = null;
        itemIndexArray = null;
        imgUrlList.clear();
        listenerList.clear();

        // 初始化articleFloors
        ArticleDetail theme = BbsArticleMgr.getInstance().
                getThemeArticleFromLocal(articleId, articleBoard);
        if (theme == null) {
            return;
        }
        articleFloors.add(theme);
        if (refreshListener != null) // 通知监听者
            refreshListener.isArticleDeleted(theme.isDeleted());
        if (!theme.isDeleted()) {// 不是被删除的帖子
            List<ArticleDetail> replyList = BbsArticleMgr.getInstance().
                    getArticleReplyList(articleId, articleBoard, -1);
            if (replyList != null && replyList.size() > 0) {
                articleFloors.addAll(replyList);
            }
        }

        // 计算itemCount
        for (int i = 0; i<articleFloors.size(); i++) {
            ArticleDetail article = articleFloors.get(i);
            itemCount = itemCount + article.getWordBlockSize()
                    + article.getImgSize() + 2;// 加上头和尾
        }
        XLog.d(TAG, "itemCount:" + itemCount);

        // 初始化itemHostArray和itemTypeArray
        itemHostArray = new int[itemCount];
        itemTypeArray = new int[itemCount];
        itemIndexArray = new int[itemCount];
        int itemIndex = 0;
        for (int i = 0; i<articleFloors.size(); i++) {
            ArticleDetail article = articleFloors.get(i);
            itemHostArray[itemIndex] = i;
            itemTypeArray[itemIndex] = HEAD_VIEW;
            itemIndex++;
            for (int j = 0; j < article.getWordBlockSize(); j++) {
                // 文字
                itemHostArray[itemIndex] = i;
                itemTypeArray[itemIndex] = CONTENT_WORD_VIEW;
                itemIndexArray[itemIndex] = j;
                itemIndex++;
                // 图片
                if (j < article.getImgSize()) {
                    itemHostArray[itemIndex] = i;
                    itemTypeArray[itemIndex] = CONTENT_IMAGE_VIEW;
                    itemIndexArray[itemIndex] = j;
                    itemIndex++;
                }
            }
            itemHostArray[itemIndex] = i;
            itemTypeArray[itemIndex] = FOOT_VIEW;
            itemIndex++;
        }

        // 初始化imageUrlList和listenerList
        for (int i = 0; i < articleFloors.size(); i++) {
            List<String> urlList = articleFloors.get(i).getImgUrls();
            imgUrlList.addAll(urlList);
            for (int j = 0; j < urlList.size(); j++) {
                listenerList.add(new ListImageDownloadListener(host, urlList, j));
            }
        }

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
        int articleIndex = itemHostArray[i];
        return articleFloors.get(articleIndex);
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
        if (articleFloors.size() == 0 || getItem(position) == null) {
            return null;
        }

        int type = getItemViewType(position);
        XLog.d(TAG, "view type:" + type + ", position:" + position);
        final ArticleDetail article = (ArticleDetail) getItem(position);
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
                } else {
                    holderHead = (HeadViewHolder) convertView.getTag();
                }

                // 楼层表示
                int articleIndex = itemHostArray[position];
                if (articleIndex == 0) {
                    holderHead.floor.setText("楼主");
                } else if (articleIndex == 1) {
                    holderHead.floor.setText("沙发");
                } else if (articleIndex == 2) {
                    holderHead.floor.setText("板凳");
                } else {
                    holderHead.floor.setText(article.getFloorCount() + "楼");
                }
                // 作者(作者id + 昵称)
                String hostAuthor = articleFloors.get(0).getAuthorName();
                final String floorAuthor = article.getAuthorName();
                holderHead.author.setText(floorAuthor + " (" + article.getAuthorNickname());
                if (floorAuthor.equals(hostAuthor)) {
                    holderHead.author.setTextColor(layer.getContext().getResources().getColor(R.color.light_red));
                    holderHead.rightBrace.setTextColor(layer.getContext().getResources().getColor(R.color.light_red));
                } else {
                    holderHead.author.setTextColor(layer.getContext().getResources().getColor(R.color.light_green));
                    holderHead.rightBrace.setTextColor(layer.getContext().getResources().getColor(R.color.light_green));
                }
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
                    holderHead.title.setText(article.getTitle());
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
                String word = article.getWordBlocks().get(wordBlockIndex);
                if (!XStringUtil.isNullOrEmpty(word)) {
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
                final String imgUrl = article.getImgUrls().get(imgIndex);
                final String localImg = imageSource.getLocalImage(imgUrl);
                holderImage.imageView.setTag(imgUrl);// TIP 关键点1，设置该imageView的tag
                // 设置图片监听
                holderImage.imageView.setOnClickListener(new ImageViewClickListener
                        (imgIndex, imgUrl, holderImage.imageView, imgUrlList));
                // 设置图片大小
                ViewGroup.LayoutParams params = holderImage.imageView.getLayoutParams();
                if (XStringUtil.isNullOrEmpty(localImg) ||
                        localImg.equals(XImageLocalUrl.IMG_ERROR) ||
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
                        new ListImageDownloadListener(host, article.getImgUrls(), imgIndex));

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
                } else {
                    holderFoot = (FootViewHolder) convertView.getTag();
                }

                // 日期
                holderFoot.time.setText(article.getDate());
                // 回复按钮
                if (article.isDeleted()) {
                    holderFoot.replyBtn.setVisibility(View.INVISIBLE);
                } else {
                    holderFoot.replyBtn.setVisibility(View.VISIBLE);
                }
                holderFoot.replyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 登陆权限检测
                        GlobalStateSource globalStateSource = (GlobalStateSource) DataRepo.
                                getInstance().getSource(SourceName.GLOBAL_STATE);
                        if (!globalStateSource.isLogin()) {
                            new DLogin(layer, true).show();
                            return;
                        }

                        Handler handler1 = layer.getFrameHandler();
                        Message msg = handler1.obtainMessage();
                        msg.what = MainMsg.BBS_REPLY_ARTICLE;
                        Bundle bundle = new Bundle();
                        bundle.putString("id", article.getId());
                        bundle.putString("board", article.getBoard());
                        bundle.putString("title", article.getTitle());
                        bundle.putInt("floor", article.getFloorCount());
                        bundle.putString("author", article.getAuthorName());
                        msg.setData(bundle);
                        handler1.sendMessage(msg);
                    }
                });
                return convertView;
            }
        }
        return null;
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
            case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                XLog.d(TAG, "SCROLL_STATE_FLING");
                mImageScrollLoader.onScroll();
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                XLog.d(TAG, "SCROLL_STATE_IDLE");
                mImageScrollLoader.onIdle();
                break;
            case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                XLog.d(TAG, "SCROLL_STATE_TOUCH_SCROLL");
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
        private ListView listView;
        private List<String> imgUrlList;
        private int imgIndex;

        ListImageDownloadListener(ListView listView, List<String> imgUrlList, int imgIndex) {
            this.listView = listView;
            this.imgUrlList = imgUrlList;
            this.imgIndex = imgIndex;
        }

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
            final ImageView imageViewByTag = (ImageView) listView.findViewWithTag(imgUrl);
            if (imageViewByTag != null) {
                final String localImg = imageSource.getLocalImage(imgUrl);
                // 设置图片大小
                ViewGroup.LayoutParams params = imageViewByTag.getLayoutParams();
                if (XStringUtil.isNullOrEmpty(localImg) ||
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
            if (XStringUtil.isNullOrEmpty(localImg)) {
                // 下载图片
                mImageScrollLoader.asyncLoadBitmap(layer.getContext(), imgUrl,
                        imageView, XImageProcessor.ImageSize.SCREEN,
                        new ListImageDownloadListener(host, imgUrlList, imgIndex));
                mImageScrollLoader.onIdle();
            } else if (localImg.equals(XImageLocalUrl.IMG_ERROR)) {
                // 下载图片
                mImageScrollLoader.asyncLoadBitmap(layer.getContext(), imgUrl,
                        imageView, XImageProcessor.ImageSize.SCREEN,
                        new ListImageDownloadListener(host, imgUrlList, imgIndex));
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

    interface RefreshListener {
        void isArticleDeleted(boolean isDeleted);
    }

}
