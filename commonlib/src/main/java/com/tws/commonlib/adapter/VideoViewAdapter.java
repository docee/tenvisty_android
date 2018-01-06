package com.tws.commonlib.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.R;
import com.tws.commonlib.bean.MyCamera;

import java.util.List;
import java.util.Map;

public class VideoViewAdapter extends SimpleAdapter {

    public interface OnButtonClickListener {
        void onButtonClick(int btnId, MyCamera camera);
    }

    OnButtonClickListener mListener;
    private LayoutInflater mInflater;
    Context context;
    private String str_state[];
    private String str_rebootState[];
    OnClickListener clickListener;
    private int str_state_background[] = new int[]{
            R.drawable.shape_state_connecting,
            R.drawable.shape_state_connecting,
            R.drawable.shape_state_online,
            R.drawable.shape_state_offline,
            R.drawable.shape_state_offline,
            R.drawable.shape_state_pwderror,
            R.drawable.shape_state_offline,
            R.drawable.shape_state_offline,
            R.drawable.shape_state_offline,
            R.drawable.shape_state_pwderror,
            R.drawable.shape_state_connecting};

    public VideoViewAdapter(Context context,
                            List<? extends Map<String, ?>> data, int resource, String[] from,
                            int[] to) {

        super(context, data, resource, from, to);
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        str_state = context.getResources().getStringArray(R.array.connect_state);
        str_rebootState = new String[]{str_state[2], context.getString(R.string.tips_rebooting_wait),
                context.getString(R.string.tips_rebooting),
                context.getString(R.string.tips_reseting_wait),
                context.getString(R.string.tips_reseting),
                context.getString(R.string.tips_upgrading_wait),
                context.getString(R.string.tips_upgrading)
        };
        clickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onButtonClick(view.getId(), (MyCamera) view.getTag());
                }
            }
        };
    }

    public void setOnButtonClickListener(OnButtonClickListener listener) {
        mListener = listener;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        @SuppressWarnings("unchecked")
        Map<String, ?> data = (Map<String, ?>) this.getItem(position);
        final MyCamera camera = (MyCamera) data.get("object");
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = this.mInflater.inflate(R.layout.camera_main_item, null);
            DisplayMetrics displayMetrics = parent.getContext().getResources().getDisplayMetrics();
            int width = displayMetrics.widthPixels;
            int height = width * 9 / 16;
            //convertView=mInflater.inflate(R.layout.camera_main_item, null);
            holder = new ViewHolder();
            holder.ll_image = (RelativeLayout) convertView.findViewById(R.id.ll_image);
            holder.ll_image.getLayoutParams().height = height;
            holder.ll_bottom_button = (LinearLayout) convertView.findViewById(R.id.ll_bottom_button);
            holder.img_snapshot = (ImageView) convertView.findViewById(R.id.img_snapshot);
            holder.txt_nikename = (TextView) convertView.findViewById(R.id.txt_nikename);
            holder.txt_state = (TextView) convertView.findViewById(R.id.txt_state);

            holder.btn_item_setting = (ImageView) convertView.findViewById(R.id.btn_item_setting);
            holder.btn_item_delete = (ImageView) convertView.findViewById(R.id.btn_item_delete);
            holder.btn_item_event = (ImageView) convertView.findViewById(R.id.btn_item_event);
            holder.ll_mask_image = (LinearLayout) convertView.findViewById(R.id.ll_mask_image);
            holder.btn_item_edit = (ImageView) convertView.findViewById(R.id.btn_item_edit);
            holder.ll_tip_password_wrong = convertView.findViewById(R.id.ll_tip_password_wrong);
            holder.ll_tip_play = convertView.findViewById(R.id.ll_tip_play);
            holder.ll_tip_disconnected = convertView.findViewById(R.id.ll_tip_disconnected);
            holder.ll_tip_connecting = convertView.findViewById(R.id.ll_tip_connecting);
            holder.img_push_alarm = convertView.findViewById(R.id.img_push_alarm);
            holder.ll_mask_image.getLayoutParams().height = height - holder.ll_bottom_button.getLayoutParams().height;

            holder.btn_item_setting.setOnClickListener(clickListener);
            holder.btn_item_event.setOnClickListener(clickListener);
            holder.btn_item_delete.setOnClickListener(clickListener);
            holder.btn_item_edit.setOnClickListener(clickListener);
            holder.ll_tip_play.findViewById(R.id.btn_play).setOnClickListener(clickListener);
            holder.ll_tip_disconnected.findViewById(R.id.btn_reconnect).setOnClickListener(clickListener);
            holder.ll_tip_password_wrong.findViewById(R.id.btn_modifyPassword).setOnClickListener(clickListener);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (holder != null) {
            holder.btn_item_delete.setTag(camera);
            holder.btn_item_edit.setTag(camera);
            holder.img_snapshot.setTag(camera);
            holder.btn_item_setting.setTag(camera);
            holder.btn_item_setting.setTag(camera);
            holder.btn_item_event.setTag(camera);
            holder.ll_tip_play.findViewById(R.id.btn_play).setTag(camera);
            holder.ll_tip_disconnected.findViewById(R.id.btn_reconnect).setTag(camera);
            holder.ll_tip_password_wrong.findViewById(R.id.btn_modifyPassword).setTag(camera);
            Bitmap snap = ((MyCamera) camera).getSnapshot();
            if (snap != null) {
                holder.img_snapshot.setImageBitmap(snap);
            } else {
                holder.img_snapshot.setImageResource(R.drawable.videoclip);
            }
            holder.txt_nikename.setText(camera.getName());
            int state = camera.connect_state;
            MyCamera.CameraState rebootState = camera.getState();
            if (rebootState != MyCamera.CameraState.None) {
                holder.txt_state.setBackgroundResource(str_state_background[0]);
                holder.txt_state.setText(str_rebootState[rebootState.ordinal()]);
            } else {
                holder.txt_state.setBackgroundResource(str_state_background[state]);
                //String conType = "";
//                try {
//                    conType = (camera.getSessionMode() == 0 ? "P2P" : (camera.getSessionMode() == 1 ? "RELAY" : "LAN"));
//                } catch (Exception ex) {
//
//                }
                holder.txt_state.setText(str_state[state]);
            }
            if(camera.getEventNum() > 0){
                holder.img_push_alarm.setVisibility(View.VISIBLE);
            }
            else{
                holder.img_push_alarm.setVisibility(View.GONE);
            }
            holder.ll_tip_password_wrong.setVisibility(View.GONE);
            holder.ll_tip_disconnected.setVisibility(View.GONE);
            holder.ll_tip_connecting.setVisibility(View.GONE);
            holder.ll_tip_play.setVisibility(View.GONE);

            if (state == NSCamera.CONNECTION_STATE_CONNECTED && camera.getState() == MyCamera.CameraState.None) {
                holder.ll_tip_play.setVisibility(View.VISIBLE);
                holder.btn_item_event.setEnabled(true);
                holder.btn_item_setting.setEnabled(true);
                holder.ll_mask_image.setEnabled(true);
            } else {
                if (camera.getState() == MyCamera.CameraState.None) {
                    if (state == NSCamera.CONNECTION_STATE_WRONG_PASSWORD) {
                        holder.ll_tip_password_wrong.setVisibility(View.VISIBLE);
                    } else if (state == NSCamera.CONNECTION_STATE_CONNECTING || state == NSCamera.CONNECTION_STATE_NONE) {
                        holder.ll_tip_connecting.setVisibility(View.VISIBLE);
                    } else {
                        holder.ll_tip_disconnected.setVisibility(View.VISIBLE);
                    }
                }
                holder.btn_item_event.setEnabled(false);
                holder.btn_item_setting.setEnabled(false);
                holder.ll_mask_image.setEnabled(false);
            }
        }
        return convertView;
    }

    public class ViewHolder {
        public ImageView img_snapshot;
        public TextView txt_nikename;
        public TextView txt_state;

        public ImageView btn_item_event;
        public ImageView btn_item_setting;
        public ImageView btn_item_delete;
        public ImageView btn_item_edit;
        public ImageView img_push_alarm;
        public LinearLayout ll_bottom_button;
        public RelativeLayout ll_image;
        public LinearLayout ll_mask_image;

        public RelativeLayout ll_tip_play;
        public RelativeLayout ll_tip_password_wrong;
        public RelativeLayout ll_tip_disconnected;
        public RelativeLayout ll_tip_connecting;


    }
}
