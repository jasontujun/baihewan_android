package com.morln.app.lbstask.ui.main;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.GlobalStateSource;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.ui.controls.viewflow.CircleFlowIndicator;
import com.morln.app.lbstask.ui.controls.viewflow.ViewFlow;
import com.morln.app.lbstask.res.BbsMsg;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseComponent;
import com.xengine.android.system.ui.XUILayer;

/**
 * 提示教程界面
 * Created by jasontujun.
 * Date: 12-9-19
 * Time: 下午12:23
 */
public class CTutorial extends XBaseComponent {

    private GlobalStateSource globalStateSource;

    // 教程类型
    private int tutorialType;
    public static final int TUTORIAL_TYPE_LEFT_BAR = 1;
    public static final int TUTORIAL_TYPE_TOP10 = 2;
    public static final int TUTORIAL_TYPE_RSS = 3;

    // 教程图片
    private static final Integer[] TUTORIAL_IMG_LEFT_BAR = new Integer[]
            {R.drawable.tutorial_left_bar_a, R.drawable.tutorial_left_bar_b, R.drawable.tutorial_left_bar_c};
    private static final Integer[] TUTORIAL_IMG_TOP10 = new Integer[]
            {R.drawable.tutorial_top10_a, R.drawable.tutorial_top10_b, R.drawable.tutorial_top10_c};
    private static final Integer[] TUTORIAL_IMG_RSS = new Integer[]
            {R.drawable.tutorial_rss_a, R.drawable.tutorial_rss_b, R.drawable.tutorial_rss_c};

    private int currentScreen;

    private ViewFlow tutorialViewFlow;
    private CircleFlowIndicator viewflowIndicator;
    private ImageAdapter tutorialAdapter;
    private Button tutorialEndBtn;

    public CTutorial(XUILayer parent) {
        super(parent);
        globalStateSource = (GlobalStateSource) DataRepo.getInstance().getSource(SourceName.GLOBAL_STATE);

        setContentView(R.layout.system_tutorial);
        tutorialEndBtn = (Button) findViewById(R.id.tutorial_end_btn);
        tutorialViewFlow = (ViewFlow) findViewById(R.id.tutorials_viewflow);
        viewflowIndicator = (CircleFlowIndicator) findViewById(R.id.tutorials_viewflow_indic);

        tutorialAdapter = new ImageAdapter();
        tutorialViewFlow.setAdapter(tutorialAdapter);
        tutorialViewFlow.setFlowIndicator(viewflowIndicator);

        tutorialViewFlow.setOnViewSwitchListener(new ViewFlow.ViewSwitchListener() {
            @Override
            public void onSwitched(View view, int position) {
                currentScreen = position;
            }
        });

        tutorialEndBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentScreen < tutorialAdapter.getCount() - 1) {
                    tutorialViewFlow.snapToScreen(currentScreen + 1);
                }else {
                    switch (tutorialType) {
                        case TUTORIAL_TYPE_LEFT_BAR:
                            globalStateSource.setTutorialLeftBar(true);
                            break;
                        case TUTORIAL_TYPE_TOP10:
                            globalStateSource.setTutorialTop10(true);
                            break;
                        case TUTORIAL_TYPE_RSS:
                            globalStateSource.setTutorialRss(true);
                            break;
                    }
                    Handler handler = getLayerHandler();
                    Message msg = handler.obtainMessage(BbsMsg.TUTORIAL_HIDE);
                    handler.sendMessage(msg);
                }
            }
        });
    }

    /**
     * 显示的教程类型
     * @param type
     */
    public void show(int type) {
        this.tutorialType = type;
        currentScreen = 0;

        switch (type) {
            case TUTORIAL_TYPE_LEFT_BAR:
                tutorialAdapter.setImgList(TUTORIAL_IMG_LEFT_BAR);
                tutorialViewFlow.setSelection(0);
                break;
            case TUTORIAL_TYPE_TOP10:
                tutorialAdapter.setImgList(TUTORIAL_IMG_TOP10);
                tutorialViewFlow.setSelection(0);
                break;
            case TUTORIAL_TYPE_RSS:
                tutorialAdapter.setImgList(TUTORIAL_IMG_RSS);
                tutorialViewFlow.setSelection(0);
                break;
        }
    }


    @Override
    public int back() {
        return XBackType.NOTHING_TO_BACK;
    }


    private class ImageAdapter extends BaseAdapter {

        private Integer[] imgList = new Integer[0];

        public void setImgList(Integer[] imgList) {
            if(imgList == null) {
                this.imgList = new Integer[0];
            }else {
                this.imgList = imgList;
            }
            notifyDataSetChanged();
        }

        public int getCount() {
            return imgList.length;
        }

        public Object getItem(int position) {
            return imgList[position];
        }

        public long getItemId(int position) {
            return position;
        }


        private class ViewHolder {
            private View img;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(convertView == null) {
                convertView = View.inflate(getContext(), R.layout.system_tutorial_img, null);
                holder = new ViewHolder();
                holder.img = convertView.findViewById(R.id.tutorial_img);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            Integer imgResource = (Integer) getItem(position);
            holder.img.setBackgroundResource(imgResource);

            return convertView;
        }

    }
}
