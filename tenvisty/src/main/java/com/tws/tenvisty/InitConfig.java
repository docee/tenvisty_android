package com.tws.tenvisty;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.tws.commonlib.base.MyConfig;

/**
 * Created by Administrator on 2017/9/19.
 */

public class InitConfig {
    Context context;

    public InitConfig(Context context) {
        this.context = context;
    }

    public void init() {
//        boolean hasUpdate=context.getResources().getBoolean(R.bool.has_check_for_update) ;//鏄惁鏈夋娴嬪崌绾х殑鍔熻兘
//        boolean sharePhoto=context.getResources().getBoolean(R.bool.has_share_photo);//鏄惁鏈夌収鐗囧垎浜殑鍔熻兘
//        boolean showDeviceInfo=context.getResources().getBoolean(R.bool.is_show_p2p_deviceinfo);//鏄惁鏄剧ず璁惧鍘傚晢銆佺増鏈浉鍏充俊鎭?
//        boolean showShoppingPager=context.getResources().getBoolean(R.bool.is_show_shoppingpager);//鏄惁鏈夎喘鐗╃晫闈?
//        boolean strictPwd=context.getResources().getBoolean(R.bool.is_strict_pwd);//鏄惁寮哄埗鏇存敼瀵嗙爜
//
//        boolean hasModelSelect = context.getResources().getBoolean(R.bool.has_model_select);//娣诲姞鎽勫儚鏈烘椂鏄惁鏈夊瀷鍙烽?夋嫨
//        boolean hasFeedback = context.getResources().getBoolean(R.bool.has_feedback);//鎰忚鍙嶉
//        boolean hasAccount = context.getResources().getBoolean(R.bool.has_account);//鏄惁闇?瑕佽处鍙?
//        //鑾峰彇璧勬簮鏂囦欢
//        String appName=context.getResources().getString(R.string.app_name);//搴旂敤鍚?
//        String apkName=context.getResources().getString(R.string.app_name_apk);//apk鏂囦欢鍚?
//        String appCheckForUpdateName=context.getResources().getString(R.string.app_update_sever_name);//浠庢湇鍔″櫒妫?娴嬪崌绾х殑APP鍚嶇О
//        String folderName=context.getResources().getString(R.string.app_folder_name);//蹇収鎵?瀛樻斁鐨勭洰褰曞悕
//        String shoppingUrl=context.getResources().getString(R.string.shopping_url);//璐墿鐣岄潰鎵?杩炴帴鐨勫湴鍧?
//        Bitmap logoBitmap= BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);//
//        int logoResId= R.drawable.ic_launcher;//logo鍥剧墖鐨勮祫婧怚D
//        int loginLogoTitleResId= R.drawable.login_logo_title;//鐧诲綍妗嗕笂闈㈢殑logo鍥剧墖璧勬簮ID
//        String gcmDefaultSenderID = context.getResources().getString(R.string.gcm_defaultSenderId);
//
//        //璁剧疆鍔熻兘
//        MyConfig.setHasCheckForUpdate(hasUpdate);
//        MyConfig.setHasSharePhoto(sharePhoto);
//        MyConfig.setShowP2PDeviceInfo(showDeviceInfo);
//        MyConfig.setShowShoppingPager(showShoppingPager);
//        MyConfig.setStrictPwd(strictPwd);
//
//        MyConfig.setHasModelSelect(hasModelSelect);
//        MyConfig.setHasFeedback(hasFeedback);
//
//        //璁剧疆璧勬簮鏂囦欢
//        MyConfig.setAppName(appName);
//        MyConfig.setApkName(apkName);
//        MyConfig.setAppCheckForUpdateName(appCheckForUpdateName);
//        MyConfig.setFolderName(folderName);
//        MyConfig.setShoppingUrl(shoppingUrl);
//        MyConfig.setAppLogo(logoBitmap);
//        MyConfig.setLogoResId(logoResId);
//        MyConfig.setLoginTitleLogoResId(loginLogoTitleResId);
//        MyConfig.setShareUrl(shoppingUrl);
//
//        //璁剧疆娣诲姞璁惧鐣岄潰閫夋嫨璁惧鐨勭晫闈?
//        MyConfig.setDeviceModeListStyle(MyConfig.DEVICE_MODEL_LIST_STYLE.MODEL_TENVIS);
//        MyConfig.setGCMDefaultSenderID(gcmDefaultSenderID);
//        MyConfig.setHasAccount(hasAccount);
//
//        int smartLinkType = context.getResources().getInteger(R.integer.smartlink_type);//鏄惁闇?瑕佽处鍙?
//        boolean hasNetworkLinkCheck = context.getResources().getBoolean(R.bool.has_network_link_check);//鏄惁闇?瑕佽处鍙?
//        MyConfig.setHasNetworkLinkCheck(hasNetworkLinkCheck);
//        MyConfig.setSmartLinkType(smartLinkType);
        String appName = context.getResources().getString(R.string.app_name).replace(" ", "");
        MyConfig.setFolderName(appName);
        MyConfig.setAppName(appName);
        MyConfig.setStrictPwd(true);
        MyConfig.setIntroImg(new int[]{R.drawable.guide_background1, R.drawable.guide_background2, R.drawable.guide_background3});
        MyConfig.setSplashImgSrc(R.drawable.splash);
        MyConfig.setAppIconSource(R.drawable.ic_launcher);
        MyConfig.setWirelessInstallHelpUrl("http://p.webgoodcam.com/" + appName + "/touch/camerafaq/detail_wireless_install.html");
        MyConfig.setUserHelpUrl("http://p.webgoodcam.com/" + appName + "/userhelp.html");

        MyConfig.setPrivacyUrl("http://p.wificam.org/" + appName + "/ysxy.html");
        MyConfig.setUmPushAppKey("59a3be75677baa1239001b30");
        MyConfig.setUmPushAppSecret("23d61a7b29d62e6771796c805123cdbb");
    }
}
