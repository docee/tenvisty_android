package com.tws.commonlib.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.tws.commonlib.R;
import com.tws.commonlib.activity.setting.WiFiListActivity;
import com.tws.commonlib.base.DateScrollItem;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.LocalPichModel;

import java.util.List;


/**
 * Created by Administrator on 2017/7/14.
 */


public class DateScrollItemListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    onStateChangedListner stateChangedListner;

    public void setStateChangedListner(onStateChangedListner stateChangedListner) {
        this.stateChangedListner = stateChangedListner;
    }

    onItemClickLinstener clickLinstener;

    public void setOnclickListner(onItemClickLinstener listner) {
        this.clickLinstener = listner;
    }

    public DateScrollItemListAdapter(Context context) {

        this.mInflater = LayoutInflater.from(context);
    }

    private boolean checkMode;

    public boolean isCheckMode() {
        return checkMode;
    }

    public DateScrollItemListAdapter(Context context, List<DateScrollItem> _sourceList) {
        this.mInflater = LayoutInflater.from(context);
        this.sourceList = _sourceList;
    }

    private List<DateScrollItem> sourceList;

    public void setLayout(int layout) {
        this.layout = layout;
    }

    private int getLayout() {
        return this.layout;
    }

    int layout;

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return sourceList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return sourceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        if (sourceList == null || sourceList.size() < position - 1) {
            return null;
        }
        DateScrollItem model = sourceList.get(position);

        if (model == null || model.subAdatper == null)
            return null;

        ViewHolder holder = null;

        if (convertView == null) {
            convertView = mInflater.inflate(layout == 0 ? R.layout.adapter_view_local_pic_item : layout, null);
            holder = new ViewHolder();
            holder.txt_title = (TextView) convertView.findViewById(R.id.txt_title);
            holder.txt_date = (TextView) convertView.findViewById(R.id.txt_date);
            holder.img_line_top = (ImageView) convertView.findViewById(R.id.img_line_top);
            holder.listview_subItemList = (GridView) convertView.findViewById(R.id.gridview_subItemList);
            holder.listview_subItemList.setNumColumns(3);
            holder.listview_subItemList.setVerticalSpacing(10);
            holder.listview_subItemList.setHorizontalSpacing(10);
            holder.ll_title = (LinearLayout) convertView.findViewById(R.id.ll_title);
            holder.img_dot = (ImageView) convertView.findViewById(R.id.img_dot);
            holder.img_group_check = convertView.findViewById(R.id.img_group_check);
            holder.listview_subItemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    int pos = (int)adapterView.getTag();
                    if (checkMode) {
                        LocalPicItemListAdapter picAdapter = (LocalPicItemListAdapter)sourceList.get(pos).subAdatper;
                        LocalPichModel picModel = picAdapter.sourceItemList.get(i);
                        picModel.checked = !picModel.checked;
                        picAdapter.notifyDataSetChanged();
                    }
                    if (clickLinstener != null) {
                        clickLinstener.onClick(view, pos, i, l);
                    }
                    //picAdapter.notifyDataSetChanged();
                }
            });
            holder.listview_subItemList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    int pos = (int)adapterView.getTag();
                    LocalPicItemListAdapter picAdapter = ((LocalPicItemListAdapter) (sourceList.get(pos).subAdatper));
                    LocalPichModel picModel = picAdapter.sourceItemList.get(i);
                    picModel.checked = true;
                    picAdapter.checkPicList.get(i).setEnabled(picModel.checked);
//                    picAdapter.checkPicList.get(i).setImageResource(picModel.checked ? R.drawable.ic_check_circle_15dp_checked : R.drawable.ic_check_circle_15dp_unchecked);
//                    picAdapter.checkPicList.get(i).setBackgroundResource(picModel.checked ? R.color.mask_press: R.color.transparent);
                    setCheckMode(true);
                    return false;
                }
            });
            holder.img_group_check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = (int)view.getTag();
                    DateScrollItem md = sourceList.get(pos);
                    md.checked = !md.checked;
                    LocalPicItemListAdapter picAdapter = (LocalPicItemListAdapter) md.subAdatper;
                    ((ImageView) view).setImageResource(md.checked ? R.drawable.ic_check_circle_24dp_checked : R.drawable.ic_check_circle_24dp_unchecked);
                    for (int i = 0; i < picAdapter.sourceItemList.size(); i++) {
                        LocalPichModel m = picAdapter.sourceItemList.get(i);
                        m.checked = md.checked;
                    }
                    picAdapter.notifyDataSetChanged();
                }
            });
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.listview_subItemList.setTag(position);
        holder.listview_subItemList.setAdapter(model.subAdatper);
        holder.img_group_check.setTag(position);
        holder.img_group_check.setSelected(model.checked);
        if (position == 0) {
            holder.ll_title.setVisibility(View.GONE);
            holder.img_dot.setVisibility(View.GONE);
        } else {
            holder.ll_title.setVisibility(View.VISIBLE);
            holder.img_dot.setVisibility(View.VISIBLE);
        }
        holder.txt_date.setText(model.date);
        holder.txt_title.setText(model.title);
        model.img_group_check = holder.img_group_check;
        if (!checkMode) {
            model.img_group_check.setVisibility(View.INVISIBLE);
        } else {
            model.img_group_check.setVisibility(View.VISIBLE);
        }
        return convertView;

    }

    public void setCheckMode(boolean b) {
        this.checkMode = b;
        for (DateScrollItem a : this.sourceList) {
            ((LocalPicItemListAdapter) a.subAdatper).checkMode = b;
            ((LocalPicItemListAdapter) a.subAdatper).notifyDataSetChanged();
        }
        if (stateChangedListner != null) {
            stateChangedListner.onCheck(b);
        }
        this.notifyDataSetChanged();
    }

    public final class ViewHolder {
        public TextView txt_date;
        public TextView txt_title;
        public GridView listview_subItemList;
        public ImageView img_line_top;
        public LinearLayout ll_title;
        public ImageView img_dot;
        public ImageView img_group_check;
    }

    public interface onItemClickLinstener {
        public void onClick(View view, int position, int subPosition, long viewid);
    }

    public interface onStateChangedListner {
        public void onCheck(boolean b);
    }


}