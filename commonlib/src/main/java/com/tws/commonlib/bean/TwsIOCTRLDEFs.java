package com.tws.commonlib.bean;

/**
 * Created by Administrator on 2018/1/7.
 */

public class TwsIOCTRLDEFs {

    /* AVAPIs IOCTRL Message Type */
    public static final int IOTYPE_USER_IPCAM_START = 0x901FF;
    public static final int IOTYPE_USER_IPCAM_STOP = 0x902FF;

    public static final int IOTYPE_USER_IPCAM_AUDIOSTART = 0x90300;
    public static final int IOTYPE_USER_IPCAM_AUDIOSTOP = 0x90301;

    public static final int IOTYPE_USER_IPCAM_SPEAKERSTART = 0x90350;
    public static final int IOTYPE_USER_IPCAM_SPEAKERSTOP = 0x90351;

    public static final int IOTYPE_USER_IPCAM_SETSTREAMCTRL_REQ = 0x90320;
    public static final int IOTYPE_USER_IPCAM_SETSTREAMCTRL_RESP = 0x90321;
    public static final int IOTYPE_USER_IPCAM_GETSTREAMCTRL_REQ = 0x90322;
    public static final int IOTYPE_USER_IPCAM_GETSTREAMCTRL_RESP = 0x90323;

    public static final int IOTYPE_USER_IPCAM_SETMOTIONDETECT_REQ = 0x90324;
    public static final int IOTYPE_USER_IPCAM_SETMOTIONDETECT_RESP = 0x90325;
    public static final int IOTYPE_USER_IPCAM_GETMOTIONDETECT_REQ = 0x90326;
    public static final int IOTYPE_USER_IPCAM_GETMOTIONDETECT_RESP = 0x90327;

    public static final int IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ = 0x90328;
    public static final int IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_RESP = 0x90329;

    public static final int IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ = 0x9032A;
    public static final int IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_RESP = 0x9032B;

    public static final int IOTYPE_USER_IPCAM_DEVINFO_REQ = 0x90330;
    public static final int IOTYPE_USER_IPCAM_DEVINFO_RESP = 0x90331;

    public static final int IOTYPE_USER_IPCAM_SETPASSWORD_REQ = 0x90332;
    public static final int IOTYPE_USER_IPCAM_SETPASSWORD_RESP = 0x90333;

    public static final int IOTYPE_USER_IPCAM_LISTWIFIAP_REQ = 0x90340;
    public static final int IOTYPE_USER_IPCAM_LISTWIFIAP_RESP = 0x90341;
    public static final int IOTYPE_USER_IPCAM_SETWIFI_REQ = 0x90342;
    public static final int IOTYPE_USER_IPCAM_SETWIFI_RESP = 0x90343;
    public static final int IOTYPE_USER_IPCAM_GETWIFI_REQ = 0x90344;
    public static final int IOTYPE_USER_IPCAM_GETWIFI_RESP = 0x90345;
    public static final int IOTYPE_USER_IPCAM_SETWIFI_REQ_2 = 0x90346;
    public static final int IOTYPE_USER_IPCAM_GETWIFI_RESP_2 = 0x90347;

    public static final int IOTYPE_USER_IPCAM_SETRECORD_REQ = 0x90310;
    public static final int IOTYPE_USER_IPCAM_SETRECORD_RESP = 0x90311;
    public static final int IOTYPE_USER_IPCAM_GETRECORD_REQ = 0x90312;
    public static final int IOTYPE_USER_IPCAM_GETRECORD_RESP = 0x90313;

    public static final int IOTYPE_USER_IPCAM_SETRCD_DURATION_REQ = 0x90314;
    public static final int IOTYPE_USER_IPCAM_SETRCD_DURATION_RESP = 0x90315;
    public static final int IOTYPE_USER_IPCAM_GETRCD_DURATION_REQ = 0x90316;
    public static final int IOTYPE_USER_IPCAM_GETRCD_DURATION_RESP = 0x90317;

    public static final int IOTYPE_USER_IPCAM_LISTEVENT_REQ = 0x90318;
    public static final int IOTYPE_USER_IPCAM_LISTEVENT_RESP = 0x90319;

    public static final int IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL = 0x9031A;
    public static final int IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL_RESP = 0x9031B;

    public static final int IOTYPE_USER_IPCAM_GET_EVENTCONFIG_REQ = 0x90400;
    public static final int IOTYPE_USER_IPCAM_GET_EVENTCONFIG_RESP = 0x90401;
    public static final int IOTYPE_USER_IPCAM_SET_EVENTCONFIG_REQ = 0x90402;
    public static final int IOTYPE_USER_IPCAM_SET_EVENTCONFIG_RESP = 0x90403;

    public static final int IOTYPE_USER_IPCAM_SET_ENVIRONMENT_REQ = 0x90360;
    public static final int IOTYPE_USER_IPCAM_SET_ENVIRONMENT_RESP = 0x90361;
    public static final int IOTYPE_USER_IPCAM_GET_ENVIRONMENT_REQ = 0x90362;
    public static final int IOTYPE_USER_IPCAM_GET_ENVIRONMENT_RESP = 0x90363;

    public static final int IOTYPE_USER_IPCAM_SET_VIDEOMODE_REQ = 0x90370;
    public static final int IOTYPE_USER_IPCAM_SET_VIDEOMODE_RESP = 0x90371;
    public static final int IOTYPE_USER_IPCAM_GET_VIDEOMODE_REQ = 0x90372;
    public static final int IOTYPE_USER_IPCAM_GET_VIDEOMODE_RESP = 0x90373;

    public static final int IOTYPE_USER_IPCAM_FORMATEXTSTORAGE_REQ = 0x90380;
    public static final int IOTYPE_USER_IPCAM_FORMATEXTSTORAGE_RESP = 0x90381;

    public static final int IOTYPE_USER_IPCAM_PTZ_COMMAND = 0x91001;

    public static final int IOTYPE_USER_IPCAM_EVENT_REPORT = 0x91FFF;

    public static final int IOTYPE_USER_IPCAM_RECEIVE_FIRST_IFRAME = 0x91002;    // Send from client, used to talk to device that

    public static final int IOTYPE_USER_IPCAM_GET_FLOWINFO_REQ = 0x90390;
    public static final int IOTYPE_USER_IPCAM_GET_FLOWINFO_RESP = 0x90391;
    public static final int IOTYPE_USER_IPCAM_CURRENT_FLOWINFO = 0x90392;

    public static final int IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ = 0x903A0;
    public static final int IOTYPE_USER_IPCAM_GET_TIMEZONE_RESP = 0x903A1;
    public static final int IOTYPE_USER_IPCAM_SET_TIMEZONE_REQ = 0x903B0;
    public static final int IOTYPE_USER_IPCAM_SET_TIMEZONE_RESP = 0x903B1;


    /* AVAPIs IOCTRL Event Type */
    public static final int AVIOCTRL_EVENT_ALL = 0x90000;
    public static final int AVIOCTRL_EVENT_MOTIONDECT = 0x90001;
    public static final int AVIOCTRL_EVENT_VIDEOLOST = 0x90002;
    public static final int AVIOCTRL_EVENT_IOALARM = 0x90003;
    public static final int AVIOCTRL_EVENT_MOTIONPASS = 0x90004;
    public static final int AVIOCTRL_EVENT_VIDEORESUME = 0x90005;
    public static final int AVIOCTRL_EVENT_IOALARMPASS = 0x90006;
    public static final int AVIOCTRL_EVENT_EXPT_REBOOT = 0x90010;
    public static final int AVIOCTRL_EVENT_SDFAULT = 0x90011;

    /* EVEENT SNAP REQUEST type */
    public static final int AVIOCTRL_EVENT_SNAP_RESP = 0x90022; // 门铃推送事件
    public static final int AVIOCTRL_EVENT_BELL_RING = 0x90023; // 响铃
    public static final int AVIOCTRL_EVENT_SNAP_NO_RING = 0x90026;// 离线图片传送

    /* AVAPIs IOCTRL Play Record Command */
    public static final int AVIOCTRL_RECORD_PLAY_PAUSE = 0x90000;
    public static final int AVIOCTRL_RECORD_PLAY_STOP = 0x90001;
    public static final int AVIOCTRL_RECORD_PLAY_STEPFORWARD = 0x90002;
    public static final int AVIOCTRL_RECORD_PLAY_STEPBACKWARD = 0x90003;
    public static final int AVIOCTRL_RECORD_PLAY_FORWARD = 0x90004;
    public static final int AVIOCTRL_RECORD_PLAY_BACKWARD = 0x90005;
    public static final int AVIOCTRL_RECORD_PLAY_SEEKTIME = 0x90006;
    public static final int AVIOCTRL_RECORD_PLAY_END = 0x90007;
    public static final int AVIOCTRL_RECORD_PLAY_START = 0x90010;

    // AVIOCTRL PTZ Command Value
    public static final int AVIOCTRL_PTZ_STOP = 0;
    public static final int AVIOCTRL_PTZ_UP = 1;
    public static final int AVIOCTRL_PTZ_DOWN = 2;
    public static final int AVIOCTRL_PTZ_LEFT = 3;
    public static final int AVIOCTRL_PTZ_LEFT_UP = 4;
    public static final int AVIOCTRL_PTZ_LEFT_DOWN = 5;
    public static final int AVIOCTRL_PTZ_RIGHT = 6;
    public static final int AVIOCTRL_PTZ_RIGHT_UP = 7;
    public static final int AVIOCTRL_PTZ_RIGHT_DOWN = 8;
    public static final int AVIOCTRL_PTZ_AUTO = 9;
    public static final int AVIOCTRL_PTZ_SET_POINT = 10;
    public static final int AVIOCTRL_PTZ_CLEAR_POINT = 11;
    public static final int AVIOCTRL_PTZ_GOTO_POINT = 12;
    public static final int AVIOCTRL_PTZ_SET_MODE_START = 13;
    public static final int AVIOCTRL_PTZ_SET_MODE_STOP = 14;
    public static final int AVIOCTRL_PTZ_MODE_RUN = 15;
    public static final int AVIOCTRL_PTZ_MENU_OPEN = 16;
    public static final int AVIOCTRL_PTZ_MENU_EXIT = 17;
    public static final int AVIOCTRL_PTZ_MENU_ENTER = 18;
    public static final int AVIOCTRL_PTZ_FLIP = 19;
    public static final int AVIOCTRL_PTZ_START = 20;

    public static final int AVIOCTRL_LENS_APERTURE_OPEN = 21;
    public static final int AVIOCTRL_LENS_APERTURE_CLOSE = 22;
    public static final int AVIOCTRL_LENS_ZOOM_IN = 23;
    public static final int AVIOCTRL_LENS_ZOOM_OUT = 24;
    public static final int AVIOCTRL_LENS_FOCAL_NEAR = 25;
    public static final int AVIOCTRL_LENS_FOCAL_FAR = 26;

    public static final int AVIOCTRL_AUTO_PAN_SPEED = 27;
    public static final int AVIOCTRL_AUTO_PAN_LIMIT = 28;
    public static final int AVIOCTRL_AUTO_PAN_START = 29;

    public static final int AVIOCTRL_PATTERN_START = 30;
    public static final int AVIOCTRL_PATTERN_STOP = 31;
    public static final int AVIOCTRL_PATTERN_RUN = 32;

    public static final int AVIOCTRL_SET_AUX = 33;
    public static final int AVIOCTRL_CLEAR_AUX = 34;
    public static final int AVIOCTRL_MOTOR_RESET_POSITION = 35;

    /* AVAPIs IOCTRL Quality Type */
    public static final int AVIOCTRL_QUALITY_UNKNOWN = 0x90000;
    public static final int AVIOCTRL_QUALITY_MAX = 0x90001;
    public static final int AVIOCTRL_QUALITY_HIGH = 0x90002;
    public static final int AVIOCTRL_QUALITY_MIDDLE = 0x90003;
    public static final int AVIOCTRL_QUALITY_LOW = 0x90004;
    public static final int AVIOCTRL_QUALITY_MIN = 0x90005;

    /* AVAPIs IOCTRL WiFi Mode */
    public static final int AVIOTC_WIFIAPMODE_ADHOC = 0x90000;
    public static final int AVIOTC_WIFIAPMODE_MANAGED = 0x90001;

    /* AVAPIs IOCTRL WiFi Enc Type */
    public static final int AVIOTC_WIFIAPENC_INVALID = 0x90000;
    public static final int AVIOTC_WIFIAPENC_NONE = 0x90001;
    public static final int AVIOTC_WIFIAPENC_WEP = 0x90002;
    public static final int AVIOTC_WIFIAPENC_WPA_TKIP = 0x90003;
    public static final int AVIOTC_WIFIAPENC_WPA_AES = 0x90004;
    public static final int AVIOTC_WIFIAPENC_WPA2_TKIP = 0x90005;
    public static final int AVIOTC_WIFIAPENC_WPA2_AES = 0x90006;

    /* AVAPIs IOCTRL Recording Type */
    public static final int AVIOTC_RECORDTYPE_OFF = 0x90000;
    public static final int AVIOTC_RECORDTYPE_FULLTIME = 0x90001;
    public static final int AVIOTC_RECORDTYPE_ALAM = 0x90002;
    public static final int AVIOTC_RECORDTYPE_MANUAL = 0x90003;

    public static final int AVIOCTRL_ENVIRONMENT_INDOOR_50HZ = 0x90000;
    public static final int AVIOCTRL_ENVIRONMENT_INDOOR_60HZ = 0x90001;
    public static final int AVIOCTRL_ENVIRONMENT_OUTDOOR = 0x90002;
    public static final int AVIOCTRL_ENVIRONMENT_NIGHT = 0x90003;

    /* AVIOCTRL VIDEO MODE */
    public static final int AVIOCTRL_VIDEOMODE_NORMAL = 0x90000;
    public static final int AVIOCTRL_VIDEOMODE_FLIP = 0x90001;
    public static final int AVIOCTRL_VIDEOMODE_MIRROR = 0x90002;
    public static final int AVIOCTRL_VIDEOMODE_FLIP_MIRROR = 0x90003;


    public static final int IOTYPE_USER_IPCAM_GET_ALARMLED_CONTRL_REQ = 0x940086;
    public static final int IOTYPE_USER_IPCAM_GET_ALARMLED_CONTRL_RESP = 0x940087;
    public static final int IOTYPE_USER_IPCAM_SET_ALARMLED_CONTRL_REQ = 0x940084;
    public static final int IOTYPE_USER_IPCAM_SET_ALARMLED_CONTRL_RESP = 0x940085;

    public static final int IOTYPE_USER_IPCAM_GET_OSD_ONOFF_REQ = 0x950023;
    public static final int IOTYPE_USER_IPCAM_GET_OSD_ONOFF_RESP = 0x950024;
    public static final int IOTYPE_USER_IPCAM_SET_OSD_ONOFF_REQ = 0x950021;
    public static final int IOTYPE_USER_IPCAM_SET_OSD_ONOFF_RESP = 0x950022;
    public static final int IOTYPE_USER_IPCAM_GET_AUSDOM_PIR_SENSITIVITY_REQ = 0x940072;
    public static final int IOTYPE_USER_IPCAM_GET_AUSDOM_PIR_SENSITIVITY_RESP = 0x940073;
    public static final int IOTYPE_USER_IPCAM_SET_AUSDOM_PIR_SENSITIVITY_REQ = 0x940070;
    public static final int IOTYPE_USER_IPCAM_SET_AUSDOM_PIR_SENSITIVITY_RESP = 0x940071;
    public static final int IOTYPE_USER_IPCAM_GET_ALARM_TIME_REQ = 0x950031;
    public static final int IOTYPE_USER_IPCAM_GET_ALARM_TIME_RESP = 0x950032;
    public static final int IOTYPE_USER_IPCAM_SET_ALARM_TIME_REQ = 0x950033;
    public static final int IOTYPE_USER_IPCAM_SET_ALARM_TIME_RESP = 0x950034;


    //user-defined cmd type
    //preset point operate
    public static final int IOTYPE_USER_IPCAM_GET_PRESET_LIST_REQ = 0x92001;
    public static final int IOTYPE_USER_IPCAM_GET_PRESET_LIST_RESP = 0x92002;

    public static final int IOTYPE_USER_IPCAM_SET_PRESET_POINT_REQ = 0x92003;
    public static final int IOTYPE_USER_IPCAM_SET_PRESET_POINT_RESP = 0x92004;

    public static final int IOTYPE_USER_IPCAM_OPR_PRESET_POINT_REQ = 0x92005;
    public static final int IOTYPE_USER_IPCAM_OPR_PRESET_POINT_RESP = 0x92006;

    //daylight saving time
//    public static final int IOTYPE_USER_IPCAM_GET_DST_REQ = 0x92007;
//    public static final int IOTYPE_USER_IPCAM_GET_DST_RESP = 0x92008;

//    public static final int IOTYPE_USER_IPCAM_SET_DST_REQ = 0x92009;
//    public static final int IOTYPE_USER_IPCAM_SET_DST_RESP = 0x9200A;

    //reboot
    public static final int IOTYPE_USER_IPCAM_REBOOT_REQ = 0x9200B;
    public static final int IOTYPE_USER_IPCAM_REBOOT_RESP = 0x9200C;

    //reset to default
    public static final int IOTYPE_USER_IPCAM_RESET_DEFAULT_REQ = 0x9200D;
    public static final int IOTYPE_USER_IPCAM_RESET_DEFAULT_RESP = 0x9200E;

    public static final int IOTYPE_USER_IPCAM_GET_UPRADE_URL_REQ = 0x92017;
    public static final int IOTYPE_USER_IPCAM_GET_UPRADE_URL_RESP = 0x92018;

    public static final int IOTYPE_USER_IPCAM_SET_UPRADE_REQ = 0x92019;
    public static final int IOTYPE_USER_IPCAM_SET_UPRADE_RESP = 0x92020;
    public static final int IOTYPE_USER_IPCAM_UPGRADE_STATUS = 0x92021;

    public static final int IOTYPE_USER_IPCAM_GET_FIRMWARE_INFO_REQ = 0x92022;
    public static final int IOTYPE_USER_IPCAM_GET_FIRMWARE_INFO_RESP = 0x92023;
    public static final int IOTYPE_USER_IPCAM_GET_TIME_INFO_REQ = 0x92024;
    public static final int IOTYPE_USER_IPCAM_GET_TIME_INFO_RESP = 0x92025;
    public static final int IOTYPE_USER_IPCAM_SET_TIME_INFO_REQ = 0x92026;
    public static final int IOTYPE_USER_IPCAM_SET_TIME_INFO_RESP = 0x92027;
    public static final int IOTYPE_USER_IPCAM_GET_ZONE_INFO_REQ = 0x92028;
    public static final int IOTYPE_USER_IPCAM_GET_ZONE_INFO_RESP = 0x92029;
    public static final int IOTYPE_USER_IPCAM_SET_ZONE_INFO_REQ = 0x9202A;
    public static final int IOTYPE_USER_IPCAM_SET_ZONE_INFO_RESP = 0x9202B;

    public static final int IOTYPE_USER_IPCAM_UPDATE_WIFI_STATUS = 0x9202C;

    //dropbox-------------------------------------------------------
    public static final int IOTYPE_USEREX_IPCAM_SET_DROPBOX_ACCESS_TOKEN_REQ = 0x94013;
    //Data:SMsgAVIoctrlExSetDropboxAccessTokenReq

    public static final int IOTYPE_USEREX_IPCAM_SET_DROPBOX_ACCESS_TOKEN_RESP = 0x94014;
    //Data: SMsgAVIoctrlExSetDropboxAccessTokenResp

    public static final int IOTYPE_USEREX_IPCAM_GET_DROPBOX_ACCESS_TOKEN_REQ = 0x94015;
    //Data:SMsgAVIoctrlExGetDropboxAccessTokenReq

    public static final int IOTYPE_USEREX_IPCAM_GET_DROPBOX_ACCESS_TOKEN_RESP = 0x94016;
    //Data: SMsgAVIoctrlExGetDropboxAccessTokenResp

    public static final int IOTYPE_USEREX_IPCAM_SET_NETSTORAGE_ENABLED_REQ = 0x94017;
    //Data: SMsgAVIoctrlExSetNetStorageEnabledReq

    public static final int IOTYPE_USEREX_IPCAM_SET_NETSTORAGE_ENABLED_RESP = 0x94018;
    //Data: SMsgAVIoctrlExSetNetStorageEnabledResp

    public static final int IOTYPE_USEREX_IPCAM_GET_NETSTORAGE_REQ = 0x94019;
    //Data: SMsgAVIoctrlExGetNetStorageReq

    public static final int IOTYPE_USEREX_IPCAM_GET_NETSTORAGE_RESP = 0x9401a;
    //Data: SMsgAVIoctrlExGetNetStorageResp

    public static final int IOTYPE_USEREX_IPCAM_SET_NETSTORAGE_REQ = 0x9401b;
    //Data: SMsgAVIoctrlExSetNetStorageReq

    public static final int IOTYPE_USEREX_IPCAM_SET_NETSTORAGE_RESP = 0x9401c;
    //Data: SMsgAVIoctrlExSetNetStorageResp


    /**
     * 以下为报警邮箱设置相关的接口
     */
    public static final int IOTYPE_USEREX_IPCAM_GET_SMTP_REQ = 0x94005;
    //Data: SMsgAVIoctrlExGetSmtpReq

    public static final int IOTYPE_USEREX_IPCAM_GET_SMTP_RESP = 0x94006;
    //Data: SmtpSetting

    public static final int IOTYPE_USEREX_IPCAM_SET_SMTP_REQ = 0x94007;
    //Data: SmtpSetting

    public static final int IOTYPE_USEREX_IPCAM_SET_SMTP_RESP = 0x94008;
    //Data: SMsgAVIoctrlExSetSmtpResp

    public static final int IOTYPE_USEREX_IPCAM_SEND_TEST_MAIL_REQ = 0x94009;
    //Data: SMsgAVIoctrlExSendMailReq
    public static final int IOTYPE_USEREX_IPCAM_SEND_TEST_MAIL_RESP = 0x9400A;
    //Data: SMsgAVIoctrlExSendMailResp

    public static final int IOTYPE_USEREX_IPCAM_GET_MAIL_STATUS_REQ = 0x9400B;
    //Data: SMsgAVIoctrlExGetMailStatusReq
    public static final int IOTYPE_USEREX_IPCAM_GET_MAIL_STATUS_RESP = 0x9400C;
    //Data: SMsgAVIoctrlExGetMailStatusResp

    /**
     * 以下为传感器相关的接口
     */
    public static final int IOTYPE_USEREX_IPCAM_GET_HUMITURE_REQ = 0x96001;
    //Data: SMsgAVIoctrlExGetSmtpReq

    public static final int IOTYPE_USEREX_IPCAM_GET_HUMITURE_RESP = 0x96002;

    //led相关
    public static final int IOTYPE_USER_IPCAM_SETALARMRING_REQ = 0x944E;
    public static final int IOTYPE_USER_IPCAM_SETALARMRING_RESP = 0x944F;
    public static final int IOTYPE_USER_IPCAM_GETALARMRING_REQ = 0x98030;
    //Data: SMsgAVIoctrlExGetSmtpReq

    public static final int IOTYPE_USER_IPCAM_GETALARMRING_RESP = 0x98031;
}
