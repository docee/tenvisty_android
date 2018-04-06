package com.tws.commonlib.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.v7.content.res.AppCompatResources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.R;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.CameraState;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.MyCamera;

import java.util.List;
import java.util.Map;

public class VideoViewAdapter extends SimpleAdapter {

    public interface OnButtonClickListener {
        void onButtonClick(int btnId, IMyCamera camera);
    }

    OnButtonClickListener mListener;
    private LayoutInflater mInflater;
    Context context;
    OnClickListener clickListener;

    public VideoViewAdapter(Context context,
                            List<? extends Map<String, ?>> data, int resource, String[] from,
                            int[] to) {

        super(context, data, resource, from, to);
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        clickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onButtonClick(view.getId(), (IMyCamera) view.getTag());
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
        final IMyCamera camera = (IMyCamera) data.get("object");
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
            holder.img_battery = (ImageView) convertView.findViewById(R.id.img_battery);
            holder.btn_item_setting = (ImageView) convertView.findViewById(R.id.btn_item_setting);
            holder.btn_item_delete = (ImageView) convertView.findViewById(R.id.btn_item_delete);
            holder.btn_item_event = (ImageView) convertView.findViewById(R.id.btn_item_event);
            holder.ll_mask_image = (LinearLayout) convertView.findViewById(R.id.ll_mask_image);
            holder.btn_item_edit = (ImageView) convertView.findViewById(R.id.btn_item_edit);
            holder.ll_tip_password_wrong = convertView.findViewById(R.id.ll_tip_password_wrong);
            holder.ll_tip_play = convertView.findViewById(R.id.ll_tip_play);
            holder.ll_tip_disconnected = convertView.findViewById(R.id.ll_tip_disconnected);
            holder.ll_tip_connecting = convertView.findViewById(R.id.ll_tip_connecting);
            holder.ll_tip_sleep = convertView.findViewById(R.id.ll_tip_sleep);
            holder.img_push_alarm = convertView.findViewById(R.id.img_push_alarm);
            holder.ll_mask_image.getLayoutParams().height = height - holder.ll_bottom_button.getLayoutParams().height;

            holder.btn_item_setting.setOnClickListener(clickListener);
            holder.btn_item_event.setOnClickListener(clickListener);
            holder.btn_item_delete.setOnClickListener(clickListener);
            holder.btn_item_edit.setOnClickListener(clickListener);
            holder.ll_tip_play.findViewById(R.id.btn_play).setOnClickListener(clickListener);
            holder.ll_tip_disconnected.findViewById(R.id.btn_reconnect).setOnClickListener(clickListener);
            holder.ll_tip_password_wrong.findViewById(R.id.btn_modifyPassword).setOnClickListener(clickListener);
            holder.ll_tip_sleep.findViewById(R.id.btn_wakeup).setOnClickListener(clickListener);
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
            holder.ll_tip_sleep.findViewById(R.id.btn_wakeup).setTag(camera);
            Bitmap snap = camera.getSnapshot();
            if (snap == null || snap.isRecycled()) {
                if (TwsTools.isSDCardValid()) {
                    try {
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inJustDecodeBounds = true;
                        String snapshotPath = TwsTools.getFilePath(camera.getUid(), TwsTools.PATH_SNAPSHOT_LIVEVIEW_AUTOTHUMB) + "/" + TwsTools.getFileNameWithTime(camera.getUid(), TwsTools.PATH_SNAPSHOT_LIVEVIEW_AUTOTHUMB);
                        snap = BitmapFactory.decodeFile(snapshotPath, opts);
                        opts.inSampleSize = opts.outWidth / 640;
                        if (opts.inSampleSize < 1) {
                            opts.inSampleSize = 1;
                        } else if (opts.inSampleSize % 2 == 1) {
                            opts.inSampleSize--;
                        }
                        opts.inJustDecodeBounds = false;
                        snap = BitmapFactory.decodeFile(snapshotPath, opts);
                        camera.setSnapshot(snap);
                    } catch (OutOfMemoryError error) {

                    }
                }

            }
            if (snap != null) {
                holder.img_snapshot.setImageBitmap(snap);
            } else {
                holder.img_snapshot.setImageResource(R.drawable.videoclip);
            }
            // holder.img_snapshot.setBackgroundResource(R.drawable.btn_scan_flash_off);
            holder.txt_nikename.setText(camera.getNickName());
            holder.txt_state.setBackgroundResource(camera.getCameraStateBackgroundColor());
            holder.txt_state.setText(camera.getCameraStateDesc());
            if (camera.getEventNum(0) > 0) {
                holder.img_push_alarm.setVisibility(View.VISIBLE);
            } else {
                holder.img_push_alarm.setVisibility(View.GONE);
            }
            holder.ll_tip_password_wrong.setVisibility(View.GONE);
            holder.ll_tip_disconnected.setVisibility(View.GONE);
            holder.ll_tip_connecting.setVisibility(View.GONE);
            holder.ll_tip_play.setVisibility(View.GONE);
            holder.ll_tip_sleep.setVisibility(View.GONE);
            if (camera.getSupplier() != IMyCamera.Supllier.AN) {
                holder.img_battery.setVisibility(View.GONE);
            } else {
                int batteryDrawable = camera.getBatteryStatus().getBatteryDrawable();
                holder.img_battery.setVisibility(View.VISIBLE);
                holder.img_battery.setImageResource(batteryDrawable);
            }
            if (camera.isConnected() && camera.getState() == CameraState.None && camera.getSupplier() != IMyCamera.Supllier.UnKnown) {
                holder.ll_tip_play.setVisibility(View.VISIBLE);
                holder.btn_item_event.setEnabled(true);
                holder.btn_item_setting.setEnabled(true);
                holder.ll_mask_image.setEnabled(true);
            } else {
                if (camera.getState() == CameraState.None) {
                    if (camera.isPasswordWrong()) {
                        holder.ll_tip_password_wrong.setVisibility(View.VISIBLE);
                    } else if (camera.isConnecting() || camera.isWakingUp() || (camera.isConnected() && camera.getSupplier() == IMyCamera.Supllier.UnKnown)) {
                        holder.ll_tip_connecting.setVisibility(View.VISIBLE);
                    } else if (camera.isSleeping()) {
                        if (camera.isWakingUp()) {
                            holder.ll_tip_connecting.setVisibility(View.VISIBLE);
                        } else {
                            holder.ll_tip_sleep.setVisibility(View.VISIBLE);
                        }
                    } else {
                       // Log.i("VideoViewAdapter", "state:" + (camera.isConnected() ? 1 : 0) + " supplier:" + camera.getSupplier() + " connectstate:" + ((MyCamera) camera).connect_state);
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
        public ImageView img_battery;
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
        public RelativeLayout ll_tip_sleep;


    }
}
