package com.morln.app.lbstask.newui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.morln.app.lbstask.R;

/**
 * Created with IntelliJ IDEA.
 * User: jasontujun
 * Date: 13-11-5
 * Time: 下午5:43
 * To change this template use File | Settings | File Templates.
 */
public class AdapterLeftMenu extends BaseAdapter {

    private Context mContext;
    private int mSelectedIndex;

    private String[] mNames = {"十大热点", "订阅收藏", "各区版面",
            "个人中心", "站内信", "设置", "关于"};
    private int[] mIcons = {R.drawable.menu_hot, R.drawable.menu_collection,
            R.drawable.menu_board, R.drawable.menu_profile, R.drawable.menu_mail,
            R.drawable.menu_setting, R.drawable.menu_about};
    private int[] mIconsSelected = {R.drawable.menu_hot_selected,
            R.drawable.menu_collection_selected, R.drawable.menu_board_selected,
            R.drawable.menu_profile_selected, R.drawable.menu_mail_selected,
            R.drawable.menu_setting_selected, R.drawable.menu_about_selected};

    public AdapterLeftMenu(Context context, int selectedIndex) {
        mContext = context;
        mSelectedIndex = selectedIndex;
    }

    public void setSelectedIndex(int index) {
        mSelectedIndex = index;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mNames.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private class ViewHolder {
        public ImageView iconView;
        public TextView nameView;
        public View nickNameFrame;
        public TextView nickNameView;
        public ImageView selectedTipView;
        public TextView mailTipView;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder holder = null;
        if(convertView == null) {
            convertView = View.inflate(mContext, R.layout.main_left_bar_item, null);
            holder = new ViewHolder();
            holder.iconView = (ImageView) convertView.findViewById(R.id.icon);
            holder.nameView = (TextView) convertView.findViewById(R.id.name);
            holder.nickNameFrame = convertView.findViewById(R.id.nick_name_frame);
            holder.nickNameView = (TextView) convertView.findViewById(R.id.nick_name);
            holder.selectedTipView = (ImageView) convertView.findViewById(R.id.select_tip);
            holder.mailTipView = (TextView) convertView.findViewById(R.id.mail_tip);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.nameView.setText(mNames[i]);
        holder.mailTipView.setVisibility(View.GONE);
        if (i == mSelectedIndex) {// 选中的条目
            holder.nameView.setTextColor(mContext.getResources().getColor(R.color.yellow_green));
            holder.iconView.setImageResource(mIconsSelected[i]);
            holder.selectedTipView.setVisibility(View.VISIBLE);
        } else {
            holder.nameView.setTextColor(mContext.getResources().getColor(R.color.dark_gray));
            holder.iconView.setImageResource(mIcons[i]);
            holder.selectedTipView.setVisibility(View.GONE);
        }

        return convertView;
    }
}
