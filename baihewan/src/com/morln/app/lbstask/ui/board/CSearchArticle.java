package com.morln.app.lbstask.ui.board;

import android.os.*;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.model.ArticleBase;
import com.morln.app.lbstask.data.model.Board;
import com.morln.app.lbstask.logic.BbsBoardMgr;
import com.morln.app.lbstask.res.MainMsg;
import com.morln.app.lbstask.ui.Linear;
import com.morln.app.lbstask.utils.AnimationUtil;
import com.morln.app.lbstask.utils.DialogUtil;
import com.morln.app.lbstask.session.StatusCode;
import com.xengine.android.system.ui.XBackType;
import com.xengine.android.system.ui.XBaseComponent;
import com.xengine.android.system.ui.XUILayer;
import com.xengine.android.utils.XStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jasontujun.
 * Date: 12-3-7
 * Time: 下午10:21
 */
public class CSearchArticle extends XBaseComponent implements Linear<ArticleBase> {
    private BbsBoardMgr bbsBoardMgr;
    private boolean beforeSearchState;// 标识搜索界面的状态
    private List<ArticleBase> searchResultList = new ArrayList<ArticleBase>();
    private int currentArticleIndex;

    // 界面
    private View searchArticleFrame;
    private EditText authorInput, titleContainInput1, titleContainInput2,
            titleNotContainInput, fromDayInput, toDayInput;
    private Button searchBtn;
    private ListView resultListView;
    private ResultListAdapter resultListAdapter;

    public CSearchArticle(XUILayer parent) {
        super(parent);
        bbsBoardMgr = BbsBoardMgr.getInstance();

        setContentView(R.layout.bbs_search_article);
        searchArticleFrame =  findViewById(R.id.search_article_frame);
        authorInput = (EditText) findViewById(R.id.author_input);
        titleContainInput1 = (EditText) findViewById(R.id.contain_input1);
        titleContainInput2 = (EditText) findViewById(R.id.contain_input2);
        titleNotContainInput = (EditText) findViewById(R.id.not_contain_input);
        fromDayInput = (EditText) findViewById(R.id.from_date_input);
        toDayInput = (EditText) findViewById(R.id.to_date_input);
        searchBtn = (Button) findViewById(R.id.search_btn);
        resultListView = (ListView) findViewById(R.id.result_list);

        fromDayInput.setText("0");
        toDayInput.setText("7");

        resultListAdapter = new ResultListAdapter();
        resultListView.setAdapter(resultListAdapter);
        resultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final ArticleBase article = (ArticleBase) resultListAdapter.getItem(i);
                currentArticleIndex = getIndexOfResultList(article.getId());
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

        goToSearch();
    }

    private void goToSearch() {
        beforeSearchState = true;
        resultListView.setVisibility(View.GONE);
        searchArticleFrame.setVisibility(View.VISIBLE);
        authorInput.setText("");
        titleContainInput1.setText("");
        titleContainInput2.setText("");
        titleNotContainInput.setText("");
        fromDayInput.setText("0");
        toDayInput.setText("7");

        searchBtn.setBackgroundResource(R.drawable.btn_search);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 搜索条件判断！
                String author = authorInput.getText().toString();
                String title1 = titleContainInput1.getText().toString();
                String title2 = titleContainInput2.getText().toString();
                String title3 = titleNotContainInput.getText().toString();
                String fromDay = fromDayInput.getText().toString();
                String toDay = toDayInput.getText().toString();

                if (TextUtils.isEmpty(fromDay)) {
                    Toast.makeText(getContext(),"请填写帖子的时间范围~~", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(fromDayInput, getContext());
                    return;
                }
                if (TextUtils.isEmpty(toDay)) {
                    Toast.makeText(getContext(),"请填写帖子的时间范围~~", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(toDayInput, getContext());
                    return;
                }
                if (!XStringUtil.isNumber(fromDay)) {
                    Toast.makeText(getContext(),"帖子的时间范围必须是数字哦！", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(fromDayInput, getContext());
                    return;
                }
                if (!XStringUtil.isNumber(toDay)) {
                    Toast.makeText(getContext(),"帖子的时间范围必须是数字哦！", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(toDayInput, getContext());
                    return;
                }
                if (Integer.parseInt(fromDay) >= Integer.parseInt(toDay)) {
                    Toast.makeText(getContext(),"帖子的时间错误！", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(fromDayInput, getContext());
                    AnimationUtil.startShakeAnimation(toDayInput, getContext());
                    return;
                }
                if (TextUtils.isEmpty(author) && TextUtils.isEmpty(title1) &&
                        TextUtils.isEmpty(title2) && TextUtils.isEmpty(title3)) {
                    Toast.makeText(getContext(),"搜索条件不足~~", Toast.LENGTH_SHORT).show();
                    AnimationUtil.startShakeAnimation(searchBtn, getContext());
                    return;
                }

                new SearchArticleTask().execute(null);
            }
        });
    }

    private void goToResult() {
        beforeSearchState = false;
        resultListView.setVisibility(View.VISIBLE);
        searchArticleFrame.setVisibility(View.GONE);
        resultListAdapter.notifyDataSetChanged();
        AnimationUtil.startListAnimation(resultListView);

        searchBtn.setBackgroundResource(R.drawable.btn_search_again);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSearch();
            }
        });
    }

    @Override
    public ArticleBase getPre() {
        if (searchResultList.size() > 1 && currentArticleIndex > 0){
            currentArticleIndex = currentArticleIndex - 1;
            return searchResultList.get(currentArticleIndex);
        } else {
            return null;
        }
    }

    @Override
    public ArticleBase getNext() {
        if (searchResultList.size() > 1 && currentArticleIndex < searchResultList.size()-1){
            currentArticleIndex = currentArticleIndex + 1;
            return searchResultList.get(currentArticleIndex);
        } else {
            return null;
        }
    }

    private int getIndexOfResultList(String articleId){
        for(int i = 0; i<searchResultList.size(); i++){
            if(searchResultList.get(i).getId().equals(articleId)){
                return i;
            }
        }
        return -1;
    }

    @Override
    public int back() {
        if (!beforeSearchState) {
            goToSearch();
            return XBackType.CHILD_BACK;
        }
        return XBackType.NOTHING_TO_BACK;
    }


    private Parcelable state;

    @Override
    public void onLayerUnCovered() {
        super.onLayerUnCovered();
        if(!beforeSearchState) {
            if(state != null) {
                resultListView.onRestoreInstanceState(state);
            }
        }
    }

    @Override
    public void onLayerCovered() {
        super.onLayerCovered();

        if (!beforeSearchState) {
            state = resultListView.onSaveInstanceState();
        }
    }



    /**
     * 帖子列表适配器
     */
    public class ResultListAdapter extends BaseAdapter{

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
        public int getCount() {
            return searchResultList.size();
        }

        /**
         * 展示搜索帖子的顺序：新->旧
         * @param i
         * @return
         */
        @Override
        public Object getItem(int i) {
            int index = searchResultList.size() - 1 - i;// 逆序
            return searchResultList.get(index);
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

            // 装饰符
            long index = getItemId(i);
            if (index%2 == 1){
                holder.decoration.setImageResource(R.color.light_purple);
            } else {
                holder.decoration.setImageResource(R.color.light_green);
            }
            // 作者
            holder.author.setText(article.getAuthorName());
            // 置顶
            holder.up.setVisibility(View.INVISIBLE);
            // 人气
            holder.popularity.setText(""+article.getReplyCount()+"/"+article.getPopularity());
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
            } else {
                holder.boardChineseTip.setText("");
            }

            return convertView;
        }
    }

    /**
     * 搜索文章的异步线程
     */
    private class SearchArticleTask extends AsyncTask<Void, Void, Void> {

        private DialogUtil.WaitingDialog waitingDialog;

        private int resultCode;

        private List<ArticleBase> resultList = new ArrayList<ArticleBase>();

        @Override
        protected void onPreExecute() {
            waitingDialog = DialogUtil.createWaitingDialog(parentLayer().getUIFrame());
            waitingDialog.setAsyncTask(this);
            waitingDialog.show();
        }
        @Override
        protected Void doInBackground(Void... para) {
            String author = authorInput.getText().toString();
            String title1 = titleContainInput1.getText().toString();
            String title2 = titleContainInput2.getText().toString();
            String title3 = titleNotContainInput.getText().toString();
            String fromDay = fromDayInput.getText().toString();
            String toDay = toDayInput.getText().toString();

            resultCode = bbsBoardMgr.searchArticle(author, title1, title2,
                    title3, fromDay, toDay, resultList);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            waitingDialog.dismiss();
            if (StatusCode.isSuccess(resultCode)) {
                searchResultList = resultList;
                goToResult();
                Toast.makeText(getContext(), "搜索成功！", Toast.LENGTH_SHORT).show();
            } else {
                switch (resultCode) {
                    case StatusCode.HTTP_EXCEPTION:
                        Toast.makeText(getContext(), "网络连接异常...", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getContext(), "没有找到哦...", Toast.LENGTH_SHORT).show();
                }
            }
        }
        @Override
        protected void onCancelled() {
            waitingDialog.dismiss();
        }
    }
}
