package com.tws.commonlib.activity.hichip;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.hichip.content.HiChipDefines;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.base.ExpandAnimation;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class MailSetting_HichipActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private IMyCamera camera;
    ToggleButton togbtn_open;
    EditText mailbox_setting_username_edt;
    EditText mailbox_setting_psw_edt;
    EditText mailbox_setting_server_edt;
    EditText mailbox_setting_port_edt;
    EditText mailbox_setting_receive_edt;
    Spinner mailbox_setting_safety_spn;
    EditText mailbox_setting_theme_edt;
    EditText mailbox_setting_message_edt;
    LinearLayout mailbox_setting_detail_layout;
    ToggleButton togbtn_advance;
    LinearLayout ll_setmail;
    private static final int ENCTYPE_NONE = 0;
    private static final int ENCTYPE_SSL = 1;
    private static final int ENCTYPE_TLS = 2;
    private static final int ENCTYPE_STARTTLS = 3;
    private String settedUserName;
    AVIOCTRLDEFs.SmtpSetting smtpInfo;
    HI_P2P_S_EMAIL_PARAM2 param;
    boolean preOpen;
    boolean isChecking = false;
    private HiChipDefines.HI_P2P_S_ALARM_PARAM alarmParam;
    /**
     * 存放一些默认的邮箱类
     */
    private List<EmailModel> emailList = new ArrayList<EmailModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mail_setting_hichip);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera = _camera;
                break;
            }
        }

        this.setTitle(getString(R.string.title_mail_setting));
        initView();
        camera.registerIOTCListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
        final NavigationBar title = (NavigationBar) findViewById(R.id.title_top);
        title.setButton(NavigationBar.NAVIGATION_BUTTON_RIGHT);
        title.setNavigationBarButtonListener(new NavigationBar.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case NavigationBar.NAVIGATION_BUTTON_RIGHT:
                        setMailSetting(false);
                        break;
                }
            }
        });
        togbtn_open = (ToggleButton) findViewById(R.id.togbtn_open);
        mailbox_setting_detail_layout = (LinearLayout) findViewById(R.id.mailbox_setting_detail_layout);
        ll_setmail = (LinearLayout) findViewById(R.id.ll_setmail);
        mailbox_setting_username_edt = (EditText) findViewById(R.id.mailbox_setting_username_edt);
        mailbox_setting_psw_edt = (EditText) findViewById(R.id.mailbox_setting_psw_edt);
        mailbox_setting_server_edt = (EditText) findViewById(R.id.mailbox_setting_server_edt);
        mailbox_setting_port_edt = (EditText) findViewById(R.id.mailbox_setting_port_edt);
        mailbox_setting_safety_spn = (Spinner) findViewById(R.id.mailbox_setting_safety_spn);
        mailbox_setting_receive_edt = (EditText) findViewById(R.id.mailbox_setting_receive_edt);
        mailbox_setting_theme_edt = (EditText) findViewById(R.id.mailbox_setting_theme_edt);
        mailbox_setting_message_edt = (EditText) findViewById(R.id.mailbox_setting_message_edt);
        ArrayAdapter<CharSequence> adapter_frequency = ArrayAdapter.createFromResource(this, R.array.safety_connection, R.layout.view_spinner_item);
        adapter_frequency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mailbox_setting_safety_spn.setAdapter(adapter_frequency);
        getMailSetting();
        togbtn_advance = (ToggleButton) this.findViewById(R.id.togbtn_advance);
        mailbox_setting_detail_layout.setVisibility(View.GONE);
        ll_setmail.setVisibility(View.GONE);
        togbtn_advance.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (((ToggleButton) v).isChecked()) {
                    ExpandAnimation animation = new ExpandAnimation(mailbox_setting_detail_layout, 250);
                    mailbox_setting_detail_layout.startAnimation(animation);
                } else {
                    ExpandAnimation animation = new ExpandAnimation(mailbox_setting_detail_layout, 250);
                    mailbox_setting_detail_layout.startAnimation(animation);
                }
            }
        });
        togbtn_open.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoubndButton, boolean b) {
                if (b) {
                    ExpandAnimation animation = new ExpandAnimation(ll_setmail, 250);
                    ll_setmail.startAnimation(animation);
                } else {
                    ExpandAnimation animation = new ExpandAnimation(ll_setmail, 250);
                    ll_setmail.startAnimation(animation);
                }
            }
        });
        preOpen = this.getIntent().getExtras().getInt("enabel", 0) == 1;
        togbtn_open.setChecked(preOpen);
        mailbox_setting_username_edt.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (smtpInfo != null && s.toString().equals(new String(smtpInfo.sender, "US-ASCII").trim())) {

                    } else {
                        //TwsToast.showToast(MailSettingActivity.this,"change");
                        autoFillData();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });

        emailList.clear();
        emailList.add(new EmailModel("yahoo.com", "smtp.mail.yahoo.com", 465, ENCTYPE_SSL));
        emailList.add(new EmailModel("outlook.com", "smtp-mail.outlook.com", 587, ENCTYPE_STARTTLS));
        emailList.add(new EmailModel("qq.com", "smtp.qq.com", 465, ENCTYPE_SSL));
        emailList.add(new EmailModel("163.com", "smtp.163.com", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("126.com", "smtp.126.com", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("yeah.net", "smtp.yeah.net", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("sohu.com", "smtp.sohu.com", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("tom.com", "smtp.tom.com", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("21cn.com", "smtp.21cn.com", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("aol.com", "smtp.aol.com", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("orange.fr", "smtp.orange.fr", 465, ENCTYPE_SSL));
        emailList.add(new EmailModel("wanadoo.fr", "smtp.orange.fr", 465, ENCTYPE_SSL));
        emailList.add(new EmailModel("hotmail.com", "smtp.live.com", 587, ENCTYPE_STARTTLS));
        emailList.add(new EmailModel("hotmail.fr", "smtp.live.com", 587, ENCTYPE_STARTTLS));
        emailList.add(new EmailModel("live.com", "smtp.live.com", 587, ENCTYPE_STARTTLS));
        emailList.add(new EmailModel("live.fr", "smtp.live.com", 587, ENCTYPE_STARTTLS));
        emailList.add(new EmailModel("msn.com", "smtp.live.com", 587, ENCTYPE_STARTTLS));
        emailList.add(new EmailModel("yahoo.fr", "smtp.mail.yahoo.fr", 465, ENCTYPE_SSL));
        emailList.add(new EmailModel("sfr.fr", "smtp.sfr.fr", 587, ENCTYPE_STARTTLS));
        emailList.add(new EmailModel("neuf.fr", "smtp.sfr.fr", 587, ENCTYPE_STARTTLS));
        emailList.add(new EmailModel("free.fr", "smtp.free.fr***", 465, ENCTYPE_SSL));
        emailList.add(new EmailModel("gmail.com", "smtp.gmail.com", 465, ENCTYPE_SSL));
        emailList.add(new EmailModel("club-internet.fr", "smtp.sfr.fr", 587, ENCTYPE_STARTTLS));
        emailList.add(new EmailModel("aol.com", "smtp.fr.aol.com", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("laposte.net", "smtp.laposte.net", 465, ENCTYPE_SSL));
        emailList.add(new EmailModel("cegetel.fr", "smtp.sfr.fr", 587, ENCTYPE_STARTTLS));
        emailList.add(new EmailModel("alice.fr", "smtp.alice.fr", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("Noos.fr", "mail.noos.fr", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("tele2.fr", "smtp.tele2.fr", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("tiscali.fr", "smtp.tiscali.fr", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("netcourrier.com", "smtp.orange.fr", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("libertysurf.fr", "mail.libertysurf.fr", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("gmx.fr", "mail.gmx.com", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("caramail.fr", "mail.gmx.com", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("gmx.com", "mail.gmx.com", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("caramail.com", "mail.gmx.com", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("bbox.fr", "smtp.bouygtel.fr", 25, ENCTYPE_NONE));
        emailList.add(new EmailModel("numericable.fr", "smtps.numericable.fr", 587, ENCTYPE_STARTTLS));

    }

    void getMailSetting() {
        showLoadingProgress();
        if (camera != null) {
            camera.sendIOCtrl(0, HiChipDefines.HI_P2P_GET_EMAIL_PARAM, new byte[0]);
        }
    }

    void setMailSetting(boolean check) {
        if(param != null) {
            if(check){
                refreshProgressTest(getString(R.string.process_testting));
            }
            else{
                showLoadingProgress(getString(R.string.process_setting));
            }
            if (preOpen != togbtn_open.isChecked()) {
                camera.sendIOCtrl(0, HiChipDefines.HI_P2P_GET_ALARM_PARAM, null);
            } else {
                int enable = togbtn_open.isChecked() ? 1 : 0;
                String smtp_svr = mailbox_setting_server_edt.getText().toString();
                String user = mailbox_setting_username_edt.getText().toString();
                String password = mailbox_setting_psw_edt.getText().toString();
                String sender = mailbox_setting_username_edt.getText().toString();
                String receiver = mailbox_setting_receive_edt.getText().toString();
                String subject = mailbox_setting_theme_edt.getText().toString();
                String message = mailbox_setting_message_edt.getText().toString();
                int ssl = mailbox_setting_safety_spn.getSelectedItemPosition();
                int smtp_port = ssl == ENCTYPE_NONE ? 25 : (ssl == ENCTYPE_STARTTLS ? 587 : 465);
                try {
                    smtp_port = Integer.parseInt(mailbox_setting_port_edt.getText().toString());
                } catch (Exception ex) {

                }
                param.setStrSvr(smtp_svr);
                param.u32Port = smtp_port;
                param.setStrUsernm(user);
                param.setStrPasswd(password);
                param.setStrTo(receiver);
                param.setStrFrom(sender);
                param.setStrSubject(subject);
                param.setStrText(message);
                param.u32LoginType = 1;
                param.u32Auth = ssl;
                isChecking = check;
                byte[] sendParam = HI_P2P_S_EMAIL_PARAM_EXT2.parseContent(param, check?1:0);

                camera.sendIOCtrl(0, HiChipDefines.HI_P2P_SET_EMAIL_PARAM_EXT, sendParam);
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.unregisterIOTCListener(this);
        }
    }

    @Override
    public void receiveFrameData(IMyCamera camera, int avChannel, Bitmap bmp) {

    }

    @Override
    public void receiveFrameInfo(IMyCamera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {

    }

    @Override
    public void receiveSessionInfo(IMyCamera camera, int resultCode) {

    }

    @Override
    public void receiveChannelInfo(IMyCamera camera, int avChannel, int resultCode) {

    }

    @Override
    public void receiveIOCtrlData(IMyCamera camera, int succ, int avIOCtrlMsgType, byte[] data) {
        Bundle bundle = new Bundle();
        bundle.putByteArray("data", data);

        Message msg = new Message();
        msg.what = avIOCtrlMsgType;
        msg.arg1 = succ;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    @Override
    public void initSendAudio(IMyCamera paramCamera, boolean paramBoolean) {

    }

    @Override
    public void receiveOriginalFrameData(IMyCamera paramCamera, int paramInt1, byte[] paramArrayOfByte1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3) {

    }

    @Override
    public void receiveRGBData(IMyCamera paramCamera, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3) {

    }

    @Override
    public void receiveRecordingData(IMyCamera paramCamera, int avChannel, int paramInt1, String path) {

    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");

            switch (msg.what) {
                case HiChipDefines.HI_P2P_GET_ALARM_PARAM: {
                    alarmParam = new HiChipDefines.HI_P2P_S_ALARM_PARAM(data);
                    alarmParam.u32EmailSnap = togbtn_open.isChecked() ? 1 : 0;
                    camera.sendIOCtrl(0, HiChipDefines.HI_P2P_SET_ALARM_PARAM, alarmParam.parseContent());
                }
                break;
                case HiChipDefines.HI_P2P_SET_ALARM_PARAM:
                    preOpen = alarmParam.u32EmailSnap == 1;
                    setMailSetting(false);
                    break;
                case HiChipDefines.HI_P2P_GET_EMAIL_PARAM:
                    dismissLoadingProgress();
                    if (data != null && data.length >= 848) {
                        param = new HI_P2P_S_EMAIL_PARAM2(data);
                        mailbox_setting_server_edt.setText(com.hichip.tools.Packet.getString(param.strSvr));
                        mailbox_setting_port_edt.setText(String.valueOf(param.u32Port));
                        mailbox_setting_receive_edt.setText(com.hichip.tools.Packet.getString(param.strTo[0]));
                        mailbox_setting_psw_edt.setText(com.hichip.tools.Packet.getString(param.strPasswd));
                        mailbox_setting_username_edt.setText(com.hichip.tools.Packet.getString(param.strUsernm));
//						mailbox_setting_sending_address_edt.setText(Packet.getString(param.strFrom));
                        mailbox_setting_theme_edt.setText(com.hichip.tools.Packet.getString(param.strSubject));
                        if (mailbox_setting_theme_edt.getText().toString().trim().equals("")) {
                            mailbox_setting_theme_edt.setText("IP Camera sent you an Email alert");
                        }
                        mailbox_setting_message_edt.setText(com.hichip.tools.Packet.getString(param.strText));
                        if (mailbox_setting_message_edt.getText().toString().trim().equals("")) {
                            mailbox_setting_message_edt.setText("Hello! Your camera has detected suspicious motion. Snapshots have been sent to your email address. Please log in to check.");
                        }
//						if(param.u32LoginType==1){
//							mailbox_setting_check_tgbtn.setChecked(true);
//						}else if(param.u32LoginType==3){
//							mailbox_setting_check_tgbtn.setChecked(false);
//						}

                        mailbox_setting_safety_spn.setSelection(param.u32Auth);
                    }
                    break;
                case HiChipDefines.HI_P2P_SET_EMAIL_PARAM_EXT:
                    if(isChecking){
                        dismissLoadingProgress();
                        if(msg.arg1 == 0){
                            TwsToast.showToast(MailSetting_HichipActivity.this,getString(R.string.tips_setting_succ));
                            camera.unregisterIOTCListener(MailSetting_HichipActivity.this);
                            MailSetting_HichipActivity.this.finish();
                            //TwsToast.showToast(MailSetting_HichipActivity.this,getString(R.string.toast_test_success));
                        }
                        else{
                            showAlert(getString(R.string.dialog_msg_emailtest_falied));
                        }
                    }
                    else{
                        if(msg.arg1 == 0){
                            if(togbtn_open.isChecked()) {
                                setMailSetting(true);
                            }
                            else{
                                dismissLoadingProgress();
                                TwsToast.showToast(MailSetting_HichipActivity.this,getString(R.string.tips_setting_succ));

                                camera.unregisterIOTCListener(MailSetting_HichipActivity.this);
                                MailSetting_HichipActivity.this.finish();
                            }
                            //TwsToast.showToast(MailSetting_HichipActivity.this,getString(R.string.tips_setting_succ));
                        }
                        else{
                            dismissLoadingProgress();
                            TwsToast.showToast(MailSetting_HichipActivity.this,getString(R.string.tips_setting_failed));
                        }
                    }
                    break;

            }
            super.handleMessage(msg);
        }
    };

    public void doClickLL(View view) {
        ((LinearLayout) view).getChildAt(1).performClick();
    }

    /**
     * 获取用户输入的邮箱是否是默认邮箱列表中的一种
     *
     * @param serverString
     * @return
     */
    private EmailModel getNeedServer(String serverString) {
        for (int i = 0; i < emailList.size(); i++) {

            if (serverString.indexOf(emailList.get(i).emailName) >= 0) {
                return emailList.get(i);
            }
        }
        return null;
    }

    private void autoFillData() {
        String emailString = mailbox_setting_username_edt.getText().toString();
        EmailModel matchServer = getNeedServer(emailString);
        if (matchServer == null) {//如果不是默认邮箱列表中的，则显示SMTP相关设置的选项
            //mailbox_setting_detail_layout.setVisibility(View.VISIBLE);
            int index = emailString.indexOf("@");

            //Log.i("EmailAlertActivity", "index="+index+"emailString"+emailString);
            System.out.println(mailbox_setting_server_edt.getText().length() + " " + emailString.length());

            //if (mailbox_setting_server_edt.getText().length() == 0 && index > 0 && index+1 != emailString.length()) {
            mailbox_setting_server_edt.setText("smtp." + emailString.substring(index + 1));

            //}
            mailbox_setting_receive_edt.setText(emailString);
            mailbox_setting_port_edt.setText("465");
            mailbox_setting_safety_spn.setSelection(1);
        } else {
            mailbox_setting_server_edt.setText(matchServer.emailSmtp);
            mailbox_setting_port_edt.setText(matchServer.emailPort + "");
            mailbox_setting_receive_edt.setText(emailString);
            mailbox_setting_safety_spn.setSelection(matchServer.emaiEncType);
        }
    }

    private class EmailModel {
        public String emailName;
        public String emailSmtp;
        public int emailPort;
        public int emaiEncType;

        public EmailModel(String emailName, String emailSmtp, int emailPort,
                          int emaiEncType) {
            super();
            this.emailName = emailName;
            this.emailSmtp = emailSmtp;
            this.emailPort = emailPort;
            this.emaiEncType = emaiEncType;
        }

    }

    /**
     * 获取邮箱设置状态的请求
     */
    private void getMailStatusReq() {

        camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USEREX_IPCAM_GET_MAIL_STATUS_REQ, AVIOCTRLDEFs.SMsgAVIoctrlExGetMailStatusReq.parseContent());
    }


    public static class HI_P2P_S_EMAIL_PARAM_EXT2 {
        public HI_P2P_S_EMAIL_PARAM2 email_param;
        public int u32Check;				/*1:check,  0:no check*/
        byte strReserved[] = new byte[8];		/*预留*/

//		public byte[] parseContent() {
//			byte[] result = new byte[860];
//
//			System.arraycopy(email_param.parseContent(), 0, result, 0, 848);
//
//			byte[] Check = Packet.intToByteArray_Little(u32Check);
//			System.arraycopy(Check, 0, result, 852, 4);
//
//
//
//			return result;
//		}

        public static byte[] parseContent(HI_P2P_S_EMAIL_PARAM2 email_param, int check) {
            byte[] result = new byte[860];

            System.arraycopy(email_param.parseContent(), 0, result, 0, 848);

            byte[] Check = com.hichip.tools.Packet.intToByteArray_Little(check);
            System.arraycopy(Check, 0, result, 848, 4);

            return result;
        }
    }

    public static class HI_P2P_S_EMAIL_PARAM2 {
        public int u32Channel;/*ipc: 0*/
        public byte strSvr[] = new byte[64];
        public int u32Port;
        public int u32Auth;
        public int u32LoginType;	/*1：开启验证   3：关闭验证*/
        public byte strUsernm[] = new byte[64];
        public byte strPasswd[] = new byte[64];
        public byte strFrom[] = new byte[64];
        public byte strTo[][] = new byte[3][64];
        public byte strSubject[] = new byte[128];
        public byte strText[] = new byte[256];

        public HI_P2P_S_EMAIL_PARAM2(byte[] byt) {

            int pos = 0;
            u32Channel = com.hichip.tools.Packet.byteArrayToInt_Little(byt, 0);
            pos += 4;
            System.arraycopy(byt, pos, strSvr, 0, 64);
            pos += 64;
            u32Port = com.hichip.tools.Packet.byteArrayToInt_Little(byt, pos);
            pos += 4;
            u32Auth = com.hichip.tools.Packet.byteArrayToInt_Little(byt, pos);
            pos += 4;
            u32LoginType = com.hichip.tools.Packet.byteArrayToInt_Little(byt, pos);
            pos += 4;
            System.arraycopy(byt, pos, strUsernm, 0, 64);
            pos += 64;
            System.arraycopy(byt, pos, strPasswd, 0, 64);
            pos += 64;
            System.arraycopy(byt, pos, strFrom, 0, 64);
            pos += 64;
            for (int i = 0; i < 3; i++) {
                System.arraycopy(byt, pos, strTo[i], 0, 64);
                pos += 64;
            }
            System.arraycopy(byt, pos, strSubject, 0, 128);
            pos += 128;
            System.arraycopy(byt, pos, strText, 0, 256);
            pos += 256;
        }

        public byte[] parseContent() {
            byte[] result = new byte[848];

            int pos = 0;
            byte[] Channel = com.hichip.tools.Packet.intToByteArray_Little(u32Channel);
            System.arraycopy(Channel, 0, result, pos, 4);
            pos += 4;//4

//			Log.v("hichip", "strSvr:"+strSvr+ "      " + "this:"+this+"   1:"+Packet.getHex(strSvr, 64));
            System.arraycopy(strSvr, 0, result, pos, 64);
            pos += 64;//68
//			Log.v("hichip","strSvr:"+strSvr+ "      " +  "this:"+this+"   2:"+Packet.getHex(strSvr, 64));


            byte[] Port = com.hichip.tools.Packet.intToByteArray_Little(u32Port);
            System.arraycopy(Port, 0, result, pos, 4);
            pos += 4;//72

            byte[] Auth = com.hichip.tools.Packet.intToByteArray_Little(u32Auth);
            System.arraycopy(Auth, 0, result, pos, 4);
            pos += 4;//76

            byte[] LoginType = com.hichip.tools.Packet.intToByteArray_Little(u32LoginType);
            System.arraycopy(LoginType, 0, result, pos, 4);
            pos += 4;//80

            System.arraycopy(strUsernm, 0, result, pos, 64);
            pos += 64;//144

            System.arraycopy(strPasswd, 0, result, pos, 64);
            pos += 64;//208

            System.arraycopy(strFrom, 0, result, pos, 64);
            pos += 64;//272

            for (int i = 0; i < 3; i++) {
                System.arraycopy(strTo[i], 0, result, pos, 64);
                pos += 64;//464
            }

            System.arraycopy(strSubject, 0, result, pos, 128);
            pos += 128;//592

            System.arraycopy(strText, 0, result, pos, 256);
            pos += 256;//848

            return result;
        }

        public void setStrSvr(String svr) {
            byte[] bSvr = svr.getBytes();
//			strSvr = new byte[64];
            Arrays.fill(strSvr, (byte) 0);
            int len = bSvr.length > 64 ? 64 : bSvr.length;

//			Log.v("hichip","bSvr:"+svr+"  len:"+len);
            System.arraycopy(bSvr, 0, strSvr, 0, len);

//			Log.v("hichip","strSvr:"+strSvr+ "      " + "this:"+this+"   strSvr:"+Packet.getHex(strSvr, 64));
        }


        public void setStrUsernm(String usernm) {
            byte[] bUsernm = usernm.getBytes();
            Arrays.fill(strUsernm, (byte) 0);
            int len = bUsernm.length > 64 ? 64 : bUsernm.length;

            System.arraycopy(bUsernm, 0, strUsernm, 0, len);
        }

        public void setStrPasswd(String passwd) {
            byte[] bPasswd = passwd.getBytes();
            Arrays.fill(strPasswd, (byte) 0);
            int len = bPasswd.length > 64 ? 64 : bPasswd.length;

            System.arraycopy(bPasswd, 0, strPasswd, 0, len);
        }

        public void setStrFrom(String from) {
            byte[] bFrom = from.getBytes();
            Arrays.fill(strFrom, (byte) 0);
            int len = bFrom.length > 64 ? 64 : bFrom.length;

            System.arraycopy(bFrom, 0, strFrom, 0, len);
        }

        public void setStrTo(String to) {
            byte[] bTo = to.getBytes();
            Arrays.fill(strTo[0], (byte) 0);
            int len = bTo.length > 64 ? 64 : bTo.length;

            System.arraycopy(bTo, 0, strTo[0], 0, len);
        }

        public void setStrSubject(String subject) {
            byte[] bSubject = subject.getBytes();
            Arrays.fill(strSubject, (byte) 0);
            int len = bSubject.length > 128 ? 128 : bSubject.length;

            System.arraycopy(bSubject, 0, strSubject, 0, len);
        }

        public void setStrText(String text) {
            byte[] bText = text.getBytes();
            Arrays.fill(strText, (byte) 0);
            int len = bText.length > 256 ? 256 : bText.length;

            System.arraycopy(bText, 0, strText, 0, len);
        }

    }
}
