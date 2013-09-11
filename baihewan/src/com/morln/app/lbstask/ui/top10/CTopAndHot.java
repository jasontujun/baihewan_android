package com.morln.app.lbstask.ui.top10;

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
 * Date: 12-3-8
 * Time: 上午12:38
 */
public class CTopAndHot extends XBaseComponent implements Linear<ArticleBase> {
    // 顶栏所选择的标签索引
    private int topBarSelectedIndex;

    private TextView btn1, btn2;
    private ImageView tip1, tip2;
    private ImageView leftBtn, rightBtn, leftBtnTip;
    private RelativeLayout contentFrame;
    private CTop10 cTop10;
    private CHot cHot;

    public CTopAndHot(XUILayer parent) {
        super(parent);
        setContentView(R.layout.main_bbs_content);

        btn1 = (TextView) findViewById(R.id.top_btn1);
        btn2 = (TextView) findViewById(R.id.top_btn2);
        tip1 = (ImageView) findViewById(R.id.top_tip1);
        tip2 = (ImageView) findViewById(R.id.top_tip2);
        leftBtn = (ImageView) findViewById(R.id.left_btn);
        leftBtnTip = (ImageView) findViewById(R.id.left_btn_tip);
        rightBtn = (ImageView) findViewById(R.id.right_btn);
        contentFrame = (RelativeLayout) findViewById(R.id.content_frame);
        cTop10 = new CTop10(parent);
        cHot = new CHot(parent);

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

        btn1.setText("全站十大");
        btn2.setText("各区热点");
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

                    contentFrame.addView(cTop10.getContent());
                    contentFrame.removeView(cHot.getContent());

                    cTop10.onLayerUnCovered();
                    cHot.onLayerCovered();
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

                    contentFrame.removeView(cTop10.getContent());
                    contentFrame.addView(cHot.getContent());

                    cTop10.onLayerCovered();
                    cHot.onLayerUnCovered();

                    // 如果各区热点为空，则自动加载一些
                    if(cHot.isEmpty()) {
                        cHot.refreshTitle();
                        cHot.getZoneHot(1);
                    }
                }
            }
        });
        BbsMailMgr.getInstance().registerNewMailListener(new Mail.NewMailListener() {
            @Override
            public void remind(int newMailNumber) {
                refreshLeftBtnTip(newMailNumber);
            }
        });

        // 默认选择十大列表
        topBarSelectedIndex = -1;
        btn1.performClick();
    }

    public void firstGetTop10(){
        cTop10.refreshTodayTop10(true);
    }



    @Override
    public ArticleBase getPre() {
        if(topBarSelectedIndex == 0) {
            return cTop10.getPre();
        }else {
            return cHot.getPre();
        }
    }

    @Override
    public ArticleBase getNext() {
        if(topBarSelectedIndex == 0) {
            return cTop10.getNext();
        }else {
            return cHot.getNext();
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
    public int back() {
        if(topBarSelectedIndex == 0){
            return cTop10.back();
        }else {
            return cHot.back();
        }
    }

    @Override
    public void onLayerUnCovered() {
        super.onLayerUnCovered();
        if (topBarSelectedIndex == 0)
            cTop10.onLayerUnCovered();
        else
            cHot.onLayerUnCovered();
    }

    @Override
    public void onLayerCovered() {
        super.onLayerCovered();
        if (topBarSelectedIndex == 0)
            cTop10.onLayerCovered();
        else
            cHot.onLayerCovered();
    }
}
