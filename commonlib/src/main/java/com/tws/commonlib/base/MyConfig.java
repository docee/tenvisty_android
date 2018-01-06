package com.tws.commonlib.base;

import android.content.Context;
import android.graphics.Bitmap;

import com.tws.commonlib.App;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2017/9/19.
 */

public class MyConfig {
     static{
         try {
             String pkName = App.GetApp().getPackageName();
             Class dbAdapter = Class.forName(pkName + ".InitConfig");
             Constructor<?> constructor = dbAdapter.getConstructor(Context.class);
             Object obj;
             obj = constructor.newInstance(App.GetApp());
             Method method0 = dbAdapter.getMethod("init");
             method0.invoke(obj);

         } catch (InstantiationException e) {

             e.printStackTrace();
         } catch (IllegalAccessException e) {

             e.printStackTrace();
         } catch (SecurityException e) {

             e.printStackTrace();
         } catch (NoSuchMethodException e) {

             e.printStackTrace();
         } catch (IllegalArgumentException e) {

             e.printStackTrace();
         } catch (InvocationTargetException e) {

             e.printStackTrace();
         } catch (ClassNotFoundException e) {

             e.printStackTrace();
         }
    }

    public static boolean isHasAccount() {
        return hasAccount;
    }

    public static void setHasAccount(boolean hasAccount) {
        MyConfig.hasAccount = hasAccount;
    }

    private  static  boolean hasAccount;

    public static String getAppName() {
        return appName;
    }

    public static void setAppName(String appName) {
        MyConfig.appName = appName;
    }

    private  static  String appName;

    private  static  boolean hasCheckForUpdate;

    public static boolean isHasCheckForUpdate() {
        return hasCheckForUpdate;
    }

    public static void setHasCheckForUpdate(boolean hasCheckForUpdate) {
        MyConfig.hasCheckForUpdate = hasCheckForUpdate;
    }

    public static String getAppCheckForUpdateName() {
        return appCheckForUpdateName;
    }

    public static void setAppCheckForUpdateName(String appCheckForUpdateName) {
        MyConfig.appCheckForUpdateName = appCheckForUpdateName;
    }

    private static String appCheckForUpdateName;

    public static String getApkName() {
        return apkName;
    }

    public static void setApkName(String apkName) {
        MyConfig.apkName = apkName;
    }

    private static  String apkName;

    public static boolean isStrictPwd() {
        return strictPwd;
    }

    public static void setStrictPwd(boolean strictPwd) {
        MyConfig.strictPwd = strictPwd;
    }

    private  static  boolean strictPwd;

    public static int getAppIconSource() {
        return appIconSource;
    }

    public static void setAppIconSource(int appIconSource) {
        MyConfig.appIconSource = appIconSource;
    }

    private  static  int appIconSource;

    private  static  boolean showShoppingPage;

    public static boolean isShowShoppingPage() {
        return showShoppingPage;
    }

    public static void setShowShoppingPage(boolean showShoppingPage) {
        MyConfig.showShoppingPage = showShoppingPage;
    }

    public static String getPackageName() {
        return App.getContext().getPackageName();
    }


    public static String getFolderName() {
        return folderName.toLowerCase();
    }

    public static void setFolderName(String folderName) {
        MyConfig.folderName = folderName;
    }

    private  static  String folderName;

    public static int getP2pInitState() {
        return p2pInitState;
    }

    public static void setP2pInitState(int p2pInitState) {
        MyConfig.p2pInitState = p2pInitState;
    }
    //0：未初始化 1：初始化中 2：已初始化
    private static  int p2pInitState;

    public static int[] getIntroImg() {
        return introImg;
    }

    public static void setIntroImg(int[] introImg) {
        MyConfig.introImg = introImg;
    }

    private static  int[] introImg;


    public static int getSplashImgSrc() {
        return splashImgSrc;
    }

    public static void setSplashImgSrc(int splashImgSrc) {
        MyConfig.splashImgSrc = splashImgSrc;
    }

    private  static  int splashImgSrc;


    public static String getWirelessInstallHelpUrl() {
        return wirelessInstallHelpUrl;
    }

    public static void setWirelessInstallHelpUrl(String wirelessInstallHelpUrl) {
        MyConfig.wirelessInstallHelpUrl = wirelessInstallHelpUrl;
    }

    private  static  String wirelessInstallHelpUrl;

    public static String getUserHelpUrl() {
        return userHelpUrl;
    }

    public static void setUserHelpUrl(String userHelpUrl) {
        MyConfig.userHelpUrl = userHelpUrl;
    }

    private  static  String userHelpUrl;

    public static String getPrivacyUrl() {
        return privacyUrl;
    }

    public static void setPrivacyUrl(String privacyUrl) {
        MyConfig.privacyUrl = privacyUrl;
    }

    private  static  String privacyUrl;

    //UM push
    public static String getUmPushAppKey() {
        return umPushAppKey;
    }

    public static void setUmPushAppKey(String umPushAppKey) {
        MyConfig.umPushAppKey = umPushAppKey;
    }

    private static String umPushAppKey;

    private  static  String umPushAppSecret;

    public static String getUmPushAppSecret() {
        return umPushAppSecret;
    }

    public static void setUmPushAppSecret(String umPushAppSecret) {
        MyConfig.umPushAppSecret = umPushAppSecret;
    }
    //    /**
//     * logo图标的资源id
//     */
//    private static int logoResId;
//
//
//    private static int loginTitleLogoResId;
//
//    /**
//     * app name
//     */
//    private static String appName;
//
//    /**
//     * apkName 用于软件升级检测以及快照及本地录像保存目录
//     */
//    private static String apkName;
//
//    /**
//     * 创建文件夹的目录名
//     */
//    private static String folderName;
//
//
//    /**
//     * 检测升级所需的文件名
//     */
//    private static String appCheckForUpdateName;
//
//    /**
//     * 购物界面链接
//     */
//    private static String shoppingUrl;
//
//    /**
//     * 分享时所带的连接
//     */
//    private static String shareUrl;
//
//    /**
//     * app logo
//     */
//    private static Bitmap appLogo;
//
//	/* gcm sender id */
//
//    private static String gcm_defaultSenderId;
//    /**
//     * 是否强制更改密码
//     */
//    private static boolean isStrictPwd;
//
//    //添加设备时是否有型号选择
//    private static boolean hasModelSelect;
//    //是否有意见反馈功能
//    private static boolean hasFeedback;
//    //是否有登录账号
//    private static boolean hasAccount;
//    //是否有一键配WIFI功能
//    private static int smartLinkType;
//    //是否网络连接切换检测
//    private static boolean hasNetworkLinkCheck;
//
//    public enum DEVICE_MODEL_LIST_STYLE {
//        MODEL_TENVIS,
//        MODEL_WEIJIA,
//        MODEL_NONE
//    }
//
//    /**
//     * 添加摄像机界面设备类型选择：
//     * weijia:
//     * new tenvis :摄像机model
//     * life online :无
//     */
//    private static DEVICE_MODEL_LIST_STYLE deviceModeListStyle;
//
//
//    /**
//     * 是否具有购物界面
//     */
//    private static boolean isShowShoppingPager;
//
//    /**
//     * 是否显示设备厂商及版本信息
//     */
//    private static boolean isShowP2PDeviceInfo;
//
//    /**
//     * 是否具有检测升级的功能
//     */
//    private static boolean hasCheckForUpdate;
//
//
//    /**
//     * 是否具有分享照片的功能
//     */
//    private static boolean hasSharePhoto;
//
//
//
//    public static String getFolderName() {
//        return folderName;
//    }
//
//    public static void setFolderName(String folderName) {
//        MyConfig.folderName = folderName;
//    }
//
//    public static String getAppCheckForUpdateName() {
//        return appCheckForUpdateName;
//    }
//
//    public static void setAppCheckForUpdateName(String appCheckForUpdateName) {
//        MyConfig.appCheckForUpdateName = appCheckForUpdateName;
//    }
//
//    public static String getAppName() {
//        return appName;
//    }
//
//    public static void setAppName(String appName) {
//        MyConfig.appName = appName;
//    }
//
//    public static String getApkName() {
//        return apkName;
//    }
//
//    public static void setApkName(String apkName) {
//        MyConfig.apkName = apkName;
//    }
//
//    public static String getShoppingUrl() {
//        return shoppingUrl;
//    }
//
//    public static void setShoppingUrl(String shoppingUrl) {
//        MyConfig.shoppingUrl = shoppingUrl;
//    }
//
//    public static Bitmap getAppLogo() {
//        return appLogo;
//    }
//
//    public static void setAppLogo(Bitmap appLogo) {
//        MyConfig.appLogo = appLogo;
//    }
//
//    public static boolean isStrictPwd() {
//        return isStrictPwd;
//    }
//
//    public static void setStrictPwd(boolean isStrictPwd) {
//        MyConfig.isStrictPwd = isStrictPwd;
//    }
//
//
//
//    public static DEVICE_MODEL_LIST_STYLE getDeviceModeListStyle() {
//        return deviceModeListStyle;
//    }
//
//    public static void setDeviceModeListStyle(
//            DEVICE_MODEL_LIST_STYLE deviceModeListStyle) {
//        MyConfig.deviceModeListStyle = deviceModeListStyle;
//    }
//
//    public static boolean isShowShoppingPager() {
//        return isShowShoppingPager;
//    }
//
//    public static void setShowShoppingPager(boolean isShowShoppingPager) {
//        MyConfig.isShowShoppingPager = isShowShoppingPager;
//    }
//
//    public static boolean isShowP2PDeviceInfo() {
//        return isShowP2PDeviceInfo;
//    }
//
//    public static void setShowP2PDeviceInfo(boolean isShowP2PDeviceInfo) {
//        MyConfig.isShowP2PDeviceInfo = isShowP2PDeviceInfo;
//    }
//
//    public static boolean isHasCheckForUpdate() {
//        return hasCheckForUpdate;
//    }
//
//    public static void setHasCheckForUpdate(boolean hasCheckForUpdate) {
//        MyConfig.hasCheckForUpdate = hasCheckForUpdate;
//    }
//
//    public static boolean isHasSharePhoto() {
//        return hasSharePhoto;
//    }
//
//    public static void setHasSharePhoto(boolean hasSharePhoto) {
//        MyConfig.hasSharePhoto = hasSharePhoto;
//    }
//
//    public static int getLogoResId() {
//        return logoResId;
//    }
//
//    public static void setLogoResId(int logoResId) {
//        MyConfig.logoResId = logoResId;
//    }
//
//    public static String getShareUrl() {
//        return shareUrl;
//    }
//
//    public static void setShareUrl(String shareUrl) {
//        MyConfig.shareUrl = shareUrl;
//    }
//
//    public static int getLoginTitleLogoResId() {
//        return loginTitleLogoResId;
//    }
//
//    public static void setLoginTitleLogoResId(int loginTitleLogoResId) {
//        MyConfig.loginTitleLogoResId = loginTitleLogoResId;
//    }
//
//    public static String getGCMDefaultSenderID(){
//        return MyConfig.gcm_defaultSenderId;
//    }
//
//    public static void setGCMDefaultSenderID(String v){
//        MyConfig.gcm_defaultSenderId = v;
//    }
//
//    public static boolean getHasModelSelect(){
//        return hasModelSelect;
//    }
//
//    public static void setHasModelSelect(boolean v){
//        MyConfig.hasModelSelect = v;
//    }
//
//    public static boolean getHasFeedback(){
//        return hasFeedback;
//    }
//
//    public static void setHasFeedback(boolean v){
//        MyConfig.hasFeedback = v;
//    }
//    public static boolean getHasAccount(){
//        return hasAccount;
//    }
//
//    public static void setHasAccount(boolean v){
//        MyConfig.hasAccount = v;
//    }
//    public static int getSmartLinkType(){
//        return smartLinkType;
//    }
//
//    public static void setSmartLinkType(int v){
//        MyConfig.smartLinkType = v;
//    }
//    public static boolean getHasNetworkLinkCheck(){
//        return hasNetworkLinkCheck;
//    }
//
//    public static void setHasNetworkLinkCheck(boolean v){
//        MyConfig.hasNetworkLinkCheck = v;
//    }
}
