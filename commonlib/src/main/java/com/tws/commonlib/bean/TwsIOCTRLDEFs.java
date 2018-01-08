package com.tws.commonlib.bean;

/**
 * Created by Administrator on 2018/1/7.
 */

public class TwsIOCTRLDEFs {

    /* AVAPIs IOCTRL Message Type */
    public static final int IOTYPE_USER_IPCAM_START = 0x01FF;
    public static final int IOTYPE_USER_IPCAM_STOP = 0x02FF;

    public static final int IOTYPE_USER_IPCAM_AUDIOSTART = 0x0300;
    public static final int IOTYPE_USER_IPCAM_AUDIOSTOP = 0x0301;

    public static final int IOTYPE_USER_IPCAM_SPEAKERSTART = 0x0350;
    public static final int IOTYPE_USER_IPCAM_SPEAKERSTOP = 0x0351;

    public static final int IOTYPE_USER_IPCAM_SETSTREAMCTRL_REQ = 0x0320;
    public static final int IOTYPE_USER_IPCAM_SETSTREAMCTRL_RESP = 0x0321;
    public static final int IOTYPE_USER_IPCAM_GETSTREAMCTRL_REQ = 0x0322;
    public static final int IOTYPE_USER_IPCAM_GETSTREAMCTRL_RESP = 0x0323;

    public static final int IOTYPE_USER_IPCAM_SETMOTIONDETECT_REQ = 0x0324;
    public static final int IOTYPE_USER_IPCAM_SETMOTIONDETECT_RESP = 0x0325;
    public static final int IOTYPE_USER_IPCAM_GETMOTIONDETECT_REQ = 0x0326;
    public static final int IOTYPE_USER_IPCAM_GETMOTIONDETECT_RESP = 0x0327;

    public static final int IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_REQ = 0x0328;
    public static final int IOTYPE_USER_IPCAM_GETSUPPORTSTREAM_RESP = 0x0329;

    public static final int IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_REQ = 0x032A;
    public static final int IOTYPE_USER_IPCAM_GETAUDIOOUTFORMAT_RESP = 0x032B;

    public static final int IOTYPE_USER_IPCAM_DEVINFO_REQ = 0x0330;
    public static final int IOTYPE_USER_IPCAM_DEVINFO_RESP = 0x0331;

    public static final int IOTYPE_USER_IPCAM_SETPASSWORD_REQ = 0x0332;
    public static final int IOTYPE_USER_IPCAM_SETPASSWORD_RESP = 0x0333;

    public static final int IOTYPE_USER_IPCAM_LISTWIFIAP_REQ = 0x0340;
    public static final int IOTYPE_USER_IPCAM_LISTWIFIAP_RESP = 0x0341;
    public static final int IOTYPE_USER_IPCAM_SETWIFI_REQ = 0x0342;
    public static final int IOTYPE_USER_IPCAM_SETWIFI_RESP = 0x0343;
    public static final int IOTYPE_USER_IPCAM_GETWIFI_REQ = 0x0344;
    public static final int IOTYPE_USER_IPCAM_GETWIFI_RESP = 0x0345;
    public static final int IOTYPE_USER_IPCAM_SETWIFI_REQ_2 = 0x0346;
    public static final int IOTYPE_USER_IPCAM_GETWIFI_RESP_2 = 0x0347;

    public static final int IOTYPE_USER_IPCAM_SETRECORD_REQ = 0x0310;
    public static final int IOTYPE_USER_IPCAM_SETRECORD_RESP = 0x0311;
    public static final int IOTYPE_USER_IPCAM_GETRECORD_REQ = 0x0312;
    public static final int IOTYPE_USER_IPCAM_GETRECORD_RESP = 0x0313;

    public static final int IOTYPE_USER_IPCAM_SETRCD_DURATION_REQ = 0x0314;
    public static final int IOTYPE_USER_IPCAM_SETRCD_DURATION_RESP = 0x0315;
    public static final int IOTYPE_USER_IPCAM_GETRCD_DURATION_REQ = 0x0316;
    public static final int IOTYPE_USER_IPCAM_GETRCD_DURATION_RESP = 0x0317;

    public static final int IOTYPE_USER_IPCAM_LISTEVENT_REQ = 0x0318;
    public static final int IOTYPE_USER_IPCAM_LISTEVENT_RESP = 0x0319;

    public static final int IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL = 0x031A;
    public static final int IOTYPE_USER_IPCAM_RECORD_PLAYCONTROL_RESP = 0x031B;

    public static final int IOTYPE_USER_IPCAM_GET_EVENTCONFIG_REQ = 0x0400;
    public static final int IOTYPE_USER_IPCAM_GET_EVENTCONFIG_RESP = 0x0401;
    public static final int IOTYPE_USER_IPCAM_SET_EVENTCONFIG_REQ = 0x0402;
    public static final int IOTYPE_USER_IPCAM_SET_EVENTCONFIG_RESP = 0x0403;

    public static final int IOTYPE_USER_IPCAM_SET_ENVIRONMENT_REQ = 0x0360;
    public static final int IOTYPE_USER_IPCAM_SET_ENVIRONMENT_RESP = 0x0361;
    public static final int IOTYPE_USER_IPCAM_GET_ENVIRONMENT_REQ = 0x0362;
    public static final int IOTYPE_USER_IPCAM_GET_ENVIRONMENT_RESP = 0x0363;

    public static final int IOTYPE_USER_IPCAM_SET_VIDEOMODE_REQ = 0x0370;
    public static final int IOTYPE_USER_IPCAM_SET_VIDEOMODE_RESP = 0x0371;
    public static final int IOTYPE_USER_IPCAM_GET_VIDEOMODE_REQ = 0x0372;
    public static final int IOTYPE_USER_IPCAM_GET_VIDEOMODE_RESP = 0x0373;

    public static final int IOTYPE_USER_IPCAM_FORMATEXTSTORAGE_REQ = 0x380;
    public static final int IOTYPE_USER_IPCAM_FORMATEXTSTORAGE_RESP = 0x381;

    public static final int IOTYPE_USER_IPCAM_PTZ_COMMAND = 0x1001;

    public static final int IOTYPE_USER_IPCAM_EVENT_REPORT = 0x1FFF;

    public static final int IOTYPE_USER_IPCAM_RECEIVE_FIRST_IFRAME = 0x1002;    // Send from client, used to talk to device that

    public static final int IOTYPE_USER_IPCAM_GET_FLOWINFO_REQ = 0x0390;
    public static final int IOTYPE_USER_IPCAM_GET_FLOWINFO_RESP = 0x0391;
    public static final int IOTYPE_USER_IPCAM_CURRENT_FLOWINFO = 0x0392;

    public static final int IOTYPE_USER_IPCAM_GET_TIMEZONE_REQ = 0x3A0;
    public static final int IOTYPE_USER_IPCAM_GET_TIMEZONE_RESP = 0x3A1;
    public static final int IOTYPE_USER_IPCAM_SET_TIMEZONE_REQ = 0x3B0;
    public static final int IOTYPE_USER_IPCAM_SET_TIMEZONE_RESP = 0x3B1;


    /* AVAPIs IOCTRL Event Type */
    public static final int AVIOCTRL_EVENT_ALL = 0x00;
    public static final int AVIOCTRL_EVENT_MOTIONDECT = 0x01;
    public static final int AVIOCTRL_EVENT_VIDEOLOST = 0x02;
    public static final int AVIOCTRL_EVENT_IOALARM = 0x03;
    public static final int AVIOCTRL_EVENT_MOTIONPASS = 0x04;
    public static final int AVIOCTRL_EVENT_VIDEORESUME = 0x05;
    public static final int AVIOCTRL_EVENT_IOALARMPASS = 0x06;
    public static final int AVIOCTRL_EVENT_EXPT_REBOOT = 0x10;
    public static final int AVIOCTRL_EVENT_SDFAULT = 0x11;

    /* EVEENT SNAP REQUEST type */
    public static final int AVIOCTRL_EVENT_SNAP_RESP = 0x22; // 门铃推送事件
    public static final int AVIOCTRL_EVENT_BELL_RING = 0x23; // 响铃
    public static final int AVIOCTRL_EVENT_SNAP_NO_RING = 0x26;// 离线图片传送

    /* AVAPIs IOCTRL Play Record Command */
    public static final int AVIOCTRL_RECORD_PLAY_PAUSE = 0x00;
    public static final int AVIOCTRL_RECORD_PLAY_STOP = 0x01;
    public static final int AVIOCTRL_RECORD_PLAY_STEPFORWARD = 0x02;
    public static final int AVIOCTRL_RECORD_PLAY_STEPBACKWARD = 0x03;
    public static final int AVIOCTRL_RECORD_PLAY_FORWARD = 0x04;
    public static final int AVIOCTRL_RECORD_PLAY_BACKWARD = 0x05;
    public static final int AVIOCTRL_RECORD_PLAY_SEEKTIME = 0x06;
    public static final int AVIOCTRL_RECORD_PLAY_END = 0x07;
    public static final int AVIOCTRL_RECORD_PLAY_START = 0x10;

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
    public static final int AVIOCTRL_QUALITY_UNKNOWN = 0x00;
    public static final int AVIOCTRL_QUALITY_MAX = 0x01;
    public static final int AVIOCTRL_QUALITY_HIGH = 0x02;
    public static final int AVIOCTRL_QUALITY_MIDDLE = 0x03;
    public static final int AVIOCTRL_QUALITY_LOW = 0x04;
    public static final int AVIOCTRL_QUALITY_MIN = 0x05;

    /* AVAPIs IOCTRL WiFi Mode */
    public static final int AVIOTC_WIFIAPMODE_ADHOC = 0x00;
    public static final int AVIOTC_WIFIAPMODE_MANAGED = 0x01;

    /* AVAPIs IOCTRL WiFi Enc Type */
    public static final int AVIOTC_WIFIAPENC_INVALID = 0x00;
    public static final int AVIOTC_WIFIAPENC_NONE = 0x01;
    public static final int AVIOTC_WIFIAPENC_WEP = 0x02;
    public static final int AVIOTC_WIFIAPENC_WPA_TKIP = 0x03;
    public static final int AVIOTC_WIFIAPENC_WPA_AES = 0x04;
    public static final int AVIOTC_WIFIAPENC_WPA2_TKIP = 0x05;
    public static final int AVIOTC_WIFIAPENC_WPA2_AES = 0x06;

    /* AVAPIs IOCTRL Recording Type */
    public static final int AVIOTC_RECORDTYPE_OFF = 0x00;
    public static final int AVIOTC_RECORDTYPE_FULLTIME = 0x01;
    public static final int AVIOTC_RECORDTYPE_ALAM = 0x02;
    public static final int AVIOTC_RECORDTYPE_MANUAL = 0x03;

    public static final int AVIOCTRL_ENVIRONMENT_INDOOR_50HZ = 0x00;
    public static final int AVIOCTRL_ENVIRONMENT_INDOOR_60HZ = 0x01;
    public static final int AVIOCTRL_ENVIRONMENT_OUTDOOR = 0x02;
    public static final int AVIOCTRL_ENVIRONMENT_NIGHT = 0x03;

    /* AVIOCTRL VIDEO MODE */
    public static final int AVIOCTRL_VIDEOMODE_NORMAL = 0x00;
    public static final int AVIOCTRL_VIDEOMODE_FLIP = 0x01;
    public static final int AVIOCTRL_VIDEOMODE_MIRROR = 0x02;
    public static final int AVIOCTRL_VIDEOMODE_FLIP_MIRROR = 0x03;


    public static final int IOTYPE_USER_IPCAM_GET_ALARMLED_CONTRL_REQ = 0x40086;
    public static final int IOTYPE_USER_IPCAM_GET_ALARMLED_CONTRL_RESP = 0x40087;
    public static final int IOTYPE_USER_IPCAM_SET_ALARMLED_CONTRL_REQ = 0x40084;
    public static final int IOTYPE_USER_IPCAM_SET_ALARMLED_CONTRL_RESP = 0x40085;

    public static final int IOTYPE_USER_IPCAM_GET_OSD_ONOFF_REQ = 0x50023;
    public static final int IOTYPE_USER_IPCAM_GET_OSD_ONOFF_RESP = 0x50024;
    public static final int IOTYPE_USER_IPCAM_SET_OSD_ONOFF_REQ = 0x50021;
    public static final int IOTYPE_USER_IPCAM_SET_OSD_ONOFF_RESP = 0x50022;
    public static final int IOTYPE_USER_IPCAM_GET_AUSDOM_PIR_SENSITIVITY_REQ = 0x40072;
    public static final int IOTYPE_USER_IPCAM_GET_AUSDOM_PIR_SENSITIVITY_RESP = 0x40073;
    public static final int IOTYPE_USER_IPCAM_SET_AUSDOM_PIR_SENSITIVITY_REQ = 0x40070;
    public static final int IOTYPE_USER_IPCAM_SET_AUSDOM_PIR_SENSITIVITY_RESP = 0x40071;
    public static final int IOTYPE_USER_IPCAM_GET_ALARM_TIME_REQ = 0x50031;
    public static final int IOTYPE_USER_IPCAM_GET_ALARM_TIME_RESP = 0x50032;
    public static final int IOTYPE_USER_IPCAM_SET_ALARM_TIME_REQ = 0x50033;
    public static final int IOTYPE_USER_IPCAM_SET_ALARM_TIME_RESP = 0x50034;


    //user-defined cmd type
    //preset point operate
    public static final int IOTYPE_USER_IPCAM_GET_PRESET_LIST_REQ = 0x2001;
    public static final int IOTYPE_USER_IPCAM_GET_PRESET_LIST_RESP = 0x2002;

    public static final int IOTYPE_USER_IPCAM_SET_PRESET_POINT_REQ = 0x2003;
    public static final int IOTYPE_USER_IPCAM_SET_PRESET_POINT_RESP = 0x2004;

    public static final int IOTYPE_USER_IPCAM_OPR_PRESET_POINT_REQ = 0x2005;
    public static final int IOTYPE_USER_IPCAM_OPR_PRESET_POINT_RESP = 0x2006;

    //daylight saving time
//    public static final int IOTYPE_USER_IPCAM_GET_DST_REQ = 0x2007;
//    public static final int IOTYPE_USER_IPCAM_GET_DST_RESP = 0x2008;

//    public static final int IOTYPE_USER_IPCAM_SET_DST_REQ = 0x2009;
//    public static final int IOTYPE_USER_IPCAM_SET_DST_RESP = 0x200A;

    //reboot
    public static final int IOTYPE_USER_IPCAM_REBOOT_REQ = 0x200B;
    public static final int IOTYPE_USER_IPCAM_REBOOT_RESP = 0x200C;

    //reset to default
    public static final int IOTYPE_USER_IPCAM_RESET_DEFAULT_REQ = 0x200D;
    public static final int IOTYPE_USER_IPCAM_RESET_DEFAULT_RESP = 0x200E;

    public static final int IOTYPE_USER_IPCAM_GET_UPRADE_URL_REQ = 0x2017;
    public static final int IOTYPE_USER_IPCAM_GET_UPRADE_URL_RESP = 0x2018;

    public static final int IOTYPE_USER_IPCAM_SET_UPRADE_REQ = 0x2019;
    public static final int IOTYPE_USER_IPCAM_SET_UPRADE_RESP = 0x2020;
    public static final int IOTYPE_USER_IPCAM_UPGRADE_STATUS = 0x2021;

    public static final int IOTYPE_USER_IPCAM_GET_FIRMWARE_INFO_REQ = 0x2022;
    public static final int IOTYPE_USER_IPCAM_GET_FIRMWARE_INFO_RESP = 0x2023;
    public static final int IOTYPE_USER_IPCAM_GET_TIME_INFO_REQ = 0x2024;
    public static final int IOTYPE_USER_IPCAM_GET_TIME_INFO_RESP = 0x2025;
    public static final int IOTYPE_USER_IPCAM_SET_TIME_INFO_REQ = 0x2026;
    public static final int IOTYPE_USER_IPCAM_SET_TIME_INFO_RESP = 0x2027;
    public static final int IOTYPE_USER_IPCAM_GET_ZONE_INFO_REQ = 0x2028;
    public static final int IOTYPE_USER_IPCAM_GET_ZONE_INFO_RESP = 0x2029;
    public static final int IOTYPE_USER_IPCAM_SET_ZONE_INFO_REQ = 0x202A;
    public static final int IOTYPE_USER_IPCAM_SET_ZONE_INFO_RESP = 0x202B;

    public static final int IOTYPE_USER_IPCAM_UPDATE_WIFI_STATUS = 0x202C;

    //dropbox-------------------------------------------------------
    public static final int IOTYPE_USEREX_IPCAM_SET_DROPBOX_ACCESS_TOKEN_REQ = 0x4013;
    //Data:SMsgAVIoctrlExSetDropboxAccessTokenReq

    public static final int IOTYPE_USEREX_IPCAM_SET_DROPBOX_ACCESS_TOKEN_RESP = 0x4014;
    //Data: SMsgAVIoctrlExSetDropboxAccessTokenResp

    public static final int IOTYPE_USEREX_IPCAM_GET_DROPBOX_ACCESS_TOKEN_REQ = 0x4015;
    //Data:SMsgAVIoctrlExGetDropboxAccessTokenReq

    public static final int IOTYPE_USEREX_IPCAM_GET_DROPBOX_ACCESS_TOKEN_RESP = 0x4016;
    //Data: SMsgAVIoctrlExGetDropboxAccessTokenResp

    public static final int IOTYPE_USEREX_IPCAM_SET_NETSTORAGE_ENABLED_REQ = 0x4017;
    //Data: SMsgAVIoctrlExSetNetStorageEnabledReq

    public static final int IOTYPE_USEREX_IPCAM_SET_NETSTORAGE_ENABLED_RESP = 0x4018;
    //Data: SMsgAVIoctrlExSetNetStorageEnabledResp

    public static final int IOTYPE_USEREX_IPCAM_GET_NETSTORAGE_REQ = 0x4019;
    //Data: SMsgAVIoctrlExGetNetStorageReq

    public static final int IOTYPE_USEREX_IPCAM_GET_NETSTORAGE_RESP = 0x401a;
    //Data: SMsgAVIoctrlExGetNetStorageResp

    public static final int IOTYPE_USEREX_IPCAM_SET_NETSTORAGE_REQ = 0x401b;
    //Data: SMsgAVIoctrlExSetNetStorageReq

    public static final int IOTYPE_USEREX_IPCAM_SET_NETSTORAGE_RESP = 0x401c;
    //Data: SMsgAVIoctrlExSetNetStorageResp

    /**
     * 以下为报警邮箱设置相关的接口
     */
    public static final int IOTYPE_USEREX_IPCAM_GET_SMTP_REQ = 0x4005;
    //Data: SMsgAVIoctrlExGetSmtpReq

    public static final int IOTYPE_USEREX_IPCAM_GET_SMTP_RESP = 0x4006;
    //Data: SmtpSetting

    public static final int IOTYPE_USEREX_IPCAM_SET_SMTP_REQ = 0x4007;
    //Data: SmtpSetting

    public static final int IOTYPE_USEREX_IPCAM_SET_SMTP_RESP = 0x4008;
    //Data: SMsgAVIoctrlExSetSmtpResp

    public static final int IOTYPE_USEREX_IPCAM_SEND_TEST_MAIL_REQ = 0x4009;
    //Data: SMsgAVIoctrlExSendMailReq
    public static final int IOTYPE_USEREX_IPCAM_SEND_TEST_MAIL_RESP = 0x400A;
    //Data: SMsgAVIoctrlExSendMailResp

    public static final int IOTYPE_USEREX_IPCAM_GET_MAIL_STATUS_REQ = 0x400B;
    //Data: SMsgAVIoctrlExGetMailStatusReq
    public static final int IOTYPE_USEREX_IPCAM_GET_MAIL_STATUS_RESP = 0x400C;
    //Data: SMsgAVIoctrlExGetMailStatusResp
    /**
     * 以下为传感器相关的接口
     */
    public static final int IOTYPE_USEREX_IPCAM_GET_HUMITURE_REQ = 0x6001;
    //Data: SMsgAVIoctrlExGetSmtpReq

    public static final int IOTYPE_USEREX_IPCAM_GET_HUMITURE_RESP = 0x6002;
    //led相关
    public static final int IOTYPE_USER_IPCAM_SETALARMRING_REQ = 0x44E;
    public static final int IOTYPE_USER_IPCAM_SETALARMRING_RESP = 0x44F;
    public static final int IOTYPE_USER_IPCAM_GETALARMRING_REQ = 0x8030;
    //Data: SMsgAVIoctrlExGetSmtpReq

    public static final int IOTYPE_USER_IPCAM_GETALARMRING_RESP = 0x8031;
}
