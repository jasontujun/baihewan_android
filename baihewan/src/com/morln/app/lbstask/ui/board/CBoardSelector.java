package com.morln.app.lbstask.ui.board;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.bbs.cache.ZoneSource;
import com.morln.app.lbstask.bbs.model.Board;
import com.morln.app.lbstask.bbs.model.Zone;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.system.ui.XBackType;
import com.morln.app.system.ui.XBaseComponent;
import com.morln.app.system.ui.XUILayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jasontujun.
 * Date: 12-9-10
 * Time: 下午9:47
 */
public class CBoardSelector extends XBaseComponent {

    private ZoneSource zoneSource;

    private int currentZoneIndex;// 当前选择的区
    private List<String> selectedBoardList;// 已经选择版面

    private ListView zoneList;
    private ZoneListAdapter zoneListAdapter;
    private ListView boardList;
    private BoardListAdapter boardListAdapter;

    public CBoardSelector(XUILayer parent) {
        super(parent);
        zoneSource = (ZoneSource) DataRepo.getInstance().getSource(SourceName.BBS_ZONE);

        setContentView(R.layout.bbs_board_selector);
        zoneList = (ListView) findViewById(R.id.zone_list);
        boardList = (ListView) findViewById(R.id.board_list);

        zoneListAdapter = new ZoneListAdapter();
        zoneList.setAdapter(zoneListAdapter);
        boardListAdapter = new BoardListAdapter();
        boardList.setAdapter(boardListAdapter);
    }

    @Override
    public int back() {
        return XBackType.NOTHING_TO_BACK;
    }

    /**
     * 刷新右边版面列表（回到顶部）
     */
    private void refreshBoard() {
        // 刷新区列表
        zoneListAdapter.notifyDataSetChanged();

        // 回到顶部
        if (!boardList.isStackFromBottom()) {
            boardList.setStackFromBottom(true);
        }
        boardList.setStackFromBottom(false);
        boardListAdapter.setBoardList(zoneSource.get(currentZoneIndex).getBoardList());
        boardListAdapter.notifyDataSetChanged();
        AnimationUtil.startVerticalSlideAnimation(getContext(), boardList);
    }

    /**
     * 重置选择器为初始状态（选择开始前）
     * @param boardList 已经选择的版面
     */
    public void reset(List<String> boardList) {
        currentZoneIndex = 0;
        if(boardList == null) {
            selectedBoardList = new ArrayList<String>();
        }else {
            selectedBoardList = new ArrayList<String>(boardList);
        }
        refreshBoard();
    }

    /**
     * 外部获取选择器选择结果（选择完成后）
     * @return
     */
    public List<String> getResult() {
        return selectedBoardList;
    }


    private boolean contains(String boardId) {
        for (int i = 0; i<selectedBoardList.size(); i++) {
            if(selectedBoardList.get(i).equals(boardId)) {
                return true;
            }
        }
        return false;
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


        private class ViewHolder {
            public TextView zoneName;
        }

        @Override
        public View getView(final int i, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if(convertView == null) {
                convertView = View.inflate(getContext(), R.layout.bbs_board_selector_zone_item, null);
                holder = new ViewHolder();
                holder.zoneName = (TextView) convertView.findViewById(R.id.zone_name);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Zone zone = (Zone) getItem(i);
            // 区名
            holder.zoneName.setText(zone.getName());
            if(currentZoneIndex == i) {
                convertView.setBackgroundResource(R.color.light_green);
            } else {
                convertView.setBackgroundResource(R.drawable.list_item_bg_gray);
            }
            // 按下监听
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(currentZoneIndex != i) {
                        currentZoneIndex = i;
                        refreshBoard();
                    }
                }
            });

            return convertView;
        }
    }


    /**
     * 版块列表
     */
    private class BoardListAdapter extends BaseAdapter {

        private List<Board> boardList = new ArrayList<Board>();

        public void setBoardList(List<Board> boardList1){
            boardList = boardList1;
            if(boardList == null){
                boardList = new ArrayList<Board>();
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return boardList.size();
        }

        @Override
        public Object getItem(int i) {
            return boardList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        private class ViewHolder {
            public TextView boardName;
            public LinearLayout addFrame;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if(convertView == null) {
                convertView = View.inflate(getContext(), R.layout.bbs_board_selector_board_item, null);
                holder = new ViewHolder();
                holder.boardName = (TextView) convertView.findViewById(R.id.board_name);
                holder.addFrame = (LinearLayout) convertView.findViewById(R.id.add_btn);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Board board = (Board) getItem(i);
            // 版块中文名
            holder.boardName.setText(board.getChinesName());
            if(contains(board.getBoardId())) {
                holder.boardName.setTextColor(getContext().getResources().getColor(R.color.gray));
                holder.addFrame.setVisibility(View.GONE);
                // 添加按钮
                convertView.setOnClickListener(null);
            } else {
                holder.boardName.setTextColor(getContext().getResources().getColor(R.color.light_green));
                holder.addFrame.setVisibility(View.VISIBLE);
                // 添加按钮
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedBoardList.add(board.getBoardId());
                        notifyDataSetChanged();
                    }
                });
            }

            return convertView;
        }
    }
}
