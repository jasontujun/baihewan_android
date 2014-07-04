package com.morln.app.lbstask.ui.mail;

import android.os.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.cache.MailSource;
import com.morln.app.lbstask.data.model.Mail;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.ui.controls.XListView;
import com.morln.app.lbstask.logic.BbsMailMgr;
import com.morln.app.lbstask.res.BbsMsg;
import com.morln.app.lbstask.res.BbsPic;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.ui.Linear;
import com.morln.app.lbstask.ui.login.DLogin;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseComponent;
import com.xengine.android.system.ui.XUILayer;

/**
 * Created by jasontujun.
 * Date: 12-7-15
 * Time: 下午10:57
 */
public class CMail extends XBaseComponent implements Linear<Mail> {
    private BbsMailMgr bbsMailMgr;
    private MailSource mailSource;

    private String currentMailUrl;// 当前阅读的邮件

    // 界面
    private RelativeLayout frame;
    private TextView btn1;
    private ImageView tip1;
    private ImageView leftBtn, rightBtn, leftBtnTip;
    private XListView contentList;
    private MailListAdapter mailListAdapter;
    private TextView nothingTip;

    // 异步线程
    private GetMailListTask refreshTask;
    private MoreMailListTask moreTask;

    public CMail(XUILayer parent) {
        super(parent);
        bbsMailMgr = BbsMailMgr.getInstance();
        mailSource = (MailSource) DefaultDataRepo
                .getInstance().getSource(SourceName.USER_MAIL);

        setContentView(R.layout.bbs_mail);
        frame = (RelativeLayout) findViewById(R.id.frame);
        btn1 = (TextView) findViewById(R.id.top_btn1);
        tip1 = (ImageView) findViewById(R.id.top_tip1);
        leftBtn = (ImageView) findViewById(R.id.left_btn);
        leftBtnTip = (ImageView) findViewById(R.id.left_btn_tip);
        rightBtn = (ImageView) findViewById(R.id.right_btn);
        contentList = (XListView) findViewById(R.id.content_list);
        nothingTip = (TextView) findViewById(R.id.nothing_tip);

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

        // 初始化顶部选择栏
        initListBottom(contentList);
        mailListAdapter = new MailListAdapter();
        contentList.setAdapter(mailListAdapter);
        contentList.setRefreshable(true);
        contentList.setOnRefreshListener(new XListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshTask = new GetMailListTask(false);
                refreshTask.execute(null);
            }
        });

        bbsMailMgr.registerNewMailListener(new Mail.NewMailListener() {
            @Override
            public void remind(int newMailNumber) {
                refreshLeftBtnTip(newMailNumber);
            }
        });
    }



    private TextView moreLabel;
    private boolean isGettingMore;
    /**
     * 初始化列表底部-“加载更多”
     * @param listView
     */
    private void initListBottom(ListView listView) {
        View contentFrame = View.inflate(getContext(), R.layout.bbs_article_list_item_bottom, null);
        moreLabel = (TextView) contentFrame.findViewById(R.id.more_label);
        moreLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isGettingMore) {
                    int minMailNo = mailSource.getMinNo();
                    if (minMailNo <= 0) {
                        AnimationUtil.startShakeAnimation(moreLabel, getContext());
                        Toast.makeText(getContext(), "没有更多了", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    refreshListBottom(true);
                    // 异步线程加载“更多”。如果失败说明没有更多了
                    if (refreshTask != null) {
                        // 取消刷新线程，防止冲突
                        refreshTask.cancel(true);
                    }
                    // 启动“查看更多”线程
                    int start = Math.max(0, minMailNo - 20);
                    moreTask = new MoreMailListTask(start);
                    moreTask.execute(null);
                }
            }
        });
        listView.addFooterView(contentFrame);
        refreshListBottom(false);
    }

    private void refreshListBottom(boolean gettingMore) {
        isGettingMore = gettingMore;
        if (isGettingMore) {
            moreLabel.setText("正在加载……");
        } else {
            moreLabel.setText("查看更多");
        }
    }


    /**
     * 刷新邮件列表
     */
    public void initMailList() {
        if (bbsMailMgr.getNewMailNumber() != mailSource.getNewMailNumber()
                || mailSource.size() == 0) {
            new GetMailListTask(true).execute(null);
        } else {
            mailListAdapter.notifyDataSetChanged();
            contentList.onRefreshComplete();
        }
    }

    @Override
    public int back() {
        return XBackType.NOTHING_TO_BACK;
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
                    mailListAdapter.notifyDataSetChanged();
                    contentList.onRefreshComplete();
                    break;
            }
        }
    };


    private Parcelable state;

    @Override
    public void onLayerUnCovered() {
        super.onLayerUnCovered();
        if (state != null) {
            contentList.onRestoreInstanceState(state);
        }
    }

    @Override
    public void onLayerCovered() {
        super.onLayerCovered();
        state = contentList.onSaveInstanceState();
    }



    /**
     * 逆序排列。故向前即原来数据列表的反向
     * @return
     */
    @Override
    public Mail getPre() {
        // 判断当前是哪一组的站内信
        int articleIndex = -1;
        for (int i =0; i<mailSource.size(); i++) {
            if (mailSource.get(i).getId().equals(currentMailUrl)) {
                articleIndex = i;
                break;
            }
        }
        // 返回下一篇站内信
        if (articleIndex != -1 && articleIndex < mailSource.size()-1) {
            Mail mail = mailSource.get(articleIndex + 1);
            currentMailUrl = mail.getId();
            return mail;
        }
        return null;
    }

    /**
     * 逆序排列。故向前即原来数据列表的反向
     * @return
     */
    @Override
    public Mail getNext() {
        // 判断当前是哪一组的那封站内信
        int articleIndex = -1;
        for (int i =0; i<mailSource.size(); i++) {
            if (mailSource.get(i).getId().equals(currentMailUrl)) {
                articleIndex = i;
                break;
            }
        }
        // 返回上一篇站内信
        if (articleIndex > 0) {
            Mail mail = mailSource.get(articleIndex - 1);
            currentMailUrl = mail.getId();
            return mail;
        }
        return null;
    }


    /**
     * 邮件列表
     */
    public class MailListAdapter extends BaseAdapter {

        private class ViewHolder {
            public RelativeLayout frame;
            public ImageView decoration;
            public ImageView label;
            public TextView author;
            public LinearLayout popFrame;
            public LinearLayout deleteFrame;
            public TextView title;
            public TextView time;
            public LinearLayout boardTipFrame;
            public TextView boardIdTip;
            public TextView boardChineseTip;
        }

        @Override
        public int getCount() {
            int count = mailSource.size();
            if (count == 0) {
                nothingTip.setVisibility(View.VISIBLE);
            } else {
                nothingTip.setVisibility(View.INVISIBLE);
            }
            return count;
        }

        @Override
        public Object getItem(int i) {
            return mailSource.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            Object item = getItem(i);
            if (item == null){
                return null;
            }

            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.bbs_article_list_item, null);
                holder = new ViewHolder();
                holder.frame = (RelativeLayout) convertView.findViewById(R.id.frame);
                holder.decoration = (ImageView) convertView.findViewById(R.id.decoration);
                holder.label = (ImageView) convertView.findViewById(R.id.label);
                holder.author = (TextView) convertView.findViewById(R.id.author);
                holder.popFrame = (LinearLayout) convertView.findViewById(R.id.pop_frame);
                holder.deleteFrame = (LinearLayout) convertView.findViewById(R.id.delete_frame);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.boardTipFrame = (LinearLayout) convertView.findViewById(R.id.board_tip_frame);
                holder.boardIdTip = (TextView) convertView.findViewById(R.id.board_id_tip);
                holder.boardChineseTip = (TextView) convertView.findViewById(R.id.board_chinese_tip);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Mail mail = (Mail) getItem(i);
            // 背景
            holder.frame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    currentMailUrl = mail.getId();
                    Handler handler1 = getFrameHandler();
                    Message msg = handler1.obtainMessage(MainMsg.SEE_MAIL_DETAIL);
                    Bundle bundle = new Bundle();
                    bundle.putString("id", mail.getId());
                    msg.setData(bundle);
                    handler1.sendMessage(msg);
                }
            });
            // 装饰符
            if (mail.getStatus() == Mail.NEW) {
                holder.decoration.setImageResource(R.color.light_red);
                holder.label.setVisibility(View.VISIBLE);
                setViewBackground(holder.label, BbsPic.LABEL_NEW);
            }else {
                holder.decoration.setImageResource(R.color.dark_gray);
                holder.label.setVisibility(View.GONE);
            }
            // 作者
            holder.author.setText(mail.getSender());
            // 人气栏
            holder.popFrame.setVisibility(View.GONE);
            // 删除栏
            holder.deleteFrame.setVisibility(View.VISIBLE);
            holder.deleteFrame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogUtil.createConfirmDialog(parentLayer().getUIFrame().getContext(),
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    new DeleteMailTask(mail.getId()).execute();
                                }
                            }, null).show("删除站内信", "确定删除“"+mail.getTitle()+"”吗？");
                }
            });
            // 标题
            holder.title.setText(mail.getTitle());
            // 时间
            holder.time.setText(mail.getDate());

            return convertView;
        }
    }


    /**
     * 获取站内信的异步线程
     */
    private class GetMailListTask extends AsyncTask<Void, Void, Integer> {

        private DialogUtil.WaitingDialog waitingDialog;

        private boolean hasDialog;

        private GetMailListTask(boolean hasDialog) {
            this.hasDialog = hasDialog;
        }

        @Override
        protected void onPreExecute() {
            if (hasDialog) {
                waitingDialog = DialogUtil.createWaitingDialog(parentLayer().getUIFrame().getContext());
                waitingDialog.setAsyncTask(this);
                waitingDialog.show();
            }
        }
        @Override
        protected Integer doInBackground(Void... para) {
            int resultCode1 = bbsMailMgr.getDefaultMailListFromWeb();
            return resultCode1;
        }
        @Override
        protected void onPostExecute(Integer resultCode) {
            if (hasDialog) {
                waitingDialog.dismiss();
            }

            mailListAdapter.notifyDataSetChanged();
            contentList.onRefreshComplete();
            if (StatusCode.isSuccess(resultCode)) {
                Toast.makeText(getContext(), "刷新站内信成功！", Toast.LENGTH_SHORT).show();
            } else {
                switch (resultCode){
                    case StatusCode.BBS_TOKEN_LOSE_EFFECTIVE:
                        new DLogin(parentLayer(), true).show("由于长时间发呆，要重新登录哦");
                        Toast.makeText(getContext(), "BBS登录失效,请重新登录！", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getContext(), "刷新站内信失败！", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
        @Override
        protected void onCancelled() {
            if (hasDialog) {
                waitingDialog.dismiss();
            }
            contentList.onRefreshComplete();
            Toast.makeText(getContext(), "刷新站内信失败！", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 获取站内信的异步线程
     */
    private class MoreMailListTask extends AsyncTask<Void, Void, Integer> {

        private int start;

        private MoreMailListTask(int start) {
            this.start = start;
        }

        @Override
        protected Integer doInBackground(Void... para) {
            int resultCode1 = bbsMailMgr.getMailListFromWeb(start);
            return resultCode1;
        }

        @Override
        protected void onPostExecute(Integer resultCode) {
            refreshListBottom(false);
            contentList.onRefreshComplete();
            if (StatusCode.isSuccess(resultCode)) {
                mailListAdapter.notifyDataSetChanged();
            } else {
                switch (resultCode) {
                    case StatusCode.BBS_TOKEN_LOSE_EFFECTIVE:
                        new DLogin(parentLayer(), true).show("由于长时间发呆，要重新登录哦");
                        Toast.makeText(getContext(), "BBS登录失效,请重新登录！", Toast.LENGTH_SHORT).show();
                        break;
                    case StatusCode.HTTP_EXCEPTION:
                        Toast.makeText(getContext(), "网络通信异常，请稍后重试……", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getContext(), "获取失败~", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
        @Override
        protected void onCancelled() {
            refreshListBottom(false);
            contentList.onRefreshComplete();
            Toast.makeText(getContext(), "获取失败~", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 删除站内信的异步线程
     */
    private class DeleteMailTask extends AsyncTask<Void, Void, Integer> {

        private DialogUtil.WaitingDialog waitingDialog;

        private String url;

        private DeleteMailTask(String url) {
            this.url = url;
        }

        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(parentLayer().getUIFrame().getContext());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }
        @Override
        protected Integer doInBackground(Void... para) {
            int resultCode1 = bbsMailMgr.deleteMail(url);
            return resultCode1;
        }
        @Override
        protected void onPostExecute(Integer resultCode) {
            waitingDialog.dismiss();
            contentList.onRefreshComplete();
            if (StatusCode.isSuccess(resultCode)) {
                Toast.makeText(getContext(), "删除站内信成功！", Toast.LENGTH_SHORT).show();
                mailListAdapter.notifyDataSetChanged();
                AnimationUtil.startListAnimation(contentList);
            } else {
                switch (resultCode) {
                    case StatusCode.BBS_TOKEN_LOSE_EFFECTIVE:
                        new DLogin(parentLayer(), true).show("由于长时间发呆，要重新登录哦");
                        Toast.makeText(getContext(), "BBS登录失效,请重新登录！", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getContext(), "删除站内信失败！", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
        @Override
        protected void onCancelled() {
            waitingDialog.dismiss();
            contentList.onRefreshComplete();
            Toast.makeText(getContext(), "删除站内信失败！", Toast.LENGTH_SHORT).show();
        }
    }

}
