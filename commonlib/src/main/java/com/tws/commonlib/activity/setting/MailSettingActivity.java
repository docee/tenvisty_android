package com.tws.commonlib.activity.setting;

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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.base.ExpandAnimation;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class MailSettingActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private IMyCamera camera;
    ToggleButton togbtn_open;
    EditText mailbox_setting_username_edt;
    EditText mailbox_setting_psw_edt;
    EditText mailbox_setting_server_edt;
    EditText mailbox_setting_port_edt;
    EditText mailbox_setting_receive_edt;
    Spinner mailbox_setting_safety_spn;
    LinearLayout mailbox_setting_detail_layout;
    ToggleButton togbtn_advance;
    LinearLayout ll_setmail;
    private static final int ENCTYPE_NONE = 0;
    private static final int ENCTYPE_SSL = 1;
    private static final int ENCTYPE_TLS = 2;
    private static final int ENCTYPE_STARTTLS = 3;
    private String settedUserName;
    AVIOCTRLDEFs.SmtpSetting smtpInfo;
    /**
     * 存放一些默认的邮箱类
     */
    private List<EmailModel> emailList = new ArrayList<EmailModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_mail_setting);
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
                        setMailSetting();
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
        ArrayAdapter<CharSequence> adapter_frequency = ArrayAdapter.createFromResource(this, R.array.safety_connection, R.layout.view_spinner_item);
        adapter_frequency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mailbox_setting_safety_spn.setAdapter(adapter_frequency);
        getMailSetting();
        togbtn_advance = (ToggleButton) this.findViewById(R.id.togbtn_advance);

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
        togbtn_open.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (((ToggleButton) v).isChecked()) {
                    ExpandAnimation animation = new ExpandAnimation(ll_setmail, 250);
                    ll_setmail.startAnimation(animation);
                } else {
                    ExpandAnimation animation = new ExpandAnimation(ll_setmail, 250);
                    ll_setmail.startAnimation(animation);
                }
            }
        });

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
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USEREX_IPCAM_GET_SMTP_REQ, AVIOCTRLDEFs.SMsgAVIoctrlExGetSmtpReq.parseContent());
        }
    }

    void setMailSetting() {
        showLoadingProgress();
        int enable = togbtn_open.isChecked()?1:0;
        String smtp_svr = mailbox_setting_server_edt.getText().toString();
        String user = mailbox_setting_username_edt.getText().toString();
        String password = mailbox_setting_psw_edt.getText().toString();
        String sender =mailbox_setting_username_edt.getText().toString();
        String receiver = mailbox_setting_receive_edt.getText().toString();
        int ssl = mailbox_setting_safety_spn.getSelectedItemPosition();
        int smtp_port = 25;
        try{
            smtp_port = Integer.parseInt(mailbox_setting_port_edt.getText().toString());
        }catch (Exception ex){

        }
        if (camera != null) {
            byte[]sendByte =  AVIOCTRLDEFs.SmtpSetting.parseContent(enable, smtp_svr, smtp_port, sender, password,receiver,ssl);
            camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USEREX_IPCAM_SET_SMTP_REQ,sendByte);
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
    public void receiveIOCtrlData(IMyCamera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {
        Bundle bundle = new Bundle();
        bundle.putInt("sessionChannel", avChannel);
        bundle.putByteArray("data", data);

        Message msg = new Message();
        msg.what = avIOCtrlMsgType;
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

                case AVIOCTRLDEFs.IOTYPE_USEREX_IPCAM_GET_SMTP_RESP:
                    dismissLoadingProgress();
                    int isEnableEmail = Packet.byteArrayToInt_Little(data, 0);
                    if (togbtn_open.isChecked() != (isEnableEmail != 0)) {
                        togbtn_open.performClick();//.
                    }
                    try {
                        smtpInfo = new AVIOCTRLDEFs.SmtpSetting(data);
                        String userString = new String(smtpInfo.user, "US-ASCII").trim();
                        settedUserName = userString;
                        String passwordString = new String(smtpInfo.password, "US-ASCII").trim();
                        String smtpServerString = new String(smtpInfo.smtp_svr, "US-ASCII").trim();
                        String smtpReceiverString = new String(smtpInfo.receiver, "US-ASCII").trim();
                        int smtpencType = smtpInfo.ssl;
                        int smtpPort = smtpInfo.smtp_port;
                        mailbox_setting_receive_edt.setText(smtpReceiverString);
                        mailbox_setting_username_edt.setText(userString);
                        mailbox_setting_psw_edt.setText(passwordString);
                        mailbox_setting_server_edt.setText(smtpServerString);
                        mailbox_setting_port_edt.setText(smtpPort + "");
                        mailbox_setting_safety_spn.setSelection(smtpencType);

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                case AVIOCTRLDEFs.IOTYPE_USEREX_IPCAM_SET_SMTP_RESP:
                    int status = Packet.byteArrayToInt_Little(data, 0);
                    dismissLoadingProgress();
                    if (status == 0) {
                        if (togbtn_open.isChecked()) {


                            AlertDialog.Builder alertbox = new AlertDialog.Builder(MailSettingActivity.this);
                            alertbox.setMessage(getText(R.string.dialog_msg_send_test_email_confirm));//"send_test_email">你是否想发送一封测试邮件?
                            alertbox.setPositiveButton(getText(R.string.send), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USEREX_IPCAM_SEND_TEST_MAIL_REQ, AVIOCTRLDEFs.SMsgAVIoctrlExSendMailReq.parseContent());
                                   showLoadingProgress(getString(R.string.process_mail_sending_test_msg));
                                    getMailStatusReq();
                                }
                            });

                            alertbox.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    MailSettingActivity.this.finish();
                                }
                            });

                            // display box
                            if (! MailSettingActivity.this.isFinishing()) {
                                alertbox.show();
                            }

                        }else {

                            AlertDialog.Builder alertbox = new AlertDialog.Builder(MailSettingActivity.this);
                            alertbox.setMessage(getText(R.string.tips_setting_succ));
                            alertbox.setNeutralButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                }
                            });
                            if (! MailSettingActivity.this.isFinishing()) {
                                alertbox.show();
                            }
                        }
                    }else {
                        AlertDialog.Builder alertbox = new AlertDialog.Builder(MailSettingActivity.this);
                        alertbox.setMessage(getText(R.string.alert_setting_fail));
                        alertbox.setNeutralButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                            }
                        });
                        if (! MailSettingActivity.this.isFinishing()) {
                            alertbox.show();
                        }
                    }
                    break;
                case AVIOCTRLDEFs.IOTYPE_USEREX_IPCAM_GET_MAIL_STATUS_RESP:
                    dismissLoadingProgress();

                    final int statusInt = Packet.byteArrayToInt_Little(data, 0);
                    int statusId = 0;
                    switch (statusInt) {
                        case 0:
                            statusId = R.string.tips_mail_send_success;
                            break;
                        case 1:
                            statusId = R.string.process_mail_connecting;
                            break;
                        case 2:
                            statusId = R.string.process_mail_verifying;//verifying">验证中
                            break;
                        case 3:
                            statusId = R.string.process_mail_sending;
                            break;
                        case 4:
                            statusId = R.string.alert_mail_connection_failed;//
                            break;
                        case 5:
                            statusId = R.string.alert_mail_setting_fail;//set_email_alert_error">邮箱地址或者邮箱密码或者smtp服务器或者smtp端口错误.
                            break;
                        case 6:
                            statusId = R.string.alert_mail_setting_fail;
                            break;

                        default:
                            break;
                    }
                    System.out.println("IOTYPE_USEREX_IPCAM_GET_MAIL_STATUS_RESP " + statusInt);
                    if (statusInt >= 1 && statusInt <= 3) {
                        showLoadingProgress(getString(statusId));
                        getMailStatusReq();
                    }else {


                        AlertDialog.Builder alertbox = new AlertDialog.Builder(MailSettingActivity.this);
                        alertbox.setMessage(getText(statusId));
                        alertbox.setNeutralButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                if (statusInt == 0) {
                                    MailSettingActivity.this.finish();
                                }
                            }
                        });
                        if (! MailSettingActivity.this.isFinishing()) {
                            alertbox.show();
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
}
