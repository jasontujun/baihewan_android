package com.morln.app.lbstask.ui.top10;

import android.graphics.drawable.BitmapDrawable;
import android.os.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.bbs.cache.ZoneSource;
import com.morln.app.lbstask.bbs.model.ArticleBase;
import com.morln.app.lbstask.bbs.model.Board;
import com.morln.app.lbstask.cache.DataRepo;
import com.morln.app.lbstask.cache.SourceName;
import com.morln.app.lbstask.ui.controls.XListView;
import com.morln.app.lbstask.logic.BbsArticleMgr;
import com.morln.app.lbstask.logic.BbsBoardMgr;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.ui.Linear;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseComponent;
import com.xengine.android.system.ui.XUILayer;
import com.xengine.android.utils.XLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 各区热点
 * Created by jasontujun.
 * Date: 12-2-24
 * Time: 下午5:02
 */
public class CHot extends XBaseComponent implements Linear<ArticleBase> {
    private BbsArticleMgr bbsArticleMgr;
    private BbsBoardMgr bbsBoardMgr;
    private ZoneSource zoneSource;
    private ArticleBase currentArticle;
    private String currentTitle;// 当前所处的区名
    private int currentZoneSec = 1;// 当前区的索引号

    // 界面
    private RelativeLayout topBarFrame;
    private RelativeLayout titleFrame;
    private TextView titleView;
    private XListView contentList;
    private ZoneListAdapter zoneListAdapter;
    private ZoneHotGroups zoneHotGroups;
    // popupWindow
    private PopupWindow zonePopup;

    // 异步线程
    private RefreshZoneHotTask refreshTask;

    public CHot(XUILayer parent) {
        super(parent);
        setContentView(R.layout.bbs_top10);
        bbsArticleMgr = BbsArticleMgr.getInstance();
        bbsBoardMgr = BbsBoardMgr.getInstance();
        zoneSource = (ZoneSource) DataRepo.getInstance().getSource(SourceName.BBS_ZONE);

        topBarFrame = (RelativeLayout) findViewById(R.id.top_frame);
        titleFrame = (RelativeLayout) findViewById(R.id.title_frame);
        titleView = (TextView) findViewById(R.id.title);
        contentList = (XListView) findViewById(R.id.content_list);

        // 初始化各区热点列表
        zoneHotGroups = new ZoneHotGroups();// 各区热点分组列表
        contentList.setRefreshable(true);
        contentList.setOnRefreshListener(new XListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                int sec = zoneSource.getZoneSecByName(currentTitle);
                refreshTask = new RefreshZoneHotTask(sec);
                refreshTask.execute(null);
            }
        });
        initListTop(contentList);
        initListBottom(contentList);
        zoneListAdapter = new ZoneListAdapter();
        contentList.setAdapter(zoneListAdapter);

        // 初始化顶部选择栏
        titleFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 弹出选择区块对话框
//                new DSelectZone(getParent()).show();
                // 弹出PopupWindow
                initZonePopWindow();
            }
        });
    }


    private TextView preLabel;
    private void initListTop(ListView listView) {
        View contentFrame = View.inflate(getContext(), R.layout.bbs_group_list_item_title, null);
        preLabel = (TextView) contentFrame.findViewById(R.id.pre_label);
        preLabel.setText("上一讨论区");
        preLabel.setVisibility(View.INVISIBLE);
        preLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int sec = zoneSource.getZoneSecByName(currentTitle) - 1;
                boolean result = getZoneHot(sec);
                if (!result) {
                    AnimationUtil.startShakeAnimation(preLabel, getContext());
                    Toast.makeText(getContext(), "前面木有了~", Toast.LENGTH_SHORT).show();
                }
            }
        });
        listView.addHeaderView(contentFrame);
    }

    private void initListBottom(ListView listView) {
        View contentFrame = View.inflate(getContext(), R.layout.bbs_group_list_item_bottom, null);
        final TextView nextLabel = (TextView) contentFrame.findViewById(R.id.next_label);
        nextLabel.setText("下一讨论区");
        nextLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int sec = zoneSource.getZoneSecByName(currentTitle) + 1;
                boolean result = getZoneHot(sec);
                if (!result) {
                    AnimationUtil.startShakeAnimation(nextLabel, getContext());
                    Toast.makeText(getContext(), "后面木有了~", Toast.LENGTH_SHORT).show();
                }
            }
        });
        listView.addFooterView(contentFrame);
    }



    protected boolean isEmpty() {
        return zoneHotGroups.getGroupSize() == 0;
    }

    public void refreshTitle() {
        if (1 <= currentZoneSec && currentZoneSec <= 11) {
            currentTitle = zoneSource.getZoneNameBySec(currentZoneSec);
            titleView.setText(currentTitle);
        }
    }

    /**
     * 某一个区的热点
     */
    public boolean getZoneHot(int sec) {
        if (1<=sec && sec <= 11) {
            // 终止刷新线程，防止冲突
            if (refreshTask != null) {
                refreshTask.cancel(true);
            }
            new GetZoneHotTask(sec).execute(null);
            return true;
        } else {
            return false;
        }
    }


    private void initZonePopWindow() {
        if (zonePopup == null) {
            View contentView =View.inflate(getContext(), R.layout.popup_selectionlist, null);
            zonePopup = new PopupWindow(contentView, screen().dp2px(210), screen().dp2px(220));

            // 数据
            final String KEY = "key";
            final ArrayList<Map<String, String>> items = new ArrayList<Map<String, String>>();
            for (int i = 0; i < zoneSource.size() - 1; i++) {
                Map<String, String> map = new HashMap<String, String>();
                map.put(KEY, zoneSource.get(i).getName());
                items.add(map);
            }

            // 布局
            ListView itemList = (ListView) contentView.findViewById(R.id.list);
            SimpleAdapter adapter = new SimpleAdapter(getContext(), items,
                    R.layout.popup_list_item_white, new String[] { KEY },
                    new int[] { R.id.item_name });
            itemList.setAdapter(adapter);
            itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    zonePopup.dismiss();
                    String zoneName = items.get(i).get(KEY);
                    int sec = zoneSource.getZoneSecByName(zoneName);
                    getZoneHot(sec);
                }
            });
        }

        zonePopup.setFocusable(true);
        zonePopup.setBackgroundDrawable(new BitmapDrawable());// KEY!!
        zonePopup.showAsDropDown(titleFrame);
        zonePopup.update();
    }


    @Override
    public ArticleBase getPre() {
        ArticleBase a = zoneHotGroups.getPre();
        if (a != null){
            currentArticle = a;
        }
        return a;
    }

    @Override
    public ArticleBase getNext() {
        ArticleBase a = zoneHotGroups.getNext();
        if (a != null) {
            currentArticle = a;
        }
        return a;
    }

    @Override
    public int back() {
        return XBackType.NOTHING_TO_BACK;
    }


    private Parcelable state;

    @Override
    public void onLayerUnCovered() {
        super.onLayerUnCovered();
        if(state != null) {
            contentList.onRestoreInstanceState(state);
        }
    }

    @Override
    public void onLayerCovered() {
        super.onLayerCovered();
        state = contentList.onSaveInstanceState();
    }


    /**
     * 获取更多各区热点的异步线程
     */
    private class GetZoneHotTask extends AsyncTask<Void, Void, Void> {

        private DialogUtil.WaitingDialog waitingDialog;

        private int resultCode;

        private int zoneSec;// 1~11

        public GetZoneHotTask(int zoneSec) {
            this.zoneSec = zoneSec;
        }

        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(parentLayer().getUIFrame());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }

        @Override
        protected Void doInBackground(Void... para) {
            resultCode = bbsArticleMgr.getZoneHotFromWeb(zoneSec);

            if (StatusCode.isSuccess(resultCode)) {
                // 刷新各区热点分组！
                zoneHotGroups.refresh();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (StatusCode.isSuccess(resultCode)) {
                // 刷新区选择栏
                currentZoneSec = zoneSec;
                refreshTitle();
                // 刷新列表
                int groupIndex = zoneHotGroups.getGroupIndex(currentTitle);
                XLog.d("HOT", "当前的区的groupIndex：" + groupIndex);

                // 回到顶部
                if (!contentList.isStackFromBottom()) {
                    contentList.setStackFromBottom(true);
                }
                contentList.setStackFromBottom(false);
                zoneListAdapter.setCurrentGroupIndex(groupIndex);
                contentList.onRefreshComplete();

                if (groupIndex != -1) {
                    AnimationUtil.startListAnimation(contentList);
                    Toast.makeText(getContext(), "获取更多热点成功！", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getContext(), "没有内容哦", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "获取更多热点失败...", Toast.LENGTH_SHORT).show();
            }
            waitingDialog.dismiss();
        }
        @Override
        protected void onCancelled() {
            waitingDialog.dismiss();
        }
    }


    /**
     * 刷新某个各区热点的异步线程
     */
    private class RefreshZoneHotTask extends AsyncTask<Void, Void, Void> {

        private int resultCode;

        private int zoneIndex;

        public RefreshZoneHotTask(int zoneIndex) {
            this.zoneIndex = zoneIndex;
        }

        @Override
        protected Void doInBackground(Void... para) {
            resultCode = bbsArticleMgr.getZoneHotFromWeb(zoneIndex);

            if (StatusCode.isSuccess(resultCode)) {
                // 刷新各区热点分组！
                zoneHotGroups.refresh();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            zoneListAdapter.notifyDataSetChanged();
            contentList.onRefreshComplete();
            if (StatusCode.isSuccess(resultCode)) {
                Toast.makeText(getContext(), "刷新成功！", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "刷新失败...", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * 各区热点分组列表适配器
     */
    public class ZoneListAdapter extends BaseAdapter {

        private List<ArticleBase> articleBaseList = new ArrayList<ArticleBase>();

        public void setCurrentGroupIndex(int currentGroupIndex) {
            if (currentGroupIndex != -1) {
                articleBaseList = zoneHotGroups.getGroup(currentGroupIndex);
                if (articleBaseList == null) {
                    articleBaseList = new ArrayList<ArticleBase>();
                }

                // 刷新按钮和向前按钮(只有第一个分组没有向前按钮)
                if (currentGroupIndex == 0) {
                    preLabel.setVisibility(View.INVISIBLE);
                }else {
                    preLabel.setVisibility(View.VISIBLE);
                }
            }else {
                articleBaseList = new ArrayList<ArticleBase>();
                preLabel.setVisibility(View.INVISIBLE);
            }

            zoneListAdapter.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return articleBaseList.size();
        }

        @Override
        public Object getItem(int index) {
            return articleBaseList.get(index);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }


        private class ViewHolder {
            public RelativeLayout frame;
            public ImageView decoration;
            public TextView author;
            public TextView up;
            public TextView popularity;
            public TextView title;
            public TextView time;
            public LinearLayout boardTipFrame;
            public TextView boardIdTip;
            public TextView boardChineseTip;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            Object item = getItem(i);
            if (item == null) {
                return null;
            }

            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.bbs_article_list_item, null);
                holder = new ViewHolder();
                holder.frame = (RelativeLayout) convertView.findViewById(R.id.frame);
                holder.decoration = (ImageView) convertView.findViewById(R.id.decoration);
                holder.author = (TextView) convertView.findViewById(R.id.author);
                holder.up = (TextView) convertView.findViewById(R.id.up);
                holder.popularity = (TextView) convertView.findViewById(R.id.hot);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.boardTipFrame = (LinearLayout) convertView.findViewById(R.id.board_tip_frame);
                holder.boardIdTip = (TextView) convertView.findViewById(R.id.board_id_tip);
                holder.boardChineseTip = (TextView) convertView.findViewById(R.id.board_chinese_tip);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ArticleBase article = (ArticleBase) getItem(i);
            // 背景
            holder.frame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentArticle = article;
                    Handler handler1 = getFrameHandler();
                    Message msg = handler1.obtainMessage();
                    msg.what = MainMsg.SEE_ARTICLE_DETAIL;
                    Bundle bundle = new Bundle();
                    bundle.putString("id", article.getId());
                    bundle.putString("board", article.getBoard());
                    msg.setData(bundle);
                    handler1.sendMessage(msg);
                }
            });
            // 装饰符
            long index = getItemId(i);
            holder.decoration.setImageResource(R.color.light_green);
            // 作者
            holder.author.setText(article.getAuthorName());
            // 置顶
            if (article.isUp()) {
                holder.up.setVisibility(View.VISIBLE);
            } else {
                holder.up.setVisibility(View.INVISIBLE);
            }
            // 人气
            holder.popularity.setText(""+article.getReplyCount());
            // 标题
            holder.title.setText(article.getTitle());
            // 时间
            holder.time.setText(article.getDate());
            // 所属版面(显示)
            holder.boardTipFrame.setVisibility(View.VISIBLE);
            holder.boardIdTip.setText(article.getBoard());
            Board board = bbsBoardMgr.getBoard(article.getBoard());
            if (board != null) {
                holder.boardChineseTip.setText(board.getChinesName());
            }else {
                holder.boardChineseTip.setText("");
            }

            return convertView;
        }
    }


    /**
     * 各区热点分组类
     */
    private class ZoneHotGroups implements
            Groups<ArticleBase>, Linear<ArticleBase> {
        private List<ZoneHotGroup> groupList = new ArrayList<ZoneHotGroup>();

        public ZoneHotGroups() {
            refresh();
        }

        /**
         * 根据数据源刷新分组
         */
        public synchronized void refresh() {
            // 刷新分组数据
            groupList.clear();
            List<String> zoneList = bbsArticleMgr.getZoneNameList();
            for (int i = 0; i < zoneList.size(); i++) {
                String zoneName = zoneList.get(i);
                ZoneHotGroup zgroup = new ZoneHotGroup(zoneName, bbsArticleMgr.getZoneListBaseName(zoneName));
                groupList.add(zgroup);
            }
        }

        @Override
        public int getGroupItemSize(int groupIndex) {
            return groupList.get(groupIndex).items.size();
        }

        @Override
        public List getGroup(int groupIndex) {
            if (0 <= groupIndex && groupIndex < groupList.size())
                return groupList.get(groupIndex).items;
            else
                return null;
        }

        @Override
        public String getGroupName(int groupIndex) {
            return groupList.get(groupIndex).name;
        }

        @Override
        public int getGroupIndex(String name) {
            for (int i = 0; i<groupList.size(); i++) {
                if (groupList.get(i).name.equals(name)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getGroupSize() {
            return groupList.size();
        }

        @Override
        public ArticleBase getItem(int groupIndex, int itemIndex) {
            return groupList.get(groupIndex).items.get(itemIndex);
        }

        /**
         * 添加组到对应位置。
         * @param groupName
         * @param items
         * @param gLocation     组的位置
         */
        @Override
        public void addGroup(String groupName, List<ArticleBase> items, int gLocation) {
            int groupIndex = getGroupIndex(groupName);
            if (groupIndex == -1) {// 不存在此组
                List<ArticleBase> zoneHotList = new ArrayList<ArticleBase>();
                for (int i = 0; i< items.size(); i++) {
                    zoneHotList.add((ArticleBase) items.get(i));
                }
                if (gLocation < 0) {
                    groupList.add(0, new ZoneHotGroup(groupName, zoneHotList));
                } else if (gLocation > groupList.size()) {
                    groupList.add(new ZoneHotGroup(groupName, zoneHotList));
                } else {
                    groupList.add(gLocation, new ZoneHotGroup(groupName, zoneHotList));
                }
            } else {// 存在此组
                for (int i = 0; i < items.size(); i++) {
                    ArticleBase ab = (ArticleBase) items.get(i);
                    groupList.get(groupIndex).items.add(ab);
                }
            }
        }

        /**
         * 添加条目到对应的组的末尾。如果此组不存在则新建一组,并把此组添加到index位置。
         * @param groupName
         * @param item
         * @param gLocation     新建组的位置
         */
        @Override
        public void addItem(String groupName, ArticleBase item, int gLocation) {
            int i = getGroupIndex(groupName);
            if (i != -1) {// 存在此组
                groupList.get(i).items.add(item);
            } else {// 不存在此组
                List<ArticleBase> itemList = new ArrayList<ArticleBase>();
                itemList.add(item);
                addGroup(groupName, itemList, gLocation);
            }
        }

        @Override
        public void deleteGroup(String groupName) {
            int i = getGroupIndex(groupName);
            if (i != -1) {
                groupList.remove(i);
            }
        }

        @Override
        public void deleteGroup(int groupIndex) {
            groupList.remove(groupIndex);
        }

        @Override
        public void deleteItem(ArticleBase item) {
            for (int i = 0; i < groupList.size(); i++) {
                if (groupList.get(i).items.remove(item)) {
                    return;
                }
            }
        }

        @Override
        public void deleteItem(int groupIndex, int itemIndex) {
            groupList.get(groupIndex).items.remove(itemIndex);
        }

        @Override
        public ArticleBase getPre() {
            if (currentArticle == null) {
                return null;
            }

            int groupIndex = -1;
            int articleIndex = -1;
            // 判断当前是哪一组的那篇帖子
            for (int i = 0; i < groupList.size(); i++) {
                List<ArticleBase> articleList = groupList.get(i).items;
                for (int j = 0; j < articleList.size(); j++) {
                    ArticleBase a = articleList.get(j);
                    if (currentArticle.getId().equals(a.getId()) &&
                            currentArticle.getBoard().equals(a.getBoard())) {
                        groupIndex = i;
                        articleIndex = j;
                        break;
                    }
                }
            }
            // 返回上一篇帖子
            if (groupIndex != -1 && articleIndex != -1) {
                // 帖子不是本组第一个
                if (articleIndex > 0) {
                    return groupList.get(groupIndex).items.get(articleIndex-1);
                    // 帖子是本组第一个,返回上一组最后一个
                } else {
                    if (groupIndex > 0 &&
                            groupList.get(groupIndex-1).items.size()>0) {
                        return groupList.get(groupIndex-1).items.
                                get(groupList.get(groupIndex - 1).items.size() - 1);
                    }
                }
            }
            return null;
        }

        @Override
        public ArticleBase getNext() {
            if (currentArticle == null) {
                return null;
            }

            int groupIndex = -1;
            int articleIndex = -1;
            // 判断当前是哪一组的那篇帖子
            for (int i = 0; i < groupList.size(); i++) {
                List<ArticleBase> articleList = groupList.get(i).items;
                for (int j = 0; j < articleList.size(); j++) {
                    ArticleBase a = articleList.get(j);
                    if (currentArticle.getId().equals(a.getId()) &&
                            currentArticle.getBoard().equals(a.getBoard())) {
                        groupIndex = i;
                        articleIndex = j;
                        break;
                    }
                }
            }
            // 返回下一篇帖子
            if (groupIndex != -1 && articleIndex != -1) {
                // 帖子不是本组最后一个
                if (articleIndex < groupList.get(groupIndex).items.size()-1) {
                    return groupList.get(groupIndex).items.get(articleIndex+1);
                    // 帖子是本组最后一个,返回下一组第一个
                } else {
                    if (groupIndex < groupList.size() - 1 &&
                            groupList.get(groupIndex+1).items.size() > 0) {
                        return groupList.get(groupIndex+1).items.get(0);
                    }
                }
            }
            return null;
        }

        private class ZoneHotGroup {
            public List<ArticleBase> items;
            public String name;
            public ZoneHotGroup(String name, List<ArticleBase> items) {
                this.items = items;
                this.name = name;
            }
        }
    }
}
