package com.morln.app.lbstask.ui.top10;

import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.morln.app.lbstask.R;
import com.morln.app.lbstask.data.cache.ZoneSource;
import com.morln.app.lbstask.data.model.Zone;
import com.morln.app.lbstask.data.cache.SourceName;
import com.morln.app.lbstask.ui.controls.ChangeImageLayout;
import com.xengine.android.data.cache.DefaultDataRepo;
import com.xengine.android.system.ui.XBaseFrame;
import com.xengine.android.system.ui.XDialog;
import com.xengine.android.system.ui.XUILayer;

/**
 * Created by jasontujun.
 * Date: 12-4-7
 * Time: 下午9:30
 */
public class DSelectZone implements XDialog {
    private ZoneSource zoneSource;
    // 界面
    private Dialog dialog;
    private XUILayer uiLayer;
    private ListView zoneListView;
    private Button cancelBtn;

    private SelectZoneListener listener;

    public DSelectZone(final XUILayer ul, SelectZoneListener listener) {
        this.uiLayer = ul;
        this.listener = listener;
        zoneSource = (ZoneSource) DefaultDataRepo
                .getInstance().getSource(SourceName.BBS_ZONE);

        XBaseFrame activity = (XBaseFrame) uiLayer.getUIFrame();
        dialog = new Dialog(activity, R.style.dialog);
        dialog.setContentView(R.layout.dialog_zone_select);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        zoneListView = (ListView) dialog.findViewById(R.id.list);
        zoneListView.setAdapter(new ZoneListAdapter());
        cancelBtn = (Button) dialog.findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    /**
     * 帖子列表
     */
    public class ZoneListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return zoneSource.size() - 3;
        }

        @Override
        public Object getItem(int i) {
            return zoneSource.get(i + 2);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        private class ViewHolder{
            public ChangeImageLayout bg;
            public TextView label;
        }
        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            Object item = getItem(i);
            if(item == null){
                return null;
            }

            ViewHolder holder = null;
            if(convertView == null) {
                convertView = View.inflate(uiLayer.getContext(), R.layout.bbs_zone_select_item, null);
                holder = new ViewHolder();
                holder.bg = (ChangeImageLayout) convertView.findViewById(R.id.bg);
                holder.label = (TextView) convertView.findViewById(R.id.label);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }
            final Zone zone = (Zone) item;
            holder.label.setText(zone.getName());
            holder.label.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                    if(listener != null) {
                        listener.onSelect(zone.getName());
                    }
                }
            });
            return convertView;
        }
    }

    public interface SelectZoneListener {
        void onSelect(String zoneName);
    }
}
