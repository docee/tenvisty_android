package com.tws.commonlib.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.tws.commonlib.bean.TwsDataValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {

	public static final String TABLE_USER = "user";
	public static final String TABLE_DEVICE = "device";
	public static final String TABLE_SEARCH_HISTORY = "search_history";
	public static final String TABLE_SNAPSHOT = "snapshot";
	public static  final String TABLE_DICTION = "diction";
	public static final String s_GCM_PHP_URL = "https://47.88.3.185/IpCamera/userLogin!updateUserToken.action";
	public static final String s_Package_name = "com.hdhawk.TENVISP2PHD";
	public static final String s_GCM_sender = "935793047540";
	public static String s_GCM_token = "";
	public static int n_mainActivity_Status = 0;

	public static final String TABLE_Mapping_LIST = "mapping_list";
	
	
	private DatabaseHelper mDbHelper;
	private Context mcontext=null;
	
	/*public DatabaseManager(Context context) {
		mDbHelper = new DatabaseHelper(context);
	}*/

	public DatabaseManager(Context ctx,int version){
		mDbHelper = new DatabaseHelper(ctx,version);
		mcontext = ctx;
	}
	public DatabaseManager(Context ctx ){
		mDbHelper = new DatabaseHelper(ctx);
		mcontext = ctx;
	}
	public SQLiteDatabase getReadableDatabase() {
		return mDbHelper.getReadableDatabase();
	}

	public long saveUser(String userNameString, String passwordString) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// 删除原来的信息
		Cursor cursor = db.query(DatabaseManager.TABLE_USER, new String[] {
				"user_name", "user_password" }, null, null, null, null, null);
		while (cursor.moveToNext()) {
			db.delete(TABLE_USER, "user_name = '" + cursor.getString(0) + "'",
					null);
		}

		if (userNameString == null) {
			Log.e(this.getClass().getSimpleName(), "saveUser fail!");
			return -1;
		}
		// 保存新的用户信息
		ContentValues values = new ContentValues();
		values.put("user_name", userNameString);
		values.put("user_password", passwordString);

		long ret = db.insertOrThrow(TABLE_USER, null, values);
		System.out.println("saveUser " + ret);
		db.close();

		return ret;
	}

	

	//设备
	/**
	 * 
	 * @param dev_nickname       设备名称
	 * @param dev_uid            设备UID
	 * @param dev_name
	 * @param dev_pwd
	 * @param view_acc           设备P2P 用户 admin
	 * @param view_pwd           设备P2P 密码
	 * @param event_notification 设备报警事件开关
	 * @param channel
	 * @param cameraStatus
	 * @param videoQuality
	 * @param serverDatabaseId
	 * @param cameraModel        设备 0->P2P
	 * @return
	 */
	public long addDevice(String dev_nickname, String dev_uid, String dev_name,
			String dev_pwd, String view_acc, String view_pwd,
			int event_notification, int channel,String cameraStatus, int videoQuality,String serverDatabaseId,int cameraModel) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("dev_nickname", dev_nickname);
		values.put("dev_uid", dev_uid);
		values.put("dev_name", dev_name);
		values.put("dev_pwd", dev_pwd);
		values.put("view_acc", view_acc);
		values.put("view_pwd", view_pwd);
		values.put("event_notification", event_notification);
		values.put("camera_channel", channel);
		values.put("cameraStatus", cameraStatus);
		values.put("ownerCameraId", serverDatabaseId);
		values.put("cameraModel", cameraModel);
		values.put("dev_videoQuality", videoQuality);

		long ret = db.insertOrThrow(TABLE_DEVICE, null, values);
		db.close();

		return ret;
	}

	//快照
	public long addSnapshot(String dev_uid, String file_path, long time) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("dev_uid", dev_uid);
		values.put("file_path", file_path);
		values.put("time", time);

		long ret = db.insertOrThrow(TABLE_SNAPSHOT, null, values);
		db.close();

		return ret;
	}
	
	public void updateServerDatabaseIdByUID(String dev_uid,
			String serverDatabaseId) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("ownerCameraId", serverDatabaseId);
		db.update(TABLE_DEVICE, values, "dev_uid = '" + dev_uid + "'", null);
		db.close();
	}

	/**
	 * 更新设备信息
	 * @param db_id
	 * @param dev_uid
	 * @param dev_nickname
	 * @param dev_name
	 * @param dev_pwd
	 * @param view_acc
	 * @param view_pwd
	 * @param event_notification
	 * @param channel
	 */
	public void updateDeviceInfoByDBID(long db_id, String dev_uid,
			String dev_nickname, String dev_name, String dev_pwd,
			String view_acc, String view_pwd, int event_notification,
			int channel,int cameraModel) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("dev_uid", dev_uid);
		values.put("dev_nickname", dev_nickname);
		values.put("dev_name", dev_name);
		values.put("dev_pwd", dev_pwd);
		values.put("view_acc", view_acc);
		values.put("view_pwd", view_pwd);
		values.put("event_notification", event_notification);
		values.put("camera_channel", channel);
		values.put("cameraModel", cameraModel);
		db.update(TABLE_DEVICE, values, "_id = '" + db_id + "'", null);
		db.close();
	}
	public void updateDeviceInfoByDBUID( String dev_uid,
									   String dev_nickname, String dev_name, String dev_pwd,
									   String view_acc, String view_pwd, int event_notification,
									   int channel,int cameraModel,int videoQuality) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("dev_nickname", dev_nickname);
		values.put("dev_name", dev_name);
		values.put("dev_pwd", dev_pwd);
		values.put("view_acc", view_acc);
		values.put("view_pwd", view_pwd);
		values.put("event_notification", event_notification);
		values.put("camera_channel", channel);
		values.put("cameraModel", cameraModel);
		values.put("dev_videoQuality", videoQuality);
		db.update(TABLE_DEVICE, values, "dev_uid = '" + dev_uid + "'", null);
		db.close();
	}
	public void updateDeviceNameByDBUID( String dev_uid,
										 String dev_nickname) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("dev_nickname", dev_nickname);
		db.update(TABLE_DEVICE, values, "dev_uid = '" + dev_uid + "'", null);
		db.close();
	}
	public void updateDeviceAskFormatSDCardByUID(String dev_uid,
			boolean askOrNot) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("ask_format_sdcard", askOrNot ? 1 : 0);
		db.update(TABLE_DEVICE, values, "dev_uid = '" + dev_uid + "'", null);
		db.close();
	}

	public void updateDeviceChannelByUID(String dev_uid, int channelIndex) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("camera_channel", channelIndex);
		db.update(TABLE_DEVICE, values, "dev_uid = '" + dev_uid + "'", null);
		db.close();
	}

	public void updateDeviceSnapshotByUID(String dev_uid, Bitmap snapshot) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("snapshot", getByteArrayFromBitmap(snapshot));
		db.update(TABLE_DEVICE, values, "dev_uid = '" + dev_uid + "'", null);
		db.close();
	}

	public void updateDeviceSnapshotByUID(String dev_uid, byte[] snapshot) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("snapshot", snapshot);
		db.update(TABLE_DEVICE, values, "dev_uid = '" + dev_uid + "'", null);
		db.close();
	}
	
	public Bitmap getSnapshotByUID(String dev_uid) {

		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor cursor = db.query(DatabaseManager.TABLE_DEVICE, new String[] {
				"_id", "dev_nickname", "dev_uid", "dev_name", "dev_pwd",
				"view_acc", "view_pwd", "event_notification", "camera_channel",
				"snapshot", "ask_format_sdcard", "cameraStatus",
				"dev_videoQuality", "ownerCameraId" }, null, null, null, null, "_id LIMIT "
				+ TwsDataValue.CAMERA_MAX_LIMITS);
		
		while (cursor.moveToNext()) {

			long db_id = cursor.getLong(cursor.getColumnIndex("_id"));

			String uidString = cursor.getString(cursor.getColumnIndex("dev_uid"));
			if (uidString.equals(dev_uid)) {
				byte[] bytsSnapshot = cursor.getBlob(cursor
						.getColumnIndex("snapshot"));
				Bitmap snapshot = (bytsSnapshot != null && bytsSnapshot.length > 0) ? DatabaseManager
						.getBitmapFromByteArray(bytsSnapshot) : null;
						
				return snapshot;
			}
		}
		return null;
	}
	

	/**
	 * 删除设备
	 * @param dev_uid
	 */
	public void removeDeviceByUID(String dev_uid) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete(TABLE_DEVICE, "dev_uid = '" + dev_uid + "'", null);
		db.close();
	}

	public void removeSnapshotByUID(String dev_uid) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		db.delete(TABLE_SNAPSHOT, "dev_uid = '" + dev_uid + "'", null);
		db.close();
	}

	public static byte[] getByteArrayFromBitmap(Bitmap bitmap) {

		if (bitmap != null && !bitmap.isRecycled()) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bitmap.compress(CompressFormat.PNG, 0, bos);
			return bos.toByteArray();
		} else {
			return null;
		}
	}

	public static BitmapFactory.Options getBitmapOptions(int scale) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;
		options.inInputShareable = true;
		options.inSampleSize = scale;

		try {
			BitmapFactory.Options.class.getField("inNativeAlloc").setBoolean(
					options, true);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return options;
	}

	public static Bitmap getBitmapFromByteArray(byte[] byts) {

		InputStream is = new ByteArrayInputStream(byts);
		return BitmapFactory.decodeStream(is, null, getBitmapOptions(2));
	}

	public long addSearchHistory(String dev_uid, int eventType,
			long start_time, long stop_time) {

		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("dev_uid", dev_uid);
		values.put("search_event_type", eventType);
		values.put("search_start_time", start_time);
		values.put("search_stop_time", stop_time);

		long ret = db.insertOrThrow(TABLE_SEARCH_HISTORY, null, values);
		db.close();

		return ret;
	}

	public long updateDeviceVideoRatio(String dev_uid,float ratio){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("dev_value", ratio+"");
        long ret = -1;
        ret = db.update(TABLE_DICTION, values, "dev_uid = '" + dev_uid + "' and dev_key='DEVICE_VIDEO_RATIO'", null);
        if(ret <= 0){
            values.put("dev_uid", dev_uid);
            values.put("dev_key", "DEVICE_VIDEO_RATIO");
            ret = db.insertOrThrow(TABLE_DICTION, null, values);
        }
        db.close();
        return ret;
    }

    public float getDeviceVideoRatio(String dev_uid){
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] selectionArgs = {dev_uid,"DEVICE_VIDEO_RATIO"};
        Cursor cursor = db.query(DatabaseManager.TABLE_DICTION, new String[] {
                "dev_value" }, "dev_uid=? and dev_key=?",selectionArgs, null, null, null);
		float ratio = 0;
        while (cursor.moveToNext()) {
            ratio = cursor.getFloat(cursor.getColumnIndex("dev_value"));
            break;
        }
        cursor.close();
        db.close();
        return ratio;
    }




	class DatabaseHelper extends SQLiteOpenHelper {

		private static final String DB_FILE = "IOTCamViewer.db";
		private static final int DB_VERSION = 9;

		static final String SQLCMD_CREATE_TABLE_USER = "CREATE TABLE "
				+ TABLE_USER + "("
				+ "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
				+ "user_name			NVARCHAR(50) NULL, "
				+ "user_password			NVARCHAR(50) NULL" + ");";

		static final String SQLCMD_CREATE_TABLE_DEVICE = "CREATE TABLE "
				+ TABLE_DEVICE + "("
				+ "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
				+ "dev_nickname			NVARCHAR(30) NULL, "
				+ "dev_uid				VARCHAR(20) NULL, "
				+ "dev_name				VARCHAR(30) NULL, "
				+ "dev_pwd				VARCHAR(30) NULL, "
				+ "view_acc				VARCHAR(30) NULL, "
				+ "view_pwd				VARCHAR(30) NULL, "
				+ "event_notification 	INTEGER, "
				+ "ask_format_sdcard		INTEGER," 
				+ "camera_channel			INTEGER, "
				+ "snapshot				BLOB, " 
				+ "cameraStatus			NVARCHAR(10) NULL, "
				+ "ownerCameraId			NVARCHAR(10) NULL, "
				+ "dev_videoQuality			INTEGER, "
				+ "cameraModel              INTEGER"
				+ ");";

		static final String SQLCMD_CREATE_TABLE_SEARCH_HISTORY = "CREATE TABLE "
				+ TABLE_SEARCH_HISTORY
				+ "("
				+ "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
				+ "dev_uid			VARCHAR(20) NULL, "
				+ "search_event_type	INTEGER, "
				+ "search_start_time	INTEGER, "
				+ "search_stop_time	INTEGER" + ");";
		static  final  String SQLCMD_CREATE_TABLE_DICTION = "CREATE TABLE "
				+ TABLE_DICTION
				+ "("
				+ "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
				+ "dev_uid			VARCHAR(20) NULL, "
				+ "dev_key	VARCHAR(20) NOT NULL, "
				+ "dev_value	VARCHAR(20) NULL" + ");";

		static final String SQLCMD_CREATE_TABLE_SNAPSHOT = "CREATE TABLE "
				+ TABLE_SNAPSHOT + "("
				+ "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
				+ "dev_uid			VARCHAR(20) NULL, " + "file_path			VARCHAR(80), "
				+ "time				INTEGER" + ");";

		static final String SQLCMD_DROP_TABLE_USER = "drop table if exists "
				+ TABLE_USER + ";";

		static final String SQLCMD_DROP_TABLE_DEVICE = "drop table if exists "
				+ TABLE_DEVICE + ";";


		static final String SQLCMD_DROP_TABLE_DICTION = "drop table if exists "
				+ TABLE_DICTION + ";";

		static final String SQLCMD_DROP_TABLE_SEARCH_HISTORY = "drop table if exists "
				+ TABLE_SEARCH_HISTORY + ";";

		static final String SQLCMD_DROP_TABLE_SNAPSHOT = "drop table if exists "
				+ TABLE_SNAPSHOT + ";";
		
		public DatabaseHelper(Context context) {
			super(context, DB_FILE, null, DB_VERSION);
		}
		
		public DatabaseHelper(Context context,int version) {
			super(context, DB_FILE, null, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SQLCMD_CREATE_TABLE_USER);
			db.execSQL(SQLCMD_CREATE_TABLE_DEVICE);
			db.execSQL(SQLCMD_CREATE_TABLE_SEARCH_HISTORY);
			db.execSQL(SQLCMD_CREATE_TABLE_SNAPSHOT);
			db.execSQL(SQLCMD_CREATE_TABLE_DICTION);
			
			//tank
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //更新版本需要进行的操作
            //	db.execSQL(SQLCMD_DROP_TABLE_USER);
            //	db.execSQL(SQLCMD_DROP_TABLE_DEVICE);
            //	db.execSQL(SQLCMD_DROP_TABLE_SEARCH_HISTORY);
            //	db.execSQL(SQLCMD_DROP_TABLE_SNAPSHOT);
            //	db.execSQL(SQLCMD_DROP_TABLE_DICTION);
			onCreate(db);
			
		}

	}
}

