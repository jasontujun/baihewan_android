package com.morln.app.lbstask.ui.board;

import android.graphics.Bitmap;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.*;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.cache.TodayHotBoardSource;
import com.morln.app.lbstask.data.cache.ZoneSource;
import com.morln.app.lbstask.data.model.Board;
import com.morln.app.lbstask.data.model.Zone;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.logic.BbsBoardMgr;
import com.morln.app.lbstask.res.BbsPic;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.utils.NumberUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseComponent;
import com.xengine.android.system.ui.XUILayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jasontujun.
 * Date: 12-3-7
 * Time: 下午10:20
 */
public class CZoneBoard extends XBaseComponent {
    private BbsBoardMgr bbsBoardMgr;
    private ZoneSource zoneSource;
    private TodayHotBoardSource todayHotBoardSource;

    // 当前版面所处的ui界面
    private int currentUIIndex;
    private static final int UI_ZONE = 0;
    private static final int UI_BOARD = 1;
    private static final int UI_SEARCH = 2;

    private String currentZone;// 当前区
    private List<Board> searchResultList;// 当前版面的搜索结果列表

    // 界面
    private RelativeLayout frame;
    private RelativeLayout topBarFrame;
    private EditText searchBoardView;
    // 内容
    private ViewFlipper contentFlipper;
    // 区
    private ListView zoneList;
    private ZoneListAdapter zoneListAdapter;
    // 版面
    private ImageView zoneIconBg;
    private ImageView zoneDecoration;
    private TextView zoneName;
    private ImageView backToZoneBtn;
    private ListView boardList;
    private BoardListAdapter boardListAdapter;
    private ResultListAdapter resultListAdapter;

    public CZoneBoard(XUILayer parent) {
        super(parent);
        bbsBoardMgr = BbsBoardMgr.getInstance();
        zoneSource = (ZoneSource) DefaultDataRepo
                .getInstance().getSource(SourceName.BBS_ZONE);
        todayHotBoardSource = (TodayHotBoardSource) DefaultDataRepo
                .getInstance().getSource(SourceName.BBS_TODAY_HOT_BOARD);

        setContentView(R.layout.bbs_zone_board);
        frame = (RelativeLayout) findViewById(R.id.frame);
        topBarFrame = (RelativeLayout) findViewById(R.id.top_frame);
        searchBoardView = (EditText) findViewById(R.id.search_input);
        contentFlipper = (ViewFlipper) findViewById(R.id.content_flipper);

        initZoneList(contentFlipper);
        initZoneBoardList(contentFlipper);
        // 搜索栏设置
        searchBoardView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                String s = searchBoardView.getText().toString();
                if (!TextUtils.isEmpty(s)) {
                    searchResultList = bbsBoardMgr.getFilteredBoardList("", s);// 任何情况下都全局搜索
                    goToSearch();// 跳到搜索结果界面
                } else {
                    backFromSearch();// 离开搜索结果页面
                }
            }
        });
    }

    /**
     * 初始化区列表界面
     * @param flipper
     */
    private void initZoneList(ViewFlipper flipper) {
        View zoneListFrame = View.inflate(getContext(), R.layout.bbs_zone_list, null);
        zoneList = (ListView) zoneListFrame.findViewById(R.id.zone_list);
        flipper.addView(zoneListFrame);

        // 列表
        zoneListAdapter = new ZoneListAdapter();
        zoneList.addHeaderView(zoneListAdapter.createHeadView());
        zoneList.setAdapter(zoneListAdapter);
    }

    /**
     * 初始化版面列表界面
     * @param flipper
     */
    private void initZoneBoardList(ViewFlipper flipper) {
        View boardListFrame = View.inflate(getContext(), R.layout.bbs_zone_board_list, null);
        zoneIconBg = (ImageView) boardListFrame.findViewById(R.id.zone_icon_bg);
        zoneDecoration = (ImageView) boardListFrame.findViewById(R.id.zone_name_decoration);
        zoneName = (TextView) boardListFrame.findViewById(R.id.zone_name);
        backToZoneBtn = (ImageView) boardListFrame.findViewById(R.id.back_to_zone_btn);// 返回
        boardList = (ListView) boardListFrame.findViewById(R.id.board_list);
        flipper.addView(boardListFrame);

        setImageViewPic(backToZoneBtn, BbsPic.GO_BACK_BTN);
        boardListAdapter = new BoardListAdapter();
        boardList.setAdapter(boardListAdapter);
        resultListAdapter = new ResultListAdapter();

        backToZoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToZone();// 返回版块
            }
        });
    }


    /**
     * 跳到搜索页面。并刷新列表
     */
    protected void goToSearch() {
        // 跳转到搜索版面
        boardList.setAdapter(resultListAdapter);
        resultListAdapter.notifyDataSetChanged();
        zoneDecoration.setImageResource(R.color.light_black);
        zoneName.setTextColor(getContext().getResources().getColor(R.color.light_black));
        zoneName.setText("搜索结果:");
        setImageViewPic(backToZoneBtn, BbsPic.GO_BACK_BTN);
        backToZoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backFromSearch();
            }
        });
        // 设置区的背景图标
        Bitmap iconBg = getBitmap(BbsPic.ICON_DEFAULT);
        BitmapDrawable bd = new BitmapDrawable(iconBg);
        bd.setColorFilter(new ColorMatrixColorFilter(Zone.getGrayColorMatrix()));
        zoneIconBg.setImageDrawable(bd);
        zoneIconBg.invalidate();

        if(currentUIIndex == UI_ZONE) {
            currentUIIndex = UI_SEARCH;
            AnimationUtil.prepareFlipAnimation(contentFlipper, false);
            contentFlipper.showNext();
        }
    }


    /**
     * 跳回搜索前的页面
     * (按back键或清空搜索栏时触发)
     */
    protected void backFromSearch() {
        searchResultList = null;

        String s = searchBoardView.getText().toString();
        if(!TextUtils.isEmpty(s)) {
            searchBoardView.setText("");
            searchBoardView.requestFocus();
        }
        setImageViewPic(backToZoneBtn, BbsPic.GO_BACK_BTN);
        goToZone();
    }


    /**
     * 跳到区列表
     */
    protected void goToZone() {
        if(currentUIIndex == UI_ZONE) {
            return;
        }
        currentUIIndex = UI_ZONE;
        currentZone = null;

        AnimationUtil.prepareFlipAnimation(contentFlipper, true);
        contentFlipper.showPrevious();
    }


    /**
     * 跳到区内版面
     */
    protected void goToBoard() {
        if(currentZone == null) {
            return;
        }
        if(currentUIIndex == UI_BOARD) {
            return;
        }
        currentUIIndex = UI_BOARD;

        setImageViewPic(backToZoneBtn, BbsPic.GO_BACK_BTN);
        backToZoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToZone();// 返回版块
            }
        });
        boardList.setAdapter(boardListAdapter);// 设置适配器

        if(currentZone.equals(TodayHotBoardSource.name)) {
            // 跳到版面列表
            zoneName.setText(TodayHotBoardSource.name);
            zoneDecoration.setImageResource(TodayHotBoardSource.colorRes);
            zoneName.setTextColor(getContext().getResources().getColor(TodayHotBoardSource.colorRes));
            // 设置区的背景图标
            Bitmap iconBg = getBitmap(TodayHotBoardSource.iconFileName);
            BitmapDrawable bd = new BitmapDrawable(iconBg);
            bd.setColorFilter(new ColorMatrixColorFilter(Zone.getGrayColorMatrix()));
            zoneIconBg.setImageDrawable(bd);
            zoneIconBg.invalidate();
            // 设置区并刷新某个区的版面
            new GetHotBoardTask().execute(null);// 抓取热门版面并刷新
        }else {
            Zone zone = zoneSource.get(currentZone);

            // 跳到版面列表
            zoneName.setText(zone.getName());
            zoneDecoration.setImageResource(zone.getColorRes());
            zoneName.setTextColor(getContext().getResources().getColor(zone.getColorRes()));
            // 设置区的背景图标
            Bitmap iconBg = getBitmap(zone.getIconFileName());
            BitmapDrawable bd = new BitmapDrawable(iconBg);
            bd.setColorFilter(new ColorMatrixColorFilter(Zone.getGrayColorMatrix()));
            zoneIconBg.setImageDrawable(bd);
            zoneIconBg.invalidate();
            // 设置区并刷新某个区的版面
            boardListAdapter.setBoardList(bbsBoardMgr.getBoardOfZone(currentZone));
        }

        AnimationUtil.prepareFlipAnimation(contentFlipper, false);
        contentFlipper.showNext();
    }


    @Override
    public int back() {
        // 先判断是否在搜索界面
        if(searchResultList != null){
            backFromSearch();
            return XBackType.CHILD_BACK;
        }

        // 然后根据的界面返回
        if(currentUIIndex == UI_ZONE) {
            return XBackType.NOTHING_TO_BACK;
        }else {
            goToZone();// 直接跳回版面界面
            return XBackType.CHILD_BACK;
        }
    }


    private Parcelable state;

    @Override
    public void onLayerUnCovered() {
        super.onLayerUnCovered();
        if(state != null) {
            boardList.onRestoreInstanceState(state);
            searchBoardView.requestFocus();
            searchBoardView.requestFocusFromTouch();
        }
    }

    @Override
    public void onLayerCovered() {
        super.onLayerCovered();
        state = boardList.onSaveInstanceState();
    }


    /**
     * 区列表
     */
    private class ZoneListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return zoneSource.size();
        }

        @Override
        public Object getItem(int i) {
            return zoneSource.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        /**
         * 随机得到一些版面名字
         * @param zoneName
         * @return
         */
        private String[] getSomeBoardFromZone(String zoneName) {
            List<Board> mBoardList = bbsBoardMgr.getBoardOfZone(zoneName);
            if(mBoardList.size() == 0) {
                return null;
            }else if(mBoardList.size() < 3) {
                String[] result = new String[mBoardList.size()];
                for(int i =0; i<mBoardList.size(); i++){
                    result[i] = mBoardList.get(i).getChinesName();
                }
                return result;
            }else {
                int[] nums = NumberUtil.generateSomeNumber(3, mBoardList.size());
                return new String[] {mBoardList.get(nums[0]).getChinesName(),
                        mBoardList.get(nums[1]).getChinesName(),
                        mBoardList.get(nums[2]).getChinesName()};
            }
        }

        public View createHeadView() {
            View frame = View.inflate(getContext(), R.layout.bbs_zone_list_item, null);
            ImageView decorator = (ImageView) frame.findViewById(R.id.decoration);
            TextView titleView = (TextView) frame.findViewById(R.id.title);
            TextView content = (TextView) frame.findViewById(R.id.content);
            TextView btnWord = (TextView) frame.findViewById(R.id.btn_word);
            ImageView btnImg = (ImageView) frame.findViewById(R.id.btn_img);

            // 设置版块名
            titleView.setText(TodayHotBoardSource.name);
            // 设置按钮
            setImageViewPic(btnImg, TodayHotBoardSource.btnImgFileName);
            // 设置颜色
            decorator.setImageResource(TodayHotBoardSource.colorRes);
            titleView.setTextColor(getContext().getResources().getColor(TodayHotBoardSource.colorRes));
            btnWord.setTextColor(getContext().getResources().getColor(TodayHotBoardSource.colorRes));
            // 设置内容
            String[] someBoardName = new String[] { "贴图版", "世界足球", "女生天地"};
            content.setTextColor(getContext().getResources().getColor(R.color.light_light_red));
            content.setText("包含: "+ someBoardName[0] +
                    "、" + someBoardName[1] + "、" + someBoardName[2] + "等");

            // 设置监听
            frame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentZone = TodayHotBoardSource.name;
                    goToBoard();
                }
            });
            return frame;
        }

        private class ViewHolder {
            public RelativeLayout frame;
            public ImageView decorator;
            public TextView title;
            public TextView content;
            public TextView btnWord;
            public ImageView btnImg;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if(convertView == null) {
                convertView = View.inflate(getContext(), R.layout.bbs_zone_list_item, null);
                holder = new ViewHolder();
                holder.frame = (RelativeLayout) convertView.findViewById(R.id.frame);
                holder.decorator = (ImageView) convertView.findViewById(R.id.decoration);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.content = (TextView) convertView.findViewById(R.id.content);
                holder.btnWord = (TextView) convertView.findViewById(R.id.btn_word);
                holder.btnImg = (ImageView) convertView.findViewById(R.id.btn_img);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Zone zone = (Zone) getItem(i);
            // 设置版块名
            holder.title.setText(zone.getName());
            // 设置按钮
            setImageViewPic(holder.btnImg, zone.getBtnImgFileName());
            // 设置颜色
            holder.decorator.setImageResource(zone.getColorRes());
            holder.title.setTextColor(getContext().getResources().getColor(zone.getColorRes()));
            holder.btnWord.setTextColor(getContext().getResources().getColor(zone.getColorRes()));
            // 设置内容
            String[] someBoardName = getSomeBoardFromZone(zone.getName());
            if(someBoardName != null && someBoardName.length != 0) {
                if(someBoardName.length < 3){
                    String str = "包含: "+someBoardName[0];
                    for(int j = 1; j < someBoardName.length; j++) {
                        str = str + "、" + someBoardName[j];
                    }
                }else {
                    holder.content.setText("包含: "+ someBoardName[0] +
                            "、" + someBoardName[1] + "、" + someBoardName[2] + "等");
                }
            }

            // 设置监听
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentZone = zone.getName();
                    goToBoard();
                }
            });
            return convertView;
        }
    }


    /**
     * 版块搜索结果列表
     */
    private class ResultListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if(searchResultList != null){
                return searchResultList.size();
            }else {
                return 0;
            }
        }

        @Override
        public Object getItem(int i) {
            if(searchResultList != null) {
                return searchResultList.get(i);
            }else {
                return null;
            }
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        private class ViewHolder {
            public RelativeLayout frame;
            public TextView boardEnglishName;
            public TextView boardChineseName;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if(convertView == null) {
                convertView = View.inflate(getContext(), R.layout.bbs_board_list_item, null);
                holder = new ViewHolder();
                holder.frame = (RelativeLayout) convertView.findViewById(R.id.frame);
                holder.boardEnglishName = (TextView) convertView.findViewById(R.id.board_english_name);
                holder.boardChineseName = (TextView) convertView.findViewById(R.id.board_chinese_name);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Board board = (Board) getItem(i);
            // 设置背景
//            setScalableBg(holder.frame, PackageName.SYSTEM, SystemPic.SELECT_FIELD);
            // 设置版块英文名
            holder.boardEnglishName.setText(board.getBoardId());
            // 设置版块中文名
            holder.boardChineseName.setText(board.getChinesName());

            // 设置监听
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 跳转到版面
                    Handler handler = getFrameHandler();
                    Message msg = handler.obtainMessage(MainMsg.SEE_BOARD);
                    Bundle bundle = new Bundle();
                    bundle.putString("board", board.getBoardId());
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            });

            convertView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return false;
                }
            });
            return convertView;
        }
    }


    /**
     * 版块列表
     */
    private class BoardListAdapter extends BaseAdapter{

        private List<Board> zoneBoardList = new ArrayList<Board>();

        public void setBoardList(List<Board> boardList1){
            zoneBoardList = boardList1;
            if(zoneBoardList == null){
                zoneBoardList = new ArrayList<Board>();
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return zoneBoardList.size();
        }

        @Override
        public Object getItem(int i) {
            return zoneBoardList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        private class ViewHolder {
            public RelativeLayout frame;
            public TextView boardEnglishName;
            public TextView boardChineseName;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if(convertView == null) {
                convertView = View.inflate(getContext(), R.layout.bbs_board_list_item, null);
                holder = new ViewHolder();
                holder.frame = (RelativeLayout) convertView.findViewById(R.id.frame);
                holder.boardEnglishName = (TextView) convertView.findViewById(R.id.board_english_name);
                holder.boardChineseName = (TextView) convertView.findViewById(R.id.board_chinese_name);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Board board = (Board) getItem(i);
            // 设置背景
//            setScalableBg(holder.frame, PackageName.SYSTEM, SystemPic.SELECT_FIELD);
            // 设置版块英文名
            holder.boardEnglishName.setText(board.getBoardId());
            // 设置版块中文名
            holder.boardChineseName.setText(board.getChinesName());

            // 设置监听
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TIP 跳转到版面
                    Handler handler = getFrameHandler();
                    Message msg = handler.obtainMessage(MainMsg.SEE_BOARD);
                    Bundle bundle = new Bundle();
                    bundle.putString("board", board.getBoardId());
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            });

            convertView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return false;
                }
            });
            return convertView;
        }
    }


    /**
     * 抓取热门版面
     */
    private class GetHotBoardTask extends AsyncTask<Void, Void, Integer> {

        private DialogUtil.WaitingDialog waitingDialog;

        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(parentLayer().getUIFrame().getContext());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }
        @Override
        protected Integer doInBackground(Void... para) {
            int resultCode = bbsBoardMgr.getHotBoard();
            return resultCode;
        }
        @Override
        protected void onPostExecute(Integer resultCode) {
            if (StatusCode.isSuccess(resultCode)) {
                List<Board> hotBoardList = new ArrayList<Board>();
                for(int i = 0; i<todayHotBoardSource.size(); i++) {
                    hotBoardList.add(todayHotBoardSource.get(i));
                }
                boardListAdapter.setBoardList(hotBoardList);
            } else {
                Toast.makeText(getContext(), "刷新热门版面失败...", Toast.LENGTH_SHORT).show();
            }
            waitingDialog.dismiss();
        }
        @Override
        protected void onCancelled() {
            waitingDialog.dismiss();
        }
    }
}
