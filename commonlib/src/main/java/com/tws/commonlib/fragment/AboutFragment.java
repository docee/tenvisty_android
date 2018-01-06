package com.tws.commonlib.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tws.commonlib.R;
import com.tws.commonlib.activity.VersionActivity;
import com.tws.commonlib.activity.WebBrowserActivity;
import com.tws.commonlib.base.MyConfig;


public class AboutFragment extends Fragment {
	private View view;
	int[] arrItemsTitle;
	int[] arrItemsIcon;
	@Override
	public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		if(view  == null) {
			view = inflater.inflate(R.layout.fragment_about, null);
			arrItemsTitle = new int[]{R.string.title_userhelp,R.string.title_privacy_policy,R.string.title_version};
			arrItemsIcon = new int[]{R.drawable.ic_userhelp,R.drawable.ic_privacypolicy,R.drawable.ic_version};
			initView();
		}
		ViewGroup parent = (ViewGroup) view.getParent();
		if (parent != null)
		{
			parent.removeView(view);
		}
//		
		return view;
	}
	private void initView() {

		ListView about_fragment_item_list=(ListView)view.findViewById(R.id.about_fragment_item_list);
		about_fragment_item_list.setAdapter(new AboutListAdapter(getActivity()));
		about_fragment_item_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				if(arrItemsTitle[position] == R.string.title_version){
					Intent intent = new Intent();
					intent.setClass(getActivity(),VersionActivity.class);
					startActivity(intent);
				}else if(arrItemsTitle[position] == R.string.title_userhelp){
					Intent intent = new Intent();
					intent.setClass(getActivity(),WebBrowserActivity.class);
					String title = AboutFragment.this.getString(R.string.title_userhelp);
					String url=MyConfig.getUserHelpUrl();
					intent.putExtra("title",title);
					intent.putExtra("url",url);
					startActivity(intent);
				}
				else  if(arrItemsTitle[position] == R.string.title_privacy_policy){
					Intent intent = new Intent();
					intent.setClass(getActivity(),WebBrowserActivity.class);
					String title = AboutFragment.this.getString(R.string.title_privacy_policy);
					String url = MyConfig.getPrivacyUrl();
					intent.putExtra("title",title);
					intent.putExtra("url",url);
					startActivity(intent);
				}

			}
		});
	}



	protected class AboutListAdapter extends BaseAdapter {

		private LayoutInflater mInflater;

		//	public VideoListAdapter(LayoutInflater layoutInflater) {
		//		this.mInflater = layoutInflater;
		//	}

		public AboutListAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);

			//		this.mContext = context;
			//		this.mInflater = layoutInflater;
		}

		@Override
		public int getCount() {
			
			return arrItemsTitle.length;
		}

		@Override
		public Object getItem(int position) {

			return arrItemsTitle[position];
		}

		@Override
		public long getItemId(int position) {

			return position;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			ViewHolder holder = null;

			if (convertView == null) {

				convertView = mInflater.inflate(R.layout.view_list_about_item, null);

				holder = new ViewHolder();
				//			holder.img = (ImageView) convertView.findViewById(R.id.img);
				holder.txt_item_title = (TextView) convertView.findViewById(R.id.txt_item_title);
				holder.img_item_icon = (ImageView)convertView.findViewById(R.id.img_item_icon);
				convertView.setTag(holder);

			} else {

				holder = (ViewHolder) convertView.getTag();
			}




			if (holder != null) {
				holder.txt_item_title.setText(getString(arrItemsTitle[position]));
				holder.img_item_icon.setImageResource(arrItemsIcon[position]);
			}

			return convertView;

		}

		public final class ViewHolder {
			//		public ImageView img;
			public TextView txt_item_title;
			public ImageView img_item_icon;
			
		}

	}

}
