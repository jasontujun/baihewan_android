package com.morln.app.lbstask.ui.collection;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.model.ArticleBase;
import com.morln.app.lbstask.data.model.Mail;
import com.morln.app.lbstask.logic.BbsMailMgr;
import com.morln.app.lbstask.res.BbsMsg;
import com.morln.app.lbstask.ui.Linear;
import com.xengine.android.system.ui.XBaseComponent;
import com.xengine.android.system.ui.XUILayer;

/**
 * Created by jasontujun.
 * Date: 12-2-29
 * Time: 下午1:27
 */
public class CRssAndCollection extends XBaseComponent implements Linear<ArticleBase> {
    private int topBarSelectedIndex;// 顶栏所选择的标签索引

    // 顶部栏
    private TextView btn1, btn2;
    private ImageView tip1, tip2;
    private ImageView leftBtn, rightBtn, leftBtnTip;
    // 内容
    private RelativeLayout contentFrame;
    private CCollection cCollection;
    private CRss cRss;

    public CRssAndCollection(XUILayer parent) {
        super(parent);
        setContentView(R.layout.main_bbs_content);

        // 顶部栏
        btn1 = (TextView) findViewById(R.id.top_btn1);
        btn2 = (TextView) findViewById(R.id.top_btn2);
        tip1 = (ImageView) findViewById(R.id.top_tip1);
        tip2 = (ImageView) findViewById(R.id.top_tip2);
        leftBtn = (ImageView) findViewById(R.id.left_btn);
        leftBtnTip = (ImageView) findViewById(R.id.left_btn_tip);
        rightBtn = (ImageView) findViewById(R.id.right_btn);
        contentFrame = (RelativeLayout) findViewById(R.id.content_frame);
        cCollection = new CCollection(parent);
        cRss = new CRss(parent);

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

        btn1.setText("我的订阅");
        btn2.setText("我的收藏");
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (topBarSelectedIndex != 0) {
                    topBarSelectedIndex = 0;
                    Resources res = getContext().getResources();
                    btn1.setTextColor(res.getColor(R.color.light_green));
                    tip1.setVisibility(View.VISIBLE);
                    btn2.setTextColor(res.getColor(R.color.gray));
                    tip2.setVisibility(View.INVISIBLE);

                    contentFrame.addView(cRss.getContent());
                    contentFrame.removeView(cCollection.getContent());

                    cRss.onLayerUnCovered();
                    cCollection.onLayerCovered();
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

                    contentFrame.removeView(cRss.getContent());
                    contentFrame.addView(cCollection.getContent());

                    cRss.onLayerCovered();
                    cCollection.onLayerUnCovered();
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
        btn1.performClick();
    }

    /**
     * 刷新
     */
    public void refresh() {
        cRss.refresh();
        cCollection.refresh();
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
            return cCollection.getPre();
        }
        return null;
    }

    @Override
    public ArticleBase getNext() {
        if(topBarSelectedIndex == 1) {
            return cCollection.getNext();
        }
        return null;
    }

    @Override
    public int back() {
        if(topBarSelectedIndex == 0){
            return cRss.back();
        }else {
            return cCollection.back();
        }
    }

    @Override
    public void onLayerUnCovered() {
        super.onLayerUnCovered();
        if (topBarSelectedIndex == 0)
            cRss.onLayerUnCovered();
        else
            cCollection.onLayerUnCovered();
    }

    @Override
    public void onLayerCovered() {
        super.onLayerCovered();
        if (topBarSelectedIndex == 0)
            cRss.onLayerCovered();
        else
            cCollection.onLayerCovered();
    }
}
