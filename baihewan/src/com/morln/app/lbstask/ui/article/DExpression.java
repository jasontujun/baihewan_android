package com.morln.app.lbstask.ui.article;

import android.app.Dialog;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.utils.ExpressionMap;
import com.morln.app.lbstask.res.BbsMsg;
import com.xengine.android.system.ui.XBaseFrame;
import com.xengine.android.system.ui.XDialog;
import com.xengine.android.system.ui.XUILayer;

/**
 * Created by jasontujun.
 * Date: 12-4-18
 * Time: 上午10:11
 */
public class DExpression implements XDialog {
    private ExpressionMap expressionMap;

    // 界面
    private Dialog dialog;
    private XUILayer uiLayer;
    private GridView gridView;

    public DExpression(XUILayer uiLayer) {
        this.uiLayer = uiLayer;
        expressionMap = ExpressionMap.getInstance();

        XBaseFrame activity = (XBaseFrame) uiLayer.getUIFrame();
        dialog = new Dialog(activity, R.style.dialog);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(R.layout.dialog_expression);
        
        gridView = (GridView) dialog.findViewById(R.id.expression_grid);
        gridView.setAdapter(new ExpressionListAdapter());
    }

    @Override
    public void show() {
        dialog.show();
    }

    @Override
    public void dismiss() {
        dialog.dismiss();
    }


    private class ExpressionListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return expressionMap.size();
        }

        @Override
        public Object getItem(int i) {
            return expressionMap.getLocalExpression(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        private class ViewHolder {
            public RelativeLayout frame;
            public ImageView expView;
        }
        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if(convertView == null) {
                convertView = View.inflate(uiLayer.getContext(), R.layout.bbs_expression_item, null);
                holder = new ViewHolder();
                holder.frame = (RelativeLayout) convertView.findViewById(R.id.frame);
                holder.expView = (ImageView) convertView.findViewById(R.id.exp);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            final int index = i;
            String picName = (String) getItem(i);
            
            uiLayer.setViewBackground(holder.expView, picName);

            // 设置监听
            holder.expView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Handler handler = uiLayer.getLayerHandler();
                    Message msg = handler.obtainMessage();
                    msg.what = BbsMsg.EXPRESSION;
                    msg.arg1 = index;
                    handler.sendMessage(msg);

                    dismiss();
                }
            });
            return convertView;
        }
    }
}
