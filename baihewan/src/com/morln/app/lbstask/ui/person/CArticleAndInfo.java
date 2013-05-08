package com.morln.app.lbstask.ui.person;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.bbs.model.ArticleBase;
import com.morln.app.lbstask.bbs.model.Mail;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.GlobalStateSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.logic.BbsMailMgr;
import com.morln.app.lbstask.res.BbsMsg;
import com.morln.app.lbstask.ui.Linear;
import com.morln.app.system.ui.XBaseComponent;
import com.morln.app.system.ui.XUILayer;

/**
 * Created by jasontujun.
 * Date: 12-3-12
 * Time: 下午12:58
 */
public class CArticleAndInfo extends XBaseComponent implements Linear<ArticleBase> {
    private GlobalStateSource globalStateSource;
    // 用户
    private String userId;

    // 顶栏所选择的标签索引
    private int topBarSelectedIndex;
    private TextView btn1, btn2;
    private ImageView tip1, tip2;
    private ImageView leftBtn, rightBtn, leftBtnTip;
    private RelativeLayout contentFrame;
    private CSelfArticle cSelfArticle;
    private CPersonInfo cPersonInfo;

    public CArticleAndInfo(XUILayer parent) {
        super(parent);
        setContentView(R.layout.main_bbs_content);
        globalStateSource = (GlobalStateSource) DataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);

        RelativeLayout frame = (RelativeLayout) findViewById(R.id.frame);
        btn1 = (TextView) findViewById(R.id.top_btn1);
        btn2 = (TextView) findViewById(R.id.top_btn2);
        tip1 = (ImageView) findViewById(R.id.top_tip1);
        tip2 = (ImageView) findViewById(R.id.top_tip2);
        leftBtn = (ImageView) findViewById(R.id.left_btn);
        leftBtnTip = (ImageView) findViewById(R.id.left_btn_tip);
        rightBtn = (ImageView) findViewById(R.id.right_btn);
        contentFrame = (RelativeLayout) findViewById(R.id.content_frame);
        cSelfArticle = new CSelfArticle(parent);
        cPersonInfo = new CPersonInfo(parent);

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler layerHandler = getLayerHandler();
                layerHandler.sendMessage(layerHandler.obtainMessage(BbsMsg.SWITCH_LEFT));
            }
        });
        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler layerHandler = getLayerHandler();
                layerHandler.sendMessage(layerHandler.obtainMessage(BbsMsg.SWITCH_RIGHT));
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(topBarSelectedIndex != 0) {
                    topBarSelectedIndex = 0;
                    Resources res = getContext().getResources();
                    btn1.setTextColor(res.getColor(R.color.light_green));
                    tip1.setVisibility(View.VISIBLE);
                    btn2.setTextColor(res.getColor(R.color.gray));
                    tip2.setVisibility(View.INVISIBLE);

                    contentFrame.removeView(cSelfArticle.getContent());
                    contentFrame.addView(cPersonInfo.getContent());

                    cSelfArticle.onLayerUnCovered();
                    cPersonInfo.onLayerCovered();

                    cPersonInfo.show(userId);
                }
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (topBarSelectedIndex != 1) {
                    topBarSelectedIndex = 1;
                    Resources res = getContext().getResources();
                    btn2.setTextColor(res.getColor(R.color.light_green));
                    tip2.setVisibility(View.VISIBLE);
                    btn1.setTextColor(res.getColor(R.color.gray));
                    tip1.setVisibility(View.INVISIBLE);

                    contentFrame.addView(cSelfArticle.getContent());
                    contentFrame.removeView(cPersonInfo.getContent());

                    cSelfArticle.onLayerCovered();
                    cPersonInfo.onLayerUnCovered();

                    cSelfArticle.show(userId);
                }
            }
        });
        BbsMailMgr.getInstance().registerNewMailListener(new Mail.NewMailListener() {
            @Override
            public void remind(int newMailNumber) {
                refreshLeftBtnTip(newMailNumber);
            }
        });

        // 初始化列表
        topBarSelectedIndex = -1;
    }


    /**
     * 设置用户对象，然后显示对应标签的界面内容(用于第一次点击“我的百合”)
     * @param userId
     */
    public void show(String userId) {
        this.userId = userId;

        if(globalStateSource.getCurrentUserName().equals(userId)) {
            btn1.setText("我的资料");
            btn2.setText("我的帖子");
        }else {
            btn1.setText("Ta的资料");
            btn2.setText("Ta的帖子");
        }

        if(topBarSelectedIndex == 0) {
            cPersonInfo.show(userId);
        }else {
            btn1.performClick();
        }
    }


    /**
     * 异步刷新列表
     */
    private void refreshLeftBtnTip(int newNumber) {
        Message msg= handler.obtainMessage(REFRESH_LIST);
        msg.arg1 = newNumber;
        handler.sendMessage(msg);
    }
    private static final int REFRESH_LIST = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_LIST:
                    if (msg.arg1 == 0) {
                        leftBtnTip.setVisibility(View.GONE);
                    } else {
                        leftBtnTip.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }
    };

    @Override
    public ArticleBase getPre() {
        if(topBarSelectedIndex == 1) {
            return cSelfArticle.getPre();
        }
        return null;
    }

    @Override
    public ArticleBase getNext() {
        if(topBarSelectedIndex == 1) {
            return cSelfArticle.getNext();
        }
        return null;
    }

    @Override
    public int back() {
        if(topBarSelectedIndex == 0) {
            return cPersonInfo.back();
        }else {
            return cSelfArticle.back();
        }
    }

    @Override
    public void onLayerUnCovered() {
        super.onLayerUnCovered();
        if (topBarSelectedIndex == 0)
            cPersonInfo.onLayerUnCovered();
        else
            cSelfArticle.onLayerUnCovered();
    }

    @Override
    public void onLayerCovered() {
        super.onLayerCovered();
        if (topBarSelectedIndex == 0)
            cPersonInfo.onLayerCovered();
        else
            cSelfArticle.onLayerCovered();
    }
}
