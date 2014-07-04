package com.morln.app.lbstask.ui.top10;

import android.os.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.model.ArticleBase;
import com.morln.app.lbstask.data.model.Board;
import com.morln.app.lbstask.data.model.Top10ArticleBase;
import com.morln.app.lbstask.data.cache.GlobalStateSource;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.ui.controls.XListView;
import com.morln.app.lbstask.logic.BbsArticleMgr;
import com.morln.app.lbstask.logic.BbsBoardMgr;
import com.morln.app.lbstask.res.BbsMsg;
import com.morln.app.lbstask.res.BbsPic;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.ui.Linear;
import com.morln.app.lbstask.ui.main.CTutorial;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseComponent;
import com.xengine.android.system.ui.XUILayer;
import com.xengine.android.utils.XLog;
import com.xengine.android.utils.XStringUtil;

import java.util.*;

/**
 * Created by jasontujun.
 * Date: 12-2-24
 * Time: 下午5:02
 */
public class CTop10 extends XBaseComponent implements Linear<ArticleBase> {
    private GlobalStateSource globalStateSource;
    private BbsArticleMgr bbsArticleMgr;
    private BbsBoardMgr bbsBoardMgr;
    private ArticleBase currentArticle;
    private String currentTitle;// 当前所处的十大日期

    // 界面
    private RelativeLayout topBarFrame;
    private RelativeLayout titleFrame;
    private TextView title;
    private XListView contentList;
    private TopListAdapter topListAdapter;
    private Top10Groups top10Groups;

    // 异步线程
    private GetHistoryTop10Task top10Task;

    public CTop10(XUILayer parent) {
        super(parent);
        globalStateSource = (GlobalStateSource) DefaultDataRepo
                .getInstance().getSource(SourceName.GLOBAL_STATE);
        bbsArticleMgr = BbsArticleMgr.getInstance();
        bbsBoardMgr = BbsBoardMgr.getInstance();

        setContentView(R.layout.bbs_top10);
        topBarFrame = (RelativeLayout) findViewById(R.id.top_frame);
        titleFrame = (RelativeLayout) findViewById(R.id.title_frame);
        title = (TextView) findViewById(R.id.title);
        contentList = (XListView) findViewById(R.id.content_list);

        // 初始化十大列表
        top10Groups = new Top10Groups();// 历史十大分组列表
        contentList.setRefreshable(true);
        contentList.setOnRefreshListener(new XListView.OnRefreshListener() { // 下拉刷新
            @Override
            public void onRefresh() {
                refreshCurrentTop10();
            }
        });
        initListTop(contentList);
        initListBottom(contentList);
        topListAdapter = new TopListAdapter();
        contentList.setAdapter(topListAdapter);

        // 设置当前日期
        currentTitle = XStringUtil.date2calendarStr(new Date(System.currentTimeMillis()));
        // 初始化顶部选择栏
        titleFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 弹出选择时间对话框
                Calendar c = XStringUtil.str2calendar(currentTitle);
                if(c == null){
                    c =Calendar.getInstance();
                }
                int year =c.get(Calendar.YEAR);
                int month=c.get(Calendar.MONTH);
                int day=c.get(Calendar.DAY_OF_MONTH);
                new DSelectDate(parentLayer(), year, month, day,
                        new DSelectDate.SelectDateListener() {
                            @Override
                            public void onSelect(int year, int month, int day) {
                                refreshHistoryTop10(year, month, day, true);
                            }
                        }).show();
            }
        });
    }

    private TextView preLabel;
    private void initListTop(ListView listView) {
        View contentFrame = View.inflate(getContext(), R.layout.bbs_group_list_item_title, null);
        preLabel = (TextView) contentFrame.findViewById(R.id.pre_label);
        preLabel.setText("后一日");
        preLabel.setVisibility(View.INVISIBLE);
        preLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar groupDate = XStringUtil.str2calendar(currentTitle);
                Calendar today = Calendar.getInstance();
                if(groupDate != null) {
                    groupDate.add(Calendar.DAY_OF_MONTH, 1);
                    if(groupDate.after(today)) {
                        Toast.makeText(getContext(), "不能查看未来的十大哦~", Toast.LENGTH_SHORT).show();
                        AnimationUtil.startShakeAnimation(preLabel, getContext());
                    }else {
                        refreshHistoryTop10(groupDate, true);
                    }
                }
            }
        });
        listView.addHeaderView(contentFrame);
    }

    private void initListBottom(ListView listView) {
        View contentFrame = View.inflate(getContext(), R.layout.bbs_group_list_item_bottom, null);
        TextView nextLabel = (TextView) contentFrame.findViewById(R.id.next_label);
        nextLabel.setText("前一日");
        nextLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar groupDate = XStringUtil.str2calendar(currentTitle);
                if(groupDate != null) {
                    groupDate.add(Calendar.DAY_OF_MONTH, -1);
                    refreshHistoryTop10(groupDate, true);
                }
            }
        });
        listView.addFooterView(contentFrame);
    }


    /**
     * 刷新当前选择日期下的十大
     */
    private void refreshCurrentTop10() {
        String todayStr = XStringUtil.date2calendarStr(new Date(System.currentTimeMillis()));
        if(currentTitle.equals(todayStr)) {
            refreshTodayTop10(false);
        }else {
            Calendar groupDate = XStringUtil.str2calendar(currentTitle);

            int year = groupDate.get(Calendar.YEAR);
            int month = groupDate.get(Calendar.MONTH) + 1;// TIP 不+1为0~11
            int day = groupDate.get(Calendar.DAY_OF_MONTH);
            refreshHistoryTop10(year, month, day, false);
        }
    }

    /**
     * 获取今天的十大（用于登陆后第一次获取）
     * TIP （判断当前登陆状态，用不同方式获取十大）
     */
    public void refreshTodayTop10(boolean hasDialog) {
        int status = globalStateSource.getLoginStatus();// 登陆状态
        if(status == GlobalStateSource.LOGIN_STATUS_ALL_LOGIN) {
            Calendar c = Calendar.getInstance();
            refreshHistoryTop10(c, hasDialog);
        }else {
            new GetBbsTop10Task(hasDialog).execute(null);
        }
    }

    /**
     * 跳转到某一天的历史十大
     * @param calendar
     */
    private void refreshHistoryTop10(Calendar calendar, boolean hasDialog) {
        if(calendar == null)
            return;

        int year  = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;// TIP 不+1为0~11
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        refreshHistoryTop10(year, month, day, hasDialog);
    }

    /**
     * 跳转到某一天的历史十大
     * @param year
     * @param month
     * @param day
     */
    private void refreshHistoryTop10(int year, int month, int day, boolean hasDialog) {
        // 终止之前的asyncTask
        if(top10Task != null) {
            top10Task.cancel(true);
        }
        // 执行新的asyncTask，防止多个异步线程导致的刷新冲突
        top10Task = new GetHistoryTop10Task(year, month, day, hasDialog);
        top10Task.execute(null);
    }


    /**
     * 显示教程
     */
    private void showTutorial() {
        Handler handler = getLayerHandler();
        Message msg = handler.obtainMessage(BbsMsg.TUTORIAL_SHOW);
        msg.arg1 = CTutorial.TUTORIAL_TYPE_TOP10;
        handler.sendMessage(msg);
    }


    @Override
    public ArticleBase getPre() {
        ArticleBase a = top10Groups.getPre();
        if(a != null){
            currentArticle = a;
        }
        return a;
    }

    @Override
    public ArticleBase getNext() {
        ArticleBase a = top10Groups.getNext();
        if(a != null){
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
     * 从网页抓取十大的异步线程
     */
    private class GetBbsTop10Task extends AsyncTask<Void, Void, Void> {

        private boolean hasDialog;

        private DialogUtil.WaitingDialog waitingDialog;

        private int resultCode;

        private GetBbsTop10Task(boolean hasDialog) {
            this.hasDialog = hasDialog;
        }

        @Override
        protected void onPreExecute() {
            if(hasDialog) {
                waitingDialog = DialogUtil.createWaitingDialog(parentLayer().getUIFrame().getContext());
                waitingDialog.setAsyncTask(this);
                waitingDialog.show();
            }
        }
        @Override
        protected Void doInBackground(Void... para) {
            resultCode = bbsArticleMgr.getTop10FromWeb();

            if(StatusCode.isSuccess(resultCode) && resultCode != StatusCode.NO_MORE_SUCCESS){
                // 刷新十大分组！
                top10Groups.refresh();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if(hasDialog) {
                waitingDialog.dismiss();
            }

            showTutorial();// 显示教程

            if (StatusCode.isSuccess(resultCode)) {
                if(resultCode == StatusCode.NO_MORE_SUCCESS) {
                    topListAdapter.notifyDataSetChanged();
                    contentList.onRefreshComplete();
                    Toast.makeText(getContext(), "没有更多了……", Toast.LENGTH_SHORT).show();
                }else {
                    currentTitle = XStringUtil.date2calendarStr(new Date(System.currentTimeMillis()));
                    title.setText("今日十大");
                    topListAdapter.setCurrentGroupIndex(0);
                    contentList.onRefreshComplete();

                    // 回到顶部
                    if(hasDialog) {
                        if (!contentList.isStackFromBottom()) {
                            contentList.setStackFromBottom(true);
                        }
                        contentList.setStackFromBottom(false);
                    }
                    if(!hasDialog) {
                        Toast.makeText(getContext(), "十大刷新成功！", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                topListAdapter.notifyDataSetChanged();
                contentList.onRefreshComplete();
                Toast.makeText(getContext(), "十大获取失败...", Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        protected void onCancelled() {
            if(hasDialog) {
                waitingDialog.dismiss();
            }
            topListAdapter.notifyDataSetChanged();
            contentList.onRefreshComplete();
        }
    }


    /**
     * 向bbs的服务器获取某一天的历史十大的异步线程
     */
    private class GetHistoryTop10Task extends AsyncTask<Void, Void, Void> {

        private boolean hasDialog;
        private DialogUtil.WaitingDialog waitingDialog;
        private int resultCode;

        private int year, month, day;

        public GetHistoryTop10Task(int year, int month, int day, boolean hasDialog){
            this.year = year;
            this.month = month;
            this.day = day;
            this.hasDialog = hasDialog;
        }

        @Override
        protected void onPreExecute() {
            if(hasDialog) {
                waitingDialog = DialogUtil.createWaitingDialog(parentLayer().getUIFrame().getContext());
                waitingDialog.setAsyncTask(this);
                waitingDialog.show();
            }
        }

        @Override
        protected Void doInBackground(Void... para) {
            // 从服务器抓
            XLog.d("TOP10", "正在获取十大，year:" + year + ",month:" + month + ",day:" + day);
            resultCode = bbsArticleMgr.getTop10FromServerByDate(getContext(), year, month, day);
            if(StatusCode.isSuccess(resultCode) && resultCode != StatusCode.NO_MORE_SUCCESS) {
                top10Groups.refresh();
            }else {
                // 如果是当天的十大，且从服务器抓取失败，则从网页抓
                String todayStr = XStringUtil.calendar2str(Calendar.getInstance());
                String historyStr = XStringUtil.date2calendarStr(year, month, day);
                if(historyStr.equals(todayStr)) {
                    resultCode = bbsArticleMgr.getTop10FromWeb();
                    if(StatusCode.isSuccess(resultCode) && resultCode != StatusCode.NO_MORE_SUCCESS) {
                        top10Groups.refresh();
                    }
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if(hasDialog) {
                waitingDialog.dismiss();
            }
            contentList.onRefreshComplete();

            showTutorial();// 显示教程

            if (StatusCode.isSuccess(resultCode)) {
                if(resultCode == StatusCode.NO_MORE_SUCCESS) {
                    topListAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "没有更多历史十大了……", Toast.LENGTH_SHORT).show();
                }else {
                    // 设置题头
                    String todayStr = XStringUtil.calendar2str(Calendar.getInstance());
                    currentTitle = XStringUtil.date2calendarStr(year, month, day);
                    if(currentTitle.equals(todayStr)) {
                        title.setText("今日十大");
                    }else {
                        title.setText(currentTitle);
                    }

                    // 刷新分组
                    int groupIndex = top10Groups.getGroupIndex(currentTitle);
                    if(groupIndex != -1) {
                        topListAdapter.setCurrentGroupIndex(groupIndex);

                        // 回到顶部
                        if(hasDialog) {
                            if (!contentList.isStackFromBottom()) {
                                contentList.setStackFromBottom(true);
                            }
                            contentList.setStackFromBottom(false);
                        }

                        if(!hasDialog) {
                            Toast.makeText(getContext(), "历史十大刷新成功！", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(getContext(), "历史十大刷新异常！！", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                switch (resultCode) {
                    case StatusCode.HTTP_EXCEPTION:
                        Toast.makeText(getContext(), "刷新失败，请确定是否连接了公网", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getContext(), "历史十大刷新失败...", Toast.LENGTH_SHORT).show();
                }
            }
        }
        @Override
        protected void onCancelled() {
            if(hasDialog) {
                waitingDialog.dismiss();
            }
            topListAdapter.notifyDataSetChanged();
            contentList.onRefreshComplete();
        }
    }


    /**
     * 十大分组列表适配器
     */
    public class TopListAdapter extends BaseAdapter {

        private List<Top10ArticleBase> top10List = new ArrayList<Top10ArticleBase>();

        /**
         * 设置组别，刷新数据源
         * @param currentGroupIndex
         */
        public void setCurrentGroupIndex(int currentGroupIndex) {
            if(currentGroupIndex != -1) {
                top10List = top10Groups.getGroup(currentGroupIndex);
                if(top10List == null) {
                    top10List = new ArrayList<Top10ArticleBase>();
                }

                // 刷新按钮和向前按钮(只有第一个分组“今日十大”会有刷新按钮，没有向前按钮)
                String todayStr = XStringUtil.date2calendarStr(new Date(System.currentTimeMillis()));
                if(todayStr.equals(currentTitle)) {
                    preLabel.setVisibility(View.INVISIBLE);
                }else {
                    preLabel.setVisibility(View.VISIBLE);
                }
            }else {
                top10List = new ArrayList<Top10ArticleBase>();
                preLabel.setVisibility(View.INVISIBLE);
            }
            notifyDataSetChanged();
        }


        @Override
        public int getCount() {
            return top10List.size() ;
        }

        @Override
        public Object getItem(int index) {
            return top10List.get(index);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        private class ViewHolder {
            public RelativeLayout frame;
            public ImageView decoration;
            public ImageView label;
            public TextView author;
            public TextView up;
            public TextView popularity;
            public TextView title;
            public TextView time;
            public LinearLayout boardTipFrame;
            public TextView boardIdTip;
            public TextView boardChineseTip;
        }

        /**
         * 会根据ViewType提供不同convertView
         * @param i
         * @param convertView
         * @param viewGroup
         * @return
         */
        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if(convertView == null) {
                convertView = View.inflate(getContext(), R.layout.bbs_article_list_item, null);
                holder = new ViewHolder();
                holder.frame = (RelativeLayout) convertView.findViewById(R.id.frame);
                holder.decoration = (ImageView) convertView.findViewById(R.id.decoration);
                holder.label = (ImageView) convertView.findViewById(R.id.label);
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

            Object item = getItem(i);
            if(item == null) {
                return null;
            }
            final Top10ArticleBase article = (Top10ArticleBase) getItem(i);
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
            // 十大装饰符
            int rank = article.getCurrentRank();
            if(rank == -1) {
                holder.decoration.setImageResource(R.color.dark_gray);
                holder.label.setVisibility(View.GONE);
            }else if(rank == 1) {
                holder.decoration.setImageResource(R.color.light_red);
                holder.label.setVisibility(View.VISIBLE);
                setViewBackground(holder.label, BbsPic.LABEL_1);
            }else if(rank == 2) {
                holder.decoration.setImageResource(R.color.light_purple);
                holder.label.setVisibility(View.VISIBLE);
                setViewBackground(holder.label, BbsPic.LABEL_2);
            }else if(rank == 3) {
                holder.decoration.setImageResource(R.color.light_blue);
                holder.label.setVisibility(View.VISIBLE);
                setViewBackground(holder.label, BbsPic.LABEL_3);
            }else {
                holder.decoration.setImageResource(R.color.light_green);
                holder.label.setVisibility(View.GONE);
            }
            // 作者
            holder.author.setText(article.getAuthorName());
            // 置顶
            if(article.isUp()) {
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
            if(board != null) {
                holder.boardChineseTip.setText(board.getChinesName());
            }else {
                holder.boardChineseTip.setText("");
            }

            return convertView;
        }
    }


    /**
     * 十大分组类
     */
    private class Top10Groups implements
            Groups<Top10ArticleBase>, Linear<ArticleBase> {
        private List<Top10Group> groupList = new ArrayList<Top10Group>();

        public Top10Groups() {
            refresh();
        }

        /**
         * 根据数据源刷新分组
         */
        public synchronized void refresh() {
            // 刷新分组数据
            groupList.clear();
            List<String> dateList = bbsArticleMgr.getTop10DateList();
            XLog.d("TOP10", "dateList size:"+dateList.size());
            for (int i = 0; i<dateList.size(); i++)  {
                // TODO 隐藏bug！！如果所有十大都是昨天的，修改一下前十的日期
                String date = dateList.get(i);
                Top10Group tgroup = new Top10Group(date, bbsArticleMgr.getTop10ListBaseDate(date));
                groupList.add(tgroup);
                // TIP 排序
                Collections.sort(tgroup.items, Top10ArticleBase.getTopComparator());
            }
        }

        /**
         * 获取所有按日期分组中的最早日期
         */
        public Date getEarliestDate() {
            if(groupList.size() == 0) {
                return null;
            }

            Date result = groupList.get(0).items.get(0).getLastTime();
            if(result == null) {
                return result;
            }

            for(int i = 1; i<groupList.size(); i++) {
                Date date = groupList.get(i).items.get(0).getLastTime();
                if(date == null){
                    return null;
                }
                if(date.before(result)){
                    result = date;
                }
            }
            return result;
        }

        @Override
        public int getGroupItemSize(int groupIndex) {
            return groupList.get(groupIndex).items.size();
        }

        @Override
        public List getGroup(int groupIndex) {
            if(0 <= groupIndex && groupIndex < groupList.size())
                return groupList.get(groupIndex).items;
            else
                return null;
        }

        @Override
        public String getGroupName(int groupIndex){
            return groupList.get(groupIndex).name;
        }

        @Override
        public int getGroupIndex(String name) {
            for(int i = 0; i<groupList.size(); i++) {
                if(groupList.get(i).name.equals(name)) {
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
        public Top10ArticleBase getItem(int groupIndex, int itemIndex) {
            return groupList.get(groupIndex).items.get(itemIndex);
        }

        /**
         * 添加组到对应位置。
         * @param groupName
         * @param items
         * @param gLocation     组的位置
         */
        @Override
        public void addGroup(String groupName, List<Top10ArticleBase> items, int gLocation) {
            int groupIndex = getGroupIndex(groupName);
            if(groupIndex == -1) {// 不存在此组
                List<Top10ArticleBase> top10List = new ArrayList<Top10ArticleBase>();
                for(int i = 0; i< items.size(); i++) {
                    top10List.add((Top10ArticleBase) items.get(i));
                }
                if(gLocation < 0) {
                    groupList.add(0, new Top10Group(groupName, top10List));
                }else if(gLocation > groupList.size()) {
                    groupList.add(new Top10Group(groupName, top10List));
                }else {
                    groupList.add(gLocation, new Top10Group(groupName, top10List));
                }
            }else {// 存在此组
                for(int i = 0; i<items.size(); i++) {
                    Top10ArticleBase ab = (Top10ArticleBase) items.get(i);
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
        public void addItem(String groupName, Top10ArticleBase item, int gLocation) {
            int i = getGroupIndex(groupName);
            if(i != -1) {// 存在此组
                groupList.get(i).items.add(item);
            }else {// 不存在此组
                List<Top10ArticleBase> itemList = new ArrayList<Top10ArticleBase>();
                itemList.add(item);
                addGroup(groupName, itemList, gLocation);
            }
        }

        @Override
        public void deleteGroup(String groupName) {
            int i = getGroupIndex(groupName);
            if(i != -1) {
                groupList.remove(i);
            }
        }

        @Override
        public void deleteGroup(int groupIndex) {
            groupList.remove(groupIndex);
        }

        @Override
        public void deleteItem(Top10ArticleBase item) {
            for(int i = 0; i<groupList.size(); i++) {
                if(groupList.get(i).items.remove(item)) {
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
            if(currentArticle == null) {
                return null;
            }

            XLog.d("FK","Top10Group 开始获取十大的上一篇！！！！");
            int groupIndex = -1;
            int articleIndex = -1;
            // 判断当前是哪一组的那篇帖子
            for(int i = 0; i<groupList.size(); i++) {
                List<Top10ArticleBase> articleList = groupList.get(i).items;
                for(int j = 0; j<articleList.size(); j++) {
                    Top10ArticleBase a = articleList.get(j);
                    if(currentArticle.getId().equals(a.getId()) &&
                            currentArticle.getBoard().equals(a.getBoard())) {
                        groupIndex = i;
                        articleIndex = j;
                        break;
                    }
                }
            }
            XLog.d("FK","当前这一篇组编号："+groupIndex+",帖子序号："+articleIndex);
            // 返回上一篇帖子
            if(groupIndex != -1 && articleIndex != -1) {
                // 帖子不是本组第一个
                if(articleIndex > 0) {
                    return groupList.get(groupIndex).items.get(articleIndex-1);
                    // 帖子是本组第一个,返回上一组最后一个
                }else {
                    if(groupIndex > 0 &&
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
            if(currentArticle == null) {
                return null;
            }

            XLog.d("FK","Top10Group 开始获取十大的下一篇！！！！");
            int groupIndex = -1;
            int articleIndex = -1;
            // 判断当前是哪一组的那篇帖子
            for(int i = 0; i<groupList.size(); i++) {
                List<Top10ArticleBase> articleList = groupList.get(i).items;
                for(int j = 0; j<articleList.size(); j++) {
                    Top10ArticleBase a = articleList.get(j);
                    if(currentArticle.getId().equals(a.getId()) &&
                            currentArticle.getBoard().equals(a.getBoard())) {
                        groupIndex = i;
                        articleIndex = j;
                        break;
                    }
                }
            }
            XLog.d("FK","当前这一篇组编号："+groupIndex+",帖子序号："+articleIndex);
            // 返回下一篇帖子
            if(groupIndex != -1 && articleIndex != -1) {
                // 帖子不是本组最后一个
                if(articleIndex < groupList.get(groupIndex).items.size()-1) {
                    return groupList.get(groupIndex).items.get(articleIndex+1);
                    // 帖子是本组最后一个,返回下一组第一个
                }else {
                    if(groupIndex < groupList.size()-1 &&
                            groupList.get(groupIndex+1).items.size()>0) {
                        return groupList.get(groupIndex+1).items.get(0);
                    }
                }
            }
            return null;
        }

        private class Top10Group {
            public List<Top10ArticleBase> items;
            public String name;
            public Top10Group(String name, List<Top10ArticleBase> items) {
                this.items = items;
                this.name = name;
            }
        }
    }
}
